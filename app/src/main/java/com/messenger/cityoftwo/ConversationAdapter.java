package com.messenger.cityoftwo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.login.widget.ProfilePictureView;
import com.mopub.mobileads.MoPubView;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Aayush on 1/15/2016.
 */
public class ConversationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int ID_TEXT = 0x00F00000;
    private static final int ID_PROFILE_IMAGE = 0x00F00001;
    private static final int ID_PROFILE_NAME = 0x00F00002;
    protected List<Integer> adLocations;
    //    private static final int ID_PROFILE_URL =
    private String mHeaderText;
    private List<Conversation> ConversationList;
    private LinearLayoutManager LayoutManager;
    private Context context;
    private boolean isWaiting;
    private ProgressDialog mWaitingDialog;
    private int selectedItem;
    private MoPubView adView;
    private boolean isLastVisible;
    private int maximumDisplayableChild;

    public ConversationAdapter(final Context context, List<Conversation> conversationList, LinearLayoutManager l) {
        ConversationList = conversationList;
        this.context = context;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            mWaitingDialog = new ProgressDialog(context, android.R.style.Theme_Material_Light_Dialog);
        else
            mWaitingDialog = new ProgressDialog(context);

        isWaiting = false;
        isLastVisible = false;
        selectedItem = -1;
        adLocations = new ArrayList<>();
        LayoutManager = l;
        maximumDisplayableChild = -1;

        mWaitingDialog.setTitle("Finding a match");
        mWaitingDialog.setMessage("Finding a new match for you.");
        mWaitingDialog.setCancelable(true);
        mWaitingDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (context instanceof AppCompatActivity) {
                    ((AppCompatActivity) context).setResult(Activity.RESULT_CANCELED);
                    ((AppCompatActivity) context).finish();
                }
            }
        });


//        String placement_id = "1727194620850368_1802484046654758";
//        adView = new AdView(context, placement_id, AdSize.BANNER_320_50);
//        if (BuildConfig.DEBUG) AdSettings.addTestDevice("1d08b53d5b715d2ee573ea4c5f88f5df");
//        adView.loadAd();

        mHeaderText = "";

    }


    @Override
    public int getItemViewType(int position) {
        return ConversationList.get(position).getFlags();
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater li = LayoutInflater.from(context);

        View view;

        if ((viewType & CityOfTwo.FLAG_SENT) == CityOfTwo.FLAG_SENT) {
            view = li.inflate(R.layout.layout_msg_sent, parent, false);
        } else if ((viewType & CityOfTwo.FLAG_RECEIVED) == CityOfTwo.FLAG_RECEIVED) {
            view = li.inflate(R.layout.layout_msg_received, parent, false);
        } else if ((viewType & CityOfTwo.FLAG_START) == CityOfTwo.FLAG_START) {
            view = li.inflate(R.layout.layout_msg_start, parent, false);
        } else if ((viewType & CityOfTwo.FLAG_END) == CityOfTwo.FLAG_END) {
            view = li.inflate(R.layout.layout_msg_end, parent, false);
        } else if ((viewType & CityOfTwo.FLAG_AD) == CityOfTwo.FLAG_AD) {
            view = li.inflate(R.layout.layout_msg_ad, parent, false);
        } else {
            view = li.inflate(R.layout.layout_msg_end, parent, false);
        }

        if ((viewType & CityOfTwo.FLAG_TEXT) == CityOfTwo.FLAG_TEXT) {
            FrameLayout container = (FrameLayout) view.findViewById(R.id.content_container);

            TextView messageTextView = (TextView) LayoutInflater.from(context)
                    .inflate(R.layout.layout_message_text, null)
                    .findViewById(R.id.message_text);

            container.addView(messageTextView);
            messageTextView.setLayoutParams(
                    new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT)
            );

            return new ContentHolder(view);
        } else if ((viewType & CityOfTwo.FLAG_PROFILE) == CityOfTwo.FLAG_PROFILE) {
            FrameLayout container = (FrameLayout) view.findViewById(R.id.content_container);

            View childView = LayoutInflater.from(context)
                    .inflate(R.layout.layout_message_profile, null);

            container.addView(childView);

            childView.setLayoutParams(
                    new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT)
            );

            return new ContentHolder(view);
        } else if (((viewType & CityOfTwo.FLAG_START) == CityOfTwo.FLAG_START) ||
                ((viewType & CityOfTwo.FLAG_END) == CityOfTwo.FLAG_END)) {
            return new GenericHolder(view);
        } else if ((viewType & CityOfTwo.FLAG_AD) == CityOfTwo.FLAG_AD) {
            return new AdHolder(view);
        } else {
            return new ContentHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, final int position) {
        Conversation currentConv = ConversationList.get(position);

        int flags = currentConv.getFlags();
        isLastVisible = false;

        if ((flags & CityOfTwo.FLAG_TEXT) == CityOfTwo.FLAG_TEXT) {
            final ContentHolder holder = (ContentHolder) viewHolder;

            final String messageText = currentConv.getText();

            String messageTime = new SimpleDateFormat("hh:mm a").format(currentConv.getTime());


            TextView messageTextView = (TextView) holder.contentContainer.findViewById(R.id.message_text);
            messageTextView.setText(messageText);

            holder.dateContainer.setText(messageTime);

            if (selectedItem != position)
                holder.dateContainer.setVisibility(View.GONE);


            holder.contentContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int oldSelected = selectedItem;
                    selectedItem = position;
                    toggleVisibility(holder.dateContainer);
                    ConversationAdapter.this.notifyItemChanged(position);
                    if (oldSelected != -1) ConversationAdapter.this.notifyItemChanged(oldSelected);
                }
            });
        } else if ((flags & CityOfTwo.FLAG_PROFILE) == CityOfTwo.FLAG_PROFILE) {
            final ContentHolder holder = (ContentHolder) viewHolder;

            try {
                JSONObject facebookObject = new JSONObject(currentConv.getText());
                String profileName = facebookObject.getString(CityOfTwo.KEY_PROFILE_NAME),
                        profileId = facebookObject.getString(CityOfTwo.KEY_PROFILE_ID);


                final Uri profileUri = CityOfTwo.getFacebookPageURI(context, profileId);


                final ProfilePictureView profileImageView = (ProfilePictureView) holder.contentContainer.findViewById(R.id.message_profile_image);

                profileImageView.setProfileId(profileId);

                TextView profileTextView = (TextView) holder.contentContainer.findViewById(R.id.message_profile_name);
                profileTextView.setText(profileName);

                holder.contentContainer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, profileUri);
                        context.startActivity(intent);
                    }
                });

            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if ((flags & CityOfTwo.FLAG_START) == CityOfTwo.FLAG_START) {
            GenericHolder holder = (GenericHolder) viewHolder;
            holder.likeList.setText(mHeaderText);
        } else if ((flags & CityOfTwo.FLAG_END) == CityOfTwo.FLAG_END) {
            GenericHolder holder = (GenericHolder) viewHolder;
            isLastVisible = true;
        } else if ((flags & CityOfTwo.FLAG_AD) == CityOfTwo.FLAG_AD) {
            if (canBindAd(position)) {
                AdHolder holder = (AdHolder) viewHolder;
                ViewGroup parent = (ViewGroup) adView.getParent();
                if (parent != null) parent.removeAllViews();
                holder.adContainer.addView(adView);
            }
//                if (adView.getParent() != null) {
//                    ViewGroup parent = (ViewGroup) adView.getParent();
//                    parent.removeAllViews();
//                }
//            }
        }
    }

    private boolean canBindAd(int position) {
        int firstItem = LayoutManager.findFirstVisibleItemPosition(),
                lastItem = LayoutManager.findLastVisibleItemPosition();

        for (int adLocation : adLocations)
            if (adLocation >= firstItem && adLocation <= lastItem && adLocation != position)
                return false;

        return true;
    }

    private void facebookLogin(final AccessToken accessToken) {

        new FacebookLogin(context, accessToken) {
            @Override
            void onSuccess(String response) {

            }

            @Override
            void onFailure(Integer status) {
                new AlertDialog.Builder(context)
                        .setTitle("Error")
                        .setMessage("An error occured.")
                        .setPositiveButton("Try Again", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                facebookLogin(accessToken);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (isWaiting) hideWaitingDialog();
                            }
                        })
                        .show();

            }
        }.execute();
    }

    protected boolean isWaiting() {
        return isWaiting;
    }

    protected void showWaitingDialog() {
        if (!isWaiting) mWaitingDialog.show();
        isWaiting = true;
    }

    protected void hideWaitingDialog() {
        if (isWaiting) mWaitingDialog.hide();
        isWaiting = false;
    }

    private void toggleVisibility(View view) {
        if (view.getVisibility() == View.VISIBLE)
            view.setVisibility(View.GONE);
        else
            view.setVisibility(View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return ConversationList.size();
    }

    public String getHeaderText() {
        return this.mHeaderText;
    }

    public void setHeaderText(String headerText) {
        this.mHeaderText = headerText;
    }

    public ProgressDialog getWaitingDialog() {
        return mWaitingDialog;
    }

    public boolean isLastVisible() {
        return isLastVisible;
    }

    public void insertItem(Conversation c, int insertPosition) {
        ConversationList.add(insertPosition, c);
        notifyItemInserted(insertPosition);

//        int heightSum = 0;
//        if (maximumDisplayableChild != -1 /* && condition for keyboard not visible */) {
//            for (int i = firstItem; i <= lastItem; i++) {
//                heightSum += LayoutManager.findViewByPosition(i).getHeight();
//            }
//            if (heightSum > LayoutManager.getHeight()) {
//                maximumDisplayableChild = lastItem - firstItem;
//            }
//        }

//        boolean canInsertAd = ++insertPosition > 6 && !isAdDisplayed;

        if (++insertPosition % 6 == 0) {
            Conversation ad = new Conversation("CHAT_AD", CityOfTwo.FLAG_AD);
            ConversationList.add(insertPosition, ad);
            notifyItemInserted(insertPosition);
            adLocations.add(insertPosition);
        }
    }

    public void setAdView(MoPubView adView) {
        this.adView = adView;
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        if (adView != null) adView.destroy();
        Log.i("Conversation Adapter", "Adapter detached from list");
    }

    private class ContentHolder extends RecyclerView.ViewHolder {
        TextView dateContainer;
        FrameLayout contentContainer;

        public ContentHolder(View itemView) {
            super(itemView);
            contentContainer = (FrameLayout) itemView.findViewById(R.id.content_container);
            dateContainer = (TextView) itemView.findViewById(R.id.time);
        }
    }

    private class GenericHolder extends RecyclerView.ViewHolder {
        TextView likeList;

        public GenericHolder(View itemView) {
            super(itemView);

            likeList = (TextView) itemView.findViewById(R.id.likes_list);
        }
    }

    private class AdHolder extends RecyclerView.ViewHolder {
        ViewGroup adContainer;

        public AdHolder(View itemView) {
            super(itemView);
            adContainer = (ViewGroup) itemView.findViewById(R.id.ad_container);
        }
    }
}
