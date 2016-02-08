package com.messenger.cityoftwo;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Aayush on 2/5/2016.
 */
public class TestActivity extends AppCompatActivity {

    List<Integer> Selected = new ArrayList<>();
    TestAdapter testAdapter;
    private ViewPager TestPager;
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
        TestPager = (ViewPager) findViewById(R.id.test_viewpager);

        ArrayList<Test> testList = new ArrayList<>();
        testAdapter = new TestAdapter(this, testList);

        TestPager.setAdapter(testAdapter);
    }

    protected void setAnswer(int answer, int position){
        if (position == 0) Selected.clear();
        Selected.add(answer);
        if (position == testAdapter.getCount() - 1)
            showStartScreen();
        else
            TestPager.setCurrentItem(++position);
    }

    private void showStartScreen() {

    }
}
