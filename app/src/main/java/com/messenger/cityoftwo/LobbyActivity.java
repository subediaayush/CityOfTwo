package com.messenger.cityoftwo;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.transition.Fade;
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

import com.facebook.AccessToken;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;
import java.util.Random;

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

    private final static String[] TIPS = new String[]{
            "",
            "Earn credits by sharing CoyRudy",
            "If you want you can share your facebook profile with stranger",
            "Apply filters to constrict matching to your preference"
    };

    //    TestHttpHandler, SignUpHttpHandler, TestSubmitHttpHandler;
    BroadcastReceiver mBroadcastReceiver;
    ProgressBar mLobbyProgressBar;
    TextView mLobbyDescription;
    CardView mLobbyTipsContainer;
    TextView mLobbyTips;
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
    private boolean tokenNotGenerated = false;
    private Integer currentTip = -1;

    private Handler tipHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        mLobbyProgressBar = (ProgressBar) findViewById(R.id.lobby_progressbar);
        mLobbyDescription = (TextView) findViewById(R.id.lobby_progress_description);

        mWelcomeLabel = (TextView) findViewById(R.id.welcome_label);

        mImageFlipper = (FrameLayout) findViewById(R.id.lobby_image_flipper);

        mImageFlipperFragment = LogoFadeFragment.newInstance();
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction()
                .replace(R.id.lobby_image_flipper, mImageFlipperFragment)
                .commit();

        fm.executePendingTransactions();
//        mBackgroundAnimationFragment = BackgroundAnimationFragment.newInstance();
//        getSupportFragmentManager().beginTransaction()
//                .replace(R.id.lobby_background, mBackgroundAnimationFragment)
//                .commit();

        mAccessToken = AccessToken.getCurrentAccessToken();
        if (savedInstanceState != null) {
            setStatus(savedInstanceState.getInt("LOBBY_STATUS"));
        } else {
            final Bundle args = getIntent().getExtras();

            final int initialX = args.getInt(CityOfTwo.KEY_LOCATION_X, -1),
                    initialY = args.getInt(CityOfTwo.KEY_LOCATION_Y, -1),
                    initialWidth = args.getInt(CityOfTwo.KEY_WIDTH, -1),
                    initialHeight = args.getInt(CityOfTwo.KEY_HEIGHT, -1);

            ViewTreeObserver treeObserver = mImageFlipper.getViewTreeObserver();
            treeObserver.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    mImageFlipper.getViewTreeObserver().removeOnPreDrawListener(this);

                    return true;
                }
            });
        }

        mReloadButton = (ImageButton) findViewById(R.id.refresh_button);

        if (!BuildConfig.DEBUG) mReloadButton.setVisibility(View.GONE);

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Bundle data = intent.getExtras();

                Log.i("LobbyReceiver", "Signal Received: " + action);

                switch (action) {
                    case CityOfTwo.ACTION_BEGIN_CHAT: {
                        Log.i("Lobby", "Starting new chat");
                        Intent conversationIntent = new Intent(LobbyActivity.this, ConversationActivity.class);

                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                            getWindow().setExitTransition(new Fade());

                            startActivityForResult(
                                    conversationIntent,
                                    CityOfTwo.ACTIVITY_PROFILE);
                        } else {

                            startActivityForResult(conversationIntent, CityOfTwo.ACTIVITY_PROFILE);

                            overridePendingTransition(
                                    android.R.anim.fade_in,
                                    android.R.anim.fade_out
                            );
                        }
                        break;
                    }
                    case CityOfTwo.ACTION_NEW_MESSAGE: {
                        break;
                    }
                    case CityOfTwo.ACTION_FCM_ID: {
                            waitForServer();
                            tokenNotGenerated = false;
                        break;
                    }
                    case CityOfTwo.ACTION_USER_OFFLINE: {
                        getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE).edit()
                                .remove(CityOfTwo.KEY_CHAT_PENDING)
                                .remove(CityOfTwo.KEY_COMMON_LIKES)
                                .remove(CityOfTwo.KEY_LAST_CHATROOM)
                                .remove(CityOfTwo.KEY_SESSION_ACTIVE)
                                .apply();

                        Log.i("LobbyActivity", "User Timeout received");

                        setStatus(BEGIN);
                        facebookLogin();
                        break;
                    }
                }
            }
        };

        mReloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE).edit()
                        .remove(CityOfTwo.KEY_LAST_CHATROOM)
                        .remove(CityOfTwo.KEY_COMMON_LIKES)
                        .remove(CityOfTwo.KEY_CHAT_PENDING)
                        .apply();

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

        setStatus(BEGIN);
//                        CityOfTwo.RegisterGCM(LobbyActivity.this);
        startLobby();


    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("LOBBY_STATUS", getStatus());
        outState.putParcelable("LOBBY_ACCESS_TOKEN", mAccessToken);
    }

    private void facebookLogin() {
        facebookLogin(AccessToken.getCurrentAccessToken());
    }

    private void startEnterAnimation() {
        mImageFlipper.setPivotX(0);
        mImageFlipper.setPivotY(0);
        mImageFlipper.setScaleX(xScaleFactor);
        mImageFlipper.setScaleY(yScaleFactor);
        mImageFlipper.setTranslationX(xDelta);
        mImageFlipper.setTranslationY(yDelta);
        mImageFlipper.setAlpha(1f);

        // Animate scale and translation to go received thumbnail to full size
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
                        // is done. Slide and fade the name in received underneath
                        // the picture.

                        setStatus(BEGIN);
//                        CityOfTwo.RegisterGCM(LobbyActivity.this);

                        startLobby();

                    }
                });
    }

    private void startLobby() {
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
                        String uniqueCode = Response.getString(CityOfTwo.KEY_ID);
                        Integer credits = Response.getInt(CityOfTwo.KEY_CREDITS);
                        Boolean filters_applied = Response.getBoolean(CityOfTwo.KEY_FILTERS_APPLIED);

                        Editor securedEditor = new SecurePreferences(
                                LobbyActivity.this,
                                CityOfTwo.SECURED_PREFERENCE
                        ).edit();

                        getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE)
                                .edit().putString(CityOfTwo.KEY_SESSION_TOKEN, sessionToken)
                                .putBoolean(CityOfTwo.KEY_FILTERS_APPLIED, filters_applied)
                                .putBoolean(CityOfTwo.KEY_USER_OFFLINE, false)
                                .apply();

                        if (filters_applied) {
                            JSONObject filters = Response.getJSONObject(CityOfTwo.KEY_FILTERS);
                            Integer minAge = filters.getInt(CityOfTwo.KEY_MIN_AGE);
                            Integer maxAge = filters.getInt(CityOfTwo.KEY_MAX_AGE);
                            Integer distance = filters.getInt(CityOfTwo.KEY_DISTANCE);
                            Boolean matchMale = filters.getBoolean(CityOfTwo.KEY_MATCH_MALE);
                            Boolean matchFemale = filters.getBoolean(CityOfTwo.KEY_MATCH_FEMALE);

                            securedEditor.putInt(CityOfTwo.KEY_MIN_AGE, minAge)
                                    .putInt(CityOfTwo.KEY_MAX_AGE, maxAge)
                                    .putInt(CityOfTwo.KEY_DISTANCE, distance)
                                    .putBoolean(CityOfTwo.KEY_MATCH_MALE, matchMale)
                                    .putBoolean(CityOfTwo.KEY_MATCH_FEMALE, matchFemale);
                        }
                        securedEditor.putString(CityOfTwo.KEY_ID, uniqueCode)
                                .putInt(CityOfTwo.KEY_CREDITS, credits)
                                .apply();

                        int pool = Response.getInt("pool");

                        if (pool != 0) {
                            waitForServer();
                        } else {
                            String testResult = getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE)
                                    .getString(CityOfTwo.KEY_TEST_RESULT, "");
                            submitTest(testResult);
                        }

                        // Modify id to adapt for testing phase

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
        }.execute();
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

                        tipHandler.removeCallbacksAndMessages(null);
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
        final SharedPreferences sharedPreferences = getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE);

        String broadcast_gcm = getString(R.string.url_broadcast_gcm),
                header = CityOfTwo.HEADER_GCM_ID,
                value = sharedPreferences.getString(CityOfTwo.KEY_REG_ID, "");

        if (value.isEmpty()) {
            value = FirebaseInstanceId.getInstance().getToken();
            if (value == null) {
                tokenNotGenerated = true;
                Log.i("Lobby", "Token not yet genereted");
                return;
            }
        }

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

        String token = "Token " + sharedPreferences.getString(CityOfTwo.KEY_SESSION_TOKEN, "");

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

    public void setLobbyDescription(String lobbyDescription) {
        if (mLobbyDescription == null)
            mLobbyDescription = (TextView) findViewById(R.id.lobby_progress_description);

        // Set the new description buffer
        mDescriptionBuffer = lobbyDescription;

        // Do not do anything else if animation is running
        // or if two consecutive description are same
        if (!mCanPutText)
            return;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
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
        } else {
            mLobbyDescription.setText(mDescriptionBuffer);
            mCanPutText = true;
        }


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

    private Pair getNewTip(int currentTip) {
        Random rng = new Random();
        int tipIndex = currentTip;
        while (tipIndex == currentTip) tipIndex = rng.nextInt(TIPS.length);

        String tip;

        if (tipIndex == 0) {
            int credits = new SecurePreferences(this, CityOfTwo.SECURED_PREFERENCE)
                    .getInt(CityOfTwo.KEY_CREDITS, 0);

            if (credits < 1) tip = "You have no credit right now. Earn credits by sharing CoyRudy.";
            else if (credits == 1) tip = "You have 1 credit.";
            else tip = String.format(Locale.getDefault(), "You have %d credits.", credits);
        } else {
            tip = TIPS[tipIndex];
        }
        return new Pair<>(tip, tipIndex);
    }

    private Pair getNewTip() {
        return getNewTip(-1);
    }

    private int getStatus() {
        return lobbyState;
    }

    private void setStatus(int state) {
        // Run id before changing state

        lobbyState = state;
        if (mImageFlipper == null)
            mImageFlipper = (FrameLayout) findViewById(R.id.lobby_image_flipper);

        // Run id after changing state
        if (lobbyState == ERROR) {

            // Code for debug version
            if (BuildConfig.DEBUG) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Editor editor = getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE).edit();
                        editor.putString(CityOfTwo.KEY_COMMON_LIKES, "chari, pari, mari")
                                .putInt(CityOfTwo.KEY_LAST_CHATROOM, 1)
                                .apply();
                        Intent conversationActivity = new Intent(
                                LobbyActivity.this,
                                ConversationActivity.class
                        );
                        Log.i("Lobby", "Opeing chat in offline");
                        startActivityForResult(conversationActivity, CityOfTwo.ACTIVITY_PROFILE);
                        overridePendingTransition(
                                android.R.anim.fade_in,
                                android.R.anim.fade_out
                        );
                    }
                }, 2000);
            }

            mLobbyProgressBar.setVisibility(View.INVISIBLE);
            mImageFlipperFragment.stopFlipping();

            setLobbyDescription("There was an error. Please try again later.");
        } else {

            mLobbyProgressBar.setVisibility(View.VISIBLE);
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

        if (resultCode == CityOfTwo.RESULT_EXIT_APP) {
            setResult(resultCode);
            finish();
            return;
        }

        restoreActivity();
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK, new Intent());
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();

        LocalBroadcastManager b = LocalBroadcastManager.getInstance(this);
        b.unregisterReceiver(mBroadcastReceiver);

        Log.i("LobbyActivity", "Activity paused");
        CityOfTwo.setApplicationState(CityOfTwo.APPLICATION_BACKGROUND);
    }

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter();

        filter.addAction(CityOfTwo.ACTION_BEGIN_CHAT);
        filter.addAction(CityOfTwo.ACTION_NEW_MESSAGE);
        filter.addAction(CityOfTwo.ACTION_USER_OFFLINE);
        filter.addAction(CityOfTwo.ACTION_FCM_ID);
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, filter);

        Log.i("LobbyActivity", "Activity Resumed");
        CityOfTwo.setApplicationState(CityOfTwo.APPLICATION_FOREGROUND);
        CityOfTwo.setCurrentActivity(CityOfTwo.ACTIVITY_LOBBY);

        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.cancel(FirebaseMessageHandler.TAG_NOTIFICATION_NEW_MESSAGE, 10044);
        nm.cancel(FirebaseMessageHandler.TAG_NOTIFICATION_CHAT_BEGIN, 10045);
        nm.cancel(FirebaseMessageHandler.TAG_NOTIFICATION_CHAT_END, 10046);

        SharedPreferences sp = getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE);

        Boolean userOnline = sp.getBoolean(CityOfTwo.KEY_SESSION_ACTIVE, true);
        Editor editor = sp.edit();

        editor.remove(CityOfTwo.KEY_SESSION_ACTIVE);

        if (!userOnline) {
            editor.remove(CityOfTwo.KEY_LAST_CHATROOM)
                    .remove(CityOfTwo.KEY_CHAT_PENDING)
                    .apply();

            setStatus(BEGIN);
            facebookLogin();
            return;
        }

        editor.apply();

        Boolean chatPending = sp.getBoolean(CityOfTwo.KEY_CHAT_PENDING, false);

        if (chatPending) {
            Log.i("Lobby", "Chat pending, opening pending chat");
            Intent conversationIntent = new Intent(this, ConversationActivity.class);
            startActivityForResult(conversationIntent, CityOfTwo.ACTIVITY_PROFILE);
        }
    }

    private void restoreActivity() {
        revealView(mImageFlipper);
        revealView(mWelcomeLabel);
        revealView(mReloadButton);
        revealView(mLobbyProgressBar);

        getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE).edit()
                .remove(CityOfTwo.KEY_LAST_CHATROOM)
                .remove(CityOfTwo.KEY_COMMON_LIKES)
                .remove(CityOfTwo.KEY_CHAT_PENDING)
                .apply();

        mLobbyDescription.setAlpha(1);

        setStatus(BEGIN);
        facebookLogin(mAccessToken);
    }

    /**
     * @return the last know best location
     */
    private Location getLastBestLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // requestPermissions();
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
//            return TODO;
        }

        Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Location locationNet = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        long GPSLocationTime = 0;
        if (null != locationGPS) {
            GPSLocationTime = locationGPS.getTime();
        }

        long NetLocationTime = 0;

        if (null != locationNet) {
            NetLocationTime = locationNet.getTime();
        }

        if (0 < GPSLocationTime - NetLocationTime) {
            return locationGPS;
        } else {
            return locationNet;
        }
    }
}
