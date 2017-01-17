package com.messenger.cityoftwo;

import android.content.Context;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by Aayush on 10/8/2016.
 */

public class InboxAdapter extends CardAdapterBase<InboxAdapter.MessageHolder> {

	private SortedList<Message> mDataset;

	private InboxEventListener mEventListener;

	private HashSet<Integer> loadingViews;

	public InboxAdapter(Context context) {
		super(context, 8);

		mDataset = new SortedList<>(Message.class, new SortedList.Callback<Message>() {
			@Override
			public int compare(Message o1, Message o2) {
				return o1.getId().compareTo(o2.getId());
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
			public boolean areContentsTheSame(Message oldItem, Message newItem) {
				return oldItem.getText().equals(newItem.getText());
			}

			@Override
			public boolean areItemsTheSame(Message item1, Message item2) {
				return item1.getId().equals(item2.getId());
			}
		});

		loadingViews = new HashSet<>();
	}

	@Override
	public MessageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext())
				.inflate(R.layout.layout_offline_message, parent, false);
		return new InboxAdapter.MessageHolder(view);
	}

	@Override
	public int getItemCount() {
		return mDataset.size();
	}

	@Override
	void onBindHolder(final MessageHolder holder, int position) {
		Message message = mDataset.get(position);
//		if (message.received.nickName.isEmpty()) {
//			holder.name.setText(message.received.name);
//		} else {
//			holder.name.setText(message.received.nickName);
//		}

		if (loadingViews.contains(position)) {
			holder.background.setVisibility(View.INVISIBLE);
			holder.loader.setVisibility(View.VISIBLE);
		} else {
			holder.background.setVisibility(View.VISIBLE);
			holder.loader.setVisibility(View.INVISIBLE);
		}

//		Picasso.with(mContext)
//				.load(message.received.icon)
//				.into(holder.icon);

		holder.message.setText(message.text);

		holder.delete.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mEventListener != null) {
					mEventListener.onMessageDelete(holder.getAdapterPosition(), holder.icon);
				}

			}
		});

		holder.reply.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mEventListener != null) {
					mEventListener.onMessageReply(holder.getAdapterPosition(), holder.icon);
				}

			}
		});

		holder.itemView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mEventListener != null) {
					mEventListener.onProfileViewed(holder.getAdapterPosition(), holder.icon);
				}
			}
		});

	}

	public Message get(int position) {
		return mDataset.get(position);
	}

	public void setDataset(ArrayList<Message> messages) {
		for (int i = mDataset.size() - 1; i >= 0; i--) {
			if (!messages.contains(mDataset.get(i)))
				mDataset.removeItemAt(i);
		}

		mDataset.addAll(messages);
	}

	public void remove(int position) {
		mDataset.removeItemAt(position);
	}

	public void setLoading(int position) {
		loadingViews.add(position);
		notifyItemChanged(position);
	}

	public void removeLoading(int position) {
		loadingViews.remove(position);
		notifyItemChanged(position);
	}

	public void setEventListener(InboxEventListener mEventListener) {
		this.mEventListener = mEventListener;
	}

	/**
	 * Created by Aayush on 1/3/2017.
	 */
	public interface InboxEventListener {
		void onMessageDelete(int position, View rootView);

		void onMessageReply(int position, View rootView);

		void onProfileViewed(int position, View rootView);
	}

	/**
	 * Created by Aayush on 10/8/2016.
	 */
	public static class MessageHolder extends RecyclerView.ViewHolder {

		ImageView icon;
		TextView name;
		TextView message;
		Button delete;
		Button reply;

		View background;
		View loader;

		public MessageHolder(View itemView) {
			super(itemView);

			icon = (ImageView) itemView.findViewById(R.id.contact_icon);
			name = (TextView) itemView.findViewById(R.id.contact_name);
			message = (TextView) itemView.findViewById(R.id.message);
			delete = (Button) itemView.findViewById(R.id.delete);
			reply = (Button) itemView.findViewById(R.id.reply);

			background = itemView.findViewById(R.id.background);
			loader = itemView.findViewById(R.id.loader);
		}
	}
}
