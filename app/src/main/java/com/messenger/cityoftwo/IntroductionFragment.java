package com.messenger.cityoftwo;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ViewFlipper;

/**
 * Created by Aayush on 4/16/2016.
 */
public class IntroductionFragment extends DialogFragment {

    Context context;
    TestFragment testFragment;

    public IntroductionFragment() {

    }

    public static IntroductionFragment newInstance(Bundle args) {

        IntroductionFragment fragment = new IntroductionFragment();
        fragment.setArguments(args);

        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.activity_introduction, container, false);

        context = getContext();

        final ViewFlipper introViewFlipper = (ViewFlipper) rootView.findViewById(R.id.intro_flipper);
        final Button pageChangeButton = (Button) rootView.findViewById(R.id.intro_next_button);
        final View testContainer = rootView.findViewById(R.id.test_container);

        testFragment = TestFragment.newInstance(getArguments());

        getChildFragmentManager().beginTransaction()
                .replace(R.id.test_container, testFragment)
                .commit();

        pageChangeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                introViewFlipper.setDisplayedChild(introViewFlipper.getDisplayedChild() + 1);
            }
        });

        introViewFlipper.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                int totalChildViews = introViewFlipper.getChildCount() - 1;
                int position = introViewFlipper.getDisplayedChild();

                Log.i("IntroductionDialog", "Page changed");
                Log.i("IntroductionDialog", "Total children: " + totalChildViews);
                Log.i("IntroductionDialog", "Current child: " + position);

                if (position == totalChildViews - 1)
                    pageChangeButton.setText("Start test");

                if (position == totalChildViews) {
                    pageChangeButton.setVisibility(View.GONE);
                    testContainer.postInvalidate();
                }
            }
        });

        return rootView;
    }
}
