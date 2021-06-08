package net.cozic.joplin.markdown.spans;

import android.graphics.Typeface;
import android.text.style.StyleSpan;

public class BoldSpan extends StyleSpan implements MarkdownSpan {

    public BoldSpan() {
        super(Typeface.BOLD);
    }
}
