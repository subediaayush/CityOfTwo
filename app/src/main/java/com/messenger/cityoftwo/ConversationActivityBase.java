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

public abstract class ConversationActivityBase extends AppCompatActivity {
}
