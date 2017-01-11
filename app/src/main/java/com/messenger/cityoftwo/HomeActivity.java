package com.messenger.cityoftwo;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

public class HomeActivity extends AppCompatActivity {

	private ViewPager homePager;
	private TabLayout tabLayout;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_home);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		homePager = (ViewPager) findViewById(R.id.home_pager);
		setupViewPager(homePager);

		tabLayout = (TabLayout) findViewById(R.id.tabs);
		tabLayout.setupWithViewPager(homePager);
	}

	private void setupViewPager(ViewPager viewPager) {
		HomePagerAdapter adapter = new HomePagerAdapter(getSupportFragmentManager());

		String token = getSharedPreferences(CityOfTwo.PACKAGE_NAME, Context.MODE_PRIVATE)
				.getString(CityOfTwo.KEY_SESSION_TOKEN, "");


		adapter.addFragment(LobbyFragment.newInstance(token), "LOBBY");
		adapter.addFragment(ContactsFragment.newInstance(token), "CONTACTS");
		adapter.addFragment(InboxFragment.newInstance(token), "INBOX");
		adapter.addFragment(RequestFragment.newInstance(token), "REQUEST");
		viewPager.setAdapter(adapter);
	}
}
