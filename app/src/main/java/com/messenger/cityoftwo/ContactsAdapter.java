package com.messenger.cityoftwo;

import android.content.Context;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.messenger.cityoftwo.dummy.DummyContent.DummyItem;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * {@link RecyclerView.Adapter} that can display a {@link DummyItem} and makes a call to the
 * specified {@link ContactAdapterWrapper.ContactsEventListener}.
 * TODO: Replace the implementation with id for your data type.
 */
public class ContactsAdapter extends RecyclerView.Adapter<ContactHolder> implements ContactsAdapterInterface {

	public static final int GUEST_MATCH = 0;
	public static final int GUEST_CONTACT = 1;

	private SortedList<Contact> mDataset;
	private HashMap<Integer, Message> mMessages;

	private Context mContext;

	private ContactAdapterWrapper.ContactsEventListener mEventListener;

	public ContactsAdapter(Context context) {

		mDataset = new SortedList<>(Contact.class, new SortedList.Callback<Contact>() {
			@Override
			public int compare(Contact o1, Contact o2) {
				int comparison;
				comparison = Contact.MESSAGE_COMPARATOR.compare(o1, o2);

				if (comparison == 0) comparison = Contact.NAME_COMPARATOR.compare(o1, o2);

				return comparison;
			}

			@Override
			public void onChanged(int position, int count) {
				notifyItemRangeChanged(position, count);
			}

			@Override
			public boolean areContentsTheSame(Contact oldItem, Contact newItem) {
				return oldItem.name.equals(newItem.name) &&
						oldItem.nickName.equals(newItem.nickName);
			}

			@Override
			public boolean areItemsTheSame(Contact item1, Contact item2) {
				return item1.id.equals(item2.id);
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

		mContext = context;
	}


//	@Override
//	public ContactHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//		View view = LayoutInflater.received(parent.getContext())
//				.inflate(R.layout.layout_contact, parent, false);
//		return new ContactHolder(view);
//	}

	@Override
	public ContactHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(mContext).inflate(
				R.layout.layout_contact,
				parent,
				false
		);
		return new ContactHolder(view);
	}

	@Override
	public void onBindViewHolder(ContactHolder holder, final int position) {
		Contact contact = mDataset.get(position);
		if (contact.nickName.isEmpty()) {
			holder.name.setText(contact.name);
		} else {
			holder.name.setText(contact.nickName);
		}

		holder.itemView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mEventListener != null) mEventListener.onProfileViewed(position);
			}
		});

//		Picasso.with(mContext)
//				.load(contact.icon)
//				.into(holder.icon);

//		holder.mItem = mValues.get(position);
//		holder.mIdView.setText(mValues.get(position).id);
//		holder.mContentView.setText(mValues.get(position).content);
//
//		holder.mView.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				if (null != mListener) {
		// Notify the active callbacks interface (the activity, if the
		// fragment is attached to one) that an item has been selected.
//					mListener.onListFragmentInteraction(holder.mItem);
//				}
//			}
//		});
	}

	@Override
	public int getItemCount() {
		return mDataset.size();
	}

	@Override
	public void setDataset(ArrayList<Contact> contacts) {
		for (int i = mDataset.size() - 1; i >= 0; i--) {
			if (!contacts.contains(mDataset.get(i)))
				mDataset.removeItemAt(i);
		}

		mDataset.addAll(contacts);
	}

	@Override
	public int insert(Contact c) {
		return mDataset.add(c);
	}

	@Override
	public void update(Contact c) {
		int i = mDataset.indexOf(c);
		if (i != SortedList.INVALID_POSITION) mDataset.updateItemAt(i, c);
	}

	@Override
	public void insertAll(ArrayList<Contact> c) {
		mDataset.addAll(c);
	}

	@Override
	public void clear() {
		mDataset.clear();
	}

	@Override
	public void setEventListener(ContactAdapterWrapper.ContactsEventListener mEventListener) {
		this.mEventListener = mEventListener;
	}

	@Override
	public Contact get(int position) {
		return mDataset.get(position);
	}


}
