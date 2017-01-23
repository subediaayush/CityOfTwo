package com.messenger.cityoftwo;

import android.content.Context;
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


		mAdapter.addFragment(LobbyFragment.newInstance(), lobbyArgs, "LOBBY");
		mAdapter.addFragment(ContactsFragment.newInstance(), contactsArgs, "CONTACTS");
//		mAdapter.addFragment(InboxFragment.newInstance(token), "INBOX");
//		mAdapter.addFragment(RequestFragment.newInstance(token), "REQUEST");

		viewPager.setAdapter(mAdapter);
	}
}
