package net.cozic.joplin.textinput;

import android.graphics.Typeface;
import android.text.Selection;
import android.text.style.StyleSpan;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReadableArray;
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
import io.noties.markwon.editor.MarkwonEditor;
import io.noties.markwon.editor.MarkwonEditorTextWatcher;
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
                if (value) {
//                    ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
                    final Markwon markwon = Markwon.builder(reactContext)
                            .usePlugin(SoftBreakAddsNewLinePlugin.create())
                            .usePlugin(TaskListPlugin.create(reactContext))
                            .usePlugin(StrikethroughPlugin.create())
                            .build();
                    final MarkwonEditor editor = MaqrkwonEditor.builder(markwon)
//                            .useEditHandler(new AbstractEditHandler<StrongEmphasisSpan>() {
//                                @Override
//                                public void configurePersistedSpans(@NonNull PersistedSpans.Builder builder) {
//                                    // Here we define which span is _persisted_ in EditText, it is not removed
//                                    //  from EditText between text changes, but instead - reused (by changing
//                                    //  position). Consider it as a cache for spans. We could use `StrongEmphasisSpan`
//                                    //  here also, but I chose Bold to indicate that this span is not the same
//                                    //  as in off-screen rendered markdown
//                                    builder.persistSpan(Bold.class, Bold::new);
//                                }
//
//                                @Override
//                                public void handleMarkdownSpan(
//                                        @NonNull PersistedSpans persistedSpans,
//                                        @NonNull Editable editable,
//                                        @NonNull String input,
//                                        @NonNull StrongEmphasisSpan span,
//                                        int spanStart,
//                                        int spanTextLength) {
//                                    // Unfortunately we cannot hardcode delimiters length here (aka spanTextLength + 4)
//                                    //  because multiple inline markdown nodes can refer to the same text.
//                                    //  For example, `**_~~hey~~_**` - we will receive `**_~~` in this method,
//                                    //  and thus will have to manually find actual position in raw user input
//                                    final MarkwonEditorUtils.Match match =
//                                            MarkwonEditorUtils.findDelimited(input, spanStart, "**", "__");
//                                    if (match != null) {
//                                        editable.setSpan(
//                                                // we handle StrongEmphasisSpan and represent it with Bold in EditText
//                                                //  we still could use StrongEmphasisSpan, but it must be accessed
//                                                //  via persistedSpans
//                                                persistedSpans.get(Bold.class),
//                                                match.start(),
//                                                match.end(),
//                                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
//                                        );
//                                    }
//                                }
//
//                                @NonNull
//                                @Override
//                                public Class<StrongEmphasisSpan> markdownSpanType() {
//                                    return StrongEmphasisSpan.class;
//                                }
//                            })
                            .build();
//                    view.addTextChangedListener(MarkwonEditorTextWatcher.withProcess(editor));

                    view.addTextChangedListener(MarkwonEditorTextWatcher.withPreRender(
                            editor, Executors.newCachedThreadPool(), view));

//                    view.addTextChangedListener(new TextWatcher() {
//
//                        private ScheduledFuture<?> future;
//
//                        @Override
//                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//                        }
//
//                        @Override
//                        public void onTextChanged(CharSequence s, int start, int before, int count) {
////                            if (count < s.length()) {
//////                                ((Spannable) s).setSpan(new ForegroundColorSpan(Color.GRAY), start, start + count,
//////                                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//////                                node = parser.parse(s.toString());
////                            }
//                        }
//
//                        @Override
//                        public void afterTextChanged(Editable s) {
//                            if (future != null) {
//                                future.cancel(true);
//                            }
//                            future = executor.schedule(() -> UiThreadUtil.runOnUiThread(() -> {
//                                markwon.setMarkdown(view, s.toString());
//                            }), 100, TimeUnit.MILLISECONDS);
//                        }
//                    });
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
