package com.messenger.cityoftwo;

import android.content.Context;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;

/**
 * Created by Aayush on 1/16/2017.
 */

public class ContactAdapterWrapper {
	private SectionedContactsAdapter sectionedContactsAdapter;
	private ContactsAdapter contactsAdapter;

	private Context mContext;
	private boolean isSectioned;

	public ContactAdapterWrapper(Context context, boolean isSectioned) {
		this.mContext = context;
		this.isSectioned = isSectioned;

		if (isSectioned) sectionedContactsAdapter = new SectionedContactsAdapter(mContext);
		else contactsAdapter = new ContactsAdapter(mContext);
	}

	public RecyclerView.Adapter getAdapter() {
		if (isSectioned) return sectionedContactsAdapter;
		else return contactsAdapter;
	}

	public int getItemCount() {
		return getAdapter().getItemCount();
	}


	public void setDataset(ArrayList<Contact> contacts) {
		if (isSectioned) sectionedContactsAdapter.setDataset(contacts);
		else contactsAdapter.setDataset(contacts);
	}

	public int insert(Contact c) {
		return isSectioned
				? sectionedContactsAdapter.insert(c)
				: contactsAdapter.insert(c);
	}

	public void insertAll(ArrayList<Contact> c) {
		if (isSectioned) sectionedContactsAdapter.insertAll(c);
		else contactsAdapter.insertAll(c);
	}

	public void clear() {
		if (isSectioned) sectionedContactsAdapter.clear();
		else contactsAdapter.clear();
	}

	public void clearSection(int section) {
		if (isSectioned) sectionedContactsAdapter.clearSection(section);
		else contactsAdapter.clear();
	}

	public void setEventListener(ContactsEventListener mEventListener) {
		if (isSectioned) sectionedContactsAdapter.setEventListener(mEventListener);
		else contactsAdapter.setEventListener(mEventListener);
	}

	public Contact get(int position) {
		return isSectioned
				? sectionedContactsAdapter.get(position)
				: contactsAdapter.get(position);
	}

	public interface ContactsEventListener {
		void onProfileViewed(int position);
	}
}
