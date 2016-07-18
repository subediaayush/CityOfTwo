package com.messenger.cityoftwo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.facebook.AccessToken;

import org.json.JSONException;
import org.json.JSONObject;

//import com.squareup.picasso.Callback;
//import com.squareup.picasso.Picasso;

/**
 * Created by Aayush on 2/5/2016.
 */
public class LobbyActivity extends AppCompatActivity {

    private final static int ERROR = -1,
            BEGIN = 0,
            SIGNED_UP = 1,
            LOGGED_IN = 2;

    //    TestHttpHandler, SignUpHttpHandler, TestSubmitHttpHandler;
    BroadcastReceiver mBroadcastReceiver, mTestBroadcastReceiver;
    ProgressBar mLobbyProgressBar;
    TextView mLobbyDescription;
    AccessToken mAccessToken;
    Boolean mCanPutText = true;
    String mDescriptionBuffer = "";
    int lobbyState = BEGIN;

    // Attributes for animation
    int xDelta;
    int yDelta;
    float xScaleFactor;
    float yScaleFactor;
    Interpolator mInterpolator = new DecelerateInterpolator();
    long mDuration = 250;

    FrameLayout mImageFlipper;
    ImageButton mReloadButton;
    LogoFadeFragment mImageFlipperFragment;
    private BackgroundAnimationFragment mBackgroundAnimationFragment;
    private TextView mWelcomeLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        mLobbyProgressBar = (ProgressBar) findViewById(R.id.lobby_progressbar);
        mLobbyDescription = (TextView) findViewById(R.id.lobby_progress_description);

        mWelcomeLabel = (TextView) findViewById(R.id.welcome_label);


        mImageFlipper = (FrameLayout) findViewById(R.id.lobby_image_flipper);

        mImageFlipperFragment = LogoFadeFragment.newInstance();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.lobby_image_flipper, mImageFlipperFragment)
                .commit();

        mBackgroundAnimationFragment = BackgroundAnimationFragment.newInstance();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.lobby_background, mBackgroundAnimationFragment)
                .commit();

        if (savedInstanceState != null) {
            mAccessToken = savedInstanceState.getParcelable("LOBBY_ACCESS_TOKEN");

            setStatus(savedInstanceState.getInt("LOBBY_STATUS"));
        } else {

            Bundle args = getIntent().getExtras();

            mAccessToken = getIntent().getParcelableExtra(CityOfTwo.KEY_ACCESS_TOKEN);

            if (!args.getBoolean(CityOfTwo.KEY_FROM_INTRO, false)) {


                final int initialX = args.getInt(CityOfTwo.KEY_LOCATION_X),
                        initialY = args.getInt(CityOfTwo.KEY_LOCATION_Y),
                        initialWidth = args.getInt(CityOfTwo.KEY_WIDTH),
                        initialHeight = args.getInt(CityOfTwo.KEY_HEIGHT);

                ViewTreeObserver treeObserver = mImageFlipper.getViewTreeObserver();
                treeObserver.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        mImageFlipper.getViewTreeObserver().removeOnPreDrawListener(this);

                        int[] location = new int[2];
                        mImageFlipper.getLocationOnScreen(location);

                        xDelta = initialX - location[0];
                        yDelta = initialY - location[1];

                        xScaleFactor = (float) initialWidth / mImageFlipper.getWidth();
                        yScaleFactor = (float) initialHeight / mImageFlipper.getHeight();

                        Log.i("xScaleFactor", String.valueOf(xScaleFactor));
                        Log.i("yScaleFactor", String.valueOf(yScaleFactor));

                        startEnterAnimation();

                        return true;
                    }
                });
            }
        }

        mReloadButton = (ImageButton) findViewById(R.id.refresh_button);

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Bundle data = intent.getExtras();

                Log.i("LobbyReceiver", "Signal Received: " + action);

                switch (action) {
                    case CityOfTwo.PACKAGE_NAME: {
                        String s = data.getString(CityOfTwo.KEY_TYPE, "");

                        if (s.equals("CHAT_BEGIN")) {
                            Intent i = new Intent(LobbyActivity.this, ConversationActivity.class);
                            i.putExtras(data);

                            LocalBroadcastManager.getInstance(LobbyActivity.this).unregisterReceiver(mBroadcastReceiver);
                            startActivityForResult(i, CityOfTwo.ACTIVITY_CONVERSATION);

                            overridePendingTransition(
                                    R.anim.slide_in_right,
                                    R.anim.slide_out_left
                            );
                        }
                        break;
                    }
                    case CityOfTwo.KEY_MESSAGE: {
                        break;
                    }
                }
            }
        };

        mReloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setStatus(BEGIN);
                facebookLogin(mAccessToken);
            }
        });

        mImageFlipper.setOnTouchListener(new View.OnTouchListener() {

            long touchDownMs = 0, lastTapTimeMs = 0;
            int numberOfTaps = 0;

            @Override
            public boolean onTouch(View v, MotionEvent event) {


                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        int pivotX = mImageFlipper.getWidth() / 2,
                                pivotY = mImageFlipper.getHeight() / 2;

                        mImageFlipper.setPivotX(pivotX);
                        mImageFlipper.setPivotY(pivotY);

                        mImageFlipper.animate().setDuration(100)
                                .scaleX(.9f)
                                .scaleY(.9f);

                        touchDownMs = System.currentTimeMillis();
                        Log.i("ImageFlipperTouch", "Event: DOWN");
                        return true;
                    }
                    case MotionEvent.ACTION_UP: {
                        int pivotX = mImageFlipper.getWidth() / 2,
                                pivotY = mImageFlipper.getHeight() / 2;

                        mImageFlipper.setPivotX(pivotX);
                        mImageFlipper.setPivotY(pivotY);

                        mImageFlipper.animate().setDuration(100)
                                .scaleX(1)
                                .scaleY(1);

                        Log.i("ImageFlipperTouch", "Event: UP");
                        if ((System.currentTimeMillis() - touchDownMs) > ViewConfiguration.getTapTimeout()) {
                            //it was not a tap
                            numberOfTaps = 0;
                            lastTapTimeMs = 0;
                            return true;
                        }

                        if (numberOfTaps > 0
                                && (System.currentTimeMillis() - lastTapTimeMs) < ViewConfiguration.getDoubleTapTimeout()) {
                            numberOfTaps += 1;
                        } else {
                            numberOfTaps = 1;
                        }

                        lastTapTimeMs = System.currentTimeMillis();

                        if (numberOfTaps == 3) {
                            //handle triple tap
                            Animation wobble = AnimationUtils.loadAnimation(
                                    LobbyActivity.this,
                                    R.anim.wobble
                            );
                            mImageFlipper.startAnimation(wobble);
                        }
                        return true;
                    }
                    default:
                        return false;
                }
            }
        });

    }


    private void startEnterAnimation() {
        mImageFlipper.setPivotX(0);
        mImageFlipper.setPivotY(0);
        mImageFlipper.setScaleX(xScaleFactor);
        mImageFlipper.setScaleY(yScaleFactor);
        mImageFlipper.setTranslationX(xDelta);
        mImageFlipper.setTranslationY(yDelta);
        mImageFlipper.setAlpha(1f);

        // Animate scale and translation to go from thumbnail to full size
        mImageFlipper.animate().setDuration(mDuration)
                .scaleX(1).scaleY(1)
                .translationX(0).translationY(0)
                .setInterpolator(mInterpolator)
                .withStartAction(new Runnable() {
                    @Override
                    public void run() {
                        mWelcomeLabel.setAlpha(0);
                        mReloadButton.setAlpha(0f);

                        revealView(mReloadButton);
                        revealView(mWelcomeLabel);
                    }
                })
                .withEndAction(new Runnable() {
                    public void run() {
                        // Animate the description in after the image animation
                        // is done. Slide and fade the text in from underneath
                        // the picture.

                        setStatus(BEGIN);
//                        CityOfTwo.RegisterGCM(LobbyActivity.this);

                        startAppLogic();

                    }
                });
    }

    private void startAppLogic() {
        switch (getStatus()) {
            case BEGIN:
                facebookLogin(mAccessToken);
                break;
            case SIGNED_UP:
                facebookLogin(mAccessToken);
                break;
            case LOGGED_IN:
                waitForServer();
                break;
            case ERROR:
                setStatus(BEGIN);
                facebookLogin(mAccessToken);
                break;
            default:
                break;
        }
    }

    private void revealView(View view) {
        view.animate().setDuration(mDuration / 2)
                .alpha(1)
                .setInterpolator(mInterpolator);
    }

    @Override
    protected void onPause() {
        super.onPause();

        LocalBroadcastManager b = LocalBroadcastManager.getInstance(this);
        b.unregisterReceiver(mBroadcastReceiver);
        b.unregisterReceiver(mTestBroadcastReceiver);

        Log.i("LobbyActivity", "Activity paused");
        CityOfTwo.setApplicationState(CityOfTwo.APPLICATION_BACKGROUND);
    }

    @Override
    protected void onResume() {
        super.onResume();

        LocalBroadcastManager b = LocalBroadcastManager.getInstance(this);
        b.registerReceiver(
                mBroadcastReceiver,
                new IntentFilter(CityOfTwo.PACKAGE_NAME)
        );
        b.registerReceiver(
                mBroadcastReceiver,
                new IntentFilter(CityOfTwo.KEY_TEST_RESULT)
        );
        b.registerReceiver(
                mBroadcastReceiver,
                new IntentFilter(CityOfTwo.KEY_MESSAGE)
        );

        Log.i("LobbyActivity", "Activity Resumed");
        CityOfTwo.setApplicationState(CityOfTwo.APPLICATION_FOREGROUND);
        CityOfTwo.setCurrentActivity(CityOfTwo.ACTIVITY_LOBBY);

        SharedPreferences sp = getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE);
        Boolean chatPending = sp.getBoolean(CityOfTwo.KEY_CHAT_PENDING, false);

        if (chatPending) {
            Intent i = new Intent(this, ConversationActivity.class);
            i.putExtra(CityOfTwo.KEY_COMMON_LIKES, sp.getString(CityOfTwo.KEY_COMMON_LIKES, ""));
            i.putExtra(CityOfTwo.KEY_TYPE, sp.getInt(CityOfTwo.KEY_TYPE, -1));
            startActivityForResult(i, CityOfTwo.ACTIVITY_CONVERSATION);
        }

        sp.edit().remove(CityOfTwo.KEY_CHAT_PENDING)
                .remove(CityOfTwo.KEY_COMMON_LIKES)
                .remove(CityOfTwo.KEY_TYPE)
                .apply();
    }

    @Override
    protected void onStart() {
        super.onStart();


    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("LOBBY_STATUS", getStatus());
        outState.putParcelable("LOBBY_ACCESS_TOKEN", mAccessToken);
    }

    private void facebookLogin(final AccessToken accessToken) {

        new FacebookLogin(this, accessToken) {
            @Override
            void onSuccess(String response) {
                try {
                    JSONObject Response = new JSONObject(response);

                    Boolean registered = Response.getBoolean("parsadi");

                    if (!registered) {
                        signUp(accessToken);
                    } else {
                        String sessionToken = Response.getString(CityOfTwo.KEY_TOKEN);
                        String uniqueCode = Response.getString(CityOfTwo.KEY_CODE);
                        Integer credits = Response.getInt(CityOfTwo.KEY_CREDITS);
                        Boolean filters_applied = Response.getBoolean(CityOfTwo.KEY_FILTERS_APPLIED);

                        SharedPreferences.Editor editor = getSharedPreferences(
                                CityOfTwo.PACKAGE_NAME,
                                MODE_PRIVATE
                        ).edit();

                        if (filters_applied) {
                            JSONObject filters = Response.getJSONObject(CityOfTwo.KEY_FILTERS);
                            Integer minAge = filters.getInt(CityOfTwo.KEY_MIN_AGE);
                            Integer maxAge = filters.getInt(CityOfTwo.KEY_MAX_AGE);
                            Integer distance = filters.getInt(CityOfTwo.KEY_DISTANCE);
                            Boolean matchMale = filters.getBoolean(CityOfTwo.KEY_MATCH_MALE);
                            Boolean matchFemale = filters.getBoolean(CityOfTwo.KEY_MATCH_FEMALE);

                            editor.putInt(CityOfTwo.KEY_MIN_AGE, minAge)
                                    .putInt(CityOfTwo.KEY_MAX_AGE, maxAge)
                                    .putInt(CityOfTwo.KEY_DISTANCE, distance)
                                    .putBoolean(CityOfTwo.KEY_MATCH_MALE, matchMale)
                                    .putBoolean(CityOfTwo.KEY_MATCH_FEMALE, matchFemale);
                        }

                        editor.putString(CityOfTwo.KEY_SESSION_TOKEN, sessionToken)
                                .putString(CityOfTwo.KEY_CODE, uniqueCode)
                                .putInt(CityOfTwo.KEY_CREDITS, credits)
                                .putBoolean(CityOfTwo.KEY_FILTERS_APPLIED, filters_applied)
                                .apply();

                        int pool = Response.getInt("pool");

                        if (pool != 0) {
                            waitForServer();
                        } else {
                            startTest(Response.getString("test"));
                        }

                        // Modify code to adapt for testing phase

                        // String test = Response.getString("test");

                        // Intent intent = new Intent(LobbyActivity.this, TestFragment.class);
                        // intent.putExtra(CityOfTwo.KEY_TEST, test);

                        // startActivityForResult(intent, CityOfTwo.ACTIVITY_TEST);
                    }

                } catch (JSONException e) {
                    setStatus(ERROR);
                    e.printStackTrace();
                }
            }

            @Override
            void onFailure(Integer status) {
                setStatus(ERROR);
            }
        };
    }

    private void startTest(final String test) {
        long duration = 500;

        final Intent introductionActivity = new Intent(this, IntroductionActivity.class);

        mLobbyDescription.animate().setDuration(duration / 4)
                .alpha(0);

        mLobbyProgressBar.animate().setDuration(duration / 4)
                .alpha(0);

        mWelcomeLabel.animate().setDuration(duration / 4)
                .alpha(0)
                .withStartAction(new Runnable() {
                    @Override
                    public void run() {
                        CityOfTwo.logoSmallBitmap = mImageFlipperFragment.getCurrentBitmap();
                        mImageFlipperFragment.stopFlipping();
                    }
                })
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        int[] location = new int[2];
                        mImageFlipper.getLocationOnScreen(location);

                        introductionActivity.putExtra(CityOfTwo.KEY_LOCATION_X, location[0]);
                        introductionActivity.putExtra(CityOfTwo.KEY_LOCATION_Y, location[1]);
                        introductionActivity.putExtra(CityOfTwo.KEY_WIDTH, mImageFlipper.getWidth());
                        introductionActivity.putExtra(CityOfTwo.KEY_HEIGHT, mImageFlipper.getHeight());
                        introductionActivity.putExtra(CityOfTwo.KEY_TEST, test);

                        startActivityForResult(introductionActivity, CityOfTwo.ACTIVITY_INTRODUCTION);

                        new Handler().postDelayed(new Runnable() {

                            @Override
                            public void run() {
                                mImageFlipper.setAlpha(0);
                            }
                        }, 100);
                        overridePendingTransition(0, 0);
                    }
                });
    }

    private void signUp(final AccessToken accessToken) {
        new FacebookSignUp(this, accessToken) {
            @Override
            void onSuccess(String response) {
                try {
                    JSONObject Response = new JSONObject(response);

                    Boolean registered = Response.getBoolean("parsadi");

                    if (!registered) {
                        setStatus(SIGNED_UP);
                        facebookLogin(accessToken);
                    }
                } catch (JSONException e) {
                    setStatus(ERROR);
                    e.printStackTrace();
                }
            }

            @Override
            void onFailure(Integer status) {
                setStatus(ERROR);
            }
        }.execute();
    }

    private void waitForServer() {
        String broadcast_gcm = getString(R.string.url_broadcast_gcm),
                header = CityOfTwo.HEADER_GCM_ID,
                value = getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE)
                        .getString(CityOfTwo.KEY_REG_ID, "");

        String[] Path = {CityOfTwo.API, broadcast_gcm};

        HttpHandler BroadcastGCMHttpHandler = new HttpHandler(CityOfTwo.HOST, Path, HttpHandler.POST, header, value) {
            @Override
            protected void onSuccess(String response) {
                try {
                    JSONObject Response = new JSONObject(response);

                    Boolean status = Response.getBoolean("parsadi");

                    if (!status) {
                        setStatus(ERROR);
                    } else {
                        setStatus(LOGGED_IN);
                    }

                } catch (JSONException e) {
                    setStatus(ERROR);
                    e.printStackTrace();
                }
            }

            @Override
            protected void onFailure(Integer status) {
                setStatus(ERROR);
            }
        };

        String token = "Token " + getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE)
                .getString(CityOfTwo.KEY_SESSION_TOKEN, "");

        BroadcastGCMHttpHandler.addHeader("Authorization", token);
        BroadcastGCMHttpHandler.execute();
    }

    private void submitTest(String answers) {
        String submit_test = getString(R.string.url_test),
                header = CityOfTwo.HEADER_TEST_RESULT;

        String[] Path = {CityOfTwo.API, submit_test};

        HttpHandler TestHttpHandler = new HttpHandler(CityOfTwo.HOST, Path, HttpHandler.POST, header, answers) {
            @Override
            protected void onSuccess(String response) {
                try {
                    JSONObject Response = new JSONObject(response);
                    Boolean status = Response.getBoolean("parsadi");

                    if (!status) {
                        setStatus(ERROR);
                    } else {
                        setStatus(LOGGED_IN);
                        waitForServer();
                    }

                } catch (JSONException e) {
                    setStatus(ERROR);
                    e.printStackTrace();
                }
            }

            @Override
            protected void onFailure(Integer status) {
                setStatus(ERROR);
            }
        };

        String token = "Token " + getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE)
                .getString(CityOfTwo.KEY_SESSION_TOKEN, "");

        TestHttpHandler.addHeader("Authorization", token);

        TestHttpHandler.execute();
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK, new Intent());
        finish();
    }


    public void setLobbyDescription(String lobbyDescription) {
        if (mLobbyDescription == null)
            mLobbyDescription = (TextView) findViewById(R.id.lobby_progress_description);

        // Set the new description buffer
        mDescriptionBuffer = lobbyDescription;

        // Do not do anything else if animation is running
        // or if two consecutive description are same
        if (!mCanPutText)
            return;


        mLobbyDescription.animate().setDuration(500)
                .alpha(0)
                .translationX(-250)
                .setInterpolator(new OvershootInterpolator())
                .withStartAction(new Runnable() {
                    @Override
                    public void run() {
                        mCanPutText = false;
                    }
                })
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        mLobbyDescription.setTranslationX(250);

                        mCanPutText = true;
                        mLobbyDescription.setText(mDescriptionBuffer);

                        mLobbyDescription.animate().setDuration(500)
                                .alpha(1)
                                .translationX(0)
                                .setInterpolator(new OvershootInterpolator())
                                .withStartAction(new Runnable() {
                                    @Override
                                    public void run() {
                                        mCanPutText = false;
                                    }
                                })
                                .withEndAction(new Runnable() {
                                    @Override
                                    public void run() {
                                        mCanPutText = true;
                                    }
                                });
                    }
                });


//        final Animation slideFromRight = AnimationUtils.loadAnimation(this, R.anim.slide_in_right);
//        final Animation slideToLeft = AnimationUtils.loadAnimation(this, R.anim.slide_out_left);

//        slideToLeft.setAnimationListener(new Animation.AnimationListener() {
//            @Override
//            public void onAnimationStart(Animation animation) {
//                mCanPutText = false;
//            }
//
//            @Override
//            public void onAnimationEnd(Animation animation) {
//                mCanPutText = true;
//                mLobbyDescription.setText(mDescriptionBuffer);
//                mLobbyDescription.startAnimation(slideFromRight);
//            }
//
//            @Override
//            public void onAnimationRepeat(Animation animation) {
//
//            }
//        });

//        slideFromRight.setAnimationListener(new Animation.AnimationListener() {
//            @Override
//            public void onAnimationStart(Animation animation) {
//                mCanPutText = false;
//            }
//
//            @Override
//            public void onAnimationEnd(Animation animation) {
//
//            }
//
//            @Override
//            public void onAnimationRepeat(Animation animation) {
//
//            }
//        });

//        mLobbyDescription.startAnimation(slideToLeft);

    }

    private int getStatus() {
        return lobbyState;
    }

    private void setStatus(int state) {
        // Run code before changing state

        lobbyState = state;
        if (mImageFlipper == null)
            mImageFlipper = (ViewFlipper) findViewById(R.id.lobby_image_flipper);

        // Run code after changing state
        if (lobbyState == ERROR) {

            // Code for debug version
            if (BuildConfig.DEBUG) {
                Intent conversationActivity = new Intent(
                        this,
                        ConversationActivity.class
                );
                startActivityForResult(conversationActivity, CityOfTwo.ACTIVITY_CONVERSATION);
                overridePendingTransition(
                        R.anim.slide_in_right,
                        R.anim.slide_out_left
                );
            }
            mLobbyProgressBar.setVisibility(View.INVISIBLE);

            mImageFlipperFragment.stopFlipping();

            setLobbyDescription("There was an error. Please try again later.");
        } else {
            mLobbyProgressBar.setVisibility(View.VISIBLE);

            if (!mImageFlipperFragment.isFlipping())
                mImageFlipperFragment.startFlipping();
        }

        if (lobbyState == BEGIN) {
            setLobbyDescription("Setting up your profile");
        }
        if (lobbyState == LOGGED_IN) {
            setLobbyDescription("Finding a match...");
        }
    }

    private Bitmap rotateBitmap(Bitmap source, int angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case CityOfTwo.ACTIVITY_INTRODUCTION: {
                if (resultCode == RESULT_OK) {
                    restoreActivity();
                    String answers = data.getExtras().getString(CityOfTwo.KEY_TEST_RESULT);
                    submitTest(answers);
                } else {
                    restoreActivity();
                }
                break;
            }
            case CityOfTwo.ACTIVITY_CONVERSATION: {
                restoreActivity();
                break;
            }
        }
    }

    private void restoreActivity() {
        revealView(mImageFlipper);
        revealView(mWelcomeLabel);
        revealView(mReloadButton);
        revealView(mLobbyProgressBar);
        mImageFlipperFragment.startFlipping();

        setLobbyDescription("");
        setLobbyDescription("Setting up your profile");
        mLobbyDescription.setAlpha(1);

        setStatus(LOGGED_IN);
        facebookLogin(mAccessToken);
    }
}
