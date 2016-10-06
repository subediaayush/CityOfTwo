package com.messenger.cityoftwo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.paolorotolo.appintro.ScrollerCustomDuration;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

//import com.squareup.picasso.Picasso;

/**
 * Created by Aayush on 2/5/2016.
 */
public class TestFragment extends Fragment {

    TestAdapter testAdapter;
    Context context;
    ArrayList<Test> questionList;
    RelativeLayout questionContainer;
    LinearLayout answerContainer;
    TextView testQuestion;
    int currentItem = 0;
    private NonSwipeableViewPager testPager;
    private ScrollerCustomDuration scroller = null;

    private TestEventListener testEventListener;
    private boolean testFinished;

    public TestFragment() {
    }

    public static TestFragment newInstance() {
        TestFragment fragment = new TestFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_test, container, false);

        context = getContext();

//        testPager = (NonSwipeableViewPager) rootView.findViewById(R.id.test_viewpager);
        questionContainer = (RelativeLayout) rootView.findViewById(R.id.question_container);
        answerContainer = (LinearLayout) rootView.findViewById(R.id.answer_container);
        testQuestion = (TextView) rootView.findViewById(R.id.test_question);

        Bundle data = getArguments();

        try {
            JSONArray Questions = new JSONArray(data.getString(CityOfTwo.KEY_TEST));
            questionList = GetQuestionList(Questions);
            Log.i("TestFragment", "Test Received");
        } catch (JSONException e) {
            e.printStackTrace();
            Log.i("TestFragment", "Test Not Received");
        }

        return rootView;
    }

    public void showItem(final int position) {
        if (position != 0)
            exitAnimation(position);
        else
            enterAnimation(position);
    }

    private void exitAnimation(final int position) {

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        Point size = new Point();
        display.getSize(size);
        final int width = size.x;

        final android.view.animation.Interpolator interpolator = new AccelerateInterpolator();

        final List<View> animationViews = new ArrayList<>();

        animationViews.add(questionContainer);

        for (int i = 0; i < answerContainer.getChildCount(); i++)
            animationViews.add(answerContainer.getChildAt(i));

        for (int i = 0; i < animationViews.size(); i++) {
            final View view = animationViews.get(i);
            final int viewCounter = i;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    view.animate().setInterpolator(interpolator)
                            .setDuration(300).translationX(-width)
                            .withEndAction(new Runnable() {
                                @Override
                                public void run() {
                                    if (viewCounter == animationViews.size() - 1)
                                        if (testFinished && testEventListener != null) {
                                            new Handler().postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    testEventListener.OnAllQuestionsAnswered();
                                                }
                                            }, 100);
                                        } else {
                                            enterAnimation(position);
                                        }
                                }
                            });
                }
            }, i * 100);
        }

    }

    private void enterAnimation(int position) {
        setupItem(position);
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        Point size = new Point();
        display.getSize(size);
        final int width = size.x;

        final android.view.animation.Interpolator interpolator = new DecelerateInterpolator();

        final List<View> animationViews = new ArrayList<>();

        animationViews.add(questionContainer);

        for (int i = 0; i < answerContainer.getChildCount(); i++)
            animationViews.add(answerContainer.getChildAt(i));

        for (int i = 0; i < animationViews.size(); i++) {
            final View view = animationViews.get(i);
            view.setTranslationX(width);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    view.animate().setInterpolator(interpolator)
                            .setDuration(300).translationX(0);
                }
            }, i * 100);
        }
    }

    private void setupItem(final int position) {
        Test test = questionList.get(position);

        testQuestion.setText(test.getQuestion());
        answerContainer.removeAllViews();

        List<AnswerPair> answers = test.getAnswers();
        for (int i = 0; i < answers.size(); i++) {
            AnswerPair answer = answers.get(i);

            final View answerView = LayoutInflater.from(context)
                    .inflate(R.layout.layout_test_option, null);
            answerView.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));

            String key = String.valueOf(position) + String.valueOf(i);
            Bitmap optionBitmap = CityOfTwo.answerBitmapList.get(key);
            ImageView optionImage = (ImageView) answerView.findViewById(R.id.option_image);
            TextView optionDesc = (TextView) answerView.findViewById(R.id.option_description);

            if (optionBitmap != null)
                optionImage.setImageBitmap(optionBitmap);
            else
                Picasso.with(context)
                        .load(answer.second)
                        .into(optionImage);

            optionDesc.setText(answer.first);

            final int answerPosition = i;
            answerView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (testEventListener != null)
                        testEventListener.OnQuestionAnswered(position, answerPosition);
                    for (int i = 0; i < answerContainer.getChildCount(); i++) {
                        View view = answerContainer.getChildAt(i)
                                .findViewById(R.id.answer_background);

                        if (i == answerPosition)
                            view.setBackgroundColor(ContextCompat.getColor(
                                    context,
                                    R.color.colorPrimaryLight
                            ));
                        else
                            view.setBackgroundColor(ContextCompat.getColor(
                                    context,
                                    android.R.color.white
                            ));
                    }
                }
            });
            answerContainer.addView(answerView);
        }
    }

    private ArrayList<Test> GetQuestionList(JSONArray questions) throws JSONException {
        ArrayList<Test> Tests = new ArrayList<>();

        for (int i = 0; i < questions.length(); i++) {
            JSONObject question = questions.getJSONObject(i);

            String q = question.keys().next();
            JSONArray answers = question.getJSONArray(q);

            ArrayList<AnswerPair> a = new ArrayList<>();

            for (int j = 0; j < answers.length(); j++) {
                String description = (String) ((JSONArray) answers.get(j)).get(0),
                        url = (String) ((JSONArray) answers.get(j)).get(1);

                a.add(new AnswerPair(description, url));
            }

            Tests.add(new Test(q, a));
        }
        return Tests;
    }

    public void setTestEventListener(TestEventListener testEventListener) {
        this.testEventListener = testEventListener;
    }

    public void nextQuestion() {
        int totalItems = questionList.size();

        if (currentItem + 1 == totalItems) {
            testFinished = true;
            exitAnimation(currentItem);
        } else {
            showItem(++currentItem);
        }
    }

    protected interface TestEventListener {
        void OnQuestionAnswered(int question, int answer);

        void OnAllQuestionsAnswered();
    }
}
