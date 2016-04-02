package com.messenger.cityoftwo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.bumptech.glide.request.target.SimpleTarget;
import com.facebook.AccessToken;
import com.facebook.Profile;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

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
    BroadcastReceiver mBroadcastReceiver;
    ProgressBar mLobbyProgressBar;
    TextView mLobbyDescription;
    AccessToken mAccessToken;
    Profile mProfile;
    Boolean mCanPutText = true;
    String mDescriptionBuffer = "";
    int lobbyState = BEGIN;
    SimpleTarget<Bitmap> mUserProfileBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        Profile profile = null;

        if (savedInstanceState != null) {
            mAccessToken = savedInstanceState.getParcelable("LOBBY_ACCESS_TOKEN");
            mProfile = savedInstanceState.getParcelable("LOBBY_PROFILE");

            setStatus(savedInstanceState.getInt("LOBBY_STATUS"));
        } else {
            mAccessToken = getIntent().getParcelableExtra(CityOfTwo.KEY_ACCESS_TOKEN);
            mProfile = getIntent().getParcelableExtra(CityOfTwo.KEY_PROFILE);
            setStatus(BEGIN);

            if (mProfile == null) finish();
        }

        ImageView ProfileImageOne = (ImageView) findViewById(R.id.lobby_progress_one);
        ImageView ProfileImageTwo = (ImageView) findViewById(R.id.lobby_progress_two);
        ImageView ProfileImageThree = (ImageView) findViewById(R.id.lobby_progress_three);
        ImageView ProfileImageFour = (ImageView) findViewById(R.id.lobby_progress_four);

        TextView ProfileTextView = (TextView) findViewById(R.id.lobby_profile_name);

        mLobbyProgressBar = (ProgressBar) findViewById(R.id.lobby_progressbar);
        mLobbyDescription = (TextView) findViewById(R.id.lobby_progress_description);

        final ViewFlipper imageFlipper = (ViewFlipper) findViewById(R.id.lobby_image_flipper);

        imageFlipper.setFlipInterval(1000);
        imageFlipper.setInAnimation(this, android.R.anim.fade_in);
        imageFlipper.setOutAnimation(this, android.R.anim.fade_out);
        imageFlipper.setAutoStart(true);
        imageFlipper.startFlipping();

        ImageButton ReloadButton = (ImageButton) findViewById(R.id.refresh_button);

        int width = ProfileImageOne.getLayoutParams().width,
                height = ProfileImageOne.getLayoutParams().height;

        Uri uri = mProfile.getProfilePictureUri(width, height);

        Picasso.with(this)
                .load(R.drawable.mipmap_1)
                .resize(width, height)
                .into(ProfileImageOne);

        Picasso.with(this)
                .load(R.drawable.mipmap_2)
                .resize(width, height)
                .into(ProfileImageTwo);
        Picasso.with(this)
                .load(R.drawable.mipmap_3)
                .resize(width, height)
                .into(ProfileImageThree);
        Picasso.with(this)
                .load(R.drawable.mipmap_4)
                .resize(width, height)
                .into(ProfileImageFour);

        ProfileTextView.setText(mProfile.getName());

        CityOfTwo.RegisterGCM(this);

        mBroadcastReceiver = new

                BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        String s = intent.getStringExtra(CityOfTwo.KEY_TYPE);

                        Log.i("Receiver", "Signal Received: " + s);

                        switch (s) {
                            case "CHAT_BEGIN":
                                Intent i = new Intent(LobbyActivity.this, ConversationActivity.class);

                                LocalBroadcastManager.getInstance(LobbyActivity.this).unregisterReceiver(mBroadcastReceiver);

                                startActivityForResult(i, CityOfTwo.ACTIVITY_CONVERSATION);
                        }
                    }
                }

        ;

        ReloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setStatus(BEGIN);
                facebookLogin(mAccessToken);
            }
        });
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


    @Override
    protected void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);

        CityOfTwo.APPLICATION_STATE = CityOfTwo.APPLICATION_BACKGROUND;
    }

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter();
        filter.addAction(CityOfTwo.PACKAGE_NAME);

        LocalBroadcastManager.getInstance(this).registerReceiver((mBroadcastReceiver), filter);

        CityOfTwo.APPLICATION_STATE = CityOfTwo.APPLICATION_FOREGROUND;
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("LOBBY_STATUS", getStatus());
        outState.putParcelable("LOBBY_ACCESS_TOKEN", mAccessToken);
        outState.putParcelable("LOBBY_PROFILE", mProfile);
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


                        // Testing/Simulating test phase

                        ArrayList<Integer> answers = new ArrayList<>();
                        answers.add(1);
                        answers.add(1);
                        answers.add(0);

                        submitTest(answers, accessToken);

                        // Modify code to adapt for testing phase

                        // String test = Response.getString("test");

                        // Intent intent = new Intent(LobbyActivity.this, TestActivity.class);
                        // intent.putExtra(CityOfTwo.KEY_TEST, test);

                        // startActivityForResult(intent, CityOfTwo.ACTIVITY_TEST);
                    }

                } catch (JSONException e) {
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
                        facebookLogin(accessToken);
                    } else {
                        // Testing/Simulating test phase

                        ArrayList<Integer> answers = new ArrayList<>();
                        answers.add(1);
                        answers.add(1);
                        answers.add(0);

                        submitTest(answers, accessToken);
                    }

                } catch (JSONException e) {
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
//                Intent intent = new Intent(LobbyActivity.this, TestActivity.class);
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

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case CityOfTwo.ACTIVITY_TEST:
                if (resultCode == RESULT_OK) {
                    submitTest(data.getExtras().getIntegerArrayList(CityOfTwo.KEY_SELECTED_ANSWER), mAccessToken);

                }
        }
    }

    private void submitTest(ArrayList<Integer> answers, final AccessToken accessToken) {
        String submit_test = getString(R.string.url_test),
                header = CityOfTwo.HEADER_TEST_RESULT;

        StringBuilder sb = new StringBuilder();
        for (Integer number : answers)
            sb.append(number != null ? number.toString() : "");

        String value = sb.toString();

        String[] Path = {CityOfTwo.API, submit_test};

        HttpHandler TestHttpHandler = new HttpHandler(CityOfTwo.HOST, Path, HttpHandler.POST, header, value) {
            @Override
            protected void onSuccess(String response) {
                try {
                    JSONObject Response = new JSONObject(response);
                    Boolean status = Response.getBoolean("parsadi");

                    if (!status) {
                        setStatus(ERROR);
                    } else {
                        waitForServer();
                    }

                } catch (JSONException e) {
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

        // Run code after changing state
        if (lobbyState == ERROR) {
            if (mLobbyProgressBar == null)
                mLobbyProgressBar = (ProgressBar) findViewById(R.id.lobby_progressbar);
            mLobbyProgressBar.setVisibility(View.INVISIBLE);

            setLobbyDescription("There was an error. Please try again later.");
        } else {
            if (mLobbyProgressBar == null)
                mLobbyProgressBar = (ProgressBar) findViewById(R.id.lobby_progressbar);
            mLobbyProgressBar.setVisibility(View.VISIBLE);
        }

        if (lobbyState == BEGIN) {
            setLobbyDescription("Setting up your profile");
        }
        if (lobbyState == LOGGED_IN) {
            setLobbyDescription("Finding a match");
        }
    }
}
