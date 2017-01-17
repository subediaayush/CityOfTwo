package com.messenger.cityoftwo;

import android.content.Context;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.messenger.cityoftwo.dummy.DummyContent.DummyItem;
import com.truizlop.sectionedrecyclerview.SimpleSectionedAdapter;

import java.util.ArrayList;

import static com.messenger.cityoftwo.ContactAdapterWrapper.ContactsEventListener;

/**
 * {@link RecyclerView.Adapter} that can display a {@link DummyItem} and makes a call to the
 * specified {@link ContactsEventListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class SectionedContactsAdapter extends SimpleSectionedAdapter<ContactHolder> {

	public static final int GUEST_MATCH = 0;
	public static final int GUEST_CONTACT = 1;

	private SortedList<Contact> mDataset;

	private Context mContext;

	private ContactsEventListener mEventListener;

	public SectionedContactsAdapter(Context context) {

		mDataset = new SortedList<>(Contact.class, new SortedList.Callback<Contact>() {
			@Override
			public int compare(Contact o1, Contact o2) {
				int comparison;
				comparison = Contact.FRIEND_COMPARATOR.compare(o1, o2);

				if (comparison == 0) comparison = Contact.MESSAGE_COMPARATOR.compare(o1, o2);
				if (comparison == 0) comparison = Contact.NAME_COMPARATOR.compare(o1, o2);

				return comparison;
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

		mContext = context;
	}


//	@Override
//	public ContactHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//		View view = LayoutInflater.received(parent.getContext())
//				.inflate(R.layout.layout_contact, parent, false);
//		return new ContactHolder(view);
//	}

	@Override
	protected String getSectionHeaderTitle(int section) {
//		if (!isSectioned) return "";
		return section == GUEST_CONTACT ? "Contacts" : "Matches";
	}

	@Override
	public int getItemCount() {
		return mDataset.size();
	}

	@Override
	protected int getSectionCount() {
		return 2;
	}

	@Override
	protected int getItemCountForSection(int section) {
		int counter = 0;
		boolean contactSection = section == GUEST_CONTACT;
		for (int i = 0; i < mDataset.size(); i++)
			if (mDataset.get(i).isFriend == contactSection) counter++;
		return counter;
	}

	@Override
	protected ContactHolder onCreateItemViewHolder(ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(mContext).inflate(
				R.layout.layout_contact,
				parent,
				false
		);
		return new ContactHolder(view);
	}

	@Override
	protected void onBindItemViewHolder(ContactHolder holder, int section, final int position) {
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

	public void setDataset(ArrayList<Contact> contacts) {
		for (int i = mDataset.size() - 1; i >= 0; i--) {
			if (!contacts.contains(mDataset.get(i)))
				mDataset.removeItemAt(i);
		}

		mDataset.addAll(contacts);
	}

	public int insert(Contact c) {
		return mDataset.add(c);
	}

	public void insertAll(ArrayList<Contact> c) {
		mDataset.addAll(c);
	}

	public void clear() {
		mDataset.clear();
	}

	public void clearSection(int section) {
		boolean contactSection = section == GUEST_CONTACT;
		for (int i = mDataset.size() - 1; i >= 0; i++) {
			if (mDataset.get(i).isFriend == contactSection) mDataset.removeItemAt(i);
		}
	}

	public void setEventListener(ContactsEventListener mEventListener) {
		this.mEventListener = mEventListener;
	}

	public Contact get(int position) {
		return mDataset.get(position);
	}
}
