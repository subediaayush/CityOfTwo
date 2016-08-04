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
import android.view.View;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.github.paolorotolo.appintro.AppIntro2Fragment;
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

        addFragment(IntroductionFragment.newInstance());

        facebookCallbackManager = CallbackManager.Factory.create();
        facebookLoginManager = LoginManager.getInstance();

        FRAGMENT_LOGIN_FACEBOOK = getTotalItems();
        addFragment(AppIntro2Fragment.newInstance(
                "Facebook Login",
                "Login into facebook to use CoyRudy",
                R.drawable.mipmap_1,
                ContextCompat.getColor(this, android.R.color.transparent)
        ));

        FRAGMENT_TEST_BEGIN = getTotalItems();
        addFragment(AppIntro2Fragment.newInstance(
                "Test",
                "We will take a test and blah blah blah",
                R.drawable.mipmap_1,
                ContextCompat.getColor(this, android.R.color.transparent)
        ));

        FRAGMENT_TEST = getTotalItems();
        testFragment = TestFragment.newInstance();
        addFragment(testFragment);

        addFragment(AppIntro2Fragment.newInstance(
                "Good",
                "Now continue to app mutha fucka",
                R.drawable.mipmap_1,
                ContextCompat.getColor(this, android.R.color.transparent)
        ));

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
    protected void nextButtonClicked(View v, int position) {
        Log.i("Introduction", "Next button clicked");
        if (position == FRAGMENT_LOGIN_FACEBOOK) {
            facebookLoginManager.logInWithReadPermissions(
                    this,
                    CityOfTwo.FACEBOOK_PERMISSION_LIST
            );
            v.setEnabled(false);
        } else if (position == FRAGMENT_TEST_BEGIN) {
            showNextPage();
            v.setEnabled(false);
        } else if (position == FRAGMENT_TEST) {
            testFragment.nextQuestion();
            v.setEnabled(false);
        } else {
            showNextPage();
        }
    }

    @Override
    protected void doneButtonClicked(View v) {
        Log.i("Introduction", "Done button clicked");

        Intent lobbyIntent = new Intent(this, LobbyActivity.class);

        lobbyIntent.putExtra(CityOfTwo.KEY_ACCESS_TOKEN, AccessToken.getCurrentAccessToken());
        lobbyIntent.putExtra(CityOfTwo.KEY_FROM_INTRO, true);
        startActivityForResult(lobbyIntent, CityOfTwo.ACTIVITY_LOBBY);
    }

    @Override
    protected void pageChanged(int position) {
        Log.i("Introduction", "Page Changed");
        if (position == FRAGMENT_TEST) testFragment.showItem(0);
    }

    private void showLoginSuccess(final AccessToken accessToken) {
        final ProgressDialog p;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            p = new ProgressDialog(this, android.R.style.Theme_Material_Light_Dialog);
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
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage("Could login to your Facebook")
                .setPositiveButton("Try again", clickListener)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getNextButton().setEnabled(true);
                    }
                })
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        facebookCallbackManager.onActivityResult(requestCode, resultCode, data);
    }


}