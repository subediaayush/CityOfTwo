package com.messenger.cityoftwo;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Aayush on 4/20/2016.
 */
public class LogoFadeFragment extends Fragment {

    private static final float FADE_ALPHA_VALUE = 0;

    List<View> mViewQueue;
    private boolean mFlipping = false;
    private ImageView mProfileImageOne;
    private ImageView mProfileImageTwo;
    private ImageView mProfileImageThree;
    private ImageView mProfileImageFour;

    private boolean isViewReady = false;
    private boolean isParentReady = false;

    public LogoFadeFragment() {
    }

    public static LogoFadeFragment newInstance() {

        Bundle args = new Bundle();

        LogoFadeFragment fragment = new LogoFadeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_logo, container, false);

        mProfileImageOne = (ImageView) view.findViewById(R.id.lobby_progress_one);
        mProfileImageTwo = (ImageView) view.findViewById(R.id.lobby_progress_two);
        mProfileImageThree = (ImageView) view.findViewById(R.id.lobby_progress_three);
        mProfileImageFour = (ImageView) view.findViewById(R.id.lobby_progress_four);

//        CityOfTwo.logoBitmap = ((BitmapDrawable) ContextCompat
//                .getDrawable(getContext(), R.drawable.drawable_coyrudy)).getBitmap();
//
//        mProfileImageOne.setImageBitmap(CityOfTwo.logoBitmap);
//        mProfileImageTwo.setImageBitmap(CityOfTwo.logoBitmap);
//        mProfileImageTwo.setRotation(90);
//        mProfileImageThree.setImageBitmap(CityOfTwo.logoBitmap);
//        mProfileImageThree.setRotation(180);
//        mProfileImageFour.setImageBitmap(CityOfTwo.logoBitmap);
//        mProfileImageFour.setRotation(270);

//        mProfileImageTwo.setImageBitmap(rotateBitmap(CityOfTwo.logoBitmap, 90));
//        mProfileImageThree.setImageBitmap(rotateBitmap(CityOfTwo.logoBitmap, 180));
//        mProfileImageFour.setImageBitmap(rotateBitmap(CityOfTwo.logoBitmap, 270));

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        isViewReady = true;

        if (isParentReady) startFlipping();
    }

    private Bitmap rotateBitmap(Bitmap source, int angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    public void startFlipping() {
        if (isViewReady) {
            if (mFlipping) return;
            flip();
        } else {
            isParentReady = true;
        }
    }

    private void flip() {
        mFlipping = true;

        setupFlipper();

        final View currentView = mViewQueue.remove(0);

        currentView.animate().setDuration(500)
                .alpha(FADE_ALPHA_VALUE)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        ViewGroup parent = (ViewGroup) currentView.getParent();
                        parent.removeView(currentView);
                        parent.addView(currentView, 0);

                        currentView.setAlpha(1);
                        mViewQueue.add(currentView);

                        if (mFlipping) flip();
                    }
                });
    }

    private void setupFlipper() {
        if (mViewQueue == null) {
            mViewQueue = new ArrayList<>();
            mViewQueue.add(mProfileImageFour);
            mViewQueue.add(mProfileImageThree);
            mViewQueue.add(mProfileImageTwo);
            mViewQueue.add(mProfileImageOne);
        }
    }

    public void stopFlipping() {
        this.mFlipping = false;
    }

    public boolean isFlipping() {
        return mFlipping;
    }

    public Bitmap getCurrentBitmap() {
        ImageView currentView;
        if (!mFlipping)
            currentView = (ImageView) mViewQueue.get(0);
        else
            currentView = (ImageView) mViewQueue.get(1);

        return ((BitmapDrawable) currentView.getDrawable()).getBitmap();
    }

    public void onViewCreated() {
    }
}
