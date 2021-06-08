package net.cozic.joplin.markdown.spans;

import android.graphics.Typeface;
import android.text.style.StyleSpan;

public class ItalicSpan extends StyleSpan implements MarkdownSpan {

    public ItalicSpan() {
        super(Typeface.ITALIC);
    }
}
