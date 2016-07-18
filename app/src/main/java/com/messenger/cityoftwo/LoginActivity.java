package com.messenger.cityoftwo;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.Arrays;
import java.util.List;

import static com.messenger.cityoftwo.CityOfTwo.isGooglePlayServicesAvailable;

/**
 * Created by Aayush on 2/3/2016.
 */
public class LoginActivity extends AppCompatActivity {
    private final List<String> permissionList = Arrays.asList("user_likes");
    private CallbackManager mCallbackManager;
    private LoginManager mLoginManager;
    private AccessTokenTracker mAccessTokenTracker;
    private Button mGetStartedButton;
    private ImageView mLogoImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);


        if (BuildConfig.DEBUG) {
            try {
//                FirebaseInstanceId.getInstance().getToken();

                String regID = getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE)
                        .getString(CityOfTwo.KEY_REG_ID, "");

                Log.i("FCM ID", regID);
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

//        FirebaseInstanceId.getInstance().getToken();

        mGetStartedButton = (Button) findViewById(R.id.get_started_button);
        mLogoImage = (ImageView) findViewById(R.id.coyrudy_logo);

        mCallbackManager = CallbackManager.Factory.create();
        mLoginManager = LoginManager.getInstance();


        mGetStartedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isGooglePlayServicesAvailable(LoginActivity.this, new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        finish();
                    }
                })) return;
                updateAccessToken(AccessToken.getCurrentAccessToken());
            }
        });

        // Registering Facebook login manager to handle login events
        mLoginManager.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                AccessToken accessToken = loginResult.getAccessToken();
                Log.i("Facebook Access Token", accessToken.getToken());

                openLobby(accessToken);
            }

            @Override
            public void onCancel() {
                Log.i("Facebook Login", "Login Cancelled");
                showLoginError();
            }

            @Override
            public void onError(FacebookException error) {
                Log.e("Facebook Login", "Login Error", error);
                showLoginError();
            }
        });


//        // Registering access toke228n tracker to handle changes in access tokens
//        mAccessTokenTracker = new AccessTokenTracker() {
//            @Override
//            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
//
//                Log.i("AccessToken", "AccessToken updated");
//                updateAccessToken(currentAccessToken);
//            }
//        };

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        int width = mLogoImage.getWidth(),
                height = mLogoImage.getHeight();

        Picasso.with(this)
                .load(R.drawable.mipmap_1)
                .resize(width, height)
                .into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        CityOfTwo.logoBitmap = bitmap;
                        Log.i("Bitmap", "Bitmap Loaded");
                    }

                    @Override
                    public void onBitmapFailed(Drawable errorDrawable) {

                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                    }
                });

    }

    private void updateAccessToken(AccessToken currentAccessToken) {

        if (currentAccessToken == null) {
            mLoginManager.logInWithReadPermissions(
                    this,
                    permissionList
            );
            Log.i("Facebook Login", "No user found");
        } else if (currentAccessToken.isExpired()) {
            Log.i("Facebook Login", "AccessToken expired");

            final ProgressDialog loginDialog;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                loginDialog = new ProgressDialog(this, android.R.style.Theme_Material_Light_Dialog);
            } else {
                loginDialog = new ProgressDialog(this);
            }

            loginDialog.setTitle("Logging in");
            loginDialog.setMessage("Please wait while we log you in");
            loginDialog.show();

            AccessToken.refreshCurrentAccessTokenAsync(new AccessToken.AccessTokenRefreshCallback() {
                @Override
                public void OnTokenRefreshed(AccessToken accessToken) {
                    Log.i("Facebook Login", "New AccessToken generated");
                    openLobby(accessToken);
                    loginDialog.cancel();
                }

                @Override
                public void OnTokenRefreshFailed(FacebookException exception) {
                    mLoginManager.logOut();

                    mLoginManager.logInWithReadPermissions(
                            LoginActivity.this,
                            permissionList
                    );
                    Log.i("Facebook Login", "Generating new AccessToken");
                    loginDialog.cancel();
                }
            });
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

    private void openLobby(final AccessToken accessToken, final Profile profile) {
        final long duration = 80;
//        launchLobbyActivity(accessToken, profile);
        mGetStartedButton.animate()
                .setDuration(duration)
                .alpha(0);

        findViewById(R.id.coyrudy_label).animate()
                .setDuration(duration)
                .alpha(0)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        launchLobbyActivity(accessToken, profile);
                    }
                });
    }

    private void launchLobbyActivity(AccessToken accessToken, Profile profile) {
        Intent i = new Intent(this, LobbyActivity.class);

        mLogoImage = (ImageView) findViewById(R.id.coyrudy_logo);

        int[] location = new int[2];
        mLogoImage.getLocationOnScreen(location);

        i.putExtra(CityOfTwo.KEY_ACCESS_TOKEN, accessToken);
        i.putExtra(CityOfTwo.KEY_PROFILE, profile);
        i.putExtra(CityOfTwo.KEY_LOCATION_X, location[0]);
        i.putExtra(CityOfTwo.KEY_LOCATION_Y, location[1]);
        i.putExtra(CityOfTwo.KEY_WIDTH, mLogoImage.getWidth());
        i.putExtra(CityOfTwo.KEY_HEIGHT, mLogoImage.getHeight());


        Log.i("Lobby Actiity", "Opening Lobby Activity");
        startActivityForResult(i, CityOfTwo.ACTIVITY_LOBBY);
        overridePendingTransition(0, 0);
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

        CityOfTwo.setCurrentActivity(CityOfTwo.ACTIVITY_LOGIN);
        CityOfTwo.setApplicationState(CityOfTwo.APPLICATION_FOREGROUND);
    }

    @Override
    protected void onPause() {
        super.onPause();

        CityOfTwo.setApplicationState(CityOfTwo.APPLICATION_BACKGROUND);
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
//                        finish();
                        recreate();
                        break;
                }
                break;
            default:
                break;
        }
    }
}
