package com.messenger.cityoftwo;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class TriangleView extends View {

    int mColor;
    int mOffset;

    float mAngle;

    Path mPath;
    Paint mPaint;


    public TriangleView(Context context) {
        super(context);
    }

    public TriangleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.TriangleView,
                0,
                0
        );

        try {
            setColor(a.getColor(R.styleable.TriangleView_shape_color, Color.rgb(255, 165, 0)));
            setAngle(a.getFloat(R.styleable.TriangleView_rotation, 0));
            setOffset(a.getInt(R.styleable.TriangleView_offset, 0));

            Log.i("Attribute", "Color " + a.getColor(R.styleable.TriangleView_shape_color, Color.rgb(0, 0, 0)));
            Log.i("Attribute", "Color " + getColor());
            Log.i("Attribute", "Rotation " + a.getFloat(R.styleable.TriangleView_rotation, 0));
            Log.i("Attribute", "Rotation " + getAngle());
        } finally {
            a.recycle();
        }
    }

    public TriangleView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.TriangleView,
                0,
                0
        );

        try {
            setColor(a.getColor(R.styleable.TriangleView_shape_color, Color.rgb(255, 165, 0)));
            setAngle(a.getFloat(R.styleable.TriangleView_rotation, 0));

            Log.i("Attribute", "Color " + a.getColor(R.styleable.TriangleView_shape_color, Color.rgb(0, 0, 0)));
            Log.i("Attribute", "Color " + getColor());
            Log.i("Attribute", "Rotation " + a.getFloat(R.styleable.TriangleView_rotation, 0));
            Log.i("Attribute", "Rotation " + getAngle());
        } finally {
            a.recycle();
        }
    }

    public int getColor() {
        return mColor;
    }

    public void setColor(int mColor) {
        this.mColor = mColor;
        invalidate();
        requestLayout();
    }

    public float getAngle() {
        return mAngle;
    }

    public void setAngle(float mAngle) {
        this.mAngle = mAngle;
        invalidate();
        requestLayout();
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth(),
                height = getHeight();

        if (mPath == null) mPath = new Path();

        mPath.moveTo(width + mOffset, -mOffset);
        mPath.lineTo(-mOffset, -mOffset);
        mPath.lineTo(-mOffset, height + mOffset);
        mPath.lineTo(width + mOffset, -mOffset);
        mPath.close();

        if (mPaint == null) mPaint = new Paint();
        mPaint.setColor(mColor);

        canvas.rotate(mAngle);
        canvas.drawPath(mPath, mPaint);
    }

    public void setOffset(int offset) {
        this.mOffset = offset;
    }
}