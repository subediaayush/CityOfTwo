package com.messenger.cityoftwo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

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

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final ImageView logo = (ImageView) view.findViewById(R.id.intro_coyrudy_logo);
        int width = logo.getLayoutParams().width,
                height = logo.getLayoutParams().height;

        Picasso.with(context)
                .load(R.drawable.mipmap_1)
                .resize(width, height)
                .into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        logo.setImageBitmap(bitmap);
                        CityOfTwo.logoBitmap = bitmap;
                        Log.i("Logo Bitmap", "Bitmap loaded");
                    }

                    @Override
                    public void onBitmapFailed(Drawable errorDrawable) {

                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                    }
                });
    }
}
