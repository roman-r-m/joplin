package net.cozic.joplin.textinput;

import android.graphics.Color;
import android.text.Editable;
import android.text.InputType;
import android.text.Selection;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewDefaults;
import com.facebook.react.uimanager.ViewManager;
import com.facebook.react.uimanager.ViewProps;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.views.textinput.ReactEditText;
import com.facebook.react.views.textinput.ReactTextInputManager;

import java.util.Collections;
import java.util.List;

public class TextInputPackage implements com.facebook.react.ReactPackage {
    @NonNull
    @Override
    public List<NativeModule> createNativeModules(@NonNull ReactApplicationContext reactContext) {
        return Collections.emptyList();
    }

    @NonNull
    @Override
    public List<ViewManager> createViewManagers(@NonNull ReactApplicationContext reactContext) {
        return Collections.singletonList(new ReactTextInputManager() {

//            @Override
//            public ReactEditText createViewInstance(ThemedReactContext context) {
//                ReactEditText editText = new MdReactEditText(context);
//                int inputType = editText.getInputType();
//                editText.setInputType(inputType & (~InputType.TYPE_TEXT_FLAG_MULTI_LINE));
//                editText.setReturnKeyType("done");
//                return editText;
//            }

            @ReactProp(name = "enableMdHighlight")
            public void setEnableMdHighlight(ReactEditText view, boolean value) {
                Log.i("ZZZ", "setEnableMdHighlight");
                if (value) {
                    view.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                            if (before > 0) {
                                ((Spannable) s).setSpan(new ForegroundColorSpan(Color.GRAY), start, start + count,
                                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            }
                        }

                        @Override
                        public void afterTextChanged(Editable s) {

                        }
                    });
                }
            }



            @Override
            public void receiveCommand(ReactEditText reactEditText, String commandId, @Nullable ReadableArray args) {
                if ("focus".equals(commandId) || "focusTextInput".equals(commandId)) {
                    Selection.removeSelection(reactEditText.getText());
                }
                super.receiveCommand(reactEditText, commandId, args);
            }
        });
    }
}
