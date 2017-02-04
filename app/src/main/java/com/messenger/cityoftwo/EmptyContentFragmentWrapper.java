package com.messenger.cityoftwo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by Aayush on 1/30/2017.
 */

public class EmptyContentFragmentWrapper extends Fragment implements View.OnClickListener {

	private static final String ARG_CONTENT = "mContent";

	private String mMessageString;

	private EmptyListListener mListener;
	private TextView mMessage;
	private View mContent;
	private Fragment mFragment;

	public static EmptyContentFragmentWrapper newInstance() {
		return newInstance("Your list is empty");
	}

	public static EmptyContentFragmentWrapper newInstance(String content) {
		EmptyContentFragmentWrapper fragment = new EmptyContentFragmentWrapper();
		Bundle args = new Bundle();
		args.putString(ARG_CONTENT, content);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			mMessageString = savedInstanceState.getString(ARG_CONTENT);
		} else {
			mMessageString = getArguments().getString(ARG_CONTENT);
		}
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View contentView = inflater.inflate(R.layout.layout_empty_placeholder, container, false);

		mMessage = (TextView) contentView.findViewById(R.id.message);
		mContent = contentView.findViewById(R.id.container);

		mMessage.setText(mMessageString);

		mMessage.setOnClickListener(this);

		return contentView;
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		if (view != null)
			getChildFragmentManager().beginTransaction().add(R.id.container, mFragment).commit();
	}

	public void setFragment(Fragment fragment) {
		mFragment = fragment;
		if (mFragment instanceof EmptyContentWrappableFragmentInterface)
			((EmptyContentWrappableFragmentInterface) mFragment).setContentWrapper(this);
	}

	public void isContentEmpty(boolean isEmpty) {
		if (isEmpty) {
			mMessage.setVisibility(View.VISIBLE);
			mContent.setVisibility(View.INVISIBLE);
		} else {
			mMessage.setVisibility(View.INVISIBLE);
			mContent.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onClick(View v) {
		if (mFragment instanceof ReloadableFragment)
			((ReloadableFragment) mFragment).reloadContent();

		isContentEmpty(false);
	}

	/**
	 * Created by Aayush on 1/30/2017.
	 */
	public interface EmptyListListener {
		void onClick();
	}
}
