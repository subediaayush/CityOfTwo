package com.messenger.cityoftwo;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

public class HomeActivity extends AppCompatActivity {

	private final String ARG_TOKEN = "token";
	private final String ARG_SECTIONED = "mode";
	private ViewPager mHomePager;
	private TabLayout mTabLayout;
	private HomePagerAdapter mAdapter;
	private Integer mCurrentTab;

	private ContactsFragment mContactsFragment;
	private LobbyFragment mLobbyFragment;

	private String mToken;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_home);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		mToken = getSharedPreferences(CityOfTwo.PACKAGE_NAME, Context.MODE_PRIVATE)
				.getString(CityOfTwo.KEY_SESSION_TOKEN, "");

		mHomePager = (ViewPager) findViewById(R.id.home_pager);
		setupViewPager(mHomePager);

		mTabLayout = (TabLayout) findViewById(R.id.tabs);
		mTabLayout.setupWithViewPager(mHomePager);
	}

	private void setupViewPager(ViewPager viewPager) {
		mAdapter = new HomePagerAdapter(getSupportFragmentManager());

		Bundle lobbyArgs = new Bundle();
		lobbyArgs.putString(LobbyFragment.ARG_TOKEN, mToken);

		Bundle contactsArgs = new Bundle();
		contactsArgs.putString(ContactsFragment.ARG_TOKEN, mToken);
		contactsArgs.putInt(ContactsFragment.ARG_SEARCH_MODE, ContactsFragment.SEARCH_MODE_CONTACTS);

		mLobbyFragment = LobbyFragment.newInstance(lobbyArgs);
		mContactsFragment = ContactsFragment.newInstance(contactsArgs);

		mContactsFragment.setListener(new ContactsFragment.ContactsFragmentListener() {

			@Override
			public void onContactSelected(final Contact contact, final int position) {
				checkIfOnline(contact, position);
			}

			@Override
			public void onContactsLoaded(int totalContacts) {

			}

			@Override
			public void onContactLoadError() {

			}
		});

		mAdapter.addFragment(mLobbyFragment, "LOBBY");
		mAdapter.addFragment(mContactsFragment, "CONTACTS");
//		mAdapter.addFragment(InboxFragment.newInstance(token), "INBOX");
//		mAdapter.addFragment(RequestFragment.newInstance(token), "REQUEST");

		viewPager.setAdapter(mAdapter);
	}

	public void checkIfOnline(final Contact contact, final int adapterPosition) {
		final OnlineCheck check = new OnlineCheck(HomeActivity.this, contact, mToken, true) {

			@Override
			void isOnline(Contact contact) {
				showProfileAcivity(contact, adapterPosition, true);
			}

			@Override
			void isOffline(Contact contact) {
				showProfileAcivity(contact, adapterPosition, false);
			}

			@Override
			void onError(Exception e) {
				showProfileAcivity(contact, adapterPosition, false);

				new AlertDialog.Builder(HomeActivity.this, R.style.AppTheme_Dialog)
						.setMessage("Could not connect to " + contact.nickName + ".")
						.setPositiveButton("Try again", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								checkIfOnline(contact, adapterPosition);
							}
						})
						.show();
			}
		};

		check.execute();
	}

	private void showProfileAcivity(Contact contact, int position, boolean b) {
		Intent contactIntent = new Intent(HomeActivity.this, ProfileActivity.class);

		int mode = b ? ContactsFragment.MODE_ONLINE : ContactsFragment.MODE_OFFLINE;

		contactIntent.putExtra(
				ProfileActivity.ARG_PROFILE_MODE,
				mode
		);

		contactIntent.putExtra(ProfileActivity.ARG_CURRENT_GUEST, contact);
		contactIntent.putExtra(ProfileActivity.ARG_CURRENT_GUEST_POSITION, position);

		startActivityForResult(contactIntent, CityOfTwo.ACTIVITY_PROFILE);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == CityOfTwo.ACTIVITY_PROFILE) {
			if (resultCode == RESULT_OK) {
				Contact contact = data.getParcelableExtra(ProfileActivity.ARG_CURRENT_GUEST);
				Integer position = data.getIntExtra(ProfileActivity.ARG_CURRENT_GUEST_POSITION, -1);

				mContactsFragment.reloadContact(contact, position);
			}
		}
	}

}
