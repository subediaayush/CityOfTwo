package com.messenger.cityoftwo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * Created by Aayush on 4/20/2016.
 */
public class BackgroundAnimationFragment extends Fragment {

    View mRootView;
    ArrayList<ImageView> mViewList;
    ArrayList<Circle> mShapeList;
    Context mContext;

    int minRadius;
    int maxRadius;
    int maxShape = 8;
    int screenHeight;
    int screenWidth;

    int maxDelay = 8500;
    int minDuration = 10000;
    int maxDuration = 18500;

    Random mRandom = new Random();

    public BackgroundAnimationFragment() {
    }

    public static BackgroundAnimationFragment newInstance() {

        Bundle args = new Bundle();

        BackgroundAnimationFragment fragment = new BackgroundAnimationFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static BackgroundAnimationFragment newInstance(Bundle args) {

        BackgroundAnimationFragment fragment = new BackgroundAnimationFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_background, container, false);

        mContext = getContext();

        mViewList = new ArrayList<>(maxShape);
        mShapeList = new ArrayList<>(maxShape);

        Collections.fill(mViewList, new ImageView(mContext));
        Collections.fill(mShapeList, new Circle());

        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        Point point = new Point();
        display.getSize(point);

        maxRadius = point.x / 2;
        minRadius = point.x / 12;
        screenHeight = point.y;
        screenWidth = point.x;

        for (int position = 0; position < maxShape; position++) {
            Circle circle = new Circle();
            setupShape(circle);
            mShapeList.add(circle);

            ImageView imageView = new ImageView(mContext);
            ((ViewGroup) mRootView).addView(imageView);
            mViewList.add(imageView);
        }

        mRootView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                mRootView.getViewTreeObserver().removeOnPreDrawListener(this);
                setupLayout();
//                startAnimation();
                return true;
            }
        });

        return mRootView;
    }

    private void setupShape(Circle circle) {
        int radius = mRandom.nextInt(maxRadius - minRadius) + minRadius,
                speed = mRandom.nextInt(maxDuration - minDuration) + minDuration,
                color = ColorList.getRandomColor(),
                delay = mRandom.nextInt(maxDelay);

        circle.setRadius(radius);
        circle.setDelay(delay);
        circle.setColor(color);
        circle.setSpeed(speed);
    }

    private void startAnimation() {
        for (int i = 0; i < maxShape; i++) {
            startAnimation(i);
        }
    }

    private void startAnimation(final int position) {
        final ImageView imageView = mViewList.get(position);
        final Circle circle = mShapeList.get(position);

        final int speed = circle.getSpeed(),
                height = -circle.getRadius() * 2,
                delay = circle.getDelay();

        imageView.animate().setDuration(speed)
                .setStartDelay(delay)
                .translationY(height)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        setupShape(circle);
                        setupBitmap(imageView, circle, position);
                        startAnimation(position);
                    }
                });
    }

    private void setupLayout() {
        for (int position = 0; position < maxShape; position++) {
            ImageView imageView = mViewList.get(position);
            Circle circle = mShapeList.get(position);

            setupBitmap(imageView, circle, position);
        }
    }

    private void setupBitmap(ImageView imageView, Circle circle, int position) {
        Paint paint;
        Bitmap bitmap;
        Canvas canvas;

        int radius = circle.getRadius(),
                color = circle.getColor();
        float width = radius * 0.3f;

        float delay = circle.getDelay();

        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(width);
        paint.setColor(ColorList.getRandomColor(mContext));

        bitmap = Bitmap.createBitmap(radius * 2, radius * 2, Bitmap.Config.ARGB_8888);

        canvas = new Canvas(bitmap);
        canvas.drawCircle(radius, radius, radius - width, paint);
//        canvas.drawLine(radius * 2, 0, radius * 2, radius * 2, paint);

        int y = radius * 2,
                x = (position + 1) * (screenWidth) / (maxShape + 2),
                translation = screenHeight + radius * 2;

        imageView.setImageBitmap(bitmap);
        imageView.setY(0);
        imageView.setX(x);

        imageView.getLayoutParams().height = radius;
        imageView.getLayoutParams().width = radius;

        imageView.setTranslationY(translation);
    }

    private class Circle {
        int radius;
        int color;
        int delay;
        int speed;

        public Circle() {
            radius = 0;
            color = 0;
            delay = 0;
            speed = 0;
        }

        public int getRadius() {
            return radius;
        }

        public void setRadius(int radius) {
            this.radius = radius;
        }

        public int getColor() {
            return color;
        }

        public void setColor(int color) {
            this.color = color;
        }

        public int getDelay() {
            return delay;
        }

        public void setDelay(int delay) {
            this.delay = delay;
        }

        public int getSpeed() {
            return speed;
        }

        public void setSpeed(int speed) {
            this.speed = speed;
        }
    }
}
