package net.cozic.joplin.markdown.spans;

import android.text.Editable;
import android.text.TextWatcher;

import net.cozic.joplin.markdown.spans.MarkdownSpan;

public class ListAutocompleteSpan implements TextWatcher, MarkdownSpan {

    private final CharSequence marker;
    private int pos;

    public ListAutocompleteSpan(CharSequence marker) {
        this.marker = marker;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (before == 0 && count == 1 && s.charAt(start) == '\n') {
            pos = start + 1;
        } else {
            pos = -1;
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (pos > 0) {
            s.insert(pos, marker);
        }
    }
}
