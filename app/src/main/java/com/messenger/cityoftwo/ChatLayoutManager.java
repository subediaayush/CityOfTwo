package com.messenger.cityoftwo;

import android.content.Context;
import android.graphics.PointF;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;

/**
 * Created by Aayush on 1/5/2017.
 */
public class ChatLayoutManager extends LinearLayoutManager {

	private static final float MILLISECONDS_PER_INCH = 36f;
	private Context mContext;


	public ChatLayoutManager(Context context) {
		super(context);
		mContext = context;
	}

	@Override
	public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
		//Create your RecyclerView.SmoothScroller instance? Check.
		LinearSmoothScroller smoothScroller = new LinearSmoothScroller(mContext) {
			@Override
			protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
				return MILLISECONDS_PER_INCH / displayMetrics.densityDpi;
			}

			//Automatically implements this method on instantiation.
			@Override
			public PointF computeScrollVectorForPosition(int targetPosition) {
				return ChatLayoutManager.this.computeScrollVectorForPosition(targetPosition);
			}
		};

		//Docs do not tell us anything about this,
		//but we need to set the position we want to scroll to.
		smoothScroller.setTargetPosition(position);

		//Call startSmoothScroll(SmoothScroller)? Check.
		startSmoothScroll(smoothScroller);
	}
}
