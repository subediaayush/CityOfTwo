package com.messenger.cityoftwo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

//import com.squareup.picasso.Picasso;

/**
 * Created by Aayush on 2/5/2016.
 */
public class TestActivity extends AppCompatActivity {

    ArrayList<Integer> SelectedAnswer = new ArrayList<>();
    TestAdapter testAdapter;
    private NonSwipeableViewPager TestPager;
    private ImageButton HelpButton;
    private ProgressBar TestProgressBar;
    private Button StartButton;
    private TextView Instruction;
    private View Introduction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        Introduction = findViewById(R.id.test_intro);
        Instruction = (TextView) findViewById(R.id.test_instruction);
        StartButton = (Button) findViewById(R.id.test_start);
        TestProgressBar = (ProgressBar) findViewById(R.id.test_progressbar);
        HelpButton = (ImageButton) findViewById(R.id.help_button);
        TestPager = (NonSwipeableViewPager) findViewById(R.id.test_viewpager);

        ImageView BackgroundView = (ImageView) findViewById(R.id.background_view);

        ArrayList<Test> questionList = new ArrayList<>();

        Bundle data = getIntent().getExtras();

        try {
            JSONArray Questions = new JSONArray(data.getString(CityOfTwo.KEY_TEST));

            questionList = GetQuestionList(Questions);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.i("TestActivity", "Test Received");

        testAdapter = new TestAdapter(this, questionList);

        TestPager.setAdapter(testAdapter);

        StartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Introduction.setVisibility(View.GONE);
            }
        });
    }

    private ArrayList<Test> GetQuestionList(JSONArray questions) throws JSONException {
        ArrayList<Test> Tests = new ArrayList<>();

        for (int i = 0; i < questions.length(); i++) {
            JSONObject question = questions.getJSONObject(i);

            String q = question.keys().next();
            JSONArray answers = question.getJSONArray(q);

            ArrayList<AnswerPair> a = new ArrayList<>();

            for (int j = 0; j < answers.length(); j++) {
                a.add((AnswerPair) answers.get(j));
            }

            Tests.add(new Test(q, a));
        }

        return Tests;

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        ImageView BackgroundView = (ImageView) findViewById(R.id.background_view);

        int width = BackgroundView.getMeasuredWidth(),
                height = BackgroundView.getMeasuredHeight();

        Glide.with(this)
                .load(R.drawable.background)
                .override(width, height)
                .centerCrop()
                .into(BackgroundView);

    }

    protected void setAnswer(int answer, int position) {
        if (position == 0) SelectedAnswer.clear();
        SelectedAnswer.add(answer);
        if (position == testAdapter.getCount() - 1)
            showStartScreen();
        else
            TestPager.setCurrentItem(++position);
    }

    private void showStartScreen() {
        Intent i = new Intent();
        i.putIntegerArrayListExtra(CityOfTwo.KEY_SELECTED_ANSWER, SelectedAnswer);

        setResult(RESULT_OK, i);
        finish();
    }

    @Override
    public void onBackPressed() {
        int currentItem = TestPager.getCurrentItem();

        if (currentItem == 0) {
            setResult(RESULT_CANCELED, new Intent());
            finish();
        } else {
            TestPager.setCurrentItem(--currentItem);
        }
    }
}
