package com.messenger.cityoftwo;

import java.util.ArrayList;

/**
 * Created by Aayush on 1/19/2017.
 */

public interface ContactsAdapterInterface {
	void setDataset(ArrayList<Contact> contacts);

	int insert(Contact c);

	void update(Contact c, int position);

	void insertAll(ArrayList<Contact> c);

	void clear();

	void setEventListener(ContactAdapterWrapper.ContactsEventListener mEventListener);

	Contact get(int position);

}
