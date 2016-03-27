package com.messenger.cityoftwo;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by Aayush on 3/19/2016.
 */
public class FontTextView extends TextView {
    public FontTextView(Context context) {
        super(context);
        setTypeface(Century.getInstance(context).getTypeFace());
    }

    public FontTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setTypeface(Century.getInstance(context).getTypeFace());
    }

    public FontTextView(Context context, AttributeSet attrs,
                        int defStyle) {
        super(context, attrs, defStyle);
        setTypeface(Century.getInstance(context).getTypeFace());
    }
}
