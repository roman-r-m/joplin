package net.cozic.joplin.markdown;

import android.graphics.Typeface;
import android.os.Build;
import android.text.Editable;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.LeadingMarginSpan;
import android.text.style.QuoteSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.TypefaceSpan;
import android.util.Log;

import androidx.annotation.NonNull;

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.views.textinput.ReactEditText;
import com.facebook.react.views.textinput.ReactTextInputManager;

import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.BlockQuote;
import org.commonmark.node.Emphasis;
import org.commonmark.node.Heading;
import org.commonmark.node.ListItem;
import org.commonmark.node.Node;
import org.commonmark.node.OrderedList;
import org.commonmark.node.SourceSpan;
import org.commonmark.node.StrongEmphasis;
import org.commonmark.parser.IncludeSourceSpans;
import org.commonmark.parser.Parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MarkdownPackage implements ReactPackage {
    @NonNull
    @Override
    public List<NativeModule> createNativeModules(@NonNull ReactApplicationContext reactContext) {
        return Collections.emptyList();
    }

    @NonNull
    @Override
    public List<ViewManager> createViewManagers(@NonNull ReactApplicationContext reactContext) {
        return Collections.singletonList(new ReactTextInputManager() {
            @ReactProp(name = "enableMarkdown")
            public void setEnableMarkdown(ReactEditText editText, boolean enableMarkdown) {
                if (enableMarkdown && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    Parser parser = Parser.builder()
                            .includeSourceSpans(IncludeSourceSpans.BLOCKS_AND_INLINES)
                            .build();

                    editText.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {

                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            long start = System.currentTimeMillis();
//                s.clearSpans();
                            for (TypefaceSpan span : s.getSpans(0, s.length(), TypefaceSpan.class)) {
                                s.removeSpan(span);
                            }
                            for (RelativeSizeSpan span : s.getSpans(0, s.length(), RelativeSizeSpan.class)) {
                                s.removeSpan(span);
                            }
                            for (LeadingMarginSpan.Standard span : s.getSpans(0, s.length(), LeadingMarginSpan.Standard.class)) {
                                s.removeSpan(span);
                            }
                            for (QuoteSpan span : s.getSpans(0, s.length(), QuoteSpan.class)) {
                                s.removeSpan(span);
                            }

                            Node node = parser.parse(s.toString());
                            ArrayList<Integer> lineStarts = new ArrayList<>();
                            lineStarts.add(0);
                            for (int i = 0; i < s.length(); i++) {
                                if (s.charAt(i) == '\n') {
                                    lineStarts.add(i + 1);
                                }
                            }
                            final float[] HEADING_SIZES = { 2.0f, 1.5f, 1.17f, 1.0f, .83f, .67f, };

                            node.accept(new AbstractVisitor() {

                                private void setSpans(Node node, Object... spans) {
                                    SourceSpan start = node.getSourceSpans().get(0);
                                    SourceSpan end = node.getSourceSpans().get(node.getSourceSpans().size() - 1);
                                    int startPos = lineStarts.get(start.getLineIndex()) + start.getColumnIndex();
                                    int endPos = lineStarts.get(end.getLineIndex()) + end.getColumnIndex() + end.getLength();
                                    for (Object span : spans) {
                                        s.setSpan(span, startPos, endPos, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    }
                                }

                                @Override
                                public void visit(Emphasis emphasis) {
                                    super.visit(emphasis);
                                    setSpans(emphasis, new TypefaceSpan(Typeface.DEFAULT_BOLD));
                                }

                                @Override
                                public void visit(Heading heading) {
                                    setSpans(heading, new TypefaceSpan(Typeface.DEFAULT_BOLD), new RelativeSizeSpan(HEADING_SIZES[heading.getLevel()]));
                                    super.visit(heading);
                                }

                                @Override
                                public void visit(ListItem listItem) {
                                    setSpans(listItem, new LeadingMarginSpan.Standard(30));
                                    super.visit(listItem);
                                }

                                @Override
                                public void visit(OrderedList orderedList) {
                                    setSpans(orderedList, new LeadingMarginSpan.Standard(30));
                                    super.visit(orderedList);
                                }

                                @Override
                                public void visit(StrongEmphasis strongEmphasis) {
                                    setSpans(strongEmphasis, new TypefaceSpan(Typeface.DEFAULT_BOLD));
                                    super.visit(strongEmphasis);
                                }

                                @Override
                                public void visit(BlockQuote blockQuote) {
                                    setSpans(blockQuote, new QuoteSpan());
                                    super.visit(blockQuote);
                                }


                            });

                            Log.w("ZZZ", "delay: " + (System.currentTimeMillis() - start));
                        }
                    });
                }
            }
        });
    }
}
