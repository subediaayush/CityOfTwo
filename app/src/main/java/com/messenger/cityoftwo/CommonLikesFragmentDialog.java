package com.messenger.cityoftwo;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link ContactAdapterWrapper.ContactsEventListener}
 * interface.
 */
public class CommonLikesFragmentDialog extends Fragment implements DialogWrappableFragmentInterface {

	public final static String ARG_SEARCH_MODE = "mode";
	public final static String ARG_CONTACTS = "contacts";
	public final static String ARG_TOKEN = "token";

	public final static int SEARCH_MODE_CONTACTS = 0b01;
	public final static int SEARCH_MODE_MATCHES = 0b10;

	public static final int MODE_ONLINE = 0;
	public static final int MODE_OFFLINE = 1;
	public static final String ARG_CURRENT_GUEST = "current_guest";
	private static final String TAG = "ContactsFragment";
	private final String KEY_MESSAGE_RECEIVED = "message_received";
	private final String KEY_MESSAGE_SENT = "message_sent";
	private final String KEY_MESSAGES = "messages";

	private Contact mGuest;

	private String mToken;

	private ArrayList<Contact> mContacts;
	private RecyclerView mLikesList;
	private TextView mEmptyView;

	private HashMap<Integer, ArrayList<Conversation>> mRecentMessages;

	private ProgressBar mLoadingView;
	private CommonLikesAdapter mContactAdapter;

	private int searchMode;

	private DialogFragmentWrapper mDialogFragmentWrapper;

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public CommonLikesFragmentDialog() {
	}

	public static CommonLikesFragmentDialog newInstance() {
		return new CommonLikesFragmentDialog();
	}

//	@Override
//	public void onAttach(Context context) {
//		super.onAttach(context);
//		if (context instanceof RequestsEventListener) {
//			mListener = (RequestsEventListener) context;
//		} else {
//			throw new RuntimeException(context.toString()
//					+ " must implement RequestsEventListener");
//		}
//	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);


		if (getArguments() != null) {
			mToken = getArguments().getString(ARG_TOKEN, "");
			mGuest = getArguments().getParcelable(ARG_CURRENT_GUEST);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_common_likes, container, false);

		mLikesList = (RecyclerView) view.findViewById(R.id.like_list);

		Context context = view.getContext();
		mLikesList.setLayoutManager(new LinearLayoutManager(context));

		if (savedInstanceState != null) {
			mToken = savedInstanceState.getString(ARG_TOKEN);
			mGuest = savedInstanceState.getParcelable(ARG_CURRENT_GUEST);
		}

		reloadInfo();

		return view;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putString(ARG_TOKEN, mToken);
		outState.putParcelable(ARG_CURRENT_GUEST, mGuest);
	}

	@Override
	public void onDetach() {
		super.onDetach();
	}

	private void reloadInfo() {
		new CommonLikesHttpHandler(mToken).execute();
	}

	private void setupAdapter(String[][] likes) {
		mContactAdapter = new CommonLikesAdapter(getContext(), likes);
		mLikesList.setAdapter(mContactAdapter);

		if (mDialogFragmentWrapper != null) mDialogFragmentWrapper.onItemsPrepared();
	}

	public void setDialogWrapper(DialogFragmentWrapper wrapper) {
		this.mDialogFragmentWrapper = wrapper;
	}


	/**
	 * This interface must be implemented by activities that contain this
	 * fragment to allow an interaction in this fragment to be communicated
	 * to the activity and potentially other fragments contained in that
	 * activity.
	 * <p/>
	 * See the Android Training lesson <a href=
	 * "http://developer.android.com/training/basics/fragments/communicating.html"
	 * >Communicating with Other Fragments</a> for more information.
	 */
	private class CommonLikesHttpHandler {

		private HttpHandler commonLikesHttpHandler;

		private String token;

		private CommonLikesHttpHandler(String token) {
			this.token = token;

			String friend_id = mGuest.id.toString();

			String[] path = {
					CityOfTwo.API,
					getString(R.string.url_get_common_likes)
			};

			commonLikesHttpHandler = new HttpHandler(
					CityOfTwo.HOST,
					path,
					HttpHandler.GET,
					CityOfTwo.KEY_FRIEND_ID,
					friend_id
			) {
				@Override
				protected void onSuccess(String response) {
					addLikes(response);
				}

				@Override
				protected void onPostExecute() {
					mDialogFragmentWrapper.onItemsPrepared();
				}
			};
			commonLikesHttpHandler.addHeader("Authorization", "Token " + this.token);

		}

		private void addLikes(String response) {
			try {
				JSONArray likesJSON = (new JSONObject(response)).getJSONArray(KEY_MESSAGES);
				int totalLikes = likesJSON.length();

				String[][] likes = new String[totalLikes][3];
//						JSONArray likesJSON = new JSONArray((new JSONObject(response)).getString(ARG_CONTACTS));
				for (int i = 0; i < totalLikes; i++) {
					JSONObject likeJSON = likesJSON.getJSONObject(i);

					likes[i][0] = likeJSON.getString(CityOfTwo.KEY_NAME);
					likes[i][1] = likeJSON.getString(CityOfTwo.KEY_CATEGORY);
					likes[i][2] = likeJSON.getString(CityOfTwo.KEY_ICON);

				}

				setupAdapter(likes);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}


		public void execute() {
			commonLikesHttpHandler.execute();
		}
	}
}
