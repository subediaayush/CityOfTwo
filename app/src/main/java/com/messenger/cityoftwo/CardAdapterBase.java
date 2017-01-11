package com.messenger.cityoftwo;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.ViewGroup;

/**
 * Created by Aayush on 10/8/2016.
 */

public abstract class CardAdapterBase<T extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<T> {

	private final static String TAG = "CardAdapterBase";
	protected Context mContext;
	private int mTopPadding;

	public CardAdapterBase(Context context, int topPadding) {
		mContext = context;

		Resources r = context.getResources();

		mTopPadding = (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, topPadding,
				r.getDisplayMetrics()
		);

		Log.i(TAG, "Top margin value: " + mTopPadding);
	}

	@Override
	public void onBindViewHolder(T holder, int position) {
		if (position == 0) {
			ViewGroup.MarginLayoutParams mp = (ViewGroup.MarginLayoutParams) holder.itemView.getLayoutParams();
			mp.topMargin = mTopPadding;

			holder.itemView.setLayoutParams(mp);
		}

		onBindHolder(holder, position);
	}

	abstract void onBindHolder(T holder, int position);
}
