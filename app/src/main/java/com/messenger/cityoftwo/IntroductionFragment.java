package com.messenger.cityoftwo;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Aayush on 4/16/2016.
 */
public class IntroductionFragment extends Fragment {

    Context context;
    TestFragment testFragment;

    public IntroductionFragment() {
    }

    public static IntroductionFragment newInstance() {
        return new IntroductionFragment();
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.layout_intro_coyrudy, container, false);

        context = getContext();

        return rootView;
    }
}
