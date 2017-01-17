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
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.Fade;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
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
import com.mopub.mobileads.MoPubView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import tourguide.tourguide.ChainTourGuide;
import tourguide.tourguide.Overlay;
import tourguide.tourguide.Sequence;
import tourguide.tourguide.ToolTip;

import static com.messenger.cityoftwo.CityOfTwo.KEY_CHATROOM_ID;
import static com.messenger.cityoftwo.CityOfTwo.KEY_CODE;
import static com.messenger.cityoftwo.CityOfTwo.KEY_COMMON_LIKES;
import static com.messenger.cityoftwo.CityOfTwo.KEY_DISTANCE;
import static com.messenger.cityoftwo.CityOfTwo.KEY_IS_TYPING;
import static com.messenger.cityoftwo.CityOfTwo.KEY_LAST_SEEN;
import static com.messenger.cityoftwo.CityOfTwo.KEY_MATCH_FEMALE;
import static com.messenger.cityoftwo.CityOfTwo.KEY_MATCH_MALE;
import static com.messenger.cityoftwo.CityOfTwo.KEY_MAX_AGE;
import static com.messenger.cityoftwo.CityOfTwo.KEY_MIN_AGE;
import static com.messenger.cityoftwo.CityOfTwo.RESULT_EXIT_APP;

public class ConversationActivity extends AppCompatActivity {
    public static final String HOST = "http://192.168.100.1:5000";
    public ChainTourGuide mTourGuideHandler;
    protected boolean isOptionsVisible = false;
    protected ImageView mLogoImage;
    protected View mOptionsDismissButton;
    protected MoPubView adView;
    ConversationAdapter mConversationAdapter;
    EditText mInputText;
    ImageButton mSendButton;
    RecyclerView mConversationListView;
    LinearLayoutManager mLayoutManager;
    View mConnectionIndicatorView;
    ViewGroup mChatOptionsContainer;
    Toolbar mToolbar;
    HttpHandler mHttpHandler;
    BroadcastReceiver mBroadcastReceiver;
    private int mConversationID;
    private ImageView mLogoShadow;
    private int mOptionsViewHeight;
    private boolean firstRun;
    private LabelledButtonLayout newChatButton;
    private LabelledButtonLayout revealButton;
    private LabelledButtonLayout filtersButton;
    private LabelledButtonLayout referralButton;
    private boolean isTourPending = false;
    private String inputBuffer = "";
    
    private Handler typingSignalHandler;
    private Handler seenSignalHandler;
    
    private Runnable typingSignalRunnable;
    private Runnable seenSignalRunnable;
    private boolean sendTypingSignalPending = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setEnterTransition(new Fade());
        }
        
        Log.i("ConversationActivity", "Activity Created");
        
        mInputText = (EditText) findViewById(R.id.input_text);
        mSendButton = (ImageButton) findViewById(R.id.send_button);
        mConversationListView = (RecyclerView) findViewById(R.id.conversation_listview);
        mChatOptionsContainer = (ViewGroup) findViewById(R.id.chat_options_container);
        
        newChatButton = (LabelledButtonLayout) findViewById(R.id.new_chat);
        revealButton = (LabelledButtonLayout) findViewById(R.id.share_facebook);
        filtersButton = (LabelledButtonLayout) findViewById(R.id.apply_filter);
        referralButton = (LabelledButtonLayout) findViewById(R.id.refer_button);
        
        mLogoImage = (ImageView) findViewById(R.id.coyrudy_logo);
        
        mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setStackFromEnd(true);
        mConversationListView.setLayoutManager(mLayoutManager);
        
        mConversationAdapter = new ConversationAdapter(this, mLayoutManager);
        
        ChatItemAnimator itemAnimator = new ChatItemAnimator(0.1f, 200);
        
        mConversationListView.setItemAnimator(itemAnimator);
        
        mConversationListView.setAdapter(mConversationAdapter);
        
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isOptionsVisible)
                    hideOptions();
                
                onSendButtonClicked(view);
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
        
        
        mInputText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                
            }
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                
            }
            
            @Override
            public void afterTextChanged(Editable s) {
                if (!sendTypingSignalPending) {
                    inputBuffer = s.toString();
                    sendTypingSignalPending = true;
                    sendTypingSignal();
                }
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
                        onNewMessageReceived(data);
                        
                        if (null == seenSignalHandler) seenSignalHandler = new Handler();
                        if (null == seenSignalRunnable) seenSignalRunnable = new Runnable() {
                            @Override
                            public void run() {
                                sendSeenSignal();
                            }
                        };
                        
                        seenSignalHandler.removeCallbacksAndMessages(null);
                        seenSignalHandler.postDelayed(seenSignalRunnable, 2000);
                        break;
                    }
                    case CityOfTwo.ACTION_LAST_SEEN: {
                        SharedPreferences sp = getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE);
                        Long lastSeen = sp.getLong(KEY_LAST_SEEN, -1);
                        
                        sp.edit().remove(KEY_LAST_SEEN).apply();
                        mConversationAdapter.setLastSeen(lastSeen);
                        break;
                    }
                    case CityOfTwo.ACTION_IS_TYPING: {
                        SharedPreferences sp = getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE);
                        Boolean isTyping = sp.getBoolean(KEY_IS_TYPING, false);
                        
                        sp.edit().remove(KEY_IS_TYPING).apply();
                        mConversationAdapter.setTyping(isTyping);
                        break;
                    }
                    case CityOfTwo.ACTION_END_CHAT: {
                        mInputText.getText().clear();
                        mInputText.setEnabled(false);
                        
                        onChatEndReceived(data);
                        break;
                    }
                    case CityOfTwo.ACTION_BEGIN_CHAT: {
                        mInputText.getText().clear();
                        mInputText.setEnabled(true);
                        
                        onChatBeginReceived(data);
                        break;
                    }
                    case CityOfTwo.ACTION_USER_OFFLINE: {
                        onUserOfflineReceived(data);
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
        
        restoreActivity(savedInstanceState);
        
        sendBeginChatSignal();
        
        firstRun = getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE)
                .getBoolean(CityOfTwo.KEY_FIRST_RUN, true);
        
        if (firstRun) startTutorial();
    }
    
    @Override
    protected void onStop() {
        super.onStop();

//		adView.destroy();
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        String headerText = mConversationAdapter.getHeaderText();

        if (mConversationAdapter.getItemCount() > 0) {
            ArrayList<String> textList = new ArrayList<>();
            ArrayList<Conversation> dataset = mConversationAdapter.getDataset();
            for (Conversation c : dataset) {
                textList.add(c.toString());
            }

            outState.putStringArrayList(CityOfTwo.KEY_CURRENT_CHAT, textList);
            outState.putString(CityOfTwo.KEY_CHAT_HEADER, headerText);
        }

        Log.i("Conversation Activity", "Instance Saved");

    }
    
    private void sendSeenSignal() {
        final SharedPreferences sp = getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE);
        String seen = getString(R.string.url_last_seen);

        JSONObject j = new JSONObject();
        try {
            j.put(CityOfTwo.HEADER_LAST_SEEN, System.currentTimeMillis());
            j.put(CityOfTwo.HEADER_CHATROOM_ID, sp.getInt(KEY_CHATROOM_ID, -1));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String[] Path = {CityOfTwo.API, seen};

        HttpHandler seenHttpHandler = new HttpHandler(CityOfTwo.HOST, Path, HttpHandler.POST, j);

        String token = "Token " + sp.getString(CityOfTwo.KEY_SESSION_TOKEN, "");
        seenHttpHandler.addHeader("Authorization", token);

        seenHttpHandler.execute();
    }
    
    private void sendTypingSignal() {
        if (null == typingSignalHandler) typingSignalHandler = new Handler();
        if (null == typingSignalRunnable) {
            typingSignalRunnable = new Runnable() {
                @Override
                public void run() {
                    String newBuffer = mInputText.getText().toString();
                    if (mInputText.hasFocus() && (!inputBuffer.equals(newBuffer))) {
                        inputBuffer = newBuffer;
                        typingSignalHandler.postDelayed(this, 2000);
                    } else sendTypingRequest(false);
                }
            };
        }
        sendTypingRequest(true);
        typingSignalHandler.postDelayed(typingSignalRunnable, 2000);
    }
    
    private void sendTypingRequest(final boolean isTyping) {
        final SharedPreferences sp = getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE);
        String typing_request = getString(R.string.url_is_typing);
        
        JSONObject j = new JSONObject();
        try {
            j.put(CityOfTwo.HEADER_IS_TYPING, isTyping);
            j.put(CityOfTwo.HEADER_CHATROOM_ID, sp.getInt(KEY_CHATROOM_ID, -1));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        String[] Path = {CityOfTwo.API, typing_request};
        
        HttpHandler typingHttpHandler = new HttpHandler(CityOfTwo.HOST, Path, HttpHandler.POST, j) {
            @Override
            protected void onPostExecute() {
                if (!isTyping) sendTypingSignalPending = false;
            }
        };
        
        String token = "Token " + sp.getString(CityOfTwo.KEY_SESSION_TOKEN, "");
        typingHttpHandler.addHeader("Authorization", token);
        
        typingHttpHandler.execute();
    }
    
    private void startTutorial() {
        List<ChainTourGuide> tourGuides = new ArrayList<>();
        
        Animation enter = new AlphaAnimation(0, 1);
        enter.setDuration(200);
        enter.setInterpolator(new AccelerateInterpolator());
        
        tourGuides.add(ChainTourGuide.init(this)
                .setToolTip(new ToolTip().setTitle("Welcome to your first chat")
                        .setDescription("Here are a few things you'll need to know")
                        .setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
                        .setGravity(Gravity.NO_GRAVITY)
                        .setEnterAnimation(enter))
                .setOverlay(new Overlay().setStyle(Overlay.Style.NoHole).disableClickThroughHole(true))
                .playLater(mLogoImage));
        
        tourGuides.add(ChainTourGuide.init(this)
                .setToolTip(new ToolTip().setTitle("CoyRudy")
                        .setDescription("Use this button to reveal some cool options")
                        .setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
                        .setGravity(Gravity.NO_GRAVITY)
                        .setEnterAnimation(enter))
                .setOverlay(new Overlay()
                        .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                showOptions();
                                isTourPending = true;
                            }
                        })
                        .disableClickThroughHole(true))
                .playLater(mLogoImage));
        
        tourGuides.add(ChainTourGuide.init(this)
                .setToolTip(new ToolTip().setTitle("New Chat")
                        .setDescription("This will end the current chat and start a new one.")
                        .setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
                        .setGravity(Gravity.NO_GRAVITY)
                        .setEnterAnimation(enter)
                ).playLater(newChatButton));
        
        tourGuides.add(ChainTourGuide.init(this)
                .setToolTip(new ToolTip().setTitle("Reveal")
                        .setDescription("You can also share your Facebook profile with the stranger.")
                        .setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
                        .setGravity(Gravity.NO_GRAVITY)
                        .setEnterAnimation(enter))
                // note that there is not Overlay here, so the default one will be used
                .playLater(revealButton));
        
        tourGuides.add(ChainTourGuide.init(this)
                .setToolTip(new ToolTip().setTitle("Filters")
                        .setDescription("Want better matches? You can filter your next match according to your preference.")
                        .setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
                        .setGravity(Gravity.NO_GRAVITY)
                        .setEnterAnimation(enter))
                // note that there is not Overlay here, so the default one will be used
                .playLater(filtersButton));
        
        tourGuides.add(ChainTourGuide.init(this)
                .setToolTip(new ToolTip().setTitle("Refer")
                        .setDescription("Like the app? Help spread the word!")
                        .setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
                        .setGravity(Gravity.NO_GRAVITY)
                        .setEnterAnimation(enter))
                .setOverlay(new Overlay()
                        .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                hideOptions();
                                isTourPending = true;
                            }
                        })
                        .disableClickThroughHole(true))
                // note that there is not Overlay here, so the default one will be used
                .playLater(referralButton));
        
        tourGuides.add(ChainTourGuide.init(this)
                .setToolTip(new ToolTip().setTitle("All set")
                        .setDescription("Enjoy!")
                        .setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
                        .setGravity(Gravity.NO_GRAVITY)
                        .setEnterAnimation(enter))
                .setOverlay(new Overlay().setStyle(Overlay.Style.NoHole)
                        .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE).edit()
                                        .putBoolean(CityOfTwo.KEY_FIRST_RUN, false).apply();
                                mTourGuideHandler.next();
                            }
                        }))
                .playLater(mLogoImage));
        
        Sequence sequence = new Sequence.SequenceBuilder()
                .add(tourGuides.toArray(new ChainTourGuide[tourGuides.size()]))
                .setDefaultOverlay(new Overlay()
                        .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mTourGuideHandler.next();
                            }
                        })
                        .disableClickThroughHole(true))
                .setDefaultPointer(null)
                .setContinueMethod(Sequence.ContinueMethod.OverlayListener)
                .build();
        
        mTourGuideHandler = ChainTourGuide.init(this).playInSequence(sequence);
    }
    
    protected void exitActivity(int resultCode) {
        setResult(resultCode);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            finishAfterTransition();
        } else finish();
    }
    
    protected void setupNewChatWindow(String headerText) {
        mConversationAdapter.clear();
        mConversationAdapter.insertItem(new Conversation("CHAT_BEGIN", CityOfTwo.FLAG_START));
        mConversationAdapter.insertItem(new Conversation("CHAT_END", CityOfTwo.FLAG_END));
        
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
        new AlertDialog.Builder(this, R.style.AppTheme_Dialog)
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
    
    protected void startNewChat() {
        endCurrentChat();
        
        exitActivity(RESULT_OK);
    }
    
    public void revealProfile(View view) {
        hideOptions();
        
        final SharedPreferences sp = getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE);
        
        Boolean showDialog = sp.getBoolean(CityOfTwo.KEY_SHOW_REVEAL_DIALOG, true);
        
        if (!showDialog) {
            revealProfile();
            return;
        }
        
        AlertDialog.Builder adb = new AlertDialog.Builder(this, R.style.AppTheme_Dialog);
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
            conversation.addFlag(CityOfTwo.FLAG_PROFILE);
            
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
                        
                        Integer credits = Response.getInt("credit");
                        
                        sharedPreferences.edit()
                                .putInt(CityOfTwo.KEY_MIN_AGE, minimumAge)
                                .putInt(CityOfTwo.KEY_MAX_AGE, maximumAge)
                                .putInt(CityOfTwo.KEY_DISTANCE, maximumDistance)
                                .putBoolean(CityOfTwo.KEY_MATCH_MALE, matchMale)
                                .putBoolean(CityOfTwo.KEY_MATCH_FEMALE, matchFemale)
                                .putBoolean(CityOfTwo.KEY_FILTERS_APPLIED, true)
                                .apply();
                        
                        new SecurePreferences(ConversationActivity.this, CityOfTwo.PACKAGE_NAME)
                                .edit().putInt(CityOfTwo.KEY_CREDITS, credits);
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
                
                s.show();
                
            }
        };
        
        String token = "Token " + sharedPreferences.getString(CityOfTwo.KEY_SESSION_TOKEN, "");
        
        filterHttpHandler.addHeader("Authorization", token);
        filterHttpHandler.execute();
        
    }
    
    public void referCoyRudy(View view) {
        hideOptions();
        
        AlertDialog.Builder adb = new AlertDialog.Builder(this, R.style.AppTheme_Dialog);
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
        adb.setNegativeButton("Copy Link", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Context context = ConversationActivity.this;
                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context
                        .getSystemService(Context.CLIPBOARD_SERVICE);
                android.content.ClipData clip = android.content.ClipData
                        .newPlainText("Unique URL", shareText);
                clipboard.setPrimaryClip(clip);
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
        shareIntent.setType("name/plain");
        
        List<ResolveInfo> resolveInfoList = getPackageManager().queryIntentActivities(shareIntent, 0);
        
        if (!resolveInfoList.isEmpty()) {
            for (ResolveInfo resolveInfo : resolveInfoList) {
                String packageName = resolveInfo.activityInfo.packageName;
                if (targetedApps.contains(packageName)) {
                    Intent targetedShareIntent = new Intent(Intent.ACTION_SEND);
                    targetedShareIntent.setComponent(new ComponentName(packageName, resolveInfo.activityInfo.name));
                    targetedShareIntent.setType("name/plain");
                    targetedShareIntent.putExtra(Intent.EXTRA_SUBJECT, "CoyRudy!");
                    targetedShareIntent.putExtra(Intent.EXTRA_TEXT, uniqueUrl);
                    
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
            
            Context context = ConversationActivity.this;
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context
                    .getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData
                    .newPlainText("Unique URL", uniqueUrl);
            clipboard.setPrimaryClip(clip);
            
            
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
        mChatOptionsContainer.animate().setInterpolator(new DecelerateInterpolator(.9f)).setDuration(200)
                .translationY(0)
                .withStartAction(new Runnable() {
                    @Override
                    public void run() {
                        if (mChatOptionsContainer.getTranslationY() >= 0)
                            mChatOptionsContainer.setTranslationY(-viewHeight);
                        mChatOptionsContainer.setVisibility(View.VISIBLE);
                    }
                })
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        if (isTourPending) {
                            mTourGuideHandler.next();
                            isTourPending = false;
                        }
                        isOptionsVisible = true;
                    }
                });
    }
    
    protected void hideOptions() {
        mOptionsDismissButton.setVisibility(View.GONE);
        final int viewHeight = mChatOptionsContainer.getHeight();
        
        mChatOptionsContainer.animate().setInterpolator(new AccelerateInterpolator(.9f)).setDuration(200)
                .translationY(-viewHeight)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        if (isTourPending) {
                            mTourGuideHandler.next();
                            isTourPending = false;
                        }
                        isOptionsVisible = false;
                        mChatOptionsContainer.setVisibility(View.INVISIBLE);
                    }
                });
    }

//    @Override
//    protected void onRestoreInstanceState(Bundle savedInstanceState) {
//        super.onRestoreInstanceState(savedInstanceState);
//
//        restoreActivity(savedInstanceState);
//        Log.i("Conversation Activity", "Instance Restored");
//    }
    
    private void sendBeginChatSignal() {
        
    }
    
    protected void restoreActivity(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            ArrayList<String> textList = savedInstanceState.getStringArrayList(CityOfTwo.KEY_CURRENT_CHAT);
            String headerText = savedInstanceState.getString(CityOfTwo.KEY_CHAT_HEADER);
            
            if (textList == null) textList = new ArrayList<>();
            
            mConversationAdapter.clear();
            
            for (String text : textList)
                mConversationAdapter.insertItem(new Conversation(text));
            
            mConversationAdapter.setHeaderText(headerText);
        } else {
            mConversationAdapter.insertItem(new Conversation("CHAT_BEGIN", CityOfTwo.FLAG_START));
            mConversationAdapter.insertItem(new Conversation("CHAT_END", CityOfTwo.FLAG_END));
//	        mConversationAdapter.insertItem(new Conversation("CHAT_AD", CityOfTwo.FLAG_AD));
        }
    }
    
    protected void sendMessage(final Conversation bufferConv) {
        final String value = bufferConv.getText().replaceFirst("\\s+$", "");
        
        mConversationAdapter.insertItem(bufferConv);
        
        if (!mConversationAdapter.isLastVisible())
            mConversationListView.smoothScrollToPosition(mConversationAdapter.getItemPosition(bufferConv));
        
        JSONObject params = new JSONObject();
        try {
            params.put(CityOfTwo.HEADER_SEND_MESSAGE, value);
            params.put(CityOfTwo.HEADER_FLAGS, bufferConv.getFlags());
            params.put(CityOfTwo.HEADER_TIME, bufferConv.getTime());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        String send_message = getString(R.string.url_send_message);
        
        
        String[] Path = {CityOfTwo.API, send_message};
        
        mHttpHandler = new HttpHandler(CityOfTwo.HOST, Path, HttpHandler.POST, params) {
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
                    mConversationAdapter.removeItem(bufferConv);
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
        new AlertDialog.Builder(this, R.style.AppTheme_Dialog)
                .setTitle("Leave conversation")
                .setMessage("Do you want to leave this conversation?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        endCurrentChat();
                        exitActivity(RESULT_CANCELED);
                    }
                })
                .setNegativeButton("No", null)
                .create().show();
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
        nm.cancel(CityOfTwo.NOTIFICATION_NEW_MESSAGE, 10044);
        nm.cancel(CityOfTwo.NOTIFICATION_NEW_CHAT, 10045);
        nm.cancel(CityOfTwo.NOTIFICATION_CHAT_END, 10046);
        
        IntentFilter filter = new IntentFilter();
        
        filter.addAction(CityOfTwo.ACTION_NEW_MESSAGE);
        filter.addAction(CityOfTwo.ACTION_BEGIN_CHAT);
        filter.addAction(CityOfTwo.ACTION_END_CHAT);
        filter.addAction(CityOfTwo.ACTION_USER_OFFLINE);
        filter.addAction(CityOfTwo.ACTION_LAST_SEEN);
        filter.addAction(CityOfTwo.ACTION_IS_TYPING);
        
        LocalBroadcastManager.getInstance(this).registerReceiver((mBroadcastReceiver), filter);
        
        if (CityOfTwo.pendingMessages != null) CityOfTwo.pendingMessages.clear();
        if (CityOfTwo.messageCounter != null) CityOfTwo.messageCounter = 0;
        
        SharedPreferences sp = getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE);
        
        Boolean userOnline = sp.getBoolean(CityOfTwo.KEY_SESSION_ACTIVE, true);
        SharedPreferences.Editor editor = sp.edit();
        
        editor.remove(CityOfTwo.KEY_SESSION_ACTIVE);
        
        if (!userOnline) {
            editor.remove(CityOfTwo.KEY_CHATROOM_ID)
                    .remove(CityOfTwo.KEY_CHAT_PENDING)
                    .apply();
            
            new AlertDialog.Builder(this, R.style.AppTheme_Dialog)
                    .setTitle("Stranger has left the chat")
                    .setMessage("Start a new chat?")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            exitActivity(RESULT_OK);
                        }
                    })
                    .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            exitActivity(CityOfTwo.RESULT_EXIT_APP);
                        }
                    }).show();
            return;
        }
        
        editor.apply();
        
        String commonLikes = sp.getString(KEY_COMMON_LIKES, "");
        Boolean chatPending = sp.getBoolean(CityOfTwo.KEY_CHAT_PENDING, false);
        Integer chatRoomId = sp.getInt(CityOfTwo.KEY_CHATROOM_ID, -1);
        
        Long lastSeen = sp.getLong(KEY_LAST_SEEN, -1);
        Boolean isTyping = sp.getBoolean(KEY_IS_TYPING, false);
        
        sp.edit().remove(KEY_LAST_SEEN).remove(KEY_IS_TYPING).apply();
        
        if (chatRoomId == -1) {
            new AlertDialog.Builder(this, R.style.AppTheme_Dialog)
                    .setTitle("Stranger has left the chat")
                    .setMessage("Start a new chat")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startNewChat();
                        }
                    })
                    .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            exitActivity(RESULT_EXIT_APP);
                        }
                    }).show();
        } else {
            DatabaseHelper db = new DatabaseHelper(this);
            ArrayList<Conversation> pendingMessages = db.retrieveMessages(chatRoomId);
            db.clearTable(chatRoomId);
            
            if (chatPending) {
                sp.edit().putBoolean(CityOfTwo.KEY_CHAT_PENDING, false).apply();
                setupNewChatWindow(commonLikes);
            }
            
            mConversationAdapter.insertItems(pendingMessages);
            mConversationListView.scrollToPosition(mConversationAdapter.getItemCount());
            
            if (lastSeen > -1) mConversationAdapter.setLastSeen(lastSeen);
            mConversationAdapter.setTyping(isTyping);
            
            if (mConversationAdapter.getItemCount() > 5) sendSeenSignal();
            
        }
    }
    
    @Override
    protected void onStart() {
        super.onStart();

//		adView = new MoPubView(this);
//		adView.setLayoutParams(new RecyclerView.LayoutParams(
//				ViewGroup.LayoutParams.MATCH_PARENT,
//				(int) CityOfTwo.dpToPixel(this, 50)
//		));
//		adView.setAdUnitId("153e5b049d414b5d93c3fbac190a0350"); // Enter your Ad Unit ID received www.mopub.com
//		adView.loadAd();
//		mConversationAdapter.setAdView(adView);
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
                sp.edit().remove(CityOfTwo.KEY_CHATROOM_ID).apply();
            }
        };
        
        String token = "Token " + sp.getString(CityOfTwo.KEY_SESSION_TOKEN, "");
        dumpHttpHandler.addHeader("Authorization", token);
        
        dumpHttpHandler.execute();
    }
    
    public void facebookLogin(final DialogInterface.OnClickListener failureClickListener) {
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
                new AlertDialog.Builder(ConversationActivity.this, R.style.AppTheme_Dialog)
                        .setTitle("Something went wrong")
                        .setMessage("Do you want to try again?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                facebookLogin(failureClickListener);
                            }
                        })
                        .setNegativeButton("No", failureClickListener)
                        .show();
            }
        };
        facebookLogin.execute();
    }

    protected void onUserOfflineReceived(Bundle data) {
        getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE).edit()
                .remove(CityOfTwo.KEY_CHAT_PENDING)
                .remove(CityOfTwo.KEY_CHATROOM_ID)
                .remove(CityOfTwo.KEY_SESSION_ACTIVE)
                .apply();

        new AlertDialog.Builder(ConversationActivity.this, R.style.AppTheme_Dialog)
                .setTitle("Your last chat has ended")
                .setMessage("Start a new chat?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startNewChat();
                    }
                })
                .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        exitActivity(RESULT_CANCELED);

                    }
                }).show();
    }

    protected void onChatBeginReceived(Bundle data) {
        new AlertDialog.Builder(ConversationActivity.this, R.style.AppTheme_Dialog)
                .setTitle("New Chat")
                .setMessage("You are now starting a new chat.")
                .setNeutralButton("Ok", null).show();

        SharedPreferences sp = getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE);
        String commonLikes = sp.getString(KEY_COMMON_LIKES, "");

        sp.edit().putBoolean(CityOfTwo.KEY_CHAT_PENDING, false).apply();

        if (mConversationAdapter.isWaiting())
            mConversationAdapter.hideWaitingDialog();

        setupNewChatWindow(commonLikes);

        mConversationAdapter.notifyDataSetChanged();
        mConversationListView.smoothScrollToPosition(mConversationAdapter.getItemCount());
    }

    protected void onChatEndReceived(Bundle data) {
        new AlertDialog.Builder(ConversationActivity.this, R.style.AppTheme_Dialog)
                .setTitle("Stranger has left the chat")
                .setMessage("Start a new chat?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startNewChat();
                    }
                })
                .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        exitActivity(CityOfTwo.RESULT_EXIT_APP);
                    }
                }).show();
    }

    protected void onNewMessageReceived(Bundle data) {
        String text = data.getString(CityOfTwo.KEY_DATA);
        Integer flags = data.getInt(CityOfTwo.KEY_MESSAGE_FLAGS);
        long time = System.currentTimeMillis();

        Conversation c = new Conversation(text, flags, time);
        c.removeFlag(CityOfTwo.FLAG_SENT);
        c.addFlag(CityOfTwo.FLAG_RECEIVED);

        mConversationAdapter.insertItem(c);

        if (!mConversationAdapter.isLastVisible())
            mConversationListView.smoothScrollToPosition(mConversationAdapter.getItemPosition(c));

    }

    protected void onSendButtonClicked(View view) {
        final String bufferText = mInputText.getText().toString().replaceFirst("\\s+$", "");

        if (!bufferText.isEmpty()) {
            mInputText.setText("");
            Conversation conversation = new Conversation(
                    bufferText,
                    CityOfTwo.FLAG_SENT | CityOfTwo.FLAG_TEXT
            );
            sendMessage(conversation);
        }
    }



}
