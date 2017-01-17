package com.messenger.cityoftwo;

import android.content.Context;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Aayush on 1/6/2017.
 */

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	public static final int MODE_CHAT = 0;
	public static final int MODE_PROFILE = 1;

	private Context mContext;
	private SortedList<Conversation> mConversationList;

	private Contact mGuest;
	private AdapterListener adapterListener;
	private int mode;

	public ChatAdapter(Context context, Contact guest) {

		mContext = context;

		mGuest = guest;

		mConversationList = new SortedList<>(Conversation.class, new SortedList.Callback<Conversation>() {
			@Override
			public int compare(Conversation o1, Conversation o2) {
				return Conversation.CONVERSATION_COMPARATOR.compare(o1, o2);
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
		});
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

		if ((viewType & CityOfTwo.FLAG_START) == CityOfTwo.FLAG_START) {
			View view = LayoutInflater.from(mContext)
					.inflate(R.layout.layout_msg_start, parent, false);
			return new ProfileViewHolder(view);
		}

		if ((viewType & CityOfTwo.FLAG_TEXT) == CityOfTwo.FLAG_TEXT) {
			if ((viewType & CityOfTwo.FLAG_SENT) == CityOfTwo.FLAG_SENT) {
				View view = LayoutInflater.from(mContext)
						.inflate(R.layout.layout_msg_sent, parent, false);
				return new TextViewHolder(view);
			}
			if ((viewType & CityOfTwo.FLAG_RECEIVED) == CityOfTwo.FLAG_RECEIVED) {
				View view = LayoutInflater.from(mContext)
						.inflate(R.layout.layout_msg_received, parent, false);
				return new TextViewHolder(view);
			}
		}

		if ((viewType & CityOfTwo.FLAG_PROFILE) == CityOfTwo.FLAG_PROFILE) {
			if ((viewType & CityOfTwo.FLAG_SENT) == CityOfTwo.FLAG_SENT) {
				View view = LayoutInflater.from(mContext)
						.inflate(R.layout.layout_profile_sent, parent, false);
				return new RevealViewHolder(view);
			}
			if ((viewType & CityOfTwo.FLAG_RECEIVED) == CityOfTwo.FLAG_RECEIVED) {
				View view = LayoutInflater.from(mContext)
						.inflate(R.layout.layout_profile_received, parent, false);
				return new RevealViewHolder(view);
			}
		}

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

	private void handleRevealItem(RevealViewHolder holder, int position, int flags) {
		if ((flags & CityOfTwo.FLAG_RECEIVED) == CityOfTwo.FLAG_RECEIVED) {
			mGuest.hasRevealed = true;

			Picasso.with(mContext)
					.load(getFBProfilePicture(mGuest))
					.into(holder.image);

			notifyItemRangeChanged(0, mConversationList.size());
		}

		holder.name.setText(mGuest.name);
	}

	private String getFBProfilePicture(Contact mGuest) {
		return null;
	}

	private void handleTextItem(TextViewHolder holder, int position, int flags) {
		Conversation c = mConversationList.get(position);

		if ((flags & CityOfTwo.FLAG_RECEIVED) == CityOfTwo.FLAG_RECEIVED) {
			Picasso.with(mContext)
					.load(
							mGuest.hasRevealed ? getFBProfilePicture(mGuest) : getRandomImage()
					)
					.into(holder.image);
		}

		holder.text.setText(c.getText());

		String messageTime = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(c.getTime());
		holder.time.setText(messageTime);
	}

	private String getRandomImage() {
		return null;
	}

	private void handleProfileItem(ProfileViewHolder holder, int position, int flags) {
		switch (mode) {
			case MODE_CHAT:

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

	public void setAdapterListener(AdapterListener listener) {
		this.adapterListener = listener;
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
		this.mode = mode;
	}

	public interface AdapterListener {
	}

	private class TextViewHolder extends RecyclerView.ViewHolder {

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

		Button message;
		Button save;
		Button reveal;
		Button facebook;
		Button likes;
		Button block;

		View optionsContainer;

		public ProfileViewHolder(View itemView) {
			super(itemView);

			name = (TextView) itemView.findViewById(R.id.profile_name);
			nickname = (TextView) itemView.findViewById(R.id.profile_nickname);
			image = (CircleImageView) itemView.findViewById(R.id.icon);
			url = (TextView) itemView.findViewById(R.id.profile_url);
			commonLikes = (TextView) itemView.findViewById(R.id.profile_commonlikes);

			message = (Button) itemView.findViewById(R.id.message);
			save = (Button) itemView.findViewById(R.id.save);
			reveal = (Button) itemView.findViewById(R.id.reveal);
			facebook = (Button) itemView.findViewById(R.id.profile);
			likes = (Button) itemView.findViewById(R.id.likes);
			block = (Button) itemView.findViewById(R.id.block);

			optionsContainer = itemView.findViewById(R.id.profile_options_container);
		}
	}

	private class RevealViewHolder extends RecyclerView.ViewHolder {
		TextView name;
		CircleImageView image;

		public RevealViewHolder(View itemView) {
			super(itemView);

			name = (TextView) itemView.findViewById(R.id.profile_name);
			image = (CircleImageView) itemView.findViewById(R.id.icon);
		}
	}
}
