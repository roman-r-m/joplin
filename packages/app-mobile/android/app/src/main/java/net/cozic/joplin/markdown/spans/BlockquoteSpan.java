package net.cozic.joplin.markdown.spans;

import android.graphics.Color;
import android.os.Build;
import android.text.style.QuoteSpan;

import androidx.annotation.RequiresApi;

public class BlockquoteSpan extends QuoteSpan implements MarkdownSpan{

    @RequiresApi(api = Build.VERSION_CODES.P)
    public BlockquoteSpan() {
        super(Color.LTGRAY, 2 * STANDARD_STRIPE_WIDTH_PX, STANDARD_GAP_WIDTH_PX);
    }
}
