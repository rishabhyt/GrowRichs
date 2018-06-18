package com.nhattuan.growrichs.ultil;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

public class TextviewPlus extends android.support.v7.widget.AppCompatTextView {

    public TextviewPlus(Context context) {
        super(context);
        init();
    }

    public TextviewPlus(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TextviewPlus(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }



    private void init() {
        if (!isInEditMode()) {
            Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/helveticaneuebold.ttf");
            setTypeface(tf);
        }
    }
}