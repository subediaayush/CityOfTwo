package com.messenger.cityoftwo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_home);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		mHomePager = (ViewPager) findViewById(R.id.home_pager);
		setupViewPager(mHomePager);

		mTabLayout = (TabLayout) findViewById(R.id.tabs);
		mTabLayout.setupWithViewPager(mHomePager);
	}

	private void setupViewPager(ViewPager viewPager) {
		mAdapter = new HomePagerAdapter(getSupportFragmentManager());

		String token = getSharedPreferences(CityOfTwo.PACKAGE_NAME, Context.MODE_PRIVATE)
				.getString(CityOfTwo.KEY_SESSION_TOKEN, "");

		Bundle lobbyArgs = new Bundle();
		lobbyArgs.putString(LobbyFragment.ARG_TOKEN, token);

		Bundle contactsArgs = new Bundle();
		contactsArgs.putString(ContactsFragment.ARG_TOKEN, token);
		contactsArgs.putInt(ContactsFragment.ARG_SEARCH_MODE, ContactsFragment.SEARCH_MODE_CONTACTS);

		mLobbyFragment = LobbyFragment.newInstance();
		mContactsFragment = ContactsFragment.newInstance();
		mContactsFragment.setListener(new ContactsFragment.ContactsFragmentListener() {
			@Override
			public void onContactSelected(Contact contact, int position) {
				Intent contactIntent = new Intent(HomeActivity.this, ProfileActivity.class);

				contactIntent.putExtra(
						ProfileActivity.ARG_PROFILE_MODE,
						ChatAdapter.MODE_CHAT
				);

				contactIntent.putExtra(ProfileActivity.ARG_CURRENT_GUEST, contact);
				contactIntent.putExtra(ProfileActivity.ARG_CURRENT_GUEST_POSITION, position);

				startActivityForResult(contactIntent, CityOfTwo.ACTIVITY_PROFILE);
			}

			@Override
			public void onContactsLoaded(int totalContacts) {

			}

			@Override
			public void onContactLoadError() {

			}
		});

		mAdapter.addFragment(mLobbyFragment, lobbyArgs, "LOBBY");
		mAdapter.addFragment(mContactsFragment, contactsArgs, "CONTACTS");
//		mAdapter.addFragment(InboxFragment.newInstance(token), "INBOX");
//		mAdapter.addFragment(RequestFragment.newInstance(token), "REQUEST");

		viewPager.setAdapter(mAdapter);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == CityOfTwo.ACTIVITY_PROFILE) {
			Contact contact = data.getParcelableExtra(ProfileActivity.ARG_CURRENT_GUEST);
			Integer position = data.getParcelableExtra(ProfileActivity.ARG_CURRENT_GUEST_POSITION);

			mContactsFragment.reloadContact(contact, position);
		}
	}

}
