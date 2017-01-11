package com.messenger.cityoftwo;

import android.content.Context;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.messenger.cityoftwo.dummy.DummyContent.DummyItem;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * {@link RecyclerView.Adapter} that can display a {@link DummyItem} and makes a call to the
 * specified {@link ContactsEventListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class ContactsAdapter extends CardAdapterBase<ContactsAdapter.ContactHolder> {

	private SortedList<Contact> mDataset;

	private ContactsEventListener mEventListener;

	public ContactsAdapter(Context context) {
		super(context, 4);
		mDataset = new SortedList<>(Contact.class, new SortedList.Callback<Contact>() {
			@Override
			public int compare(Contact o1, Contact o2) {
				return o1.name.compareTo(o2.name);
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
			public boolean areContentsTheSame(Contact oldItem, Contact newItem) {
				return oldItem.getName().equals(newItem.getName());
			}

			@Override
			public boolean areItemsTheSame(Contact item1, Contact item2) {
				return item1.getCode().equals(item2.getCode());
			}
		});
	}


	@Override
	public ContactHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext())
				.inflate(R.layout.layout_contact, parent, false);
		return new ContactHolder(view);
	}

	@Override
	public int getItemCount() {
		return mDataset.size();
	}

	@Override
	public void onBindViewHolder(final ContactHolder holder, int position) {
		Contact contact = mDataset.get(position);
		if (contact.nickName.isEmpty()) {
			holder.name.setText(contact.name);
		} else {
			holder.name.setText(contact.nickName);
		}

		Picasso.with(mContext)
				.load(contact.icon)
				.into(holder.icon);

//		holder.mItem = mValues.get(position);
//		holder.mIdView.setText(mValues.get(position).code);
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
	void onBindHolder(ContactHolder holder, int position) {

	}

	public void setDataset(ArrayList<Contact> contacts) {
		for (int i = mDataset.size() - 1; i >= 0; i--) {
			if (!contacts.contains(mDataset.get(i)))
				mDataset.removeItemAt(i);
		}

		mDataset.addAll(contacts);
	}

	public void setEventListener(ContactsEventListener mEventListener) {
		this.mEventListener = mEventListener;
	}

	public interface ContactsEventListener {
		void onProfileViewed(int position);
	}

	public class ContactHolder extends RecyclerView.ViewHolder {

		ImageView icon;
		TextView name;

		public ContactHolder(View view) {
			super(view);

			icon = (ImageView) view.findViewById(R.id.contact_icon);
			name = (TextView) view.findViewById(R.id.contact_name);
		}
	}
}
