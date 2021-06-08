package net.cozic.joplin.markdown.spans;

import android.text.Editable;
import android.text.style.ClickableSpan;
import android.view.View;

import androidx.annotation.NonNull;

public class CheckboxSpan extends ClickableSpan {

    private final Editable target;
    private final int startPos;

    public CheckboxSpan(Editable target, int startPos) {

        this.target = target;
        this.startPos = startPos;
    }

    @Override
    public void onClick(@NonNull View widget) {
        if (target.charAt(startPos + 1) == ' ') {
            target.replace(startPos + 1, startPos + 2, "X");
        } else {
            target.replace(startPos + 1, startPos + 2, " ");
        }
    }
}
