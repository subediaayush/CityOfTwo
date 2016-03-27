package com.messenger.cityoftwo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.facebook.AccessToken;
import com.facebook.Profile;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

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
    ArrayList<String> mLobbyDescriptionBuffer = new ArrayList<>();
    int lobbyState = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        Profile profile = null;

        try {
            mAccessToken = getIntent().getParcelableExtra(CityOfTwo.KEY_ACCESS_TOKEN);
            profile = getIntent().getParcelableExtra(CityOfTwo.KEY_PROFILE);

            if (profile == null) throw new NullPointerException("");
        } catch (Exception e) {
            Log.i("Error", "Error while loading user");
            setResult(RESULT_CANCELED, new Intent());
            finish();
            return;
        }

        CircleImageView ProfileImageView = (CircleImageView) findViewById(R.id.lobby_profile_image);
        CircleImageView ProfileMaskedImageView = (CircleImageView) findViewById(R.id.lobby_profile_masked);
        TextView ProfileTextView = (TextView) findViewById(R.id.lobby_profile_name);

        mLobbyProgressBar = (ProgressBar) findViewById(R.id.lobby_progressbar);
        mLobbyDescription = (TextView) findViewById(R.id.lobby_progress_description);

        ViewFlipper imageFlipper = (ViewFlipper) findViewById(R.id.lobby_image_flipper);
        imageFlipper.setAutoStart(true);
        imageFlipper.setFlipInterval(1000);
        imageFlipper.startFlipping();
        imageFlipper.setInAnimation(this, R.anim.card_flip_in);
        imageFlipper.setOutAnimation(this, R.anim.card_flip_out);

        ImageButton ReloadButton = (ImageButton) findViewById(R.id.refresh_button);

        int width = ProfileImageView.getLayoutParams().width,
                height = ProfileImageView.getLayoutParams().height;

        Uri uri = profile.getProfilePictureUri(width, height);

        Picasso.with(this)
                .load(uri)
                .centerCrop()
                .placeholder(R.drawable.user_placeholder)
                .error(R.drawable.user_placeholder)
                .resize(width, height)
                .into(ProfileImageView);

        Picasso.with(this)
                .load(R.drawable.user_placeholder)
                .centerCrop()
                .resize(width, height)
                .into(ProfileMaskedImageView);

        ProfileTextView.setText(profile.getName());

        CityOfTwo.RegisterGCM(this);

        mBroadcastReceiver = new BroadcastReceiver() {
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
        };

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
            default:
                break;
        }

    }


    @Override
    protected void onPause() {
        super.onPause();

        CityOfTwo.APPLICATION_STATE = CityOfTwo.APPLICATION_BACKGROUND;
    }

    @Override
    protected void onResume() {
        super.onResume();

        CityOfTwo.APPLICATION_STATE = CityOfTwo.APPLICATION_FOREGROUND;
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState != null) {
            mAccessToken = savedInstanceState.getParcelable("LOBBY_ACCESS_TOKEN");
            mProfile = savedInstanceState.getParcelable("LOBBY_PROFILE");

            setStatus(savedInstanceState.getInt("LOBBY_STATUS"));
        } else {
            mAccessToken = getIntent().getParcelableExtra(CityOfTwo.KEY_ACCESS_TOKEN);
            mProfile = getIntent().getParcelableExtra(CityOfTwo.KEY_PROFILE);
            setStatus(BEGIN);
        }
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
            protected void onPreRun() {
                setLobbyDescription("Please wait while we set up your profile...");
            }

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

                        submitTest(answers);

                        waitForServer();

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
                setLobbyDescription("Seems like there is a problem.\n" +
                        "Please try again later.");
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
            protected void onPreRun() {
                setLobbyDescription("Please wait while we set up your profile...");
            }

            @Override
            protected void onSuccess(String response) {
                try {
                    JSONObject Response = new JSONObject(response);

                    Boolean registered = Response.getBoolean("parsadi");

                    if (!registered) {
                        facebookLogin(accessToken);
                    } else {

                        setLobbyDescription("Please wait while we find someone for you to talk to.");

                        // Testing/Simulating test phase

                        ArrayList<Integer> answers = new ArrayList<>();
                        answers.add(1);
                        answers.add(1);
                        answers.add(0);

                        submitTest(answers);
                        waitForServer();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            protected void onFailure(Integer status) {
                setLobbyDescription("Seems like there is a problem.\n" +
                        "Please try again later.");
                setStatus(ERROR);
            }
        };

        SignUpHttpHandler.execute();

//        String test = getString(R.string.url_test);
//        path[1] = test;

//        TestHttpHandler = new HttpHandler(CityOfTwo.HOST, path) {
//            @Override
//            protected void onPreRun() {
//                setLobbyDescription("Currently downloading your test to set up your profile...");
//            }
//
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
//                setLobbyDescription("Seems like there is a problem.\n" +
//                        "Please try again later.");
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
                setLobbyDescription("Waiting for server to respond");
            }

            @Override
            protected void onSuccess(String response) {
                try {
                    JSONObject Response = new JSONObject(response);

                    Boolean status = Response.getBoolean("parsadi");

                    if (!status) {
                        setLobbyDescription("Seems like there is a problem.\n" +
                                "Please try again later.");
                        setStatus(ERROR);
                    } else {
                        setLobbyDescription("Please wait while we find someone for you to talk to.");
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
                setLobbyDescription("Seems like there is a problem.\n" +
                        "Please try again later.");
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
                    submitTest(data.getExtras().getIntegerArrayList(CityOfTwo.KEY_SELECTED_ANSWER));

                }
        }
    }

    private void submitTest(ArrayList<Integer> answers) {
        String submit_test = getString(R.string.url_test),
                header = CityOfTwo.HEADER_TEST_RESULT;

        StringBuilder sb = new StringBuilder();
        for (Integer number : answers)
            sb.append(number != null ? number.toString() : "");

        String value = sb.toString();

        String[] Path = {CityOfTwo.API, submit_test};

        HttpHandler TestHttpHandler = new HttpHandler(CityOfTwo.HOST, Path, HttpHandler.POST, header, value) {
            @Override
            protected void onPreRun() {
                setLobbyDescription("Submitting your test results to set up your profile...");
            }

            @Override
            protected void onSuccess(String response) {
                try {
                    JSONObject Response = new JSONObject(response);
                    Boolean status = Response.getBoolean("parsadi");

                    if (!status) {
                        setLobbyDescription("Seems like there is a problem.\n" +
                                "Please try again later.");
                        setStatus(ERROR);
                    } else {
                        setLobbyDescription("Please wait while we find someone for you to talk to.");
                        waitForServer();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            protected void onFailure(Integer status) {
                setLobbyDescription("Seems like there is a problem.\n" +
                        "Please try again later.");
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

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter();
        filter.addAction(CityOfTwo.PACKAGE_NAME);

        LocalBroadcastManager.getInstance(this).registerReceiver((mBroadcastReceiver), filter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
    }

    public void setLobbyDescription(String lobbyDescription) {
        if (mLobbyDescription == null)
            mLobbyDescription = (TextView) findViewById(R.id.lobby_progress_description);

        // Set the new description buffer
        mDescriptionBuffer = lobbyDescription;

        // Do not do anything else if animation is running
        // or if two consecutive description are same
        try {
            if (!lobbyDescription.equals(mLobbyDescriptionBuffer.get(0)))
                mLobbyDescriptionBuffer.add(lobbyDescription);
        } catch (IndexOutOfBoundsException e) {
            mLobbyDescriptionBuffer.add(lobbyDescription);
        }

        if (!mCanPutText) {
            return;
        }

        final String currentDescription = mLobbyDescriptionBuffer.get(0);
        mLobbyDescriptionBuffer.remove(0);

        final Animation slideFromRight = AnimationUtils.loadAnimation(this, R.anim.slide_from_right);
        Animation slideToLeft = AnimationUtils.loadAnimation(this, R.anim.slide_to_left);

        slideToLeft.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mCanPutText = false;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mCanPutText = true;
                mLobbyDescription.setText(currentDescription);
                mLobbyDescription.startAnimation(slideFromRight);
                if (!mLobbyDescriptionBuffer.isEmpty())
                    setLobbyDescription(mLobbyDescriptionBuffer.get(0));
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
        } else {
            if (mLobbyProgressBar == null)
                mLobbyProgressBar = (ProgressBar) findViewById(R.id.lobby_progressbar);
            mLobbyProgressBar.setVisibility(View.VISIBLE);

        }
    }
}
