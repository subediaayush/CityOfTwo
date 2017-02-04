package com.messenger.cityoftwo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.util.SortedList;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.login.widget.ProfilePictureView;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.messenger.cityoftwo.R.id.line;

/**
 * Created by Aayush on 1/15/2016.
 */
public class ConversationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private static final int ID_TEXT = 0x00F00000;
	private static final int ID_PROFILE_IMAGE = 0x00F00001;
	private static final int ID_PROFILE_NAME = 0x00F00002;

	private static final String TAG = "ConversationAdapter";

	protected List<Integer> adLocations;
	//    private static final int ID_PROFILE_URL =
	private String mHeaderText;
	private SortedList<Conversation> ConversationList;
	private LinearLayoutManager LayoutManager;
	private Context context;
	private boolean isWaiting;
	private ProgressDialog mWaitingDialog;
	private int selectedItem;
	//	private MoPubView adView;
	private boolean isLastVisible;
	private int maximumDisplayableChild;
	private int currentAdLocation;

	private Conversation indicatorConversation;

	public ConversationAdapter(final Context context, LinearLayoutManager l) {
		ConversationList = new SortedList<>(Conversation.class, new SortedList.Callback<Conversation>() {
			@Override
			public int compare(Conversation o1, Conversation o2) {
				return Conversation.CONVERSATION_COMPARATOR.compare(o1, o2);
			}

			@Override
			public void onChanged(int position, int count) {
				notifyItemRangeChanged(position, count);
			}

			@Override
			public boolean areContentsTheSame(Conversation oldItem, Conversation newItem) {
				return oldItem.getText().equals(newItem.getText());
			}

			@Override
			public boolean areItemsTheSame(Conversation item1, Conversation item2) {
				return item1.getText().equals(item2.getText()) &&
						item1.getFlags().equals(item2.getFlags()) &&
						item1.getTime() == item2.getTime();
			}

			@Override
			public void onInserted(int position, int count) {
				notifyItemRangeInserted(position, count);
			}

			@Override
			public void onRemoved(int position, int count) {
				notifyItemRangeRemoved(position, count);
			}

			@Override
			public void onMoved(int fromPosition, int toPosition) {
				notifyItemMoved(fromPosition, toPosition);
			}
		});
		this.context = context;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
			mWaitingDialog = new ProgressDialog(context, R.style.AppTheme_Dialog);
		else
			mWaitingDialog = new ProgressDialog(context);

		isWaiting = false;
		isLastVisible = false;
		selectedItem = -1;
		currentAdLocation = -1;
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

		indicatorConversation = new Conversation(
				"CHAT_LAST_SEEN",
				CityOfTwo.FLAG_INDICATOR,
				System.currentTimeMillis()
		);
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		Log.i(TAG, "onCreateViewHolder");

		LayoutInflater li = LayoutInflater.from(context);

		View view;

		if ((viewType & CityOfTwo.FLAG_SENT) == CityOfTwo.FLAG_SENT) {
			view = li.inflate(R.layout.layout_msg_sent, parent, false);
		} else if ((viewType & CityOfTwo.FLAG_RECEIVED) == CityOfTwo.FLAG_RECEIVED) {
			view = li.inflate(R.layout.layout_msg_received, parent, false);
		} else if ((viewType & CityOfTwo.FLAG_START) == CityOfTwo.FLAG_START) {
			view = li.inflate(R.layout.layout_msg_start, parent, false);
		} else if ((viewType & CityOfTwo.FLAG_END) == CityOfTwo.FLAG_END) {
			view = li.inflate(R.layout.layout_msg_empty, parent, false);
		} else if ((viewType & CityOfTwo.FLAG_INDICATOR) == CityOfTwo.FLAG_INDICATOR) {
			view = li.inflate(R.layout.layout_msg_indicators, parent, false);
		} else {
			view = li.inflate(R.layout.layout_msg_empty, parent, false);
		}

//		if ((viewType & CityOfTwo.FLAG_TEXT) == CityOfTwo.FLAG_TEXT) {
//			FrameLayout container = (FrameLayout) view.findViewById(R.id.content_container);
//
//			TextView messageTextView = (TextView) LayoutInflater.received(context)
//					.inflate(R.layout.layout_message_text, null)
//					.findViewById(R.id.message_text);
//
//			container.addView(messageTextView);
//			messageTextView.setLayoutParams(
//					new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
//							ViewGroup.LayoutParams.WRAP_CONTENT)
//			);
//
//			return new ContentHolder(view);
//		} else if ((viewType & CityOfTwo.FLAG_PROFILE) == CityOfTwo.FLAG_PROFILE) {
//			FrameLayout container = (FrameLayout) view.findViewById(R.id.content_container);
//
//			View childView = LayoutInflater.received(context)
//					.inflate(R.layout.layout_profile_sent, null);
//
//			container.addView(childView);
//
//			childView.setLayoutParams(
//					new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
//							ViewGroup.LayoutParams.WRAP_CONTENT)
//			);
//
//			return new ContentHolder(view);
//		} else if (((viewType & CityOfTwo.FLAG_START) == CityOfTwo.FLAG_START) ||
//				((viewType & CityOfTwo.FLAG_END) == CityOfTwo.FLAG_END)) {
//			return new GenericHolder(view);
//		} else if ((viewType & CityOfTwo.FLAG_REQUEST) == CityOfTwo.FLAG_REQUEST) {
//			return new AdHolder(view);
//		} else if ((viewType & CityOfTwo.FLAG_INDICATOR) == CityOfTwo.FLAG_INDICATOR) {
//			return new IndicatorHolder(view);
//		} else {
			return new ContentHolder(view);
//		}
	}

	@Override
	public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, final int position) {
		Log.i(TAG, "onBindViewHolder for " + position);
		Conversation currentConv = ConversationList.get(position);

		int flags = currentConv.getFlags();
		isLastVisible = false;

		if ((flags & CityOfTwo.FLAG_TEXT) == CityOfTwo.FLAG_TEXT) {
			final ContentHolder holder = (ContentHolder) viewHolder;

			final String messageText = currentConv.getText();

			String messageTime = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(currentConv.getTime());

			TextView messageTextView = (TextView) holder.contentContainer.findViewById(R.id.message_text);
			messageTextView.setText(messageText);

			holder.dateContainer.setText(messageTime);

			if (selectedItem == position) {
				holder.dateContainer.setVisibility(View.VISIBLE);
			}

			holder.contentContainer.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					int oldSelected = selectedItem;
					if (oldSelected == position) {
						selectedItem = -1;
					} else {
						selectedItem = position;
					}
					toggleVisibility(holder.dateContainer);
//                    ConversationAdapter.this.notifyItemChanged(position);
					if (oldSelected != -1) ConversationAdapter.this.notifyItemChanged(oldSelected);
				}
			});
		} else if ((flags & CityOfTwo.FLAG_PROFILE) == CityOfTwo.FLAG_PROFILE) {
			final ContentHolder holder = (ContentHolder) viewHolder;

			try {
				JSONObject facebookObject = new JSONObject(currentConv.getText());
				String profileName = facebookObject.getString(CityOfTwo.KEY_PROFILE_NAME),
						profileId = facebookObject.getString(CityOfTwo.KEY_PROFILE_ID);


				final Uri profileUri = FacebookHelper.getFacebookPageURI(context, profileId);

				String messageTime = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(currentConv.getTime());
				holder.dateContainer.setText(messageTime);

				final ProfilePictureView profileImageView = (ProfilePictureView) holder.contentContainer.findViewById(R.id.message_profile_image);

				profileImageView.setProfileId(profileId);

				TextView profileTextView = (TextView) holder.contentContainer.findViewById(R.id.profile_name);
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

		} else if ((flags & CityOfTwo.FLAG_END) == CityOfTwo.FLAG_END) {
			isLastVisible = true;
		} else if ((flags & CityOfTwo.FLAG_INDICATOR) == CityOfTwo.FLAG_INDICATOR) {
			IndicatorHolder holder = (IndicatorHolder) viewHolder;

			holder.typingIndicator.setVisibility(View.GONE);
			holder.seenIndicator.setVisibility(View.GONE);
			if ((flags & CityOfTwo.FLAG_TYPING) == CityOfTwo.FLAG_TYPING) {
				holder.typingIndicator.setVisibility(View.VISIBLE);
			}
			if ((flags & CityOfTwo.FLAG_LAST_SEEN) == CityOfTwo.FLAG_LAST_SEEN) {
				String lastSeen = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(currentConv.getTime());

				holder.lastSeen.setText(lastSeen);
				holder.seenIndicator.setVisibility(View.VISIBLE);
			}

		}

//		if ((flags & CityOfTwo.FLAG_SENT) == CityOfTwo.FLAG_SENT ||
//				(flags & CityOfTwo.FLAG_RECEIVED) == CityOfTwo.FLAG_RECEIVED) {
//			Conversation previousItem = ConversationList.get(position - 1);
//			int previousItemFlag = previousItem.getFlags();
//
//			boolean previousIndicatorType = (previousItemFlag & CityOfTwo.FLAG_INDICATOR) == CityOfTwo.FLAG_INDICATOR;
//			if (previousIndicatorType) {
//				previousItem = ConversationList.get(position - 2);
//				previousItemFlag = previousItem.getFlags();
//			}
//
//			boolean sameType = (flags & CityOfTwo.FLAG_SENT) == (previousItemFlag & CityOfTwo.FLAG_SENT) &&
//					(flags & CityOfTwo.FLAG_RECEIVED) == (previousItemFlag & CityOfTwo.FLAG_RECEIVED);
//
//			final ContentHolder holder = (ContentHolder) viewHolder;
//			if (sameType) {
//				if (position != selectedItem) holder.dateContainer.setVisibility(View.GONE);
//			} else {
//				holder.dateContainer.setVisibility(View.VISIBLE);
//			}
//		}

	}

	@Override
	public int getItemViewType(int position) {
		return ConversationList.get(position).getFlags();
	}

	@Override
	public int getItemCount() {
		return ConversationList.size();
	}

	private void setRecyclableLater(final RecyclerView.ViewHolder viewHolder) {
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				viewHolder.setIsRecyclable(true);
			}
		}, 200);
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
				new AlertDialog.Builder(context, R.style.AppTheme_Dialog)
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

	public void insertItem(Conversation c) {
		ConversationList.add(c);
	}

	public void insertItems(ArrayList<Conversation> c) {
		ConversationList.addAll(c);
	}

//	public void setAdView(MoPubView adView) {
//		this.adView = adView;
//	}

	public int getItemPosition(Conversation conversation) {
		return ConversationList.indexOf(conversation);
	}

	public void removeItem(Conversation conversation) {
		ConversationList.remove(conversation);
	}

	public void clear() {
		ConversationList.clear();
	}

	public ArrayList<Conversation> getDataset() {
		ArrayList<Conversation> conversations = new ArrayList<>();
		for (int i = 0; i < ConversationList.size(); i++) {
			conversations.add(ConversationList.get(i));
		}
		return conversations;
	}

	public void setTyping(Boolean isTyping) {
		int isTypingPosition = ConversationList.indexOf(indicatorConversation);

		if (isTyping) {
			indicatorConversation.addFlag(CityOfTwo.FLAG_TYPING);
		} else {
			indicatorConversation.removeFlag(CityOfTwo.FLAG_TYPING);
		}

		if (isTypingPosition == SortedList.INVALID_POSITION) {
			if (isTyping) ConversationList.add(indicatorConversation);
		} else {
			ConversationList.updateItemAt(isTypingPosition, indicatorConversation);
		}
	}

	public void setLastSeen(Long lastSeen) {
		int lastSeenPosition = ConversationList.indexOf(indicatorConversation);

		indicatorConversation.setTime(lastSeen);
		indicatorConversation.addFlag(CityOfTwo.FLAG_LAST_SEEN);

		if (lastSeenPosition == SortedList.INVALID_POSITION) {
			ConversationList.add(indicatorConversation);
		} else {
			ConversationList.updateItemAt(lastSeenPosition, indicatorConversation);
		}
	}


	protected class ContentHolder extends RecyclerView.ViewHolder {
		TextView dateContainer;
		FrameLayout contentContainer;
		View lineContainer;

		public ContentHolder(View itemView) {
			super(itemView);
//			contentContainer = (FrameLayout) itemView.findViewById(R.id.content_container);
			dateContainer = (TextView) itemView.findViewById(R.id.time);
			lineContainer = itemView.findViewById(line);
		}
	}

	protected class GenericHolder extends RecyclerView.ViewHolder {
		TextView likeList;
		View introContainer;

		public GenericHolder(View itemView) {
			super(itemView);

			likeList = (TextView) itemView.findViewById(R.id.likes_list);
			introContainer = itemView.findViewById(R.id.chat_introduction_view);
		}
	}

	protected class IndicatorHolder extends RecyclerView.ViewHolder {
		ImageView typingIndicator;
		View seenIndicator;
		TextView lastSeen;

		public IndicatorHolder(View itemView) {
			super(itemView);

			typingIndicator = (ImageView) itemView.findViewById(R.id.type_indicator);
			seenIndicator = itemView.findViewById(R.id.seen_indicator);
			lastSeen = (TextView) itemView.findViewById(R.id.seen_time);
		}
	}

	protected class AdHolder extends RecyclerView.ViewHolder {
		ViewGroup adContainer;

		public AdHolder(View itemView) {
			super(itemView);
			adContainer = (ViewGroup) itemView.findViewById(R.id.ad_container);
		}
	}
}
