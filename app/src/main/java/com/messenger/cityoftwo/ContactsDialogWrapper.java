package com.messenger.cityoftwo;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.FrameLayout;

/**
 * Created by Aayush on 1/28/2017.
 */

public class ContactsDialogWrapper extends DialogFragment implements ContactsFragment.ContactsFragmentListener, WrappableFragment {

	private static final String ARG_DIALOG_TITLE = "title";
	private ContactsFragment.ContactsFragmentListener mListener;
	private DialogWrapper mDialogWrapper;

	public static ContactsDialogWrapper newInstance(Bundle args) {
		ContactsDialogWrapper fragment = new ContactsDialogWrapper();
		if (args != null) fragment.setArguments(args);
		return fragment;
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Context context = getContext();
		AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AppTheme_Dialog);

		Bundle params = getArguments();

		ContactsFragment contactsFragment = ContactsFragment.newInstance(params);
		contactsFragment.setListener(this);

		String title = params.getString(ARG_DIALOG_TITLE, "Contacts");

		builder.setTitle(title);

		int id = View.generateViewId();
		FrameLayout f = new FrameLayout(context);
		f.setId(id);

		getChildFragmentManager().beginTransaction().add(f.getId(), contactsFragment).commit();

		builder.setView(f);

		return builder.create();
	}

	@Override
	public void onContactSelected(Contact contact, int position) {
		if (mListener != null) mListener.onContactSelected(contact, position);
	}

	@Override
	public void onContactsLoaded(int totalContacts) {
		if (mListener != null) mListener.onContactsLoaded(totalContacts);
	}

	@Override
	public void onContactLoadError() {
		if (mListener != null) mListener.onContactLoadError();
	}

	public void setListener(ContactsFragment.ContactsFragmentListener listener) {
		this.mListener = listener;
	}

	@Override
	public void setWrapper(DialogWrapper wrapper) {
		mDialogWrapper = wrapper;
	}
}
