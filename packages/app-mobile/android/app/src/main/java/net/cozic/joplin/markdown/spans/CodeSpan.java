package net.cozic.joplin.markdown.spans;

import android.graphics.Color;
import android.text.TextPaint;
import android.text.style.TextAppearanceSpan;

public class CodeSpan extends TextAppearanceSpan implements MarkdownSpan {

    public CodeSpan() {
        super("monospace", 0, -1, null, null);
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        super.updateDrawState(ds);
        ds.bgColor = Color.argb(0xFF, 0xf1, 0xf1, 0xf1);
    }
}
