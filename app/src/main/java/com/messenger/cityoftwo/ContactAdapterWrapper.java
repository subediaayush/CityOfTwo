package com.messenger.cityoftwo;

import android.content.Context;
import android.support.v7.widget.RecyclerView;

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
			sectionedContactsAdapter = new SectionedContactsAdapter(mContext);
		else
			contactsAdapter = new ContactsAdapter(mContext);
	}

	public RecyclerView.Adapter getAdapter() {
		if (isSectioned) return sectionedContactsAdapter;
		else return contactsAdapter;
	}

	public int getItemCount() {
		return getAdapter().getItemCount();
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

	public void clearSection(int section) {
		if (isSectioned) sectionedContactsAdapter.clearSection(section);
		else contactsAdapter.clear();
	}

	public interface ContactsEventListener {
		void onProfileViewed(int position);
	}
}
