package net.cozic.joplin;

import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewManager;

import java.util.Collections;
import java.util.List;

public class MdEditorPackage implements ReactPackage {
    @NonNull
    @Override
    public List<NativeModule> createNativeModules(@NonNull ReactApplicationContext reactContext) {
        return Collections.emptyList();
    }

    @NonNull
    @Override
    public List<ViewManager> createViewManagers(@NonNull ReactApplicationContext reactContext) {
        return Collections.singletonList(new SimpleViewManager<LinearLayout>() {

            @NonNull
            @Override
            public String getName() {
                return "MdEditor";
            }

            @NonNull
            @Override
            protected LinearLayout createViewInstance(@NonNull ThemedReactContext reactContext) {
                Log.i("ZZZ", "creating new MD editor");

                LinearLayout rootView = new LinearLayout(reactContext);
                rootView.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
                rootView.setOrientation(LinearLayout.HORIZONTAL);

                EditText editText = new EditText(reactContext);
                int inputType = editText.getInputType();
                editText.setInputType(inputType & (~InputType.TYPE_TEXT_FLAG_MULTI_LINE));

                rootView.addView(editText);

                return rootView;
            }
        });
    }
}
