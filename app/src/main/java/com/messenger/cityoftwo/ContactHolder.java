package com.messenger.cityoftwo;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Aayush on 1/14/2017.
 */
public class ContactHolder extends RecyclerView.ViewHolder {

	ImageView icon;
	TextView name;

	public ContactHolder(View view) {
		super(view);

		icon = (ImageView) view.findViewById(R.id.contact_icon);
		name = (TextView) view.findViewById(R.id.contact_name);
	}
}
