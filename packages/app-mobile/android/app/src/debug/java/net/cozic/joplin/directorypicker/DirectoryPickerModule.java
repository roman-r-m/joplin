package net.cozic.joplin.directorypicker;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.OpenableColumns;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.IntentCompat;
import androidx.loader.content.CursorLoader;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;

import java.io.File;
import java.util.concurrent.atomic.AtomicReference;

public class DirectoryPickerModule extends ReactContextBaseJavaModule implements ActivityEventListener {

    private static final int CODE = 54321;

    private final ReactApplicationContext reactContext;
    private final AtomicReference<Promise> promise = new AtomicReference<>(null);

    public DirectoryPickerModule(ReactApplicationContext reactContext) {
        this.reactContext = reactContext;
        reactContext.addActivityEventListener(this);
    }

    @NonNull
    @Override
    public String getName() {
        return "DirectoryPicker";
    }

    @ReactMethod
    public void isAvailable(Promise promise) {
        promise.resolve(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @ReactMethod
    public void pick(Promise promise) {
        try {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            intent.putExtra("android.content.extra.SHOW_ADVANCED", true);
            this.promise.set(promise);
            if (!reactContext.startActivityForResult(intent, CODE, null)) {
                promise.reject("1", "Failed to pick directory");
                this.promise.set(null);
            }
        } catch (Exception e) {
            promise.reject(e);
            this.promise.set(null);
        }
    }

    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        if (requestCode != CODE) {
            return;
        }
        Promise promise = this.promise.getAndSet(null);
        if (resultCode != Activity.RESULT_OK) {
            promise.reject("2", "Cancelled");
            return;
        }
//        WritableMap map = Arguments.createMap();

        // ext sdcard: content://com.android.externalstorage.documents/tree/1DEB-1712%3AJoplin
        Uri uri = data.getData();
//        map.putString("uri", uri.toString());
//        map.putString("patht", getFileName(uri, activity.getContentResolver()));
//        promise.resolve(map);
//        if (uri.getAuthority().equalsIgnoreCase("com.android.externalstorage.documents")) {
//            String path = uri.getPath().replace("/tree", "/storage")
//        }
        promise.resolve("/storage/1DEB-1712/Joplin");
    }

    @Override
    public void onNewIntent(Intent intent) {
    }

    private String getFileName(Uri uri, ContentResolver contentResolver) {
        if (ContentResolver.SCHEME_FILE.equals(uri.getScheme())) {
            File file = new File(uri.getPath());
            return file.getName();
        } else if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
            String name = null;
            CursorLoader cursorLoader = new CursorLoader(reactContext, uri, null, null, null, null);
            Cursor cursor = cursorLoader.loadInBackground();
//            Cursor cursor = contentResolver.query(uri, null, null, null, null);
            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                        name = cursor.getString(nameIndex);
                    }
                } finally {
                    cursor.close();
                }
            }
            return name;
        } else {
            Log.w("joplin", "Unknown URI scheme: " + uri.getScheme());
            return null;
        }
    }
}
