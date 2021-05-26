package net.cozic.joplin.textinput;

import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.InputType;
import android.text.Selection;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewManager;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.views.textinput.ReactEditText;
import com.facebook.react.views.textinput.ReactTextInputManager;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;

import io.noties.markwon.AbstractMarkwonPlugin;
import io.noties.markwon.Markwon;
import io.noties.markwon.SoftBreakAddsNewLinePlugin;
import io.noties.markwon.core.MarkwonTheme;
import io.noties.markwon.core.spans.StrongEmphasisSpan;
import io.noties.markwon.editor.AbstractEditHandler;
import io.noties.markwon.editor.MarkwonEditor;
import io.noties.markwon.editor.MarkwonEditorTextWatcher;
import io.noties.markwon.editor.MarkwonEditorUtils;
import io.noties.markwon.editor.PersistedSpans;
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin;
import io.noties.markwon.ext.tasklist.TaskListPlugin;

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

            @Override
            public ReactEditText createViewInstance(ThemedReactContext context) {
                ReactEditText editText = new ReactEditText(context);
                int inputType = editText.getInputType();
                editText.setInputType(inputType & (~InputType.TYPE_TEXT_FLAG_MULTI_LINE));
                editText.setReturnKeyType("done");
                editText.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
                return editText;
            }

            @ReactProp(name = "enableMdHighlight")
            public void setEnableMdHighlight(ReactEditText view, boolean value) {
                if (value) {
                    final Markwon markwon = Markwon.builder(reactContext)
                            .usePlugin(SoftBreakAddsNewLinePlugin.create())
                            .usePlugin(TaskListPlugin.create(reactContext))
                            .usePlugin(StrikethroughPlugin.create())
                            .usePlugin(new AbstractMarkwonPlugin() {
                                @Override
                                public void configureTheme(@NonNull MarkwonTheme.Builder builder) {
                                    builder.headingTypeface(Typeface.DEFAULT_BOLD);
                                    float[] multipliers = new float[] {2.0f, 1.5f, 1.3f, 1.2f, 1.1f, 1.0f};
                                    builder.headingTextSizeMultipliers(multipliers);

                                    builder.codeTextColor(Color.GREEN);
                                    builder.codeBackgroundColor(Color.GRAY);

                                    builder.codeBlockTextColor(Color.GREEN);
                                    builder.codeBlockBackgroundColor(Color.GRAY);
                                }
                            })
                            .build();

                    final MarkwonEditor editor = MarkwonEditor.builder(markwon)
                            .useEditHandler(new AbstractEditHandler<StrongEmphasisSpan>() {
                                @Override
                                public void configurePersistedSpans(@NonNull PersistedSpans.Builder builder) {
                                    // Here we define which span is _persisted_ in EditText, it is not removed
                                    //  from EditText between text changes, but instead - reused (by changing
                                    //  position). Consider it as a cache for spans. We could use `StrongEmphasisSpan`
                                    //  here also, but I chose Bold to indicate that this span is not the same
                                    //  as in off-screen rendered markdown
                                    builder.persistSpan(Bold.class, Bold::new);
                                }

                                @Override
                                public void handleMarkdownSpan(
                                        @NonNull PersistedSpans persistedSpans,
                                        @NonNull Editable editable,
                                        @NonNull String input,
                                        @NonNull StrongEmphasisSpan span,
                                        int spanStart,
                                        int spanTextLength) {
                                    // Unfortunately we cannot hardcode delimiters length here (aka spanTextLength + 4)
                                    //  because multiple inline markdown nodes can refer to the same text.
                                    //  For example, `**_~~hey~~_**` - we will receive `**_~~` in this method,
                                    //  and thus will have to manually find actual position in raw user input
                                    final MarkwonEditorUtils.Match match =
                                            MarkwonEditorUtils.findDelimited(input, spanStart, "**", "__");
                                    if (match != null) {
                                        editable.setSpan(
                                                // we handle StrongEmphasisSpan and represent it with Bold in EditText
                                                //  we still could use StrongEmphasisSpan, but it must be accessed
                                                //  via persistedSpans
                                                persistedSpans.get(Bold.class),
                                                match.start(),
                                                match.end(),
                                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                                        );
                                    }
                                }

                                @NonNull
                                @Override
                                public Class<StrongEmphasisSpan> markdownSpanType() {
                                    return StrongEmphasisSpan.class;
                                }
                            })
                            .build();
//                    view.addTextChangedListener(MarkwonEditorTextWatcher.withProcess(editor));

                    view.addTextChangedListener(MarkwonEditorTextWatcher.withPreRender(
                            editor, Executors.newCachedThreadPool(), view));
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

    private static final class Bold extends StyleSpan {

        public Bold() {
            super(Typeface.BOLD);
        }
    }
}
