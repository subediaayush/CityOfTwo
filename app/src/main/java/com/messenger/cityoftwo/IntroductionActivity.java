package com.messenger.cityoftwo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ViewFlipper;

/**
 * Created by Aayush on 4/19/2016.
 */
public class IntroductionActivity extends AppCompatActivity {

    TestFragment testFragment;
    BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_introduction);

        final ViewFlipper introViewFlipper = (ViewFlipper) findViewById(R.id.intro_flipper);
        final Button pageChangeButton = (Button) findViewById(R.id.intro_next_button);
        final View testContainer = findViewById(R.id.test_container);

        testFragment = TestFragment.newInstance(getIntent().getExtras());

        getSupportFragmentManager().beginTransaction()
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

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Bundle data = intent.getExtras();

                switch (action) {
                    case CityOfTwo.KEY_TEST_RESULT: {
                        String answers = data.getString(CityOfTwo.KEY_SELECTED_ANSWER, "");

                        if (!answers.isEmpty()) {
                            IntroductionActivity.this.setResult(RESULT_OK, intent);
                            finish();
                        }
                        break;
                    }
                }
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();

        LocalBroadcastManager.getInstance(this)
                .registerReceiver(broadcastReceiver, new IntentFilter(CityOfTwo.KEY_TEST_RESULT));
    }

    @Override
    protected void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(broadcastReceiver);
    }
}
