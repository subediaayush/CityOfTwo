package com.messenger.cityoftwo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
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
    Interpolator interpolator = new DecelerateInterpolator();
    long mDuration = 500;

    ViewFlipper mImageFlipper;
    ImageView mLogoImage;
    ImageButton mReloadButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        mLobbyProgressBar = (ProgressBar) findViewById(R.id.lobby_progressbar);
        mLobbyDescription = (TextView) findViewById(R.id.lobby_progress_description);

        mImageFlipper = (ViewFlipper) findViewById(R.id.lobby_image_flipper);
        mLogoImage = (ImageView) findViewById(R.id.lobby_logo_dummy);

        if (savedInstanceState != null) {
            mAccessToken = savedInstanceState.getParcelable("LOBBY_ACCESS_TOKEN");

            setStatus(savedInstanceState.getInt("LOBBY_STATUS"));
        } else {
            Bundle args = getIntent().getExtras();

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

            mAccessToken = getIntent().getParcelableExtra(CityOfTwo.KEY_ACCESS_TOKEN);
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
                            LocalBroadcastManager.getInstance(LobbyActivity.this).unregisterReceiver(mBroadcastReceiver);
                            startActivityForResult(i, CityOfTwo.ACTIVITY_CONVERSATION);
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
    }

    private void startEnterAnimation() {
        mImageFlipper.setPivotX(0);
        mImageFlipper.setPivotY(0);
        mImageFlipper.setScaleX(xScaleFactor);
        mImageFlipper.setScaleY(yScaleFactor);
        mImageFlipper.setTranslationX(xDelta);
        mImageFlipper.setTranslationY(yDelta);
        mImageFlipper.setAlpha(1.f);

//        final View traingleBackground = findViewById(R.id.triangle_background);
        final TextView welcomeLabel = (TextView) findViewById(R.id.welcome_label);

        // Animate scale and translation to go from thumbnail to full size
        mImageFlipper.animate().setDuration(mDuration)
                .scaleX(1).scaleY(1)
                .translationX(0).translationY(0)
                .setInterpolator(interpolator)
                .withStartAction(new Runnable() {
                    @Override
                    public void run() {
                        welcomeLabel.setAlpha(0.f);
                        mReloadButton.setAlpha(0.f);

//                        traingleBackground.setAlpha(1);
//
//                        ColorDrawable backgroundColor = new ColorDrawable(ContextCompat.getColor(
//                                LobbyActivity.this,
//                                R.color.colorPrimaryDark)
//                        );
//                        backgroundColor.setAlpha(255);
//                        findViewById(R.id.lobby_background).setBackground(backgroundColor);
                        revealView(mReloadButton);
                        revealView(welcomeLabel);
                    }
                })
                .withEndAction(new Runnable() {
                    public void run() {
                        // Animate the description in after the image animation
                        // is done. Slide and fade the text in from underneath
                        // the picture.

                        mImageFlipper.setFlipInterval(1000);
                        mImageFlipper.setInAnimation(LobbyActivity.this, android.R.anim.fade_in);
                        mImageFlipper.setOutAnimation(LobbyActivity.this, android.R.anim.fade_out);

                        setStatus(BEGIN);
                        CityOfTwo.RegisterGCM(LobbyActivity.this);

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
                .setInterpolator(interpolator);
    }


    @Override
    protected void onStop() {
        super.onStop();

        LocalBroadcastManager b = LocalBroadcastManager.getInstance(this);
        b.unregisterReceiver(mBroadcastReceiver);
        b.unregisterReceiver(mTestBroadcastReceiver);
    }

    @Override
    protected void onPause() {
        super.onPause();

        Log.i("LobbyActivity", "Activity paused");
        CityOfTwo.APPLICATION_STATE = CityOfTwo.APPLICATION_BACKGROUND;
    }

    @Override
    protected void onResume() {
        super.onResume();

        CityOfTwo.APPLICATION_STATE = CityOfTwo.APPLICATION_FOREGROUND;
        Log.i("LobbyActivity", "Activity Resumed");
    }

    @Override
    protected void onStart() {
        super.onStart();

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

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("LOBBY_STATUS", getStatus());
        outState.putParcelable("LOBBY_ACCESS_TOKEN", mAccessToken);
    }

    private void facebookLogin(final AccessToken accessToken) {
        String login = getString(R.string.url_login);

        String[] path = {login};

        final String header = CityOfTwo.HEADER_ACCESS_TOKEN,
                token = accessToken.getToken();

        HttpHandler LoginHttpHandler = new HttpHandler(CityOfTwo.HOST, path, HttpHandler.POST, header, token) {
            @Override
            protected void onSuccess(String response) {
                try {
                    JSONObject Response = new JSONObject(response);

                    Boolean registered = Response.getBoolean("parsadi");

                    if (!registered) {
                        signUp(accessToken);
                    } else {
                        String sessionToken = Response.getString("token");

                        getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE)
                                .edit()
                                .putString(CityOfTwo.KEY_SESSION_TOKEN, sessionToken)
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
            protected void onFailure(Integer status) {
                setStatus(ERROR);
            }
        };

        LoginHttpHandler.execute();
    }

    private void startTest(String test) {
        Intent intent = new Intent(this, IntroductionActivity.class);
        intent.putExtra(CityOfTwo.KEY_TEST, test);
        startActivityForResult(intent, CityOfTwo.ACTIVITY_INTRODUCTION);
    }

    private void signUp(final AccessToken accessToken) {
        String signup = getString(R.string.url_signup);
        String[] path = {signup};
        String token = accessToken.getToken();

        HttpHandler SignUpHttpHandler = new HttpHandler(CityOfTwo.HOST, path, HttpHandler.POST, CityOfTwo.HEADER_ACCESS_TOKEN, token) {
            @Override
            protected void onSuccess(String response) {
                try {
                    JSONObject Response = new JSONObject(response);

                    Boolean registered = Response.getBoolean("parsadi");

                    if (!registered) {
                        startTest(Response.getString("test"));

                        // Testing/Simulating test phase
//
//                        ArrayList<Integer> answers = new ArrayList<>();
//                        answers.add(1);
//                        answers.add(1);
//                        answers.add(0);
//
//                        submitTest(answers, accessToken);
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

        SignUpHttpHandler.execute();

//        String test = getString(R.string.url_test);
//        path[1] = test;

//        TestHttpHandler = new HttpHandler(CityOfTwo.HOST, path) {
//            @Override
//            protected void onSuccess(String response) {
//                Intent intent = new Intent(LobbyActivity.this, TestFragment.class);
//
//                Bundle extras = new Bundle();
//                intent.putExtra(CityOfTwo.KEY_TEST, response);
//
//                startActivityForResult(intent, CityOfTwo.ACTIVITY_TEST);
//            }
//
//            @Override
//            protected void onFailure(Integer status) {
//            }
//        };

    }

    private void waitForServer() {
        String broadcast_gcm = getString(R.string.url_broadcast_gcm),
                header = CityOfTwo.HEADER_GCM_ID,
                value = getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE)
                        .getString("REG_ID", "");

        String[] Path = {CityOfTwo.API, broadcast_gcm};

        HttpHandler BroadcastGCMHttpHandler = new HttpHandler(CityOfTwo.HOST, Path, HttpHandler.POST, header, value) {
            @Override
            protected void onPreRun() {

            }

            @Override
            protected void onSuccess(String response) {
                try {
                    JSONObject Response = new JSONObject(response);

                    Boolean status = Response.getBoolean("parsadi");

                    if (!status) {
                        setStatus(ERROR);
                    } else {
                        setStatus(LOGGED_IN);
                        mBroadcastReceiver = new BroadcastReceiver() {
                            @Override
                            public void onReceive(Context context, Intent intent) {
                                Bundle data = intent.getExtras();

                                String message = data.getString(CityOfTwo.KEY_MESSAGE, "");

                                if (!message.isEmpty()) {
                                    switch (message) {
                                        case "START_CHAT":
                                            Intent i = new Intent(LobbyActivity.this, ConversationActivity.class);

                                            startActivityForResult(i, CityOfTwo.ACTIVITY_CONVERSATION);
                                    }
                                }
                            }
                        };

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

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        final ImageView ProfileImageOne = (ImageView) findViewById(R.id.lobby_progress_one);
        final ImageView ProfileImageTwo = (ImageView) findViewById(R.id.lobby_progress_two);
        final ImageView ProfileImageThree = (ImageView) findViewById(R.id.lobby_progress_three);
        final ImageView ProfileImageFour = (ImageView) findViewById(R.id.lobby_progress_four);

        int width = ProfileImageOne.getWidth(),
                height = ProfileImageOne.getHeight();

        mLogoImage.setImageBitmap(CityOfTwo.logoBitmap);

//        Picasso.with(this)
//                .load(R.drawable.mipmap_1)
//                .resize(width, height)
//                .into(ProfileImageOne);
//
//        Picasso.with(this)
//                .load(R.drawable.mipmap_2)
//                .resize(width, height)
//                .into(ProfileImageTwo);
//
//        Picasso.with(this)
//                .load(R.drawable.mipmap_3)
//                .resize(width, height)
//                .into(ProfileImageThree);
//
//        Picasso.with(this)
//                .load(R.drawable.mipmap_4)
//                .resize(width, height)
//                .into(ProfileImageFour);

        ProfileImageOne.setImageBitmap(CityOfTwo.logoBitmap);
        ProfileImageTwo.setImageBitmap(rotateBitmap(CityOfTwo.logoBitmap, 90));
        ProfileImageThree.setImageBitmap(rotateBitmap(CityOfTwo.logoBitmap, 180));
        ProfileImageFour.setImageBitmap(rotateBitmap(CityOfTwo.logoBitmap, 270));
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


        final Animation slideFromRight = AnimationUtils.loadAnimation(this, R.anim.slide_from_right);
        final Animation slideToLeft = AnimationUtils.loadAnimation(this, R.anim.slide_to_left);

        slideToLeft.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mCanPutText = false;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mCanPutText = true;
                mLobbyDescription.setText(mDescriptionBuffer);
                mLobbyDescription.startAnimation(slideFromRight);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        slideFromRight.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mCanPutText = false;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mCanPutText = true;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        mLobbyDescription.startAnimation(slideToLeft);

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
            if (mLobbyProgressBar == null)
                mLobbyProgressBar = (ProgressBar) findViewById(R.id.lobby_progressbar);
            mLobbyProgressBar.setVisibility(View.INVISIBLE);

            mImageFlipper.stopFlipping();

            setLobbyDescription("There was an error. Please try again later.");
        } else {
            if (mLobbyProgressBar == null)
                mLobbyProgressBar = (ProgressBar) findViewById(R.id.lobby_progressbar);
            mLobbyProgressBar.setVisibility(View.VISIBLE);

            if (!mImageFlipper.isFlipping())
                mImageFlipper.startFlipping();
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
                    String answers = data.getStringExtra(CityOfTwo.KEY_TEST_RESULT);
                    submitTest(answers);
                }
            }
        }
    }
}
