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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.Profile;
import com.facebook.login.widget.ProfilePictureView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.messenger.cityoftwo.CityOfTwo.KEY_CHATROOM_ID;
import static com.messenger.cityoftwo.CityOfTwo.KEY_CODE;
import static com.messenger.cityoftwo.CityOfTwo.KEY_COMMON_LIKES;
import static com.messenger.cityoftwo.CityOfTwo.KEY_DISTANCE;
import static com.messenger.cityoftwo.CityOfTwo.KEY_MATCH_FEMALE;
import static com.messenger.cityoftwo.CityOfTwo.KEY_MATCH_MALE;
import static com.messenger.cityoftwo.CityOfTwo.KEY_MAX_AGE;
import static com.messenger.cityoftwo.CityOfTwo.KEY_MIN_AGE;
import static com.messenger.cityoftwo.CityOfTwo.mBackgroundConversation;

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

        mInputText = (EditText) findViewById(R.id.input_text);
        mSendButton = (ImageButton) findViewById(R.id.send_button);
        mConversationListView = (RecyclerView) findViewById(R.id.conversation_listview);
        mChatOptionsContainer = (ViewGroup) findViewById(R.id.chat_options_container);

        LabelledButtonLayout newChatButton = (LabelledButtonLayout) findViewById(R.id.new_chat),
                revealButton = (LabelledButtonLayout) findViewById(R.id.share_facebook),
                filtersButton = (LabelledButtonLayout) findViewById(R.id.apply_filter),
                referralButton = (LabelledButtonLayout) findViewById(R.id.refer_button);

        mLogoImage = (ImageView) findViewById(R.id.coyrudy_logo);

        mLogoImage.setImageBitmap(CityOfTwo.logoBitmap);


//        mConnectionIndicatorView = findViewById(R.id.network_indicator);

//        setSupportActionBar(mToolbar);

//        getSupportActionBar().setTitle("Conversation");

        mConversationList = new ArrayList<>();
        mConversationAdapter = new ConversationAdapter(this, mConversationList);

        ChatItemAnimator itemAnimator = new ChatItemAnimator(0.1f, 200);

        mConversationListView.setItemAnimator(itemAnimator);

        setupChatWindow(getIntent().getExtras());

        mConversationListView.setAdapter(mConversationAdapter);

        final LinearLayoutManager l = new LinearLayoutManager(this);
        l.setStackFromEnd(true);
        mConversationListView.setLayoutManager(l);

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String bufferText = mInputText.getText().toString().replaceFirst("\\s+$", "");

                if (isOptionsVisible)
                    hideOptions();

                if (!bufferText.isEmpty()) {
                    mInputText.setText("");
                    Conversation conversation = new Conversation(bufferText);
                    conversation.addFlag(CityOfTwo.FLAG_SENT);
                    conversation.addFlag(CityOfTwo.FLAG_TEXT);
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

                String message = data.getString(CityOfTwo.KEY_TYPE);

                Log.i("ConversationReceiver", "Signal Received: " + message);

                switch (message) {
                    case CityOfTwo.KEY_MESSAGE: {
                        String text = data.getString(CityOfTwo.KEY_TEXT);
                        Integer flags = Integer.parseInt(data.getString(CityOfTwo.KEY_MESSAGE_FLAGS));
                        Long time = Long.parseLong(data.getString(CityOfTwo.KEY_TIME, "0"));
                        Conversation c = new Conversation(text);
                        c.setFlag(flags);
                        c.removeFlag(CityOfTwo.FLAG_SENT);
                        c.addFlag(CityOfTwo.FLAG_RECEIVED);

                        mConversationList.add(mConversationList.size() - 1, c);

                        mConversationAdapter.notifyItemInserted(mConversationList.indexOf(c));
                        mConversationListView.smoothScrollToPosition(mConversationList.size());
                        break;
                    }
                    case "END_CHAT": {
                        mInputText.getText().clear();
                        mInputText.setEnabled(false);

                        Conversation c = new Conversation("", CityOfTwo.FLAG_CHAT_END);

                        mConversationList.add(mConversationList.size() - 1, c);
                        mConversationAdapter.notifyItemInserted(mConversationList.indexOf(c));
                        mConversationListView.smoothScrollToPosition(mConversationList.size());
                        break;
                    }
                    case "BEGIN_CHAT": {
                        mInputText.getText().clear();
                        mInputText.setEnabled(true);

                        mConversationList.clear();
                        getIntent().putExtras(data);

                        if (mConversationAdapter.isWaiting())
                            mConversationAdapter.hideWaitingDialog();

                        setupChatWindow(data);

                        mConversationAdapter.notifyDataSetChanged();
                        mConversationListView.smoothScrollToPosition(mConversationList.size());
                        break;
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

        sendBeginChatSignal();
    }

    private void setupChatWindow(Bundle data) {
        mConversationList.add(new Conversation("CHAT_BEGIN", CityOfTwo.FLAG_START));
        mConversationList.add(new Conversation("CHAT_END", CityOfTwo.FLAG_END));

        String likeMessage = "";
        if (data != null) {
            String[] likes = data.getString(CityOfTwo.KEY_COMMON_LIKES, "").split("\\, ");
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
        }
        mConversationAdapter.setHeaderText(likeMessage);
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

        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setTitle("Reveal Profile");

        View profileView = getLayoutInflater().inflate(R.layout.layout_reveal_profile, null);
        ProfilePictureView profileImageView = (ProfilePictureView) profileView.findViewById(R.id.message_profile_image);
        TextView profileTextView = (TextView) profileView.findViewById(R.id.message_profile_name);

        Profile userProfile = Profile.getCurrentProfile();

        profileImageView.setProfileId(userProfile.getId());

        profileTextView.setText(userProfile.getName());
        profileTextView.setTextColor(ContextCompat.getColor(this, R.color.Black));
        adb.setView(profileView);
        adb.setPositiveButton("Share", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
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
        });
        adb.show();

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

                        getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE).edit()
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

        String token = "Token " + getSharedPreferences(CityOfTwo.PACKAGE_NAME, Context.MODE_PRIVATE)
                .getString(CityOfTwo.KEY_SESSION_TOKEN, "");

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

        String uniqueCode = getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE)
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

        List<LabeledIntent> shareIntentList = new ArrayList<LabeledIntent>();
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
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState != null) {
            ArrayList<String> textList = savedInstanceState.getStringArrayList(CityOfTwo.KEY_CURRENT_CHAT);
            mConversationList.clear();
            for (String text : textList)
                mConversationList.add(new Conversation(text));
        }

        mConversationAdapter.notifyDataSetChanged();
        mConversationListView.scrollToPosition(
                mConversationList.size()
        );
    }

    @Override
    protected void onPause() {
        super.onPause();

        Log.i("ConverstionActivity", "Activity Paused");
        CityOfTwo.setApplicationState(CityOfTwo.APPLICATION_FOREGROUND);

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.i("ConverstionActivity", "Activity Resumed");
        CityOfTwo.setApplicationState(CityOfTwo.APPLICATION_FOREGROUND);
        CityOfTwo.setCurrentActivity(CityOfTwo.ACTIVITY_CONVERSATION);

        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        nm.cancel("CHAT_NOTIFICATION", 0);

        if (mBackgroundConversation == null)
            mBackgroundConversation = new ArrayList<>();

        SharedPreferences sp = getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE);

        Boolean chatPending = sp.getBoolean(CityOfTwo.KEY_CHAT_PENDING, false);

        if (chatPending) {
            mConversationList.clear();
            mBackgroundConversation.clear();

            String commonLikes = sp.getString(KEY_COMMON_LIKES, "");
            Integer chatroomId = sp.getInt(KEY_CHATROOM_ID, -1);
            Bundle data = new Bundle();
            data.putString(KEY_COMMON_LIKES, commonLikes);
            setupChatWindow(data);
            sp.edit().putBoolean(CityOfTwo.KEY_CHAT_PENDING, false).apply();
        }

        mConversationList.addAll(mConversationList.size() - 1, mBackgroundConversation);
        mConversationAdapter.notifyDataSetChanged();
        mBackgroundConversation.clear();

        IntentFilter filter = new IntentFilter();

        filter.addAction(CityOfTwo.PACKAGE_NAME);
        LocalBroadcastManager.getInstance(this).registerReceiver((mBroadcastReceiver), filter);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (!mConversationList.isEmpty()) {
            ArrayList<String> textList = new ArrayList<>();
            for (Conversation c : mConversationList) {
                textList.add(c.toString());
            }
            outState.putStringArrayList(CityOfTwo.KEY_CURRENT_CHAT, textList);
            outState.putInt(CityOfTwo.KEY_CURRENT_CHAT_ID, mConversationID);
        }


    }

    private void sendMessage(final Conversation bufferConv) {
        mConversationList.add(mConversationList.size() - 1, bufferConv);

        mConversationAdapter.notifyItemInserted(mConversationList.indexOf(bufferConv));
        mConversationListView.smoothScrollToPosition(mConversationList.size());

        final String value = bufferConv.getText().replaceFirst("\\s+$", "");

        JSONObject params = new JSONObject();
        try {
            params.put(CityOfTwo.HEADER_SEND_MESSAGE, value);
            params.put(CityOfTwo.HEADER_FLAGS, bufferConv.getFlags());
            params.put(CityOfTwo.HEADER_TIME, bufferConv.getTime().getTime());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String send_message = getString(R.string.url_send_message);

        if (!value.isEmpty()) {
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

                        if (!status) onFailure(getResponseStatus());

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
        String dump_chat = getString(R.string.url_chat_end);

        JSONObject j = new JSONObject();
        try {
            j.put(CityOfTwo.HEADER_CHATROOM_ID, getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE)
                    .getInt(KEY_CHATROOM_ID, -1));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String[] Path = {CityOfTwo.API, dump_chat};

        HttpHandler endChatHttpHandler = new HttpHandler(CityOfTwo.HOST, Path, HttpHandler.POST, j) {
            @Override
            protected void onPostExecute() {
                getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE)
                        .edit()
                        .remove(CityOfTwo.KEY_CHATROOM_ID)
                        .apply();
            }
        };

        String token = "Token " + getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE)
                .getString(CityOfTwo.KEY_SESSION_TOKEN, "");
        endChatHttpHandler.addHeader("Authorization", token);

        endChatHttpHandler.execute();
    }


    @Override
    protected void onStop() {
        super.onStop();
    }
}
