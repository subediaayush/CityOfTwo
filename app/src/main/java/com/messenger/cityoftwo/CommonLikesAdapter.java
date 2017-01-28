package com.messenger.cityoftwo;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.truizlop.sectionedrecyclerview.SimpleSectionedAdapter;

import java.util.ArrayList;
import java.util.HashMap;

public class CommonLikesAdapter extends SimpleSectionedAdapter<CommonLikesAdapter.LikeHolder> {

	private final String[][] mDataset;
	private final Context mContext;

	private final HashMap<String, ArrayList<Integer>> mSections;
	private final ArrayList<String> mSectionKey;

	public CommonLikesAdapter(Context context, String[][] items) {
		mDataset = items;
		mContext = context;

		mSections = new HashMap<>();
		mSectionKey = new ArrayList<>();

		for (int i = 0; i < mDataset.length; i++) {
			String[] like = mDataset[i];
			String category = like[1];
			if (!mSections.containsKey(category)) {
				mSections.put(category, new ArrayList<Integer>());
				mSectionKey.add(category);
			}
			mSections.get(category).add(i);
		}
	}

	@Override
	protected int getSectionCount() {
		return mSections.size();
	}

	@Override
	protected int getItemCountForSection(int section) {
		return mSections.get(mSectionKey.get(section)).size();
	}

	@Override
	protected LikeHolder onCreateItemViewHolder(ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext())
				.inflate(R.layout.layout_like, parent, false);
		return new LikeHolder(view);
	}

	@Override
	protected void onBindItemViewHolder(LikeHolder holder, int section, int position) {
		holder.name.setText(mDataset[position][0]);

		Picasso.with(mContext)
				.load(mDataset[position][2])
				.into(holder.icon);
	}

	@Override
	protected String getSectionHeaderTitle(int section) {
		return mSectionKey.get(section);
	}

	public class LikeHolder extends RecyclerView.ViewHolder {

		public final TextView name;
		public final ImageView icon;

		public LikeHolder(View view) {
			super(view);

			name = (TextView) view.findViewById(R.id.name);
			icon = (ImageView) view.findViewById(R.id.icon);
		}

		@Override
		public String toString() {
			return super.toString() + " '" + name.getText() + "'";
		}
	}
}
