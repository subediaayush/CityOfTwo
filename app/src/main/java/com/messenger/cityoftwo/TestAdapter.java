package com.messenger.cityoftwo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

//import com.squareup.picasso.Picasso;

/**
 * Created by Aayush on 2/6/2016.
 */
public abstract class TestAdapter extends PagerAdapter {

    Context context;
    ArrayList<Test> Tests;

    StringBuilder answer;

    public TestAdapter(Context context) {
        this(context, new ArrayList<Test>());

    }

    public TestAdapter(Context context, ArrayList<Test> tests) {
        this.context = context;
        Tests = tests;
        answer = new StringBuilder();
    }

    @Override
    public int getCount() {
        return Tests.size();
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.layout_test, container, false);

        List<View> animationViews = new ArrayList<>();

        ViewGroup questionContainer = (ViewGroup) view.findViewById(R.id.question_container);
        ViewGroup answerContainer = (ViewGroup) view.findViewById(R.id.answer_container);

        TextView question = (TextView) view.findViewById(R.id.test_question);
        TextView questionNumber = (TextView) view.findViewById(R.id.test_number);

        questionNumber.setText(String.format(Locale.getDefault(), "%d.", position));

        Test t = Tests.get(position);

        question.setText(t.getQuestion());

        ArrayList<AnswerPair> answers = t.getAnswers();

        int elevation = (int) CityOfTwo.dpToPixel(context, 10);

        animationViews.add(questionContainer);

        for (int i = 0; i < answers.size(); i++) {
            AnswerPair answer = answers.get(i);

            View answerView = LayoutInflater.from(context)
                    .inflate(R.layout.layout_test_option, null);

            ((TextView) answerView.findViewById(R.id.option_description)).setText(answer.first);

            String key = String.valueOf(position) + String.valueOf(i);

            Bitmap optionBitmap = CityOfTwo.answerBitmapList.get(key);
            ImageView optionImage = (ImageView) answerView.findViewById(R.id.option_image);

            if (optionBitmap != null) optionImage.setImageBitmap(optionBitmap);
            else Picasso.with(context)
                    .load(answer.second)
                    .into(optionImage);

            final int answerPosition = i;
            answerView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    OnAnswerSelected(position, answerPosition);
                }
            });

            animationViews.add(view);
            answerContainer.addView(answerView);
        }
        container.addView(view);

        enterAnimation(animationViews);

        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    private void enterAnimation(List<View> views) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        Point size = new Point();
        display.getSize(size);
        int width = size.x;

        final Interpolator interpolator = new AccelerateDecelerateInterpolator();

        for (int i = 0; i < views.size(); i++) {
            final View view = views.get(i);
            view.setTranslationX(width);
            new Handler().postDelayed(
                    new Runnable() {
                        @Override
                        public void run() {
                            view.animate().setDuration(200).setInterpolator(interpolator)
                                    .translationX(0);
                        }
                    },
                    i * 100);
        }
    }

    private void exitAnimation(final int position, final List<View> views) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        Point size = new Point();
        display.getSize(size);
        final int width = size.x;

        final Interpolator interpolator = new AccelerateDecelerateInterpolator();

        for (int i = 0; i < views.size(); i++) {
            final View view = views.get(i);
            final int viewCounter = i;
            new Handler().postDelayed(
                    new Runnable() {
                        @Override
                        public void run() {
                            view.animate().setDuration(200).setInterpolator(interpolator)
                                    .translationX(-width)
                                    .withEndAction(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (viewCounter == views.size() - 1) {
                                                OnItemsRemoved(position);
                                            }
                                        }
                                    });
                        }
                    },
                    i * 100);
        }
    }

    protected void setNextItem(int position, ViewGroup currentView) {
        List<View> animationViews = new ArrayList<>();
        for (int i = 0; i < currentView.getChildCount(); i++) {
            animationViews.add(currentView.getChildAt(i));
        }
        exitAnimation(position, animationViews);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }

    abstract void OnAnswerSelected(int question, int answer);

    abstract void OnItemsRemoved(int position);


}
