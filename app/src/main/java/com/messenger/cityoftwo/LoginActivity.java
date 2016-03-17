package com.messenger.cityoftwo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.squareup.picasso.Picasso;

/**
 * Created by Aayush on 2/3/2016.
 */
public class LoginActivity extends AppCompatActivity {
    private CallbackManager mCallbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());

        setContentView(R.layout.activity_login);

        ImageView BackgroundView = (ImageView) findViewById(R.id.background_view);

        mCallbackManager = CallbackManager.Factory.create();

        if (isLoggedIn()) {
            OpenLobby();
            return;
        } else {
            Log.i("Facebook Login", "Not logged in");
        }

        LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions("user_likes");

        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                AccessToken accessToken = loginResult.getAccessToken();
                Log.i("Facebook Access Token", accessToken.getToken());

                OpenLobby(accessToken);
            }

            @Override
            public void onCancel() {
                Log.i("Facebook Login", "Login Cancelled");
            }

            @Override
            public void onError(FacebookException error) {
                Log.i("Facebook Login", "Login Error");
            }
        });

    }

    private void OpenLobby(AccessToken accessToken, Profile profile) {
        Intent i = new Intent(this, LobbyActivity.class);

        i.putExtra(CityOfTwo.KEY_ACCESS_TOKEN, accessToken);
        i.putExtra(CityOfTwo.KEY_PROFILE, profile);

        startActivityForResult(i, CityOfTwo.ACTIVITY_LOBBY);
    }

    private void OpenLobby() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        Profile profile = Profile.getCurrentProfile();

        OpenLobby(accessToken, profile);
    }

    private void OpenLobby(AccessToken accessToken) {
        Profile profile = Profile.getCurrentProfile();

        OpenLobby(accessToken, profile);
    }

    private boolean isLoggedIn() {
            AccessToken accessToken = AccessToken.getCurrentAccessToken();
            return accessToken != null;
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Logs 'install' and 'messenger activate' App Events.
        AppEventsLogger.activateApp(this);
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
    protected void onPause() {
        super.onPause();

        // Logs 'messenger deactivate' App Event.
        AppEventsLogger.deactivateApp(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mCallbackManager.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case CityOfTwo.ACTIVITY_LOBBY:
                if (resultCode == RESULT_CANCELED)
//                    finish();
                break;
            default:
                break;
        }
    }
}
