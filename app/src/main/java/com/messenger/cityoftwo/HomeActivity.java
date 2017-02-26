package com.messenger.cityoftwo;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;

import static com.messenger.cityoftwo.ProfileFragment.ARG_CURRENT_GUEST;

public class HomeActivity extends ChatListenerPumpedActivity {

	private final String ARG_TOKEN = "token";
	private final String ARG_SECTIONED = "mode";
	private ViewPager mHomePager;
	private TabLayout mTabLayout;
	private HomePagerAdapter mAdapter;
	private Integer mCurrentTab;

	private BroadcastReceiver mReceiver;

	private ContactsFragment mContactsFragment;
	private LobbyFragment mLobbyFragment;

	private String mToken;
	private EmptyContentFragmentWrapper mEmptyFragment;

	@Override
	protected int getContentLayout() {
		return R.layout.activity_home;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		mToken = getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE)
				.getString(CityOfTwo.KEY_SESSION_TOKEN, "");

		mHomePager = (ViewPager) findViewById(R.id.home_pager);
		setupViewPager(mHomePager);

		mTabLayout = (TabLayout) findViewById(R.id.tabs);
		mTabLayout.setupWithViewPager(mHomePager);


	}

	@Override
	protected void onPause() {
		super.onPause();

		LocalBroadcastManager.getInstance(this)
				.unregisterReceiver(mReceiver);
	}

	@Override
	protected void onResume() {
		super.onResume();

		SharedPreferences sp = getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE);
		int chatroomId = sp.getInt(CityOfTwo.KEY_LAST_CHATROOM, -1);

		if (chatroomId != -1) startConversation();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		mContactsFragment.reloadContent();
		mLobbyFragment.reloadContent();
	}

	private void startConversation() {
		DatabaseHelper db = new DatabaseHelper(HomeActivity.this);

		SharedPreferences sp = getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE);
		int chatroomId = sp.getInt(CityOfTwo.KEY_LAST_CHATROOM, -1);
		int guestId = sp.getInt(CityOfTwo.KEY_LAST_GUEST, -1);

		Contact contact = db.loadGuest(guestId);
		if (contact != null) contact.lastMessages.addAll(db.retrieveMessages(chatroomId));

		showProfileAcivity(contact, chatroomId);
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

		mLobbyFragment.setListener(new LobbyFragment.LobbyFragmentListener() {

			@Override
			public void onViewProfile(Contact contact) {
				showProfile(contact);
			}
		});
		mContactsFragment.setListener(new ContactsFragment.ContactsFragmentListener() {

			@Override
			public void onContactSelected(final Contact contact) {
				showProfile(contact);
			}

			@Override
			public void onContactsLoaded(int totalContacts) {

			}

			@Override
			public void onContactLoadError() {

			}
		});

		mEmptyFragment = EmptyContentFragmentWrapper.newInstance("You seem to have no saved contact." +
				" Tap here to try again.");
		mEmptyFragment.setFragment(mContactsFragment);

		mAdapter.addFragment(mLobbyFragment, "LOBBY");
		mAdapter.addFragment(mEmptyFragment, "CONTACTS");
//		mAdapter.addFragment(InboxFragment.newInstance(token), "INBOX");
//		mAdapter.addFragment(RequestFragment.newInstance(token), "REQUEST");

		viewPager.setAdapter(mAdapter);
	}

	private void showProfile(final Contact contact) {
		Bundle params = new Bundle();
		params.putParcelable(ARG_CURRENT_GUEST, contact);

		final ProfileFragment profileFragment = ProfileFragment.newInstance(params);
		profileFragment.setEventListener(new ProfileFragment.ProfileFragmentListener() {
			@Override
			public void onEditProfile() {

			}

			@Override
			public void onChatRequestSent(int requestId) {
				showSentRequest(contact, requestId);
				profileFragment.dismiss();
			}

			@Override
			public void onOfflineMessageSent() {
				profileFragment.dismiss();
			}

			@Override
			public void onViewProfile(String fid) {
				Uri uri = FacebookHelper.getFacebookPageURI(HomeActivity.this, fid);

				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				startActivity(intent);

			}
		});
		FragmentManager fm = getSupportFragmentManager();
		profileFragment.show(fm, "CONTACT");
	}

	public void showProfileAfterCheck(final Contact contact, final int adapterPosition) {

	}

	private void showProfileAcivity(Contact contact, int chatRoomId) {
		SharedPreferences sp = getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE);

		if (chatRoomId > -1) sp.edit().putInt(CityOfTwo.KEY_LAST_CHATROOM, chatRoomId)
				.putInt(CityOfTwo.KEY_LAST_GUEST, contact.id).apply();
		Intent contactIntent = new Intent(HomeActivity.this, ProfileActivity.class);

		contactIntent.putExtra(ProfileActivity.ARG_CURRENT_GUEST, contact);
		contactIntent.putExtra(ProfileActivity.ARG_CHATROOM_ID, chatRoomId);

		startActivityForResult(contactIntent, CityOfTwo.ACTIVITY_PROFILE);
	}

	@Override
	int getActivityCode() {
		return CityOfTwo.ACTIVITY_HOME;
	}
}
