package net.cozic.joplin.markdown;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.text.Editable;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.LeadingMarginSpan;
import android.text.style.StyleSpan;
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

import net.cozic.joplin.markdown.spans.BlockquoteSpan;
import net.cozic.joplin.markdown.spans.BoldSpan;
import net.cozic.joplin.markdown.spans.CodeSpan;
import net.cozic.joplin.markdown.spans.HeadingSpan;
import net.cozic.joplin.markdown.spans.ItalicSpan;
import net.cozic.joplin.markdown.spans.MarkdownSpan;
import net.cozic.joplin.markdown.spans.UnderlineSpan;

import org.commonmark.Extension;
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension;
import org.commonmark.ext.ins.Ins;
import org.commonmark.ext.ins.InsExtension;
import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.BlockQuote;
import org.commonmark.node.BulletList;
import org.commonmark.node.Code;
import org.commonmark.node.CustomNode;
import org.commonmark.node.Delimited;
import org.commonmark.node.Emphasis;
import org.commonmark.node.FencedCodeBlock;
import org.commonmark.node.Heading;
import org.commonmark.node.IndentedCodeBlock;
import org.commonmark.node.Link;
import org.commonmark.node.ListItem;
import org.commonmark.node.Node;
import org.commonmark.node.OrderedList;
import org.commonmark.node.SourceSpan;
import org.commonmark.node.StrongEmphasis;
import org.commonmark.node.ThematicBreak;
import org.commonmark.parser.IncludeSourceSpans;
import org.commonmark.parser.Parser;
import org.commonmark2.ext.autolink.AutolinkExtension;
import org.commonmark2.ext.task.list.items.TaskListItemMarker;
import org.commonmark2.ext.task.list.items.TaskListItemsExtension;

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

            private TextWatcher mdRenderer;

            @ReactProp(name = "enableMarkdown")
            public void setEnableMarkdown(ReactEditText editText, boolean enableMarkdown) {
                if (enableMarkdown && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    List<Extension> extensions = new ArrayList<>();
                    extensions.add(AutolinkExtension.create());
                    extensions.add(StrikethroughExtension.create());
                    extensions.add(InsExtension.create());
                    extensions.add(TaskListItemsExtension.create());

                    Parser parser = Parser.builder()
                            .extensions(extensions)
                            .includeSourceSpans(IncludeSourceSpans.BLOCKS_AND_INLINES)
                            .build();

                    mdRenderer = new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                            Log.i("ZZZ", "beforeTextChanged: start=" + start + ", count=" + count + ", after=" + after);
                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {

                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            long start = System.currentTimeMillis();

                            for (TypefaceSpan span : s.getSpans(0, s.length(), TypefaceSpan.class)) {
                                s.removeSpan(span);
                            }
                            for (StyleSpan span : s.getSpans(0, s.length(), StyleSpan.class)) {
                                s.removeSpan(span);
                            }
                            for (MarkdownSpan span : s.getSpans(0, s.length(), MarkdownSpan.class)) {
                                s.removeSpan(span);
                            }
                            for (LeadingMarginSpan.Standard span : s.getSpans(0, s.length(), LeadingMarginSpan.Standard.class)) {
                                s.removeSpan(span);
                            }
                            for (ForegroundColorSpan span : s.getSpans(0, s.length(), ForegroundColorSpan.class)) {
                                s.removeSpan(span);
                            }
                            for (CheckboxSpan span : s.getSpans(0, s.length(), CheckboxSpan.class)) {
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


                            node.accept(new AbstractVisitor() {

                                private void setSpans(Node node, Object... spans) {
                                    SourceSpan start = node.getSourceSpans().get(0);
                                    SourceSpan end = node.getSourceSpans().get(node.getSourceSpans().size() - 1);
                                    int startPos = lineStarts.get(start.getLineIndex()) + start.getColumnIndex();
                                    int endPos = lineStarts.get(end.getLineIndex()) + end.getColumnIndex() + end.getLength();
                                    if (node instanceof Delimited) {
                                        startPos += ((Delimited) node).getOpeningDelimiter().length();
                                        endPos -= ((Delimited) node).getClosingDelimiter().length();
                                    }
                                    for (Object span : spans) {
                                        s.setSpan(span, startPos, endPos, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    }
                                    if (node instanceof Delimited) {
                                        int startDelimiterLen = ((Delimited) node).getOpeningDelimiter().length();
                                        int endDelimiterLen = ((Delimited) node).getClosingDelimiter().length();
                                        s.setSpan(new ForegroundColorSpan(Color.LTGRAY), startPos - startDelimiterLen, startPos, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                        s.setSpan(new ForegroundColorSpan(Color.LTGRAY), endPos, endPos + endDelimiterLen, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    }
                                }

                                @Override
                                public void visit(Emphasis emphasis) {
                                    super.visit(emphasis);
                                    setSpans(emphasis, new ItalicSpan());
                                }

                                @Override
                                public void visit(Heading heading) {
                                    if (heading.getLevel() < 6) {
                                        setSpans(heading, new BoldSpan(), new HeadingSpan(heading.getLevel()));
                                    }
                                    super.visit(heading);
                                }

                                @Override
                                public void visit(ListItem listItem) {
//                                    setSpans(listItem, new LeadingMarginSpan.Standard(30));
                                    super.visit(listItem);
                                }

                                @Override
                                public void visit(OrderedList orderedList) {
                                    setSpans(orderedList,
                                            new LeadingMarginSpan.Standard(30),
                                            new ListAutocompleteSpan(orderedList.getDelimiter() + " "));
                                    super.visit(orderedList);
                                }

                                @Override
                                public void visit(BulletList bulletList) {
                                    setSpans(bulletList,
                                            new LeadingMarginSpan.Standard(30),
                                            new ListAutocompleteSpan(bulletList.getBulletMarker() + " "));
                                    super.visit(bulletList);
                                }

                                @Override
                                public void visit(StrongEmphasis strongEmphasis) {
                                    setSpans(strongEmphasis, new TypefaceSpan(Typeface.DEFAULT_BOLD));
                                    super.visit(strongEmphasis);
                                }

                                @Override
                                public void visit(BlockQuote blockQuote) {
                                    setSpans(blockQuote, new BlockquoteSpan());
                                    super.visit(blockQuote);
                                }

                                @Override
                                public void visit(Code code) {
                                    super.visit(code);
                                    setSpans(code, new CodeSpan());
                                }

                                @Override
                                public void visit(IndentedCodeBlock indentedCodeBlock) {
                                    super.visit(indentedCodeBlock);
                                    setSpans(indentedCodeBlock, new LeadingMarginSpan.Standard(30), new CodeSpan());
                                }

                                @Override
                                public void visit(FencedCodeBlock fencedCodeBlock) {
                                    super.visit(fencedCodeBlock);
                                    setSpans(fencedCodeBlock, new CodeSpan());
                                }

                                @Override
                                public void visit(ThematicBreak thematicBreak) {
                                    super.visit(thematicBreak);
                                }

                                @Override
                                public void visit(Link link) {
                                    super.visit(link);
                                    // use URLSpan
                                }

                                @Override
                                public void visit(CustomNode customNode) {
                                    if (customNode instanceof TaskListItemMarker) {
                                        TaskListItemMarker taskListItemMarker = (TaskListItemMarker) customNode;

                                        SourceSpan span = customNode.getSourceSpans().get(0);
                                        int startPos = lineStarts.get(span.getLineIndex()) + span.getColumnIndex();
                                        int endPos = startPos + 3;
                                        // TODO requires LinkMovementMethod to be set on TextView
                                        s.setSpan(new CheckboxSpan(s, startPos), startPos, endPos, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    } else if (customNode instanceof Ins) {
                                        Ins ins = (Ins) customNode;
                                        setSpans(ins, new UnderlineSpan());
                                    }
                                    super.visit(customNode);
                                }
                            });

                            Log.w("ZZZ", "delay: " + (System.currentTimeMillis() - start));
                        }
                    };
                    editText.addTextChangedListener(mdRenderer);
                } else {
                    editText.removeTextChangedListener(mdRenderer);
                }
            }
        });
    }
}
