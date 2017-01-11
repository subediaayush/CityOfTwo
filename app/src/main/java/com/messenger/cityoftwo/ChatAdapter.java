package com.messenger.cityoftwo;

import android.content.Context;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * Created by Aayush on 1/6/2017.
 */

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private Context mContext;
	private SortedList<Conversation> mConversationList;

	public ChatAdapter(Context context) {

		mContext = context;

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
			if ((viewType & CityOfTwo.FLAG_SENT) == CityOfTwo.FLAG_SENT) {
				View view = LayoutInflater.from(mContext)
						.inflate(R.layout.layout_msg_received, parent, false);
				return new TextViewHolder(view);
			}
		}

		if ((viewType & CityOfTwo.FLAG_PROFILE) == CityOfTwo.FLAG_PROFILE) {
			if ((viewType & CityOfTwo.FLAG_TEXT) == CityOfTwo.FLAG_TEXT) {
				if ((viewType & CityOfTwo.FLAG_SENT) == CityOfTwo.FLAG_SENT) {
					View view = LayoutInflater.from(mContext)
							.inflate(R.layout.layout_profile_sent, parent, false);
					return new RevealViewHolder(view);
				}
				if ((viewType & CityOfTwo.FLAG_SENT) == CityOfTwo.FLAG_SENT) {
					View view = LayoutInflater.from(mContext)
							.inflate(R.layout.layout_profile_received, parent, false);
					return new RevealViewHolder(view);
				}
			}
		}

		return new GenericViewHolder(LayoutInflater.from(mContext).inflate(
				R.layout.layout_msg_empty, parent, false
		));
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

	}

	@Override
	public int getItemViewType(int position) {
		return super.getItemViewType(position);
	}

	@Override
	public int getItemCount() {
		return mConversationList.size();
	}

	public int insert(Conversation c) {
		return mConversationList.add(c);
	}

	public void insertAll(ArrayList<Conversation> conversations) {
		mConversationList.addAll(conversations);
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

	private class TextViewHolder extends RecyclerView.ViewHolder {
		public TextViewHolder(View itemView) {
			super(itemView);
		}
	}

	private class GenericViewHolder extends RecyclerView.ViewHolder {
		public GenericViewHolder(View itemView) {
			super(itemView);
		}
	}

	private class ProfileViewHolder extends RecyclerView.ViewHolder {
		public ProfileViewHolder(View itemView) {
			super(itemView);
		}
	}

	private class RevealViewHolder extends RecyclerView.ViewHolder {
		public RevealViewHolder(View itemView) {
			super(itemView);
		}
	}
}
