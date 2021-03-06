package com.messenger.cityoftwo;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Button;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by Aayush on 4/19/2016.
 */
public class IntroductionActivity extends IntroductionActivityBase {
    private int FRAGMENT_TEST_BEGIN = -1;
    private int FRAGMENT_LOGIN_FACEBOOK = -1;
    private int FRAGMENT_TEST = -1;

    private LoginManager facebookLoginManager;
    private CallbackManager facebookCallbackManager;

    private TestFragment testFragment;
    private String testAnswers = "";

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        Picasso.with(this)
                .load(R.drawable.logo_bitmap)
                .into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        CityOfTwo.logoBitmap = bitmap;
                    }

                    @Override
                    public void onBitmapFailed(Drawable errorDrawable) {

                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                    }
                });

//        addFragment(IntroductionFragment.newInstance());

        addFragment(IntroFragment.newInstance(
                R.layout.layout_intro_coyrudy,
                "Welcome to CoyRudy"
        ));

        addFragment(IntroFragment.newInstance(
                "CoyRudy",
                "Chat anonymously with like minded strangers",
                R.drawable.demo_convo,
                ContextCompat.getColor(this, android.R.color.transparent),
                ContextCompat.getColor(this, R.color.colorPrimary),
                ContextCompat.getColor(this, R.color.colorPrimary)
        ));

        addFragment(IntroFragment.newInstance(
                "CoyRudy",
                "You can also reveal yourself or filter future matches",
                R.drawable.demo_reveal,
                ContextCompat.getColor(this, android.R.color.transparent),
                ContextCompat.getColor(this, R.color.colorPrimary),
                ContextCompat.getColor(this, R.color.colorPrimary)
        ));

        facebookCallbackManager = CallbackManager.Factory.create();
        facebookLoginManager = LoginManager.getInstance();

        FRAGMENT_LOGIN_FACEBOOK = getTotalItems();
        addFragment(IntroFragment.newInstance(
                "Sign up",
                "We use Facebook likes to match two strangers with similar intersts.",
                R.drawable.demo_likes,
                ContextCompat.getColor(this, android.R.color.transparent),
                ContextCompat.getColor(this, R.color.colorPrimary),
                ContextCompat.getColor(this, R.color.colorPrimary)
        ));

        FRAGMENT_TEST_BEGIN = getTotalItems();
        addFragment(IntroFragment.newInstance(
                "Almost There",
                "Just a 3-question personality test and we're done!",
                R.drawable.demo_test,
                ContextCompat.getColor(this, android.R.color.transparent),
                ContextCompat.getColor(this, R.color.colorPrimary),
                ContextCompat.getColor(this, R.color.colorPrimary)
        ));

        FRAGMENT_TEST = getTotalItems();
        testFragment = TestFragment.newInstance();
        addFragment(testFragment);

        addFragment(IntroFragment.newInstance(
                R.layout.layout_intro_coyrudy,
                "You're all set. Enjoy!"
        ));

//        addFragment(IntroFragment.newInstance(
//                "Enjoy",
//                "Sign Up successful! You can now start chating.",
//                R.drawable.logo_bitmap,
//                ContextCompat.getColor(this, android.R.color.transparent),
//                ContextCompat.getColor(this, R.color.colorPrimary),
//                ContextCompat.getColor(this, R.color.colorPrimary)
//        ));

        facebookLoginManager.registerCallback(facebookCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                AccessToken accessToken = loginResult.getAccessToken();
                Log.i("Facebook Access Token", accessToken.getToken());

                showLoginSuccess(accessToken);
            }

            @Override
            public void onCancel() {
                Log.i("Facebook Login", "Login Cancelled");
                showLoginErrorDialog(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        facebookLoginManager.logInWithReadPermissions(
                                IntroductionActivity.this,
                                CityOfTwo.FACEBOOK_PERMISSION_LIST
                        );
                    }
                });
            }

            @Override
            public void onError(FacebookException error) {
                Log.e("Facebook Login", "Login Error", error);
                showLoginErrorDialog(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        facebookLoginManager.logInWithReadPermissions(
                                IntroductionActivity.this,
                                CityOfTwo.FACEBOOK_PERMISSION_LIST
                        );
                    }
                });
            }
        });
        testFragment.setTestEventListener(new TestFragment.TestEventListener() {
            @Override
            public void OnQuestionAnswered(int question, int answer) {
                testAnswers += String.valueOf(answer);
                getNextButton().setEnabled(true);
            }

            @Override
            public void OnAllQuestionsAnswered() {
                getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE).edit()
                        .putString(CityOfTwo.KEY_TEST_RESULT, testAnswers).apply();

                showNextPage();
            }
        });
    }

    @Override
    protected void nextButtonClicked(Button button, int position) {
        Log.i("Introduction", "Next button clicked");
        if (position == FRAGMENT_LOGIN_FACEBOOK) {
            facebookLoginManager.logInWithReadPermissions(
                    this,
                    CityOfTwo.FACEBOOK_PERMISSION_LIST
            );
            button.setEnabled(false);
        } else if (position == FRAGMENT_TEST_BEGIN) {
            showNextPage();
            button.setEnabled(false);
        } else if (position == FRAGMENT_TEST) {
            testFragment.nextQuestion();
            button.setEnabled(false);
        } else {
            showNextPage();
        }
    }

    @Override
    protected void doneButtonClicked(Button button) {
        Log.i("Introduction", "Done button clicked");

        Intent lobbyIntent = new Intent(this, LobbyActivity.class);

        lobbyIntent.putExtra(CityOfTwo.KEY_ACCESS_TOKEN, AccessToken.getCurrentAccessToken());
        lobbyIntent.putExtra(CityOfTwo.KEY_FROM_INTRO, true);
        startActivityForResult(lobbyIntent, CityOfTwo.ACTIVITY_LOBBY);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    @Override
    protected void pageChanged(int position) {
        Button nextButton = getNextButton();

        Log.i("Introduction", "Page Changed");
        if (position == FRAGMENT_TEST) testFragment.showItem(0);

        if (position == FRAGMENT_TEST_BEGIN) nextButton.setText("begin");

        else if (position == FRAGMENT_LOGIN_FACEBOOK) nextButton.setText("sign up");

        else nextButton.setText("next");
    }

    private void showLoginSuccess(final AccessToken accessToken) {
        final ProgressDialog p;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            p = new ProgressDialog(this, R.style.AppTheme_Dialog);
        } else {
            p = new ProgressDialog(this);
        }

        p.setTitle("Please wait");
        p.setMessage("Setting up your account");
        p.setCancelable(false);
        p.show();

//        final FacebookLogin facebookLogin = new FacebookLogin(this, accessToken) {
//            @Override
//            void onSuccess(String response) {
//                int currentChild = introViewFlipper.getDisplayedChild();
//                introViewFlipper.setDisplayedChild(currentChild + 1);
//            }
//
//            @Override
//            void onFailure(Integer status) {
//                final FacebookLogin var = this;
//                showLoginErrorDialog(new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        facebookSignUp.execute();
//                    }
//                });
//            }
//        };

        final FacebookSignUp facebookSignUp = new FacebookSignUp(this, accessToken) {
            @Override
            void onSuccess(String response) {

                JSONObject Response;
                try {
                    Response = new JSONObject(response);
                    Boolean registered = Response.getBoolean("parsadi");

                    if (!registered) {
                        JSONArray test = Response.getJSONArray("test");
                        setupTest(test);
                        showNextPage();
                        getNextButton().setEnabled(true);
                        p.cancel();
                    }
                } catch (JSONException e) {
                    onFailure(-1);
                }
            }

            @Override
            void onFailure(Integer status) {
                final FacebookSignUp var = this;
                showLoginErrorDialog(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        var.execute();
                    }
                });
            }
        };

        facebookSignUp.execute();

    }

    private void setupTest(JSONArray questions) {
        CityOfTwo.answerBitmapList = new HashMap<>();
        try {
            for (int i = 0; i < questions.length(); i++) {
                JSONObject question = questions.getJSONObject(i);

                String q = question.keys().next();
                JSONArray answers = question.getJSONArray(q);

                for (int j = 0; j < answers.length(); j++) {
                    String url = (String) ((JSONArray) answers.get(j)).get(1);

                    final String key = String.valueOf(i) + String.valueOf(j);
                    Picasso.with(this)
                            .load(url)
                            .into(new Target() {
                                @Override
                                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                    CityOfTwo.answerBitmapList.put(key, bitmap);
                                }

                                @Override
                                public void onBitmapFailed(Drawable errorDrawable) {
                                }

                                @Override
                                public void onPrepareLoad(Drawable placeHolderDrawable) {
                                }
                            });
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Bundle test = new Bundle();
        test.putString(CityOfTwo.KEY_TEST, questions.toString());

        testFragment.setArguments(test);
    }

    private void showLoginErrorDialog(DialogInterface.OnClickListener clickListener) {
        new AlertDialog.Builder(this, R.style.AppTheme_Dialog)
                .setTitle("Error")
                .setMessage("Could login to your Facebook")
                .setPositiveButton("Try again", clickListener)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getNextButton().setEnabled(true);
                    }
                })
                .setCancelable(false)
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        facebookCallbackManager.onActivityResult(requestCode, resultCode, data);
    }


}