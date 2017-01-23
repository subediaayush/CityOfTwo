package com.messenger.cityoftwo;

import android.content.Context;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.messenger.cityoftwo.dummy.DummyContent.DummyItem;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * {@link RecyclerView.Adapter} that can display a {@link DummyItem} and makes a call to the
 * specified {@link com.messenger.cityoftwo.ContactAdapterWrapper.ContactsEventListener}.
 * TODO: Replace the implementation with id for your data type.
 */
public class RequestAdapter extends CardAdapterBase<InboxAdapter.MessageHolder> {
	SortedList<Message> mDataset;

	private RequestsEventListener mEventListener;

	private HashSet<Integer> loadingViews;

	public RequestAdapter(Context context) {
		super(context, 8);

		mDataset = new SortedList<>(Message.class, new SortedList.Callback<Message>() {
			@Override
			public int compare(Message o1, Message o2) {
				return o1.getId().compareTo(o2.getId());
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
	public InboxAdapter.MessageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext())
				.inflate(R.layout.layout_request, parent, false);
		return new InboxAdapter.MessageHolder(view);
	}

	@Override
	public int getItemCount() {
		return mDataset.size();
	}

	@Override
	void onBindHolder(final InboxAdapter.MessageHolder holder, int position) {
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
					mEventListener.onRequestDecline(holder.getAdapterPosition());
				}

			}
		});

		holder.reply.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mEventListener != null) {
					mEventListener.onRequestAccept(holder.getAdapterPosition());
				}

			}
		});
		holder.itemView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mEventListener != null) {
					mEventListener.onProfileViewed(holder.getAdapterPosition());
				}
			}
		});
	}

	public void setLoading(int position) {
		loadingViews.add(position);
		notifyItemChanged(position);
	}

	public void removeLoading(int position) {
		loadingViews.remove(position);
		notifyItemChanged(position);
	}

	public void setDataset(ArrayList<Message> messages) {
		for (int i = mDataset.size() - 1; i >= 0; i--) {
			if (!messages.contains(mDataset.get(i)))
				mDataset.removeItemAt(i);
		}

		mDataset.addAll(messages);
	}

	public void setEventListener(RequestsEventListener mEventListener) {
		this.mEventListener = mEventListener;
	}

	public interface RequestsEventListener {
		void onRequestAccept(int position);

		void onRequestDecline(int position);

		void onProfileViewed(int position);

	}
}
