package com.messenger.cityoftwo;

import android.animation.Animator;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import static com.messenger.cityoftwo.CityOfTwo.answerBitmapList;
import static com.messenger.cityoftwo.CityOfTwo.isGooglePlayServicesAvailable;

/**
 * Created by Aayush on 4/19/2016.
 */
public class IntroductionActivity extends AppCompatActivity {

    TestFragment mTestFragment;
    BroadcastReceiver broadcastReceiver;
    Interpolator mInterpolator = new DecelerateInterpolator();
    private ViewFlipper introViewFlipper;
    private Button nextButton;
    private ViewFlipper testContainer;
    private View mLogoBackground;
    private ImageView mLogoImage;
    private int xDelta;
    private int yDelta;
    private float xScaleFactor;
    private float yScaleFactor;
    private int currentSelectedAnswer = -1;
    private int testAnswer = 0;
    private long mDuration = 500;
    private LoginManager loginManager;
    private CallbackManager callbackManager;
    private JSONArray test;

    private int totalQuestions;
    private int currentQuestion;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_introduction);

        int testTaken = getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE)
                .getInt(CityOfTwo.KEY_TEST_RESULT, -1);

        if (!isGooglePlayServicesAvailable(this, new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                finish();
            }
        })) return;

        if (testTaken != -1) {
            Intent loginIntent = new Intent(this, LoginActivity.class);
            startActivity(loginIntent);
            finish();
            return;
        }

        introViewFlipper = (ViewFlipper) findViewById(R.id.intro_flipper);
        nextButton = (Button) findViewById(R.id.intro_next_button);
        testContainer = (ViewFlipper) findViewById(R.id.test_container);

//        mLogoImage = (ImageView) findViewById(R.id.coyrudy_logo);
//        mLogoBackground = findViewById(R.id.coyrudy_logo_background);

        mTestFragment = TestFragment.newInstance(getIntent().getExtras());
        callbackManager = CallbackManager.Factory.create();
        loginManager = LoginManager.getInstance();


//        getSupportFragmentManager().beginTransaction()
//                .replace(R.id.test_container, mTestFragment)
//                .commit();

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View currentIntroView = introViewFlipper.getCurrentView();
                int currentViewId = currentIntroView.getId();
                if (currentViewId == R.id.intro_facebook_login) {
                    loginManager.logInWithReadPermissions(
                            IntroductionActivity.this,
                            CityOfTwo.FACEBOOK_PERMISSION_LIST
                    );
                } else if (currentViewId == R.id.test_container) {
                    testAnswer = testAnswer * 10 + currentSelectedAnswer;
                    if (testContainer.getDisplayedChild() + 1 < testContainer.getChildCount()) {
                        testContainer.setDisplayedChild(testContainer.getDisplayedChild() + 1);
                        nextButton.setEnabled(false);
                    } else {
                        int currentChild = introViewFlipper.getDisplayedChild();
                        introViewFlipper.setDisplayedChild(currentChild + 1);
                    }
                } else if (currentViewId == R.id.intro_last) {
                    Intent lobbyIntent = new Intent(IntroductionActivity.this, LobbyActivity.class);
                    lobbyIntent.putExtra(CityOfTwo.KEY_ACCESS_TOKEN, AccessToken.getCurrentAccessToken());
                    lobbyIntent.putExtra(CityOfTwo.KEY_FROM_INTRO, true);
                    startActivity(lobbyIntent);
                    finish();
                } else {
                    int currentChild = introViewFlipper.getDisplayedChild();
                    introViewFlipper.setDisplayedChild(currentChild + 1);
                }

                if (currentViewId == R.id.intro_test_desc) {
                    setupTest(test);
                    nextButton.setEnabled(false);
                }

            }
        });

        loginManager.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                AccessToken accessToken = loginResult.getAccessToken();
                Log.i("Facebook Access Token", accessToken.getToken());

                showLoginSuccess(accessToken);
            }

            @Override
            public void onCancel() {
                Log.i("Facebook Login", "Login Cancelled");
                showLoginErrorDialog(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        loginManager.logInWithReadPermissions(
                                IntroductionActivity.this,
                                CityOfTwo.FACEBOOK_PERMISSION_LIST
                        );
                    }
                });
            }

            @Override
            public void onError(FacebookException error) {
                Log.e("Facebook Login", "Login Error", error);
                showLoginErrorDialog(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        loginManager.logInWithReadPermissions(
                                IntroductionActivity.this,
                                CityOfTwo.FACEBOOK_PERMISSION_LIST
                        );
                    }
                });
            }
        });


//        mLogoImage.setImageBitmap(CityOfTwo.logoSmallBitmap);

//        introViewFlipper.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
//            @Override
//            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
//                int totalChildViews = introViewFlipper.getChildCount() - 1;
//                int position = introViewFlipper.getDisplayedChild();
//
//                if (position == totalChildViews - 1)
//                    nextButton.setText("Start test");
//
//                if (position == totalChildViews) {
//                    nextButton.setVisibility(View.GONE);
//                    testContainer.postInvalidate();
//                }
//            }
//        });

//        broadcastReceiver = new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                String action = intent.getAction();
//                Bundle data = intent.getExtras();
//
//                switch (action) {
//                    case CityOfTwo.KEY_TEST_RESULT: {
//                        Intent resultIntent = new Intent();
//                        String answers = data.getString(CityOfTwo.KEY_SELECTED_ANSWER, "");
//
//                        if (!answers.isEmpty()) {
//                            resultIntent.putExtra(CityOfTwo.KEY_TEST_RESULT, answers);
//                            IntroductionActivity.this.setResult(RESULT_OK, resultIntent);
//                            finish();
//                        }
//                        break;
//                    }
//                }
//            }
//        };

//        if (savedInstanceState == null) {
//            Bundle args = getIntent().getExtras();
//
//            final int initialX = args.getInt(CityOfTwo.KEY_LOCATION_X),
//                    initialY = args.getInt(CityOfTwo.KEY_LOCATION_Y),
//                    initialWidth = args.getInt(CityOfTwo.KEY_WIDTH),
//                    initialHeight = args.getInt(CityOfTwo.KEY_HEIGHT);
//
//            mLogoImage.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
//                @Override
//                public boolean onPreDraw() {
//                    mLogoImage.getViewTreeObserver().removeOnPreDrawListener(this);
//
//                    int[] location = new int[2];
//                    mLogoImage.getLocationOnScreen(location);
//
//                    xDelta = initialX - location[0];
//                    yDelta = initialY - location[1];
//
//                    xScaleFactor = (float) initialWidth / mLogoImage.getWidth();
//                    yScaleFactor = (float) initialHeight / mLogoImage.getHeight();
//
//                    startEnterAnimation();
//
//                    return true;
//                }
//            });
//        }
    }

    private void showLoginErrorDialog(DialogInterface.OnClickListener clickListener) {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage("Could login to your Facebook")
                .setPositiveButton("Try again", clickListener)
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showLoginSuccess(final AccessToken accessToken) {
        final ProgressDialog p;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            p = new ProgressDialog(this, android.R.style.Theme_Material_Light_Dialog);
        } else {
            p = new ProgressDialog(this);
        }
        p.setTitle("Please wait");
        p.setMessage("Setting up your account");
        p.setCancelable(false);
        p.show();

//        final FacebookLogin facebookLogin = new FacebookLogin(this, accessToken) {
//            @Override
//            void onSuccess(String response) {
//                int currentChild = introViewFlipper.getDisplayedChild();
//                introViewFlipper.setDisplayedChild(currentChild + 1);
//            }
//
//            @Override
//            void onFailure(Integer status) {
//                final FacebookLogin var = this;
//                showLoginErrorDialog(new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        facebookSignUp.execute();
//                    }
//                });
//            }
//        };

        final FacebookSignUp facebookSignUp = new FacebookSignUp(this, accessToken) {
            @Override
            void onSuccess(String response) {

                JSONObject Response;
                try {
                    Response = new JSONObject(response);
                    Boolean registered = Response.getBoolean("parsadi");

                    if (!registered) {
                        test = Response.getJSONArray("test");
                        int currentChild = introViewFlipper.getDisplayedChild();
                        introViewFlipper.setDisplayedChild(currentChild + 1);
                        p.hide();
                    }
                } catch (JSONException e) {
                    onFailure(-1);
                }
            }

            @Override
            void onFailure(Integer status) {
                final FacebookSignUp var = this;
                showLoginErrorDialog(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        var.execute();
                    }
                });
            }
        };

        facebookSignUp.execute();

    }

    private void setupTest(JSONArray questions) {
        ArrayList<Test> tests = new ArrayList<>();

        totalQuestions = questions.length();

        try {
            for (int i = 0; i < questions.length(); i++) {
                JSONObject question = questions.getJSONObject(i);

                String q = question.keys().next();
                JSONArray answers = question.getJSONArray(q);

                ArrayList<AnswerPair> a = new ArrayList<>();

                for (int j = 0; j < answers.length(); j++) {
                    String description = (String) ((JSONArray) answers.get(j)).get(0),
                            url = (String) ((JSONArray) answers.get(j)).get(1);
                    answerBitmapList = new HashMap<>();
                    final String key = String.valueOf(i) + String.valueOf(j);
                    Picasso.with(this)
                            .load(url)
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

                    a.add(new AnswerPair(description, url));
                }

                tests.add(new Test(q, a));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        for (Test test : tests) {
            View testView = LayoutInflater.from(this).inflate(R.layout.layout_test, null);

            TextView questionView = (TextView) testView.findViewById(R.id.test_question);
            final ListView answerListView = (ListView) testView.findViewById(R.id.test_answers);
            questionView.setText(test.getQuestion());

            ArrayList<AnswerPair> answers = test.getAnswers();
            AnswersAdapter answersAdapter = new AnswersAdapter(this, answers, tests.indexOf(test));
            answersAdapter.setOnSelectedListener(new AnswersAdapter.OnSelectedListener() {
                @Override
                public void OnSelected(AnswerPair answer, int position) {
                    answerListView.setSelection(position);
                    currentSelectedAnswer = position;
                    nextButton.setEnabled(true);
                }
            });
            answerListView.setAdapter(answersAdapter);
            testContainer.addView(testView, testContainer.getChildCount());
        }

    }


    private void startEnterAnimation() {
        mLogoImage.setPivotX(0);
        mLogoImage.setPivotY(0);
        mLogoImage.setScaleX(xScaleFactor);
        mLogoImage.setScaleY(yScaleFactor);
        mLogoImage.setTranslationX(xDelta);
        mLogoImage.setTranslationY(yDelta);

        // Animate scale and translation to go from thumbnail to full size
        mLogoImage.animate().setDuration(mDuration)
                .scaleX(1).scaleY(1)
                .translationX(0).translationY(0)
                .setInterpolator(mInterpolator)
                .withStartAction(new Runnable() {
                    @Override
                    public void run() {
                        nextButton.setTranslationY(nextButton.getHeight());

                        introViewFlipper.setScaleX(.5f);
                        introViewFlipper.setScaleY(.5f);
                        introViewFlipper.setAlpha(0);

                        mLogoBackground.setVisibility(View.INVISIBLE);

                        nextButton.animate().setDuration(mDuration)
                                .translationY(0);
                    }
                })
                .withEndAction(new Runnable() {
                    public void run() {
                        // Animate the description in after the image animation
                        // is done. Slide and fade the text in from underneath
                        // the picture.

                        introViewFlipper.animate().setDuration(mDuration / 2)
                                .scaleX(1).scaleY(1)
                                .alpha(1);

                        int cx = mLogoBackground.getWidth() / 2,
                                cy = mLogoBackground.getHeight() / 2;

                        int radius = Math.max(
                                introViewFlipper.getWidth(),
                                introViewFlipper.getHeight()
                        );

                        mLogoBackground.setVisibility(View.VISIBLE);
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                            Animator anim = ViewAnimationUtils.createCircularReveal(mLogoBackground, cx, cy, 0, radius);

                            anim.setInterpolator(new AccelerateDecelerateInterpolator());
                            anim.setDuration(400);

                            anim.start();
                        } else {
                            mLogoBackground.setScaleX(.01f);
                            mLogoBackground.animate().scaleX(1);
                        }
                    }
                });

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
