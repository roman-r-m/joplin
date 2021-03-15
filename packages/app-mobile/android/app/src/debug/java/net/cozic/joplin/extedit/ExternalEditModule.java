package net.cozic.joplin.extedit;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.RNFetchBlob.Utils.FileProvider;
import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import java.io.File;
import java.util.concurrent.atomic.AtomicReference;

import static android.app.Activity.RESULT_OK;

public class ExternalEditModule extends ReactContextBaseJavaModule implements ActivityEventListener {

    private final int CODE = 1233;

    private final ReactApplicationContext reactContext;
    private final AtomicReference<Promise> promise = new AtomicReference<>(null);

    public ExternalEditModule(ReactApplicationContext reactContext) {
        this.reactContext = reactContext;
        reactContext.addActivityEventListener(this);
    }

    @NonNull
    @Override
    public String getName() {
        return "ExternalEditModule";
    }

    @ReactMethod
    public void open(String path, Promise promise) {
//        Intent intent = new Intent(Intent.ACTION_VIEW);
        Intent intent = new Intent(Intent.ACTION_EDIT);

        // Get URI and MIME type of file
        Uri uri = FileProvider.getUriForFile(reactContext, "net.cozic.joplin.provider", new File(path));
//        Uri uri = FileProvider.getUriForFile(reactContext, "net.cozc.joplin.fileprovider", new File(path));

        intent.setDataAndType(uri, "text/plain");
//        intent.setDataAndType(uri, "text/markdown; charset=UTF-8");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        this.promise.set(promise);
        reactContext.startActivityForResult(intent, CODE, null);
    }

    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        if (requestCode != CODE) {
            return;
        }
        Promise promise = this.promise.getAndSet(null);
        if (promise == null) {
            return;
        }
        if (resultCode != RESULT_OK) {
            promise.reject("1", "Failed to edit");
        }
        promise.resolve(true);
    }

    @Override
    public void onNewIntent(Intent intent) {

    }
}
