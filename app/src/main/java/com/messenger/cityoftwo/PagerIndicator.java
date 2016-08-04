package com.messenger.cityoftwo;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Aayush on 8/4/2016.
 */
public class PagerIndicator extends Fragment {
    private List<View> views;
    private int selected = 0;
    private Context context;

    private ViewGroup container;

    public PagerIndicator() {
    }

    public static PagerIndicator newInstance(Context context) {

        Bundle args = new Bundle();

        PagerIndicator fragment = new PagerIndicator();
        fragment.setArguments(args);
        fragment.context = context;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        views = new ArrayList<>();

        LinearLayout parentLayout = new LinearLayout(context);
        parentLayout.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        int value10DP = (int) CityOfTwo.dpToPixel(context, 10);

        parentLayout.setGravity(Gravity.CENTER_VERTICAL);
        parentLayout.setDividerPadding(value10DP);

        this.container = parentLayout;
        return parentLayout;
    }

    protected void addNewIndicator() {
        View view = new View(context);
        view.setBackground(ContextCompat.getDrawable(context, R.drawable.indicator));
        view.setAlpha(.35f);
        int dimen = (int) CityOfTwo.dpToPixel(context, 10);

        view.setLayoutParams(new LinearLayout.LayoutParams(dimen, dimen));

        view.setScaleX(.8f);
        view.setScaleY(.8f);

        container.addView(view);
        views.add(view);

    }

    protected void setSelected(int position) {
        notifyHolder(selected, position);
        selected = position;
    }

    private void notifyHolder(int oldPosition, int newPosition) {
        View oldView = views.get(oldPosition);
        View newView = views.get(newPosition);

        oldView.animate().setDuration(150).scaleX(.8f).scaleY(.8f).alpha(.25f);
        newView.animate().setDuration(200).scaleX(1).scaleY(1).alpha(1);
    }
}
