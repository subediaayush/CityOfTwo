package com.messenger.cityoftwo;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Aayush on 2/3/2016.
 */
public class LoginActivity extends AppCompatActivity {
    private final List<String> permissionList = Arrays.asList("user_likes");
    private CallbackManager mCallbackManager;
    private LoginManager mLoginManager;
    private AccessTokenTracker mAccessTokenTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());

        setContentView(R.layout.activity_login);

        if (BuildConfig.DEBUG) {
            try {
                String regID = getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE)
                        .getString("REG_ID", "EMPTY");

                Log.i("GCM ID", regID);
            } catch (NullPointerException e) {
                Log.e("NullPointer Exception", "Shared Preferences not found");
            }

//            Intent i = new Intent(this, LobbyActivity.class);
//
//            AccessToken accessToken = AccessToken.getCurrentAccessToken();
//            Profile profile = Profile.getCurrentProfile();
//
//            i.putExtra(CityOfTwo.KEY_ACCESS_TOKEN, accessToken);
//            i.putExtra(CityOfTwo.KEY_PROFILE, profile);
//
//            startActivityForResult(i, CityOfTwo.ACTIVITY_LOBBY);
//            return;
//
//            startActivity(new Intent(this, ConversationActivity.class));
//            return;
        }

        Button getStartedButton = (Button) findViewById(R.id.get_started_button);

        mCallbackManager = CallbackManager.Factory.create();
        mLoginManager = LoginManager.getInstance();

        getStartedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateAccessToken(
                        AccessToken.getCurrentAccessToken()
                );
            }
        });

        // Registering Facebook login manager to handle login events
        mLoginManager.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                AccessToken accessToken = loginResult.getAccessToken();
                Log.i("Facebook Access Token", accessToken.getToken());

//                openLobby(accessToken);
            }

            @Override
            public void onCancel() {
                Log.i("Facebook Login", "Login Cancelled");
                showLoginError();
            }

            @Override
            public void onError(FacebookException error) {
                Log.i("Facebook Login", "Login Error");
                showLoginError();
            }
        });

        // Registering access toke228n tracker to handle changes in access tokens
        mAccessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {

                Log.i("AccessToken", "AccessToken updated");
                updateAccessToken(currentAccessToken);
            }
        };

    }

    private void updateAccessToken(AccessToken currentAccessToken) {
        if (currentAccessToken == null) {
            // No AccessToken found
            // Attempting to re login
            mLoginManager.logInWithReadPermissions(
                    this,
                    permissionList
            );
            Log.i("Facebook Login", "No user found");
        } else {
            // Starting app with current logged in account
            openLobby(currentAccessToken);
            Log.i("Facebook Login", "Logging in with current account");
        }
    }

    private void showLoginError() {
        AlertDialog.Builder adb = new AlertDialog.Builder(this);

        adb.setTitle("Oops");
        adb.setMessage("Something went wrong!");
        adb.setPositiveButton("Try Again", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mLoginManager == null) mLoginManager = LoginManager.getInstance();
                mLoginManager.logInWithReadPermissions(
                        LoginActivity.this,
                        permissionList
                );
            }
        });
        adb.setNegativeButton("Exit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        Dialog warningDialog = adb.create();
        warningDialog.show();
    }

    private void openLobby(AccessToken accessToken, Profile profile) {
        Intent i = new Intent(this, LobbyActivity.class);

        i.putExtra(CityOfTwo.KEY_ACCESS_TOKEN, accessToken);
        i.putExtra(CityOfTwo.KEY_PROFILE, profile);

        Log.i("Lobby Actiity", "Opening Lobby Activity");
        startActivityForResult(i, CityOfTwo.ACTIVITY_LOBBY);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

    }

    private void openLobby() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        Profile profile = Profile.getCurrentProfile();

        openLobby(accessToken, profile);
    }

    private void openLobby(AccessToken accessToken) {
        Profile profile = Profile.getCurrentProfile();

        openLobby(accessToken, profile);
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
            case CityOfTwo.ACTIVITY_LOBBY:
                switch (resultCode) {
                    case RESULT_CANCELED:
                        recreate();
                        break;
                    case RESULT_OK:
                        finish();
                        break;
                }
                break;
            default:
                break;
        }
    }
}
