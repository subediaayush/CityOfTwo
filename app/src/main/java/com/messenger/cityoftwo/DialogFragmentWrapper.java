package com.messenger.cityoftwo;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by Aayush on 1/28/2017.
 */

public class DialogFragmentWrapper extends DialogFragment {

	private static final String ARG_DIALOG_TITLE = "title";

	private Fragment mFragment;

	private TitleHolder mTitleHolder;

	public static DialogFragmentWrapper newInstance(String title) {
		DialogFragmentWrapper fragment = new DialogFragmentWrapper();
		Bundle args = new Bundle();
		args.putString(ARG_DIALOG_TITLE, title);
		fragment.setArguments(args);
		return fragment;
	}

	public static DialogFragmentWrapper newInstance() {
		DialogFragmentWrapper fragment = new DialogFragmentWrapper();
		fragment.setArguments(new Bundle());
		return fragment;
	}

	public void setFragment(Fragment fragment) {
		this.mFragment = fragment;
		if (mFragment instanceof DialogWrappableFragmentInterface)
			((DialogWrappableFragmentInterface) mFragment).setDialogWrapper(this);
	}

//	@NonNull
//	@Override
//	public Dialog onCreateDialog(Bundle savedInstanceState) {
//		Context context = getContext();
//
//		Bundle params = getArguments();
//		String title = params.getString(ARG_DIALOG_TITLE, "");
//
//		View v = LayoutInflater.from(context).inflate(R.layout.layout_dialog_wrapper, null);
//
//		mTitleHolder = new TitleHolder(v);
//
//		if (title.isEmpty()) mTitleHolder.setVisibility(View.GONE);
//		else {
//			mTitleHolder.setTitle(title);
//
//			if (!(mFragment instanceof DialogWrappableFragmentInterface)) mTitleHolder.showIndicator(false);
//		}


//		return new AlertDialog.Builder(context, R.style.AppTheme_Dialog)
//				.setView(mTitleHolder.view)
//				.create();
//	}

//	@Override
//	public void onActivityCreated(Bundle savedInstanceState) {
//		super.onActivityCreated(savedInstanceState);
//		getChildFragmentManager().beginTransaction().add(R.id.dialog_container, mFragment).commit();
//	}


	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setStyle(STYLE_NO_TITLE, 0);
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		Context context = getContext();

		Bundle params = getArguments();
		String title = params.getString(ARG_DIALOG_TITLE, "");

		View v = LayoutInflater.from(context).inflate(R.layout.layout_dialog_wrapper, null);

		mTitleHolder = new TitleHolder(v);

		if (title.isEmpty()) mTitleHolder.setVisibility(View.GONE);
		else {
			mTitleHolder.setTitle(title);
			if (!(mFragment instanceof DialogWrappableFragmentInterface))
				mTitleHolder.showIndicator(false);
		}

		return v;
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		getChildFragmentManager().beginTransaction().add(R.id.dialog_container, mFragment).commit();
	}

	public void showIndicator(boolean show) {
		if (mTitleHolder != null) mTitleHolder.showIndicator(show);
	}

	public void onItemsPrepared() {
		showIndicator(false);
	}

	public void show(FragmentManager manager) {
		String tag = getArguments().getString(ARG_DIALOG_TITLE, "Dialog");
		super.show(manager, tag);
	}

	private class TitleHolder {

		View view;

		TextView title;
		View indicator;

//		boolean showIndicator;

		public TitleHolder(View view) {
			this.view = view.findViewById(R.id.title_container);

			title = (TextView) view.findViewById(R.id.title);
			indicator = view.findViewById(R.id.loading);
		}

		protected void setVisibility(int visibility) {
			view.setVisibility(visibility);

//			if (visibility == View.VISIBLE)
		}

		public void setTitle(String title) {
			this.title.setText(title);
		}

		public void showIndicator(boolean show) {
//			showIndicator = show;

			if (show) indicator.setVisibility(View.VISIBLE);
			else indicator.setVisibility(View.GONE);
		}
	}
}
