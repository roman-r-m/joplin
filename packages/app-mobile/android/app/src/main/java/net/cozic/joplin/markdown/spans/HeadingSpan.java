package net.cozic.joplin.markdown.spans;

import android.text.style.RelativeSizeSpan;

public class HeadingSpan extends RelativeSizeSpan implements MarkdownSpan {

    private static final float[] HEADING_SIZES = {2.0f, 1.5f, 1.17f, 1.0f, .83f, .83f,};

    public HeadingSpan(int level) {
        super(HEADING_SIZES[level]);
    }
}
