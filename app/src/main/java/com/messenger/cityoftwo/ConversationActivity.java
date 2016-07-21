package com.messenger.cityoftwo;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.LabeledIntent;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.Profile;
import com.facebook.login.widget.ProfilePictureView;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.messenger.cityoftwo.CityOfTwo.KEY_CHATROOM_ID;
import static com.messenger.cityoftwo.CityOfTwo.KEY_CHAT_PENDING;
import static com.messenger.cityoftwo.CityOfTwo.KEY_CODE;
import static com.messenger.cityoftwo.CityOfTwo.KEY_COMMON_LIKES;
import static com.messenger.cityoftwo.CityOfTwo.KEY_DISTANCE;
import static com.messenger.cityoftwo.CityOfTwo.KEY_MATCH_FEMALE;
import static com.messenger.cityoftwo.CityOfTwo.KEY_MATCH_MALE;
import static com.messenger.cityoftwo.CityOfTwo.KEY_MAX_AGE;
import static com.messenger.cityoftwo.CityOfTwo.KEY_MIN_AGE;
import static com.messenger.cityoftwo.CityOfTwo.KEY_USER_OFFLINE;

public class ConversationActivity extends AppCompatActivity {
    public static final String HOST = "http://192.168.100.1:5000";
    List<Conversation> mConversationList;
    ConversationAdapter mConversationAdapter;
    EditText mInputText;
    ImageButton mSendButton;
    RecyclerView mConversationListView;
    View mConnectionIndicatorView;
    ViewGroup mChatOptionsContainer;
    Toolbar mToolbar;
    HttpHandler mHttpHandler;
    BroadcastReceiver mBroadcastReceiver;
    private int mConversationID;
    private boolean isOptionsVisible = false;
    private ImageView mLogoShadow;
    private ImageView mLogoImage;
    private View mOptionsDismissButton;

    private int mOptionsViewHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        Log.i("ConversationActivity", "Activity Created");

        mInputText = (EditText) findViewById(R.id.input_text);
        mSendButton = (ImageButton) findViewById(R.id.send_button);
        mConversationListView = (RecyclerView) findViewById(R.id.conversation_listview);
        mChatOptionsContainer = (ViewGroup) findViewById(R.id.chat_options_container);

        LabelledButtonLayout newChatButton = (LabelledButtonLayout) findViewById(R.id.new_chat),
                revealButton = (LabelledButtonLayout) findViewById(R.id.share_facebook),
                filtersButton = (LabelledButtonLayout) findViewById(R.id.apply_filter),
                referralButton = (LabelledButtonLayout) findViewById(R.id.refer_button);

        mLogoImage = (ImageView) findViewById(R.id.coyrudy_logo);

        if (CityOfTwo.logoBitmap != null) mLogoImage.setImageBitmap(CityOfTwo.logoBitmap);
        else Picasso.with(this).load(R.drawable.mipmap_1).into(mLogoImage);


//        mConnectionIndicatorView = findViewById(R.id.network_indicator);

//        setSupportActionBar(mToolbar);

//        getSupportActionBar().setTitle("Conversation");

        mConversationList = new ArrayList<>();
        mConversationAdapter = new ConversationAdapter(this, mConversationList);

        ChatItemAnimator itemAnimator = new ChatItemAnimator(0.1f, 200);

        mConversationListView.setItemAnimator(itemAnimator);

        mConversationListView.setAdapter(mConversationAdapter);

        final LinearLayoutManager l = new LinearLayoutManager(this);
        l.setStackFromEnd(true);
        mConversationListView.setLayoutManager(l);

        mConversationList.add(new Conversation("CHAT_BEGIN", CityOfTwo.FLAG_START));
        mConversationList.add(new Conversation("CHAT_END", CityOfTwo.FLAG_END));

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String bufferText = mInputText.getText().toString().replaceFirst("\\s+$", "");

                if (isOptionsVisible)
                    hideOptions();

                if (!bufferText.isEmpty()) {
                    mInputText.setText("");
                    Conversation conversation = new Conversation(
                            bufferText,
                            CityOfTwo.FLAG_SENT | CityOfTwo.FLAG_TEXT
                    );
                    sendMessage(conversation);
                }
            }
        });

        mLogoImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.i("Logo Clicked", "true");
                if (isOptionsVisible)
                    hideOptions();
                else
                    showOptions();
            }
        });

        mOptionsDismissButton = findViewById(R.id.option_dismiss_button);
        mOptionsDismissButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isOptionsVisible) hideOptions();
            }
        });

        mInputText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus && isOptionsVisible) hideOptions();
            }
        });
//        mLogoImage.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                switch (event.getAction()) {
//                    case MotionEvent.ACTION_DOWN: {
//                        int pivotX = mLogoImage.getWidth() / 2,
//                                pivotY = mLogoImage.getHeight() / 2;
//
//                        mLogoImage.setPivotX(pivotX);
//                        mLogoImage.setPivotY(pivotY);
//
//                        mLogoImage.animate().setDuration(100)
//                                .scaleX(.9f)
//                                .scaleY(.9f);
//
//                        Log.i("ImageFlipperTouch", "Event: DOWN");
//                        return true;
//                    }
//                    case MotionEvent.ACTION_UP: {
//                        int pivotX = mLogoImage.getWidth() / 2,
//                                pivotY = mLogoImage.getHeight() / 2;
//
//                        mLogoImage.setPivotX(pivotX);
//                        mLogoImage.setPivotY(pivotY);
//
//                        mLogoImage.animate().setDuration(100)
//                                .scaleX(1)
//                                .scaleY(1);
//
//                        Log.i("ImageFlipperTouch", "Event: UP");
//                        return true;
//                    }
//                    default:
//                        return false;
//                }
//            }
//        });

        mChatOptionsContainer.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(final View v, boolean hasFocus) {
                Log.i("Options Focus", String.valueOf(hasFocus));
                if (!hasFocus) {
                    hideOptions();
                }
            }
        });

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle data = intent.getExtras();

                String action = intent.getAction();

                Log.i("ConversationReceiver", "Signal Received: " + action);

                switch (action) {
                    case CityOfTwo.ACTION_NEW_MESSAGE: {

                        String text = data.getString(CityOfTwo.KEY_TEXT);
                        Integer flags = data.getInt(CityOfTwo.KEY_MESSAGE_FLAGS);
                        Date time = new Date(data.getLong(CityOfTwo.KEY_TIME, 0));

                        Conversation c = new Conversation(text, flags, time);
                        c.removeFlag(CityOfTwo.FLAG_SENT);
                        c.addFlag(CityOfTwo.FLAG_RECEIVED);

                        int insertPosition = mConversationList.size() - 1;

                        mConversationList.add(insertPosition, c);

                        mConversationAdapter.notifyItemInserted(insertPosition);
                        if (!mConversationAdapter.isLastVisible())
                            mConversationListView.smoothScrollToPosition(mConversationList.size());
                        break;
                    }
                    case CityOfTwo.ACTION_END_CHAT: {
                        mInputText.getText().clear();
                        mInputText.setEnabled(false);

                        new AlertDialog.Builder(ConversationActivity.this)
                                .setTitle("Stranger has left the chat")
                                .setMessage("Start a new chat")
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        startNewChat();
                                    }
                                })
                                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        exitActivity(CityOfTwo.RESULT_EXIT_APP);
                                    }
                                });
                        break;
                    }
                    case CityOfTwo.ACTION_BEGIN_CHAT: {
                        mInputText.getText().clear();
                        mInputText.setEnabled(true);

                        SharedPreferences sp = getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE);
                        String commonLikes = sp.getString(KEY_COMMON_LIKES, "");

                        sp.edit().putBoolean(CityOfTwo.KEY_CHAT_PENDING, false).apply();

                        if (mConversationAdapter.isWaiting())
                            mConversationAdapter.hideWaitingDialog();

                        setupNewChatWindow(commonLikes);

                        mConversationAdapter.notifyDataSetChanged();
                        mConversationListView.smoothScrollToPosition(mConversationList.size());
                        break;
                    }
                    case CityOfTwo.ACTION_USER_OFFLINE: {
                        getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE).edit()
                                .remove(CityOfTwo.KEY_CHAT_PENDING)
                                .remove(CityOfTwo.KEY_CHATROOM_ID)
                                .apply();

                        exitActivity(RESULT_CANCELED);
                    }
                }
            }
        };

        mChatOptionsContainer.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                mChatOptionsContainer.getViewTreeObserver().removeOnPreDrawListener(this);
                hideOptions();
                return true;
            }
        });

        restoreActivity(savedInstanceState);

        sendBeginChatSignal();
    }

    private void exitActivity(int resultExitApp) {
        setResult(resultExitApp);
        finish();
    }

    private void setupNewChatWindow(String headerText) {
        mConversationList.clear();
        mConversationList.add(new Conversation("CHAT_BEGIN", CityOfTwo.FLAG_START));
        mConversationList.add(new Conversation("CHAT_END", CityOfTwo.FLAG_END));

        String likeMessage = "";
        String[] likes = headerText.split("\\, ");
        int totalLikes = likes.length;
        if (totalLikes > 0) {
            StringBuilder likeMessageBuilder = new StringBuilder();
            for (int i = 0; i < totalLikes; i++) {
                likeMessageBuilder.append(likes[i]);
                if (i + 2 < totalLikes)
                    likeMessageBuilder.append(", ");
                else if (i + 2 == totalLikes)
                    likeMessageBuilder.append(" and ");
                else
                    likeMessageBuilder.append(".");
            }
            likeMessage = likeMessageBuilder.toString();
        }

        mConversationAdapter.setHeaderText(likeMessage);
        mConversationAdapter.notifyDataSetChanged();
    }

    public void newChat(View view) {
        hideOptions();
        new AlertDialog.Builder(this)
                .setTitle("Leave conversation")
                .setMessage("Do you want to leave this conversation?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startNewChat();
                    }

                })
                .setNegativeButton("No", null)
                .create().show();
    }

    private void startNewChat() {
        endCurrentChat();

        if (!mConversationAdapter.isWaiting())
            mConversationAdapter.showWaitingDialog();
    }

    public void revealProfile(View view) {
        hideOptions();

        final SharedPreferences sp = getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE);

        Boolean showDialog = sp.getBoolean(CityOfTwo.KEY_SHOW_REVEAL_DIALOG, true);

        if (!showDialog) {
            revealProfile();
            return;
        }

        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setTitle("Reveal Profile");

        View profileView = getLayoutInflater().inflate(R.layout.layout_reveal_profile, null);
        ProfilePictureView profileImageView = (ProfilePictureView) profileView.findViewById(R.id.message_profile_image);
        TextView profileTextView = (TextView) profileView.findViewById(R.id.message_profile_name);
        CheckBox profileCheckBox = (CheckBox) profileView.findViewById(R.id.donot_show_dialog);

        profileCheckBox.setChecked(!showDialog);

        profileCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sp.edit().putBoolean(CityOfTwo.KEY_SHOW_REVEAL_DIALOG, !isChecked)
                        .apply();
            }
        });

        Profile userProfile = Profile.getCurrentProfile();

        profileImageView.setProfileId(userProfile.getId());

        profileTextView.setText(userProfile.getName());
        profileTextView.setTextColor(ContextCompat.getColor(this, R.color.Black));
        adb.setView(profileView);
        adb.setPositiveButton("Share", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                revealProfile();
            }
        });
        adb.show();

    }

    private void revealProfile() {
        JSONObject revealProfile = new JSONObject();
        try {
            Profile currentProfile = Profile.getCurrentProfile();
            revealProfile.put(CityOfTwo.KEY_PROFILE_NAME, currentProfile.getName());
            revealProfile.put(CityOfTwo.KEY_PROFILE_ID, currentProfile.getId());

            Conversation conversation = new Conversation(revealProfile.toString());
            conversation.addFlag(CityOfTwo.FLAG_SENT);
            conversation.addFlag(CityOfTwo.FLAG_REVEAL);

            sendMessage(conversation);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void applyFilter(View view) {
        hideOptions();

        FiltersFragment filtersFragment = FiltersFragment.newInstance();
        filtersFragment.setOnDialogEventListener(new FiltersFragment.OnDialogEventListener() {
            @Override
            public void OnFiltersApply(JSONObject filters) {
                sendFilters(filters);
            }

            @Override
            public void OnCoyRudyShared() {
                referCoyRudy(null);
            }
        });
        filtersFragment.show(getSupportFragmentManager(), "Filter");


//        AlertDialog.Builder adb = new AlertDialog.Builder(this);
//        adb.setTitle("Filters");
//        adb.setView(R.layout.layout_apply_filter);
//        adb.show();

    }

    public void sendFilters(final JSONObject filters) {
        final SharedPreferences sharedPreferences = getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE);

        String send_message = getString(R.string.url_send_filter);

        String[] Path = {CityOfTwo.API, send_message};

        HttpHandler filterHttpHandler = new HttpHandler(CityOfTwo.HOST, Path, HttpHandler.POST, filters) {

            @Override
            protected void onSuccess(String response) {
                try {
                    JSONObject Response = new JSONObject(response);

                    Boolean status = Response.getBoolean("parsadi");

                    if (!status) onFailure(getResponseStatus());
                    else {
                        Integer minimumAge = filters.getInt(KEY_MIN_AGE),
                                maximumAge = filters.getInt(KEY_MAX_AGE),
                                maximumDistance = filters.getInt(KEY_DISTANCE);
                        Boolean matchFemale = filters.getBoolean(KEY_MATCH_FEMALE),
                                matchMale = filters.getBoolean(KEY_MATCH_MALE);

                        sharedPreferences.edit()
                                .putInt(CityOfTwo.KEY_MIN_AGE, minimumAge)
                                .putInt(CityOfTwo.KEY_MAX_AGE, maximumAge)
                                .putInt(CityOfTwo.KEY_DISTANCE, maximumDistance)
                                .putBoolean(CityOfTwo.KEY_MATCH_MALE, matchMale)
                                .putBoolean(CityOfTwo.KEY_MATCH_FEMALE, matchFemale)
                                .putBoolean(CityOfTwo.KEY_FILTERS_APPLIED, true)
                                .apply();
                    }
                } catch (Exception e) {
                    onFailure(getResponseStatus());
                    e.printStackTrace();
                }
            }

            @Override
            protected void onFailure(Integer status) {
                final Snackbar s = Snackbar.make(findViewById(R.id.snackbar_container),
                        "Could not apply filters",
                        Snackbar.LENGTH_SHORT
                );

                s.setAction("Try Again", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sendFilters(filters);
                    }
                });

                View view = s.getView();
                view.setBackgroundColor(ContextCompat.getColor(
                        ConversationActivity.this,
                        R.color.colorSnackBarError
                ));

                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        s.dismiss();
                    }
                });

                s.setActionTextColor(ContextCompat.getColor(
                        ConversationActivity.this,
                        R.color.colorSnackBarText
                ));

//                View view = s.getView();
//                FrameLayout.LayoutParams params =(FrameLayout.LayoutParams)view.getLayoutParams();
//                params.gravity = Gravity.TOP;
//                view.setLayoutParams(params);
//
                s.show();

            }
        };

        String token = "Token " + sharedPreferences.getString(CityOfTwo.KEY_SESSION_TOKEN, "");

        filterHttpHandler.addHeader("Authorization", token);
        filterHttpHandler.execute();

    }

    public void referCoyRudy(View view) {
        hideOptions();

        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setTitle("Refer CoyRudy");

        View referCoyRudyView = LayoutInflater.from(this)
                .inflate(R.layout.layout_share_coyrudy, null);
        TextView linkTextView = (TextView) referCoyRudyView.findViewById(R.id.coyrudy_link);

        String uniqueCode = new SecurePreferences(this, CityOfTwo.SECURED_PREFERENCE)
                .getString(KEY_CODE, "");

        final String shareText = getString(R.string.url_share_coyrudy) + uniqueCode;

        linkTextView.setText(shareText);

        adb.setView(referCoyRudyView);
        adb.setPositiveButton("Share", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onShareCoyRudy(shareText);
            }
        });
        adb.show();

    }

    private void onShareCoyRudy(String uniqueUrl) {

        List<String> targetedApps = new ArrayList<>();
        targetedApps.add("com.facebook.katana");
        targetedApps.add("com.facebook.orca");
        targetedApps.add("com.twitter.android");
        targetedApps.add("com.google.android.apps.plus");

        List<LabeledIntent> shareIntentList = new ArrayList<>();
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");

        List<ResolveInfo> resolveInfoList = getPackageManager().queryIntentActivities(shareIntent, 0);

        if (!resolveInfoList.isEmpty()) {
            for (ResolveInfo resolveInfo : resolveInfoList) {
                String packageName = resolveInfo.activityInfo.packageName;
                if (targetedApps.contains(packageName)) {
                    Intent targetedShareIntent = new Intent(Intent.ACTION_SEND);
                    targetedShareIntent.setComponent(new ComponentName(packageName, resolveInfo.activityInfo.name));
                    targetedShareIntent.setType("text/plain");
                    targetedShareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "CoyRudy!");
                    targetedShareIntent.putExtra(android.content.Intent.EXTRA_TEXT, uniqueUrl);

                    shareIntentList.add(new LabeledIntent(
                            targetedShareIntent,
                            packageName,
                            resolveInfo.loadLabel(getPackageManager()),
                            resolveInfo.icon
                    ));
                }
            }
        }
        if (shareIntentList.isEmpty()) {
            final Snackbar s = Snackbar.make(
                    mConversationListView,
                    "Link copied to clipboard",
                    Snackbar.LENGTH_SHORT
            );


            View view = s.getView();
            view.setBackgroundColor(ContextCompat.getColor(
                    ConversationActivity.this,
                    R.color.colorSnackBarDefault
            ));

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    s.dismiss();
                }
            });

            s.setActionTextColor(ContextCompat.getColor(
                    ConversationActivity.this,
                    R.color.colorSnackBarText
            ));

            s.show();
            return;
        }
        // convert shareIntentList to array
        LabeledIntent[] extraIntents = shareIntentList.toArray(new LabeledIntent[shareIntentList.size()]);

        Intent chooserIntent = Intent.createChooser(new Intent(), "Select app to share");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, extraIntents);
        startActivity(chooserIntent);
    }

    private void showOptions() {
        mOptionsDismissButton.setVisibility(View.VISIBLE);
        final int viewHeight = mChatOptionsContainer.getHeight();
//
//
        mChatOptionsContainer.animate().setInterpolator(new DecelerateInterpolator()).setDuration(100)
                .translationY(0)
                .withStartAction(new Runnable() {
                    @Override
                    public void run() {
                        if (mChatOptionsContainer.getTranslationY() >= 0)
                            mChatOptionsContainer.setTranslationY(-viewHeight);
                        mChatOptionsContainer.setVisibility(View.VISIBLE);
                        Log.i("Show Options", "Initial View Height: " + viewHeight);
                        Log.i("Show Options", "Initial View Translation: " + mChatOptionsContainer.getTranslationY());
                    }
                })
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        isOptionsVisible = true;

                        Log.i("Show Options", "Final View Height: " + viewHeight);
                        Log.i("Show Options", "Final View Translation: " + mChatOptionsContainer.getTranslationY());

                    }
                });
    }

    private void hideOptions() {
        mOptionsDismissButton.setVisibility(View.GONE);
        final int viewHeight = mChatOptionsContainer.getHeight();

        mChatOptionsContainer.animate().setInterpolator(new AccelerateInterpolator()).setDuration(100)
                .translationY(-viewHeight)
                .withStartAction(new Runnable() {
                    @Override
                    public void run() {
                        Log.i("Remove Options", "Initial View Height: " + viewHeight);
                        Log.i("Remove Options", "Initial View Translation: " + mChatOptionsContainer.getTranslationY());
                    }
                })
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        isOptionsVisible = false;
                        mChatOptionsContainer.setVisibility(View.INVISIBLE);

                        Log.i("Remove Options", "Final View Height: " + viewHeight);
                        Log.i("Remove Options", "Final View Translation: " + mChatOptionsContainer.getTranslationY());
                    }
                });
    }


    private void sendBeginChatSignal() {

    }

    private void exitActivity(int resultCode, Intent intent) {
        setResult(resultCode, intent);
        finish();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        String headerText = mConversationAdapter.getHeaderText();

        if (!mConversationList.isEmpty()) {
            ArrayList<String> textList = new ArrayList<>();
            for (Conversation c : mConversationList) {
                textList.add(c.toString());
            }
            outState.putStringArrayList(CityOfTwo.KEY_CURRENT_CHAT, textList);
            outState.putString(CityOfTwo.KEY_CHAT_HEADER, headerText);
        }

        Log.i("Conversation Activity", "Instance Saved");

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        restoreActivity(savedInstanceState);
        Log.i("Conversation Activity", "Instance Restored");
    }

    protected void restoreActivity(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            ArrayList<String> textList = savedInstanceState.getStringArrayList(CityOfTwo.KEY_CURRENT_CHAT);
            String headerText = savedInstanceState.getString(CityOfTwo.KEY_CHAT_HEADER);

            if (textList == null) textList = new ArrayList<>();

            for (String text : textList)
                mConversationList.add(new Conversation(text));

            mConversationAdapter.setHeaderText(headerText);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        Log.i("ConversationActivity", "Activity Paused");
        CityOfTwo.setApplicationState(CityOfTwo.APPLICATION_BACKGROUND);

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.i("ConversationActivity", "Activity Resumed");
        CityOfTwo.setApplicationState(CityOfTwo.APPLICATION_FOREGROUND);
        CityOfTwo.setCurrentActivity(CityOfTwo.ACTIVITY_CONVERSATION);

        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.cancel(CityOfTwo.NOTIFICATION_NEW_MESSAGE, 0);

        IntentFilter filter = new IntentFilter();

        filter.addAction(CityOfTwo.ACTION_NEW_MESSAGE);
        filter.addAction(CityOfTwo.ACTION_BEGIN_CHAT);
        filter.addAction(CityOfTwo.ACTION_END_CHAT);
        LocalBroadcastManager.getInstance(this).registerReceiver((mBroadcastReceiver), filter);

        if (CityOfTwo.pendingMessages != null) CityOfTwo.pendingMessages.clear();
        if (CityOfTwo.messageCounter != null) CityOfTwo.pendingMessages.clear();


        SharedPreferences sp = getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE);

        Boolean userOffline = sp.getBoolean(KEY_USER_OFFLINE, false);
        if (userOffline) {
            sp.edit().remove(KEY_CHAT_PENDING)
                    .remove(KEY_CHATROOM_ID)
                    .apply();

            new AlertDialog.Builder(this)
                    .setTitle("Timeout")
                    .setMessage("Login again")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            exitActivity(RESULT_OK);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            exitActivity(CityOfTwo.RESULT_EXIT_APP);
                        }
                    }).show();
            return;
        }

        String commonLikes = sp.getString(KEY_COMMON_LIKES, "");
        Boolean chatPending = sp.getBoolean(CityOfTwo.KEY_CHAT_PENDING, false);
        Integer chatRoomId = sp.getInt(CityOfTwo.KEY_CHATROOM_ID, -1);

        if (chatRoomId == -1) {
            new AlertDialog.Builder(this)
                    .setTitle("Stranger has left the chat")
                    .setMessage("Start a new chat")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startNewChat();
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            setResult(CityOfTwo.RESULT_EXIT_APP);
                            finish();
                        }
                    });
        } else {
            DatabaseHelper db = new DatabaseHelper(this);
            ArrayList<Conversation> pendingMessages = db.retrieveMessages(chatRoomId);
            db.clearTable(chatRoomId);

            if (chatPending) {
                sp.edit().putBoolean(CityOfTwo.KEY_CHAT_PENDING, false).apply();
                setupNewChatWindow(commonLikes);
            }
            mConversationList.addAll(mConversationList.size() - 1, pendingMessages);
            mConversationAdapter.notifyDataSetChanged();
            mConversationListView.scrollToPosition(mConversationList.size());
        }
    }

    private void sendMessage(final Conversation bufferConv) {
        final String value = bufferConv.getText().replaceFirst("\\s+$", "");
        int insertPosition = mConversationList.size() - 1;

        mConversationList.add(insertPosition, bufferConv);

        mConversationAdapter.notifyItemInserted(insertPosition);
        if (!mConversationAdapter.isLastVisible())
            mConversationListView.smoothScrollToPosition(mConversationList.size());

        JSONObject params = new JSONObject();
        try {
            params.put(CityOfTwo.HEADER_SEND_MESSAGE, value);
            params.put(CityOfTwo.HEADER_FLAGS, bufferConv.getFlags());
            params.put(CityOfTwo.HEADER_TIME, bufferConv.getTime().getTime());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String send_message = getString(R.string.url_send_message);


        String[] Path = {CityOfTwo.API, send_message};

        mHttpHandler = new HttpHandler(CityOfTwo.HOST, Path, HttpHandler.POST, params) {
            @Override
            protected void onPreRun() {
            }

            @Override
            protected void onSuccess(String response) {
                try {
                    JSONObject Response = new JSONObject(response);

                    Boolean status = Response.getBoolean("parsadi");

                    if (!status)
                        onFailure(getResponseStatus());
                    else
                        Log.i("Conversation activity", "Message sent successfully");

                } catch (Exception e) {
                    onFailure(getResponseStatus());
                    e.printStackTrace();
                }
            }

            @Override
            protected void onFailure(Integer status) {
//                    Toast.makeText(ConversationActivity.this, "Message couldnot be sent." +
//                            " Please try again later", Toast.LENGTH_SHORT).show();

                final Snackbar s = Snackbar.make(
                        mConversationListView,
                        "Your message was not sent!",
                        Snackbar.LENGTH_SHORT
                );

                s.setAction("Try Again", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Conversation newConversation = new Conversation(bufferConv.getText(), bufferConv.getFlags());
                        sendMessage(newConversation);
                    }
                });
                View view = s.getView();
                view.setBackgroundColor(ContextCompat.getColor(
                        ConversationActivity.this,
                        R.color.colorSnackBarError
                ));

                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        s.dismiss();
                    }
                });

                s.setActionTextColor(ContextCompat.getColor(
                        ConversationActivity.this,
                        R.color.colorSnackBarText
                ));

                s.show();

                if (!BuildConfig.DEBUG) {
                    mConversationList.remove(bufferConv);
                    mConversationAdapter.notifyDataSetChanged();
                }
            }
        };

        String token = "Token " + getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE)
                .getString(CityOfTwo.KEY_SESSION_TOKEN, "");

        mHttpHandler.addHeader("Authorization", token);
        mHttpHandler.execute();
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Leave conversation")
                .setMessage("Do you want to leave this conversation?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        endCurrentChat();
                        exitActivity(RESULT_CANCELED, new Intent());
                    }
                })
                .setNegativeButton("No", null)
                .create().show();
    }

    private void endCurrentChat() {
        final SharedPreferences sp = getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE);
        String dump_chat = getString(R.string.url_chat_end);

        JSONObject j = new JSONObject();
        try {
            j.put(CityOfTwo.HEADER_CHATROOM_ID, sp.getInt(KEY_CHATROOM_ID, -1));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String[] Path = {CityOfTwo.API, dump_chat};

        HttpHandler dumpHttpHandler = new HttpHandler(CityOfTwo.HOST, Path, HttpHandler.POST, j) {
            @Override
            protected void onPostExecute() {
                sp.edit()
                        .remove(CityOfTwo.KEY_CHATROOM_ID)
                        .apply();
            }
        };

        String token = "Token " + sp.getString(CityOfTwo.KEY_SESSION_TOKEN, "");
        dumpHttpHandler.addHeader("Authorization", token);

        dumpHttpHandler.execute();
    }

    public void facebookLogin(final DialogInterface.OnClickListener clickListener) {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();

        final FacebookLogin facebookLogin = new FacebookLogin(this, accessToken) {
            @Override
            void onSuccess(String response) {
                try {
                    JSONObject j = new JSONObject(response);
                    Boolean status = j.getBoolean("parsadi");
                    if (!status) onFailure(-1);
                    else getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE).edit()
                            .putBoolean(CityOfTwo.KEY_USER_OFFLINE, false).apply();
                } catch (JSONException e) {
                    onFailure(-1);
                }
            }

            @Override
            void onFailure(Integer status) {
                new AlertDialog.Builder(ConversationActivity.this)
                        .setTitle("Something went wrong")
                        .setMessage("Do you want to try again?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                facebookLogin(clickListener);
                            }
                        })
                        .setNegativeButton("No", clickListener)
                        .show();
            }
        };
        facebookLogin.execute();
    }
}
