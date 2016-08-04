package com.messenger.cityoftwo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.widget.ViewFlipper;

/**
 * Created by Aayush on 7/18/2016.
 */
public class IndicatorViewFlipper extends ViewFlipper {

    Paint paint = new Paint();
    Context context;
    Boolean drawIndicators = true;

    public IndicatorViewFlipper(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        if (!drawIndicators) return;

        int width = getWidth();

        float margin = 2;
        float radius = 5;
        float cx = width / 2 - ((radius + margin) * 2 * getChildCount() / 2);
        float cy = getHeight() - 30;

        canvas.save();

        for (int i = 0; i < getChildCount(); i++) {
            if (i == getDisplayedChild()) {
                paint.setColor(ContextCompat.getColor(context, R.color.colorAccent));
                canvas.drawCircle(cx, cy, radius, paint);

            } else {
                paint.setColor(Color.GRAY);
                canvas.drawCircle(cx, cy, radius, paint);
            }
            cx += 2 * (radius + margin);
        }
        canvas.restore();
    }

    public void showPageIndicator(boolean showIndicator) {
        drawIndicators = showIndicator;
    }

}