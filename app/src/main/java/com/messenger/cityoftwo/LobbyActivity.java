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
import android.widget.ImageView;
import android.widget.TextView;

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

    //    TestHttpHandler, SignUpHttpHandler, TestSubmitHttpHandler;
    BroadcastReceiver mBroadcastReceiver;

    //    ProgressBar mLobbyProgressBar;
    TextView mLobbyDescription;

    AccessToken mAccessToken;
    Profile mProfile;
    Boolean mCanPutText = true;
    String mDescriptionBuffer = "";

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
        TextView ProfileTextView = (TextView) findViewById(R.id.lobby_profile_name);

//        mLobbyProgressBar = (ProgressBar) findViewById(R.id.lobby_progressbar);
        mLobbyDescription = (TextView) findViewById(R.id.lobby_progress_description);

        ImageButton ReloadButton = (ImageButton) findViewById(R.id.refresh_button);

        int width = ProfileImageView.getLayoutParams().width,
                height = ProfileImageView.getLayoutParams().height;

        Uri uri = profile.getProfilePictureUri(width, height);

        Picasso.with(this)
                .load(uri)
                .centerCrop()
                .placeholder(R.drawable.user_placeholder)
                .error(R.drawable.user_placeholder)
                .resize(100, 100)
                .into(ProfileImageView);

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
                Status.setStatus(State.BEGIN);
                facebookLogin(mAccessToken);
            }
        });

        switch (Status.getStatus()) {
            case BEGIN:
                facebookLogin(mAccessToken);
                break;
            case SIGNED_UP:
                facebookLogin(mAccessToken);
                break;
            case LOGGED_IN:
                waitForServer();
                break;
            default:
                break;
        }

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState != null) {
            mAccessToken = savedInstanceState.getParcelable("LOBBY_ACCESS_TOKEN");
            mProfile = savedInstanceState.getParcelable("LOBBY_PROFILE");

            State s = State.values()[savedInstanceState.getInt("LOBBY_STATUS")];
            Status.setStatus(s);
        } else {
            mAccessToken = getIntent().getParcelableExtra(CityOfTwo.KEY_ACCESS_TOKEN);
            mProfile = getIntent().getParcelableExtra(CityOfTwo.KEY_PROFILE);
            Status.setStatus(State.BEGIN);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("LOBBY_STATUS", Status.getStatus().ordinal());
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
            }

            @Override
            protected void onSuccess(String response) {
                try {
                    JSONObject Response = new JSONObject(response);

                    Boolean status = Response.getBoolean("parsadi");

                    if (!status) {
                        setLobbyDescription("Seems like there is a problem.\n" +
                                "Please try again later.");
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

        ImageView BackgroundView = (ImageView) findViewById(R.id.background_view);

        int width = BackgroundView.getMeasuredWidth(),
                height = BackgroundView.getMeasuredHeight();

        Picasso.with(this)
                .load(R.drawable.background)
                .resize(width, height)
                .centerCrop()
                .into(BackgroundView);
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

    public void setLobbyDescription(final String lobbyDescription) {
        if (mLobbyDescription == null)
            mLobbyDescription = (TextView) findViewById(R.id.lobby_progress_description);

        if (lobbyDescription.equals(mLobbyDescription.getText())) return;

        mDescriptionBuffer = lobbyDescription;
        if (!mCanPutText) {
            return;
        }

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

    public enum State {
        BEGIN,
        SIGNED_UP,
        LOGGED_IN
    }

    static class Status {
        static State status = State.BEGIN;

        public static State getStatus() {
            return status;
        }

        public static void setStatus(State s) {
            status = s;
            Log.i("Lobby Status", "Status set " + status.name());
        }
    }


}
