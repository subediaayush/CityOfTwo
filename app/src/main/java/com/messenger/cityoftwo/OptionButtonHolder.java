package com.messenger.cityoftwo;

import android.support.annotation.DrawableRes;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Aayush on 1/25/2017.
 */

public class OptionButtonHolder {
	public View view;
	public TextView text;
	public ImageView icon;

	public OptionButtonHolder(View view) {
		this.view = view;
		this.icon = (ImageView) ((ViewGroup) view).getChildAt(0);
		this.text = (TextView) ((ViewGroup) view).getChildAt(1);
	}

	public void setOnClickListener(View.OnClickListener listener) {
		view.setOnClickListener(listener);
	}

	public void setText(String text) {
		this.text.setText(text);
	}

	public void setIcon(@DrawableRes int resId) {
		this.icon.setImageResource(resId);
	}
}
