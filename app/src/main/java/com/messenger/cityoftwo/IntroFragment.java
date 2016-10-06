package com.messenger.cityoftwo;

import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * Created by Aayush on 8/25/2016.
 */
public class IntroFragment extends Fragment {
	protected static final String ARG_TITLE = "title";
	protected static final String ARG_DESC = "desc";
	protected static final String ARG_DRAWABLE = "drawable";
	protected static final String ARG_BG_COLOR = "bg_color";
	protected static final String ARG_TITLE_COLOR = "title_color";
	protected static final String ARG_DESC_COLOR = "desc_color";
	private static final String TAG = "IntroFragment";
	private static final String ARG_CUSTOM_LAYOUT = "custom_layout";


	private int drawable, bgColor, titleColor, descColor, layoutId;
	private boolean hasCustomLayout;
	private String title, description;

	private LinearLayout mainLayout;

	public IntroFragment() {
	}

	public static IntroFragment newInstance(int layoutId, CharSequence description) {
		IntroFragment slide = new IntroFragment();
		slide.layoutId = layoutId;
		Bundle args = new Bundle();
		args.putString(ARG_DESC, description.toString());
		args.putBoolean(ARG_CUSTOM_LAYOUT, true);
		slide.setArguments(args);
		return slide;
	}


	public static IntroFragment newInstance(CharSequence title, CharSequence description, int imageDrawable, int bgColor, int titleColor, int descColor) {
		IntroFragment slide = new IntroFragment();
		slide.layoutId = R.layout.template_intro;
		Bundle args = new Bundle();
		args.putString(ARG_TITLE, title.toString());
		args.putString(ARG_DESC, description.toString());
		args.putInt(ARG_DRAWABLE, imageDrawable);
		args.putInt(ARG_BG_COLOR, bgColor);
		args.putInt(ARG_TITLE_COLOR, titleColor);
		args.putInt(ARG_DESC_COLOR, descColor);
		args.putBoolean(ARG_CUSTOM_LAYOUT, false);
		slide.setArguments(args);
		return slide;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setRetainInstance(true);

		if (getArguments() != null && getArguments().size() != 0) {

			hasCustomLayout = getArguments().getBoolean(ARG_CUSTOM_LAYOUT);

			if (hasCustomLayout) {
				drawable = -1;
				title = "";

				description = getArguments().getString(ARG_DESC);

				descColor = 0;
				bgColor = 0;
				titleColor = 0;
			} else {
				drawable = getArguments().getInt(ARG_DRAWABLE);
				title = getArguments().getString(ARG_TITLE);
				description = getArguments().getString(ARG_DESC);
				bgColor = getArguments().getInt(ARG_BG_COLOR);
				descColor = getArguments().containsKey(ARG_DESC_COLOR) ? getArguments().getInt(ARG_DESC_COLOR) : 0;
				titleColor = getArguments().containsKey(ARG_TITLE_COLOR) ? getArguments().getInt(ARG_TITLE_COLOR) : 0;
			}
		}
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		if (savedInstanceState != null) {
			drawable = savedInstanceState.getInt(ARG_DRAWABLE);
			title = savedInstanceState.getString(ARG_TITLE);
			description = savedInstanceState.getString(ARG_DESC);

			bgColor = savedInstanceState.getInt(ARG_BG_COLOR);
			titleColor = savedInstanceState.getInt(ARG_TITLE_COLOR);
			descColor = savedInstanceState.getInt(ARG_DESC_COLOR);

			hasCustomLayout = savedInstanceState.getBoolean(ARG_CUSTOM_LAYOUT);
		}
	}


	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View v = inflater.inflate(layoutId, container, false);

		if (hasCustomLayout) {
			TextView d = (TextView) v.findViewById(R.id.description);

			d.setText(description);
		} else {
			TextView t = (TextView) v.findViewById(R.id.title);
			TextView d = (TextView) v.findViewById(R.id.description);
			ImageView i = (ImageView) v.findViewById(R.id.image);
			mainLayout = (LinearLayout) v.findViewById(R.id.main_layout);

			t.setText(title);
			if (titleColor != 0) {
				t.setTextColor(titleColor);
			}

			d.setText(description);
			if (descColor != 0) {
				d.setTextColor(descColor);
			}

			Picasso.with(getContext())
					.load(drawable)
					.into(i);

			mainLayout.setBackgroundColor(bgColor);
		}

		return v;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(ARG_DRAWABLE, drawable);

		outState.putString(ARG_TITLE, title);
		outState.putString(ARG_DESC, description);

		outState.putInt(ARG_BG_COLOR, bgColor);
		outState.putInt(ARG_TITLE_COLOR, titleColor);
		outState.putInt(ARG_DESC_COLOR, descColor);

		outState.putBoolean(ARG_CUSTOM_LAYOUT, hasCustomLayout);
	}

	public int getDefaultBackgroundColor() {
		return bgColor;
	}

	public void setBackgroundColor(@ColorInt int backgroundColor) {
		mainLayout.setBackgroundColor(backgroundColor);
	}
}
