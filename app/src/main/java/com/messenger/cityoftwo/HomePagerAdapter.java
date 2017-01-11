package com.messenger.cityoftwo;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Aayush on 10/6/2016.
 */

public class HomePagerAdapter extends FragmentPagerAdapter {
	private final List<Fragment> fragments;
	private final List<String> fragmentTitles;

	public HomePagerAdapter(FragmentManager fm) {
		super(fm);

		fragments = new ArrayList<>();
		fragmentTitles = new ArrayList<>();
	}

	@Override
	public Fragment getItem(int position) {
		return fragments.get(position);
	}

	@Override
	public int getCount() {
		return fragments.size();
	}

	public String getPageTitle(int position) {
		return fragmentTitles.get(position);
	}

	public void addFragment(Fragment f, String title) {
		fragments.add(f);
		fragmentTitles.add(title);
	}
}
