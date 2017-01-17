package com.messenger.cityoftwo;

import android.os.Bundle;
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
	private final List<Bundle> fragmentArgs;

	public HomePagerAdapter(FragmentManager fm) {
		super(fm);

		fragments = new ArrayList<>();
		fragmentTitles = new ArrayList<>();
		fragmentArgs = new ArrayList<>();
	}

	@Override
	public Fragment getItem(int position) {
		Fragment fragment = fragments.get(position);
		if (fragment.getArguments() == null) fragment.setArguments(fragmentArgs.get(position));
		return fragments.get(position);
	}

	@Override
	public int getCount() {
		return fragments.size();
	}

	@Override
	public int getItemPosition(Object object) {
		return POSITION_NONE;
	}

	public String getPageTitle(int position) {
		return fragmentTitles.get(position);
	}

	public void addFragment(Fragment f, Bundle arg, String title) {
		fragments.add(f);
		fragmentArgs.add(arg);
		fragmentTitles.add(title);
	}
}
