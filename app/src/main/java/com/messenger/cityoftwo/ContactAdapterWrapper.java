package com.messenger.cityoftwo;

import android.content.Context;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * Created by Aayush on 1/16/2017.
 */

public class ContactAdapterWrapper implements ContactsAdapterInterface {
	private SectionedContactsAdapter sectionedContactsAdapter;
	private ContactsAdapter contactsAdapter;

	private Context mContext;

	private boolean isSectioned;

	public ContactAdapterWrapper(Context context, int searchMode) {
		this.mContext = context;

		isSectioned = searchMode ==
				(ContactsFragment.SEARCH_MODE_MATCHES | ContactsFragment.SEARCH_MODE_CONTACTS);

		if (isSectioned)
			sectionedContactsAdapter = new SectionedContactsAdapter(mContext, this);
		else
			contactsAdapter = new ContactsAdapter(mContext, this);
	}

	public ContactHolder createItemViewHolder(ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(mContext).inflate(
				R.layout.layout_contact,
				parent,
				false
		);
		return new ContactHolder(view);
	}

	public void bindItemView(final ContactHolder holder, final int position) {
		final Contact contact = getDataset().get(position);

		holder.name.setText(contact.nickName);

		if (contact.hasRevealed) {
			new FacebookHelper(mContext, contact.fid, -1, "name") {
				@Override
				public void onResponse(String response) {
					contact.name = response;
					update(contact, position);
					holder.name.setText(contact.name);
				}

				@Override
				public void onError() {
				}
			}.execute();

			FacebookHelper.loadFacebookProfilePicture(mContext, contact.fid, -1, holder.image);
		} else {
			Utils.loadRandomPicture(mContext, contact.id, holder.image);
		}

		holder.itemView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (getListener() != null) getListener().onProfileViewed(position);
			}
		});

	}

	private SortedList<Contact> getDataset() {
		if (isSectioned) return sectionedContactsAdapter.getDataset();
		else return contactsAdapter.getDataset();
	}

	@Override
	public void setDataset(ArrayList<Contact> contacts) {
		if (isSectioned) sectionedContactsAdapter.setDataset(contacts);
		else contactsAdapter.setDataset(contacts);
	}

	@Override
	public int insert(Contact c) {
		return isSectioned
				? sectionedContactsAdapter.insert(c)
				: contactsAdapter.insert(c);
	}

	@Override
	public void update(Contact c, int position) {
		if (isSectioned) sectionedContactsAdapter.update(c, position);
		else contactsAdapter.update(c, position);
	}

	@Override
	public void insertAll(ArrayList<Contact> c) {
		if (!c.isEmpty()) if (isSectioned) sectionedContactsAdapter.insertAll(c);
		else contactsAdapter.insertAll(c);
	}

	@Override
	public void clear() {
		if (isSectioned) sectionedContactsAdapter.clear();
		else contactsAdapter.clear();
	}

	@Override
	public void setEventListener(ContactsEventListener mEventListener) {
		if (isSectioned) sectionedContactsAdapter.setEventListener(mEventListener);
		else contactsAdapter.setEventListener(mEventListener);
	}

	@Override
	public Contact get(int position) {
		return isSectioned
				? sectionedContactsAdapter.get(position)
				: contactsAdapter.get(position);
	}

	public ContactsEventListener getListener() {
		if (isSectioned) return sectionedContactsAdapter.getEventListener();
		else return contactsAdapter.getEventListener();
	}

	public RecyclerView.Adapter getAdapter() {
		if (isSectioned) return sectionedContactsAdapter;
		else return contactsAdapter;
	}

	public int getItemCount() {
		return getAdapter().getItemCount();
	}

	public void clearSection(int section) {
		if (isSectioned) sectionedContactsAdapter.clearSection(section);
		else contactsAdapter.clear();
	}

	public interface ContactsEventListener {
		void onProfileViewed(int position);
	}
}
