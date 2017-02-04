package com.messenger.cityoftwo;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Aayush on 2/3/2017.
 */

public class ProfileFragment extends BottomSheetDialogFragment {

	public static final String ARG_CURRENT_GUEST = "current_guest";
	DialogFragmentWrapper mWrapper;
	Contact mGuest;
	private TextView mName;
	private TextView mNickname;
	private CircleImageView mImage;
	private TextView mUrl;
	private TextView mStatus;
	private TextView mCommonLikes;

	private View mSend;
	private TextView mOfflineMessage;
	private View mRequest;

	private ProfileFragment.ProfileFragmentListener mEventListener;

	private BottomSheetBehavior mBottomSheetBehavior;
	private BottomSheetBehavior.BottomSheetCallback mBottomSheetCallback = new BottomSheetBehavior.BottomSheetCallback() {
		@Override
		public void onStateChanged(@NonNull View bottomSheet, int newState) {
			if (newState == BottomSheetBehavior.STATE_HIDDEN)
				ProfileFragment.super.dismiss();
		}

		@Override
		public void onSlide(@NonNull View bottomSheet, float slideOffset) {

		}
	};
	private View mOfflineContainer;

	public static ProfileFragment newInstance(Bundle args) {
		ProfileFragment fragment = new ProfileFragment();
		fragment.setArguments(args);
		return fragment;
	}

	private void initView(View itemView) {
		mName = (TextView) itemView.findViewById(R.id.profile_name);
		mNickname = (TextView) itemView.findViewById(R.id.profile_nickname);
		mImage = (CircleImageView) itemView.findViewById(R.id.icon);
		mUrl = (TextView) itemView.findViewById(R.id.profile_url);
		mStatus = (TextView) itemView.findViewById(R.id.profile_status);
		mCommonLikes = (TextView) itemView.findViewById(R.id.profile_commonlikes);

		mRequest = itemView.findViewById(R.id.request_chat);
		mOfflineMessage = (TextView) itemView.findViewById(R.id.offline_message);
		mSend = itemView.findViewById(R.id.send_offline_message);
		mOfflineContainer = itemView.findViewById(R.id.offline_container);
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Context context = getContext();

		BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);

		View view = View.inflate(context, R.layout.fragment_profile, null);

		initView(view);
		mGuest = getArguments().getParcelable(ARG_CURRENT_GUEST);

		if (!mGuest.hasRevealed) {
			mName.setVisibility(View.GONE);
			mUrl.setVisibility(View.GONE);

			Utils.loadRandomPicture(context, mGuest.id, mImage);
		} else {
			mName.setVisibility(View.VISIBLE);
			mUrl.setVisibility(View.VISIBLE);

			mName.setText(mGuest.name);
			mUrl.setText(mGuest.name + " at fb.com");
			mUrl.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (mEventListener != null) mEventListener.onViewProfile(mGuest.fid);
				}
			});

			FacebookHelper.loadFacebookProfilePicture(context, mGuest.fid, -1, mImage);
		}

		if (mGuest.nickName.isEmpty()) {
			mNickname.setVisibility(View.GONE);
		} else {
			mNickname.setVisibility(View.VISIBLE);
			mNickname.setText(mGuest.nickName);
		}

		if (mGuest.status.isEmpty()) {
			mStatus.setVisibility(View.GONE);
		} else {
			mStatus.setVisibility(View.VISIBLE);
			mStatus.setText(mGuest.status);
		}

		String[] likes = mGuest.topLikes;

		String commonLikesMessages = "Likes " +
				Utils.getReadableList(likes) + " and " +
				(mGuest.commonLikes - likes.length) + " others";

		mCommonLikes.setText(commonLikesMessages);

		mRequest.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sendChatRequest();
			}
		});

		if (mGuest.isFriend) mSend.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String message = mOfflineMessage.getText().toString();
				if (!message.isEmpty()) sendOfflineMessage(message);
			}
		});
		else mOfflineContainer.setVisibility(View.GONE);

		dialog.setContentView(view);

		mBottomSheetBehavior = BottomSheetBehavior.from((View) view.getParent());

		dialog.setOnShowListener(new DialogInterface.OnShowListener() {
			@Override
			public void onShow(DialogInterface dialog) {
				mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
				mBottomSheetBehavior.setSkipCollapsed(true);
				mBottomSheetBehavior.setBottomSheetCallback(mBottomSheetCallback);
			}
		});

		return dialog;
	}

	private void sendOfflineMessage(String message) {

	}

	private void sendChatRequest() {
		final Context context = getContext();
		String token = context.getSharedPreferences(CityOfTwo.PACKAGE_NAME, Context.MODE_PRIVATE)
				.getString(CityOfTwo.KEY_SESSION_TOKEN, "");
		final OnlineCheck check = new OnlineCheck(getContext(), mGuest, token, true) {

			@Override
			void isOnline(Contact contact, int requestId) {
				if (mEventListener != null) mEventListener.onChatRequestSent(requestId);
			}

			@Override
			void isOffline(Contact contact) {
				onError(new Exception());
			}

			@Override
			void onError(Exception e) {
				Toast.makeText(context, mGuest.nickName + "is not available right now." +
						"Try sending an offline message", Toast.LENGTH_SHORT).show();
			}
		};

		check.execute();
	}

	@Override
	public void dismiss() {
		mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
	}

	public void setEventListener(ProfileFragmentListener mEventListener) {
		this.mEventListener = mEventListener;
	}

	/**
	 * Created by Aayush on 2/3/2017.
	 */
	public interface ProfileFragmentListener {
		void onEditProfile();

		void onChatRequestSent(int requestId);

		void onOfflineMessageSent();

		void onViewProfile(String fid);
	}
}
