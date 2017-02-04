package com.messenger.cityoftwo;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Aayush on 1/6/2017.
 */

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	public static final int STATUS_PENDING = 0;
	public static final int STATUS_SENT = 1;
	public static final int STATUS_SEEN = 2;
	public static final int STATUS_ERROR = -1;

	private final String TAG = "ChatAdapter";

	private Context mContext;
	private SortedList<Conversation> mConversationList;

	private HashSet<Integer> mSentMessages;
	private HashSet<Integer> mErrorMessages;

	private int mLastSeen;

	private Contact mGuest;
	private int mChatroomId;

	private Boolean requestResponse = null;

	private ChatEventListener mEventListener;

	public ChatAdapter(Context context, Contact guest, int chatroomId) {

		mContext = context;

		mGuest = guest;
		mChatroomId = chatroomId;

		mLastSeen = -1;

		mSentMessages = new HashSet<>();
		mErrorMessages = new HashSet<>();

		mConversationList = new SortedList<>(Conversation.class, new SortedList.Callback<Conversation>() {
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
						item1.getTime() == item2.getTime() &&
						item1.getFlags().equals(item2.getFlags());
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
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

		if ((viewType & CityOfTwo.FLAG_START) == CityOfTwo.FLAG_START) {
			View view = LayoutInflater.from(mContext)
					.inflate(R.layout.layout_msg_start, parent, false);
			Log.i(TAG, "Assigning Profile holder. FLAG: " + Integer.toBinaryString(viewType));
			return new ProfileViewHolder(view);
		}

		if ((viewType & CityOfTwo.FLAG_REQUEST) == CityOfTwo.FLAG_REQUEST) {
			View view = LayoutInflater.from(mContext)
					.inflate(R.layout.layout_msg_request, parent, false);
			Log.i(TAG, "Assigning Profile holder. FLAG: " + Integer.toBinaryString(viewType));
			return new RequestViewHolder(view);
		}

		if ((viewType & CityOfTwo.FLAG_TEXT) == CityOfTwo.FLAG_TEXT) {
			if ((viewType & CityOfTwo.FLAG_SENT) == CityOfTwo.FLAG_SENT) {
				View view = LayoutInflater.from(mContext)
						.inflate(R.layout.layout_msg_sent, parent, false);
				Log.i(TAG, "Assigning Text (Sent) holder. FLAG: " + Integer.toBinaryString(viewType));
				return new TextViewHolder(view);
			}
			if ((viewType & CityOfTwo.FLAG_RECEIVED) == CityOfTwo.FLAG_RECEIVED) {
				View view = LayoutInflater.from(mContext)
						.inflate(R.layout.layout_msg_received, parent, false);
				Log.i(TAG, "Assigning Text (Recv) holder. FLAG: " + Integer.toBinaryString(viewType));
				return new TextViewHolder(view);
			}
		}

		if ((viewType & CityOfTwo.FLAG_PROFILE) == CityOfTwo.FLAG_PROFILE) {
			if ((viewType & CityOfTwo.FLAG_SENT) == CityOfTwo.FLAG_SENT) {
				View view = LayoutInflater.from(mContext)
						.inflate(R.layout.layout_profile_sent, parent, false);
				Log.i(TAG, "Assigning Reveal (Sent) holder. FLAG: " + Integer.toBinaryString(viewType));
				return new RevealViewHolder(view);
			}
			if ((viewType & CityOfTwo.FLAG_RECEIVED) == CityOfTwo.FLAG_RECEIVED) {
				View view = LayoutInflater.from(mContext)
						.inflate(R.layout.layout_profile_received, parent, false);
				Log.i(TAG, "Assigning Reveal (Recv)holder. FLAG: " + Integer.toBinaryString(viewType));
				return new RevealViewHolder(view);
			}
		}

		Log.i(TAG, "No holder found. Assigning Empty holder. FLAG: " + Integer.toBinaryString(viewType));

		return new GenericViewHolder(LayoutInflater.from(mContext).inflate(
				R.layout.layout_msg_empty, parent, false
		));
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		int flags = holder.getItemViewType();

		if ((flags & CityOfTwo.FLAG_START) == CityOfTwo.FLAG_START) {
			handleProfileItem((ProfileViewHolder) holder, position, flags);
		} else if ((flags & CityOfTwo.FLAG_TEXT) == CityOfTwo.FLAG_TEXT) {
			handleTextItem((TextViewHolder) holder, position, flags);
		} else if ((flags & CityOfTwo.FLAG_PROFILE) == CityOfTwo.FLAG_PROFILE) {
			handleRevealItem((RevealViewHolder) holder, position, flags);
		} else if ((flags & CityOfTwo.FLAG_REQUEST) == CityOfTwo.FLAG_REQUEST) {
			handleRequestItem((RequestViewHolder) holder, position, flags);
		}
	}

	@Override
	public int getItemViewType(int position) {
		return mConversationList.get(position).getFlags();
	}

	@Override
	public int getItemCount() {
		return mConversationList.size();
	}

	private void handleRequestItem(final RequestViewHolder holder, int position, int flags) {
		String message;
		if ((requestResponse == null) && ((flags & CityOfTwo.FLAG_RECEIVED) == CityOfTwo.FLAG_RECEIVED)) {
			message = mGuest.nickName + " wants to add you as a contact.";

			holder.response.setVisibility(View.VISIBLE);

			holder.accept.setEnabled(true);
			holder.decline.setEnabled(true);

			holder.accept.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					holder.accept.setEnabled(false);
					holder.decline.setEnabled(false);
					if (mEventListener != null) mEventListener.onAcceptRequest(true);
				}
			});

			holder.decline.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					holder.accept.setEnabled(false);
					holder.decline.setEnabled(false);
					if (mEventListener != null) mEventListener.onAcceptRequest(false);
				}
			});
		} else {
			holder.response.setVisibility(View.GONE);

			if ((flags & CityOfTwo.FLAG_SENT) == CityOfTwo.FLAG_SENT) {
				message = "You sent " + mGuest.nickName + " a request.";
			} else {
				String response = requestResponse ? " accepted " : " declined ";
				message = "You" + response + mGuest.nickName + "'s request.";
			}
		}
		holder.request.setText(message);
	}

	private void handleRevealItem(final RevealViewHolder holder, final int position, int flags) {
		final String fbid;
		if ((flags & CityOfTwo.FLAG_SENT) == CityOfTwo.FLAG_SENT) {
			SharedPreferences sp = mContext.getSharedPreferences(CityOfTwo.PACKAGE_NAME, Context.MODE_PRIVATE);
			fbid = sp.getString(CityOfTwo.KEY_FBID, "");
		} else {
			fbid = mGuest.fid;

			if (!mGuest.hasRevealed) {
				mGuest.hasRevealed = true;
				notifyItemRangeChanged(0, mConversationList.size());
			}
		}

		new FacebookHelper(mContext, fbid, mChatroomId, "name") {
			@Override
			public void onResponse(String response) {
				mGuest.name = response;
			}

			@Override
			public void onError() {
				mGuest.name = "Facebook User";
			}
		}.execute();

		FacebookHelper.loadFacebookProfilePicture(mContext, fbid, mChatroomId, holder.image);

		holder.name.setText(mGuest.name);

		if (Conversation.containsFlag(flags, CityOfTwo.FLAG_SENT)) setItemStatus(holder, position);

		holder.itemView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mEventListener != null) mEventListener.onViewProfile(fbid);
			}
		});
	}

	private void handleTextItem(TextViewHolder holder, final int position, int flags) {
		Conversation c = mConversationList.get(position);

		if ((flags & CityOfTwo.FLAG_RECEIVED) == CityOfTwo.FLAG_RECEIVED) {
			int nextFlag = mConversationList.get(position + 1).getFlags();
			if ((nextFlag & CityOfTwo.FLAG_RECEIVED) != CityOfTwo.FLAG_RECEIVED) {

				if (mGuest.hasRevealed)
					FacebookHelper.loadFacebookProfilePicture(mContext, mGuest.fid, mChatroomId, holder.image);
				else Utils.loadRandomPicture(mContext, mGuest.id, holder.image);

				holder.image.setVisibility(View.VISIBLE);
			} else {
				holder.image.setVisibility(View.INVISIBLE);
			}
		}

		holder.text.setText(c.getText());

		String messageTime;
		messageTime = Utils.humanizeDateTime(c.getTime());

		holder.time.setText(messageTime);

		if (c.containsFlag(CityOfTwo.FLAG_SENT)) setItemStatus(holder, position);

		holder.text.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mEventListener != null)
					mEventListener.onConversationClicked(position, getStatus(position));
			}
		});
	}

	private void setItemStatus(StatusViewHolder holder, int position) {
		int status = getStatus(position);

		@ColorInt
		int color = Color.TRANSPARENT;
		if (status == STATUS_SEEN && position == mLastSeen) color = Color.GREEN;
		else if (status == STATUS_ERROR) color = Color.RED;
		else if (status == STATUS_SENT) color = Color.YELLOW;

		holder.status.setBackgroundColor(color);
	}

	private int getStatus(int position) {
		if (mErrorMessages.contains(position)) return STATUS_ERROR;
		if (position <= mLastSeen) return STATUS_SEEN;
		if (mSentMessages.contains(position)) return STATUS_SENT;

		return STATUS_PENDING;
	}

	public void setRequestResponse(Boolean requestResponse) {
		this.requestResponse = requestResponse;
		notifyItemRangeChanged(0, getItemCount());
	}

	private void handleProfileItem(ProfileViewHolder holder, int position, int flags) {

		if (!mGuest.hasRevealed) {
			holder.name.setVisibility(View.GONE);
			holder.url.setVisibility(View.GONE);

			Utils.loadRandomPicture(mContext, mGuest.id, holder.image);
		} else {
			holder.name.setVisibility(View.VISIBLE);
			holder.url.setVisibility(View.VISIBLE);

			holder.name.setText(mGuest.name);
			holder.url.setText(mGuest.name + " at fb.com");
			holder.url.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (mEventListener != null) mEventListener.onViewProfile(mGuest.fid);
				}
			});

			FacebookHelper.loadFacebookProfilePicture(mContext, mGuest.fid, mChatroomId, holder.image);
		}

		if (mGuest.nickName.isEmpty()) {
			holder.nickname.setVisibility(View.GONE);
		} else {
			holder.nickname.setVisibility(View.VISIBLE);
			holder.nickname.setText(mGuest.nickName);
		}

		if (mGuest.status.isEmpty()) {
			holder.status.setVisibility(View.GONE);
		} else {
			holder.status.setVisibility(View.VISIBLE);
			holder.status.setText(mGuest.status);
		}

		String[] likes = mGuest.topLikes;

		String commonLikesMessages = "Likes " +
				Utils.getReadableList(likes) + " and " +
				(mGuest.commonLikes - likes.length) + " others";

		holder.commonLikes.setText(commonLikesMessages);

		holder.reveal.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mEventListener != null) mEventListener.onRevealContact();
			}
		});

		holder.likes.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mEventListener != null) mEventListener.onViewCommonLikes();
			}
		});

		if (mGuest.isFriend) {
			holder.save.setText("Remove from contacts");
			holder.save.setIcon(R.drawable.ic_unsave);
			holder.save.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (mEventListener != null) mEventListener.onRemoveContact();
				}
			});
		} else {
			holder.save.setText("Save as Contact");
			holder.save.setIcon(R.drawable.ic_send_request);
			holder.save.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (mEventListener != null) mEventListener.onSaveContact();
				}
			});
		}
	}

	public int insert(Conversation c) {
		return mConversationList.add(c);
	}

	public void insertAll(ArrayList<Conversation> conversations) {
		if (conversations != null) mConversationList.addAll(conversations);
	}

	public Conversation removeAt(int position) {
		return mConversationList.removeItemAt(position);
	}

	public boolean remove(Conversation c) {
		return mConversationList.remove(c);
	}

	public void clear() {
		mConversationList.clear();
	}

	public void setGuest(Contact guest) {
		this.mGuest = guest;
	}

	public ArrayList<Conversation> getDataset() {
		ArrayList<Conversation> list = new ArrayList<>();

		for (int i = 0; i < mConversationList.size(); i++)
			list.add(mConversationList.get(i));

		return list;
	}

	public void setMode(int mode) {
	}

	public void setChatEventListener(ChatEventListener c) {
		mEventListener = c;
	}

	public void setLastSeen(long lastSeen) {
		Conversation lastSeenWrapper = new Conversation("last_seen", CityOfTwo.FLAG_LAST_SEEN, lastSeen);

		int chatLength = getItemCount();

		int latestIndex;

		for (latestIndex = mLastSeen == -1 ? 0 : mLastSeen; latestIndex < chatLength - 1; latestIndex++) {
			Conversation c = mConversationList.get(latestIndex);
			int comp = Conversation.TIME_COMPARATOR.compare(lastSeenWrapper, c);

			if (c.containsFlag(CityOfTwo.FLAG_SENT)) {
				setStatus(latestIndex, STATUS_SEEN);
			}

			if (comp == -1) break;
		}

		latestIndex--;
		if (latestIndex > -1) {
			Conversation c = mConversationList.get(latestIndex);

			if (!c.containsFlag(CityOfTwo.FLAG_SENT | CityOfTwo.FLAG_TEXT)) {
				for (int j = latestIndex; j > 0; j--) {
					Conversation c_ = mConversationList.get(j);
					if (c_.containsFlag(CityOfTwo.FLAG_SENT | CityOfTwo.FLAG_TEXT)) {
						latestIndex = j;
						break;
					}
				}
			}
		}

		if (mLastSeen != latestIndex) {
			mLastSeen = latestIndex;
			notifyItemRangeChanged(0, mLastSeen + 1);
		}

		Log.d(TAG, "Last seen item set: " + mLastSeen);

	}

	public void setStatus(int index, int status) {
		if (status == STATUS_ERROR) {
			mErrorMessages.add(index);
			mSentMessages.remove(index);
		} else if (status == STATUS_SENT) {
			mSentMessages.add(index);
			mErrorMessages.remove(index);
		} else if (status == STATUS_SEEN || status == STATUS_PENDING) {
			mSentMessages.remove(index);
			mErrorMessages.remove(index);
		}

		notifyItemChanged(index);
	}

	public Conversation get(int i) {
		return mConversationList.get(i);
	}


	public interface ChatEventListener {
		void onConversationClicked(int postion, int status);

		void onEditNickname();

		void onSendMessage();

		void onSaveContact();

		void onRevealContact();

		void onViewProfile(String fbid);

		void onViewCommonLikes();

		void onBlockProfile();

		void onAcceptRequest(boolean accept);

		void onRemoveContact();
	}

	/**
	 * Created by Aayush on 1/25/2017.
	 */
	public static class RequestViewHolder extends RecyclerView.ViewHolder {

		TextView request;
		View response;

		Button accept;
		Button decline;

		public RequestViewHolder(View itemView) {
			super(itemView);

			request = (TextView) itemView.findViewById(R.id.request_message);

			response = itemView.findViewById(R.id.request_response);

			accept = (Button) itemView.findViewById(R.id.request_accept);
			decline = (Button) itemView.findViewById(R.id.request_decline);
		}
	}

	private class TextViewHolder extends StatusViewHolder {

		TextView time;
		TextView text;
		CircleImageView image;

		public TextViewHolder(View itemView) {
			super(itemView);

			time = (TextView) itemView.findViewById(R.id.time);
			text = (TextView) itemView.findViewById(R.id.message_text);

			image = (CircleImageView) itemView.findViewById(R.id.icon);
		}
	}

	private class RevealViewHolder extends StatusViewHolder {
		TextView name;
		CircleImageView image;

		public RevealViewHolder(View itemView) {
			super(itemView);

			name = (TextView) itemView.findViewById(R.id.profile_name);
			image = (CircleImageView) itemView.findViewById(R.id.profile_icon);
		}
	}

	private class StatusViewHolder extends RecyclerView.ViewHolder {

		ImageView status;

		public StatusViewHolder(View itemView) {
			super(itemView);

			status = (ImageView) itemView.findViewById(R.id.status);
		}
	}
	private class GenericViewHolder extends RecyclerView.ViewHolder {
		public GenericViewHolder(View itemView) {
			super(itemView);
		}
	}

	private class ProfileViewHolder extends RecyclerView.ViewHolder {

		TextView name;
		TextView nickname;
		CircleImageView image;
		TextView status;
		TextView url;
		TextView commonLikes;

		OptionButtonHolder save;
		OptionButtonHolder reveal;
		OptionButtonHolder likes;

		View optionsContainer;

		public ProfileViewHolder(View itemView) {
			super(itemView);

			name = (TextView) itemView.findViewById(R.id.profile_name);
			nickname = (TextView) itemView.findViewById(R.id.profile_nickname);
			image = (CircleImageView) itemView.findViewById(R.id.icon);
			url = (TextView) itemView.findViewById(R.id.profile_url);
			status = (TextView) itemView.findViewById(R.id.profile_status);
			commonLikes = (TextView) itemView.findViewById(R.id.profile_commonlikes);

			save = new OptionButtonHolder(itemView.findViewById(R.id.save));
			reveal = new OptionButtonHolder(itemView.findViewById(R.id.reveal));
			likes = new OptionButtonHolder(itemView.findViewById(R.id.likes));

			optionsContainer = itemView.findViewById(R.id.profile_options_container);
		}
	}
}
