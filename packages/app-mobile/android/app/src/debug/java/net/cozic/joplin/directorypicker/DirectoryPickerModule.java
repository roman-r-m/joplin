package net.cozic.joplin.directorypicker;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

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
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        this.promise.set(promise);
        if (!reactContext.startActivityForResult(intent, CODE, null)) {
            promise.reject("1", "Failed to pick directory");
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
        Uri uri = data.getData();
        promise.resolve(uri.toString());
    }

    @Override
    public void onNewIntent(Intent intent) {
    }
}
