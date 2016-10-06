package com.messenger.cityoftwo;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by Aayush on 6/30/2016.
 */
public class LabelledButtonLayout extends LinearLayout {

    ImageView mImageView;
    TextView mTextView;

    OnClickListener mClickListener;

    public LabelledButtonLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.LabelledButtonLayout, 0, 0);

        String buttonText = a.getString(R.styleable.LabelledButtonLayout_text);
        Drawable buttonImage = a.getDrawable(R.styleable.LabelledButtonLayout_image);
        Drawable buttonBackground = a.getDrawable(R.styleable.LabelledButtonLayout_bg);

        a.recycle();

        setOrientation(LinearLayout.VERTICAL);
        setGravity(Gravity.CENTER_HORIZONTAL);

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.layout_labelled_button, this, true);

        View view = getRootView();

        mImageView = (ImageView) getChildAt(0);
        mTextView = (TextView) getChildAt(1);
        setBackground(buttonBackground);

        if (buttonImage != null) mImageView.setImageDrawable(buttonImage);
        mTextView.setText(buttonText);

    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }
}
