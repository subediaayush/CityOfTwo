package com.messenger.cityoftwo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Aayush on 4/19/2016.
 */
public abstract class IntroductionActivityBase extends AppCompatActivity {

    protected PagerAdapter pagerAdapter;
    private List<Fragment> fragments;
    private NonSwipeableViewPager viewPager;
    private ViewGroup indicatorContainer;

    private Button nextButton;
    private Button doneButton;
    private int currentPage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_introduction);

        fragments = new ArrayList<>();
        viewPager = (NonSwipeableViewPager) findViewById(R.id.view_pager);
        pagerAdapter = new PagerAdapter(getSupportFragmentManager(), fragments);
        viewPager.setAdapter(pagerAdapter);

        changePagerScroller();
        currentPage = 0;

        indicatorContainer = (ViewGroup) findViewById(R.id.indicator_container);

        nextButton = (Button) findViewById(R.id.next);
        doneButton = (Button) findViewById(R.id.done);

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextButtonClicked((Button) v, viewPager.getCurrentItem());
            }
        });

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doneButtonClicked((Button) v);
            }
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                pageChanged(position);
                setCurrentPage(position);
                if (position + 1 == fragments.size()) showDoneButton();
                else hideDoneButton();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    private void changePagerScroller() {
        try {
            Field mScroller = null;
            mScroller = ViewPager.class.getDeclaredField("mScroller");
            mScroller.setAccessible(true);
            ViewPagerScroller scroller = new ViewPagerScroller(viewPager.getContext());
            mScroller.set(viewPager, scroller);
        } catch (Exception e) {
            Log.e("Introduction Activity", "Error changing scroller", e);
        }
    }

    protected void setupPager() {
//        int totalItems = fragments.size();
//
//        viewPager.setCurrentItem(totalItems - 1);
//        viewPager.setCurrentItem(0);

    }

    protected void addFragment(Fragment fragment) {
        fragments.add(fragment);
        pagerAdapter.notifyDataSetChanged();

        addNewIndicator();

        if (fragments.size() <= 1)
            showDoneButton();
        else
            hideDoneButton();
    }

    private void showDoneButton() {
        nextButton.setVisibility(View.GONE);
        doneButton.setVisibility(View.VISIBLE);
    }

    private void hideDoneButton() {
        nextButton.setVisibility(View.VISIBLE);
        doneButton.setVisibility(View.GONE);
    }

    protected void showNextPage() {
        int nextItem = viewPager.getCurrentItem() + 1,
                totalItems = fragments.size();

        viewPager.setCurrentItem(nextItem, true);
        if (nextItem + 1 == totalItems) {
            showDoneButton();
        }

    }

    protected void addNewIndicator() {
        View view = new View(this);
        view.setBackground(ContextCompat.getDrawable(this, R.drawable.indicator));
        view.setAlpha(.35f);
        int dimen = (int) CityOfTwo.dpToPixel(this, 10);

        view.setLayoutParams(new LinearLayout.LayoutParams(dimen, dimen));

        view.setScaleX(.1f);
        view.setScaleY(.1f);

        if (indicatorContainer.getChildCount() == 0)
            view.animate().setDuration(250).scaleX(1).scaleY(1).alpha(1);
        else view.animate().setDuration(250).scaleX(.8f).scaleY(.8f);

        indicatorContainer.addView(view);
    }

    protected void setCurrentPage(int position) {
        notifyHolder(currentPage, position);
        currentPage = position;
    }

    private void notifyHolder(int oldPosition, int newPosition) {
        View oldView = indicatorContainer.getChildAt(oldPosition);
        View newView = indicatorContainer.getChildAt(newPosition);

        oldView.animate().setDuration(150).scaleX(.8f).scaleY(.8f).alpha(.25f);
        newView.animate().setDuration(200).scaleX(1).scaleY(1).alpha(1);
    }

    protected Button getNextButton() {
        return nextButton;
    }

    protected ViewPager getPager() {
        return viewPager;
    }

    protected PagerAdapter getAdapter() {
        return pagerAdapter;
    }

    protected Fragment getItem(int position) {
        return pagerAdapter.getItem(position);
    }

    protected int getTotalItems() {
        return fragments.size();
    }

    protected abstract void nextButtonClicked(Button v, int position);

    protected abstract void doneButtonClicked(Button v);

    protected abstract void pageChanged(int position);
}