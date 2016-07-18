package com.messenger.cityoftwo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

//import com.squareup.picasso.Picasso;

/**
 * Created by Aayush on 2/5/2016.
 */
public class TestFragment extends Fragment {

    ArrayList<Integer> SelectedAnswer = new ArrayList<>();
    TestAdapter testAdapter;
    Context context;
    BroadcastReceiver broadcastReceiver;
    private NonSwipeableViewPager TestPager;
    private View Introduction;

    public TestFragment() {
    }

    public static TestFragment newInstance(Bundle args) {

        TestFragment fragment = new TestFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.activity_test, container, false);

        context = getContext();

        TestPager = (NonSwipeableViewPager) rootView.findViewById(R.id.test_viewpager);

        ArrayList<Test> questionList = new ArrayList<>();

        Bundle data = getArguments();

        try {
            JSONArray Questions = new JSONArray(data.getString(CityOfTwo.KEY_TEST));

            questionList = GetQuestionList(Questions);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.i("TestFragment", "Test Not Received");
        }
        Log.i("TestFragment", "Test Received");

        testAdapter = new TestAdapter(context, questionList);

        CityOfTwo.answerBitmapList = new HashMap<>();

        for (int i = 0; i < questionList.size(); i++) {
            ArrayList<AnswerPair> answerPairs = questionList.get(i).getAnswers();
            for (int j = 0; j < answerPairs.size(); j++) {
                final String key = String.valueOf(i) + String.valueOf(j);
                Picasso.with(context)
                        .load(answerPairs.get(j).second)
                        .into(new Target() {
                            @Override
                            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                CityOfTwo.answerBitmapList.put(key, bitmap);
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

        TestPager.setAdapter(testAdapter);

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle extras = intent.getExtras();
                try {
                    String answers = extras.getString(CityOfTwo.KEY_SELECTED_ANSWER, "");
                    if (!answers.isEmpty()) {
                        LocalBroadcastManager b = LocalBroadcastManager.getInstance(context);

                        Intent i = new Intent();
                        i.setAction(CityOfTwo.KEY_TEST_RESULT);
                        i.putExtra(CityOfTwo.KEY_SELECTED_ANSWER, answers);
                        b.sendBroadcast(i);
                        return;
                    }
                    Integer position = extras.getInt(CityOfTwo.KEY_CURRENT_ANSWER, -1);
                    if (position != -1) {
                        TestPager.setCurrentItem(++position);
                        return;
                    }
                } catch (NullPointerException e) {
                    TestPager.setCurrentItem(TestPager.getCurrentItem() + 1);
                }
            }
        };
        return rootView;
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

    protected void setAnswer(int answer, int position) {
        if (position == 0) SelectedAnswer.clear();
        SelectedAnswer.add(answer);
        if (position == testAdapter.getCount() - 1)
            showStartScreen();
        else
            TestPager.setCurrentItem(++position);
    }

    @Override
    public void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(context).unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();

        LocalBroadcastManager.getInstance(context).registerReceiver(
                broadcastReceiver,
                new IntentFilter(CityOfTwo.KEY_TEST)
        );
    }

    private void showStartScreen() {
        Intent i = new Intent();
        i.putIntegerArrayListExtra(CityOfTwo.KEY_SELECTED_ANSWER, SelectedAnswer);

//        setResult(RESULT_OK, i);
//        finish();
    }
}
