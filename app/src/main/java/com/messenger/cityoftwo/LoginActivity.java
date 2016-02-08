package com.messenger.cityoftwo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

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

        mCallbackManager = CallbackManager.Factory.create();

        if (isLoggedIn()) {
            OpenLobby();
            return;
        } else {
            Log.i("Facebook Login", "Not logged in");
        }

        LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                AccessToken accessToken = loginResult.getAccessToken();
                Profile profile = Profile.getCurrentProfile();

                OpenLobby(accessToken, profile);

                Log.i("Facebook Login", "Login Success: " + profile.getName() + " ");
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

        i.putExtra(TesseraApplication.ACCESS_TOKEN, accessToken);
        i.putExtra(TesseraApplication.PROFILE, profile);

        startActivityForResult(i, TesseraApplication.LOGIN);
    }

    private void OpenLobby() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
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
    protected void onPause() {
        super.onPause();

        // Logs 'messenger deactivate' App Event.
        AppEventsLogger.deactivateApp(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mCallbackManager.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case TesseraApplication.LOGIN:
                if (resultCode == RESULT_CANCELED)
                    finish();
                break;
            default:
                break;
        }
    }
}
