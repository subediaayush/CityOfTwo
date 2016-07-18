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

/**
 * Created by Aayush on 4/20/2016.
 */
public class LogoFadeFragment extends Fragment {

    Queue<View> mViewQueue;
    private boolean mFlipping = false;

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

        ImageView mProfileImageOne = (ImageView) view.findViewById(R.id.lobby_progress_one);
        ImageView mProfileImageTwo = (ImageView) view.findViewById(R.id.lobby_progress_two);
        ImageView mProfileImageThree = (ImageView) view.findViewById(R.id.lobby_progress_three);
        ImageView mProfileImageFour = (ImageView) view.findViewById(R.id.lobby_progress_four);

        mProfileImageOne.setImageBitmap(CityOfTwo.logoBitmap);
        mProfileImageTwo.setImageBitmap(rotateBitmap(CityOfTwo.logoBitmap, 90));
        mProfileImageThree.setImageBitmap(rotateBitmap(CityOfTwo.logoBitmap, 180));
        mProfileImageFour.setImageBitmap(rotateBitmap(CityOfTwo.logoBitmap, 270));

        mViewQueue = new Queue<>();
        mViewQueue.enqueue(mProfileImageOne);
        mViewQueue.enqueue(mProfileImageTwo);
        mViewQueue.enqueue(mProfileImageThree);
        mViewQueue.enqueue(mProfileImageFour);

        return view;
    }

    private Bitmap rotateBitmap(Bitmap source, int angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    public void startFlipping() {
        mFlipping = true;

        final View currentView = mViewQueue.dequeue();

        currentView.animate().setDuration(500)
                .alpha(0.f)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        ViewGroup parent = (ViewGroup) currentView.getParent();
                        parent.removeView(currentView);
                        parent.addView(currentView, 0);

                        currentView.setAlpha(1.f);
                        mViewQueue.enqueue(currentView);

                        if (mFlipping) startFlipping();
                    }
                });
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
}
