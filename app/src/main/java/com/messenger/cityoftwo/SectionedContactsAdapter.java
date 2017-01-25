package com.messenger.cityoftwo;

import android.content.Context;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.messenger.cityoftwo.dummy.DummyContent.DummyItem;
import com.truizlop.sectionedrecyclerview.HeaderViewHolder;
import com.truizlop.sectionedrecyclerview.SimpleSectionedAdapter;

import java.util.ArrayList;
import java.util.Comparator;

import static com.messenger.cityoftwo.ContactAdapterWrapper.ContactsEventListener;

/**
 * {@link RecyclerView.Adapter} that can display a {@link DummyItem} and makes a call to the
 * specified {@link ContactsEventListener}.
 * TODO: Replace the implementation with id for your data type.
 */
public class SectionedContactsAdapter extends SimpleSectionedAdapter<ContactHolder> implements ContactsAdapterInterface {

	public static final String GUEST_MATCH = "Matches";
	public static final String GUEST_CONTACT = "Contacts";
	private static final String TAG = "SectionedContactAdapter";
	private final Comparator<Contact> mDataComparator =
			new Comparator<Contact>() {
				@Override
				public int compare(Contact o1, Contact o2) {
					int comparison;
					comparison = Contact.FRIEND_COMPARATOR.compare(o1, o2);

					if (comparison == 0) comparison = Contact.MESSAGE_COMPARATOR.compare(o1, o2);
					if (comparison == 0) comparison = Contact.NAME_COMPARATOR.compare(o1, o2);

					return comparison;

				}

			};

	//	private SimpleSectionedAdapter.<Integer, Integer> mDataSection;
	private SortedList<Contact> mDataset;
	private ArrayList<String> mSections;
	private Context mContext;
	private ContactsEventListener mEventListener;

	public SectionedContactsAdapter(Context context) {

//		mDataset = new ArrayList<>();
		mSections = new ArrayList<>();

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
			public void onChanged(int position, int count) {
				notifyItemRangeChanged(position, count);
			}

			@Override
			public boolean areContentsTheSame(Contact oldItem, Contact newItem) {
				return oldItem.name.equals(newItem.name) &&
						oldItem.lastMessages.size() == newItem.lastMessages.size() &&
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

	protected int getSectionCount() {
		return mSections.size();
	}

	protected int getItemCountForSection(int section) {
		int sectionCount = getSectionCount();

		if (sectionCount == 1) {
			return mDataset.size();
		}

		int counter = 0;
		boolean isContact = mSections.get(section).equals(GUEST_CONTACT);
		for (int i = 0; i < mDataset.size(); i++) {
			boolean isGuestFriend = mDataset.get(i).isFriend;
			if (isGuestFriend == isContact) counter++;
		}
		return counter;
	}

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
	protected void onBindSectionHeaderViewHolder(HeaderViewHolder holder, int section) {
		Log.i(TAG, "Section: " + getSectionHeaderTitle(section) + " Items: " + getItemCountForSection(section));
		super.onBindSectionHeaderViewHolder(holder, section);
	}

	protected String getSectionHeaderTitle(int section) {
		return mSections.get(section);
	}

	@Override
	public void setDataset(ArrayList<Contact> contacts) {
		for (int i = mDataset.size() - 1; i >= 0; i--) {
			if (!contacts.contains(mDataset.get(i)))
				mDataset.removeItemAt(i);
		}


		for (Contact contact : contacts) {
			if (setupSections(contact)) break;
		}

		mDataset.addAll(contacts);

		notifyItemRangeChanged(0, mDataset.size());
	}

	@Override
	public int insert(Contact c) {
		mDataset.add(c);

		setupSections(c);
//		notifyItemInserted(position);
		return mDataset.add(c);
	}

	@Override
	public void update(Contact c, int position) {

		if (position != SortedList.INVALID_POSITION) {
			mDataset.updateItemAt(position, c);
//			notifyItemChanged(position);
		}

		mSections.clear();
		for (int i = 0; i < mDataset.size(); i++) {
			if (setupSections(mDataset.get(i))) break;
		}
	}

	@Override
	public void insertAll(ArrayList<Contact> contacts) {
		int i = mDataset.size();
		mDataset.addAll(contacts);

		for (Contact contact : contacts)
			if (setupSections(contact)) break;

//		notifyItemRangeInserted(i, contacts.size());

//		Collections.sort(mDataset, mDataComparator);
//
//		notifyDataSetChanged();
	}

	@Override
	public void clear() {
		int size = mDataset.size();
		mDataset.clear();
		mSections.clear();
//		notifyItemRangeRemoved(0, size);
	}

	@Override
	public void setEventListener(ContactsEventListener mEventListener) {
		this.mEventListener = mEventListener;
	}

	@Override
	public Contact get(int position) {
		return mDataset.get(position);
	}

	private boolean setupSections(Contact contact) {
		if (contact.isFriend && !mSections.contains(GUEST_CONTACT)) mSections.add(GUEST_CONTACT);
		else if (!contact.isFriend && !mSections.contains(GUEST_MATCH)) mSections.add(GUEST_MATCH);

		return mSections.size() >= 2;
	}

	public void clearSection(int section) {
		boolean contactSection = mSections.get(section).equals(GUEST_CONTACT);
		for (int i = mDataset.size() - 1; i >= 0; i++) {
			if (mDataset.get(i).isFriend == contactSection) {
				mDataset.removeItemAt(i);
//				notifyItemRemoved(i);
			}

			mSections.remove(section);
		}
	}

}
