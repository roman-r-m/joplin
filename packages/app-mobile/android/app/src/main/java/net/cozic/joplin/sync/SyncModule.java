package net.cozic.joplin.sync;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.Operation;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.facebook.react.ReactNativeHost;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;

import java.util.concurrent.TimeUnit;

public class SyncModule extends ReactContextBaseJavaModule {

    public SyncModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @NonNull
    @Override
    public String getName() {
        return "SyncModule";
    }

    @ReactMethod
    public void configure(final ReadableMap options) {
        Log.i("ZZZ", "Start sync");

        // TODO make periodic
        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(SyncWorker.class)
//        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(SyncWorker.class,
//                PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS, TimeUnit.MILLISECONDS)
                .setConstraints(new Constraints.Builder()
                        .setRequiresStorageNotLow(true)
                        .build())
                .build();
//        Operation operation = WorkManager.getInstance(getReactApplicationContext()).enqueueUniquePeriodicWork("JoplinSync",
//                ExistingPeriodicWorkPolicy.KEEP, workRequest);

        Operation operation = WorkManager.getInstance(getReactApplicationContext()).enqueue(workRequest);

        Log.i("ZZZ", "Result: " + operation);
    }
}
