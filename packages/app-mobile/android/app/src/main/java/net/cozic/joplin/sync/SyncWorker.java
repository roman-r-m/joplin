package net.cozic.joplin.sync;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.facebook.react.ReactInstanceManager;
import com.facebook.react.ReactNativeHost;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.UiThreadUtil;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.jstasks.HeadlessJsTaskConfig;
import com.facebook.react.jstasks.HeadlessJsTaskContext;
import com.facebook.react.jstasks.HeadlessJsTaskEventListener;

import net.cozic.joplin.MainApplication;

import java.util.concurrent.CountDownLatch;

import static com.facebook.react.HeadlessJsTaskService.acquireWakeLockNow;

public class SyncWorker extends Worker implements HeadlessJsTaskEventListener {

    private final HeadlessJsTaskConfig taskConfig;
    private final CountDownLatch onComplete = new CountDownLatch(1);

    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        this.taskConfig = new HeadlessJsTaskConfig("JoplinSyncTask", new WritableNativeMap());
    }

    @Override
    @NonNull
    public Result doWork() {
        Log.i("ZZZ", "Do the work!");

        UiThreadUtil.runOnUiThread(() -> {
            ReactNativeHost reactNativeHost = ((MainApplication) getApplicationContext()).getReactNativeHost();
            ReactApplicationContext context = (ReactApplicationContext) reactNativeHost.getReactInstanceManager().getCurrentReactContext();
            ReactInstanceManager reactInstanceManager = reactNativeHost.getReactInstanceManager();
            ReactContext reactContext = reactInstanceManager.getCurrentReactContext();
            if (reactContext == null) {
                reactInstanceManager.addReactInstanceEventListener(
                        new ReactInstanceManager.ReactInstanceEventListener() {
                            @Override
                            public void onReactContextInitialized(ReactContext reactContext) {
                                invokeStartTask(reactContext, taskConfig);
                                reactInstanceManager.removeReactInstanceEventListener(this);
                            }
                        });
                reactInstanceManager.createReactContextInBackground();
            } else {
                invokeStartTask(reactContext, taskConfig);
            }
        });

        // TODO change to Listenable worker
        try {
            onComplete.await();
        } catch (InterruptedException e) {
            e.printStackTrace(); // TODO
        }

        Log.i("ZZZ", "Sync finished");

        return Result.success();
    }

    private void invokeStartTask(ReactContext reactContext, final HeadlessJsTaskConfig taskConfig) {
        acquireWakeLockNow(reactContext);
        final HeadlessJsTaskContext headlessJsTaskContext = HeadlessJsTaskContext.getInstance(reactContext);
        headlessJsTaskContext.addTaskEventListener(this);
        UiThreadUtil.runOnUiThread(() -> headlessJsTaskContext.startTask(taskConfig));
    }

    @Override
    public void onHeadlessJsTaskStart(int taskId) {

    }

    @Override
    public void onHeadlessJsTaskFinish(int taskId) {
        onComplete.countDown();
    }
}
