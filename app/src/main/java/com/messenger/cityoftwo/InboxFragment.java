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

/**
 * Created by Aayush on 10/8/2016.
 */

public class InboxFragment extends Fragment implements InboxAdapter.InboxEventListener {

	private static final String ARG_TOKEN = "token";

	private static final String KEY_MESSAGES = "messages";
	private static final String KEY_TOKEN = "token";

	private String mToken;

	private ArrayList<Message> mMessages;

	private RecyclerView mMessageList;
	private TextView mEmptyView;
	private ProgressBar mLoadingView;
	private InboxAdapter mInboxAdapter;

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public InboxFragment() {
	}

	public static InboxFragment newInstance(String token) {
		InboxFragment fragment = new InboxFragment();
		Bundle args = new Bundle();

		args.putString(ARG_TOKEN, token);
		fragment.setArguments(args);
		return fragment;
	}

//	@Override
//	public void onAttach(Context context) {
//		super.onAttach(context);
//		if (context instanceof InboxEventListener) {
//			mListener = (InboxEventListener) context;
//		} else {
//			throw new RuntimeException(context.toString()
//					+ " must implement InboxEventListener");
//		}
//	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments() != null) {
			mToken = getArguments().getString(ARG_TOKEN);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_inbox, container, false);

		mMessageList = (RecyclerView) view.findViewById(R.id.message_list);
		mEmptyView = (TextView) view.findViewById(R.id.empty_view);
		mLoadingView = (ProgressBar) view.findViewById(R.id.loading_view);

		mEmptyView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mEmptyView.setVisibility(View.INVISIBLE);
				mLoadingView.setVisibility(View.VISIBLE);
				mMessageList.setVisibility(View.INVISIBLE);

				reloadInfo();
			}
		});


		if (savedInstanceState != null) {
			mMessages = savedInstanceState.getParcelableArrayList(KEY_MESSAGES);
			mToken = savedInstanceState.getString(KEY_TOKEN);

			mLoadingView.setVisibility(View.INVISIBLE);
			if (mMessages.isEmpty()) {
				mMessageList.setVisibility(View.INVISIBLE);
				mEmptyView.setVisibility(View.VISIBLE);
			} else {
				mMessageList.setVisibility(View.VISIBLE);
				mEmptyView.setVisibility(View.INVISIBLE);
			}
		} else {
			mMessageList.setVisibility(View.INVISIBLE);
			mEmptyView.setVisibility(View.INVISIBLE);
			mLoadingView.setVisibility(View.VISIBLE);

			reloadInfo();
		}

		Context context = view.getContext();
		mInboxAdapter = new InboxAdapter(context);
		mMessageList.setLayoutManager(new LinearLayoutManager(context));
		mMessageList.setAdapter(mInboxAdapter);

		mInboxAdapter.setEventListener(this);

		return view;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putParcelableArrayList(KEY_MESSAGES, mMessages);
		outState.putString(KEY_TOKEN, mToken);
	}

	@Override
	public void onDetach() {
		super.onDetach();
	}

	private void reloadInfo() {
		new InboxHttpHandler(mToken).execute();
	}

	@Override
	public void onMessageDelete(final int position, View rootView) {
		String[] path = {
				CityOfTwo.API,
				getString(R.string.url_delete_message)
		};

		JSONObject message = null;
		try {
			message = new JSONObject(mInboxAdapter.get(position).toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}

		mInboxAdapter.setLoading(position);

		new HttpHandler(
				CityOfTwo.HOST,
				path,
				HttpHandler.POST,
				message
		) {
			@Override
			protected void onSuccess(String response) {
				try {
					JSONObject j = new JSONObject(response);

					boolean deleted = j.getBoolean("prasadi");
					if (deleted) {
						mInboxAdapter.removeLoading(position);
						mInboxAdapter.remove(position);
					}
				} catch (JSONException e) {
					e.printStackTrace();
					onFailure(-1);
				}
			}

			@Override
			protected void onFailure(Integer status) {
				mInboxAdapter.removeLoading(position);
			}
		}.execute();
	}

	@Override
	public void onMessageReply(final int position, View rootView) {
		String[] path = {
				CityOfTwo.API,
				getString(R.string.url_is_online)
		};

		mInboxAdapter.setLoading(position);

		final Contact guest = mInboxAdapter.get(position).from;

		String code = guest.code;

		new HttpHandler(
				CityOfTwo.HOST,
				path,
				HttpHandler.POST,
				CityOfTwo.KEY_CODE,
				code
		) {
			@Override
			protected void onSuccess(String response) {
				try {
					JSONObject j = new JSONObject(response);

					boolean online = j.getBoolean(CityOfTwo.KEY_IS_ONLINE);
					if (online) {
						startConversation(guest);
					} else {
						showProfile(guest);
					}
				} catch (JSONException e) {
					e.printStackTrace();
					onFailure(-1);
				}
			}

			@Override
			protected void onFailure(Integer status) {
				mInboxAdapter.removeLoading(position);
			}
		}.execute();
	}

	@Override
	public void onProfileViewed(int position, View rootView) {
		final Contact guest = mInboxAdapter.get(position).from;

		showProfile(guest);
	}

	private void showProfile(Contact guest) {

	}

	private void startConversation(Contact guest) {

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
//	public interface InboxEventListener {
//		 TODO: Update argument type and name
//		void onListFragmentInteraction(DummyContent.DummyItem item);
//	}


	private class DeleteMessage {
		private HttpHandler httpHandler;
		private String token;

		private DeleteMessage(String token) {
		}

		public void onPostExecute() {
		}
	}

	private class InboxHttpHandler {
		private HttpHandler httpHandler;
		private String token;

		private InboxHttpHandler(String token) {
			this.token = token;

			String[] path = {
					CityOfTwo.API,
					getString(R.string.url_get_messages)
			};

			httpHandler = new HttpHandler(
					CityOfTwo.HOST,
					path
			) {
				@Override
				protected void onSuccess(String response) {
					try {
						ArrayList<Message> contactsmessages = new ArrayList<>();
						JSONArray j = (new JSONObject(response)).getJSONArray(KEY_MESSAGES);
//						JSONArray j = new JSONArray((new JSONObject(response)).getString(KEY_CONTACTS));
						for (int i = 0; i < j.length(); i++) {
							contactsmessages.add(new Message(j.getString(i)));
						}
						mInboxAdapter.setDataset(contactsmessages);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}

				@Override
				protected void onFailure(Integer status) {

				}

				@Override
				protected void onPostExecute() {
					mLoadingView.setVisibility(View.INVISIBLE);
					if (mInboxAdapter.getItemCount() == 0) {
						mMessageList.setVisibility(View.INVISIBLE);
						mEmptyView.setVisibility(View.VISIBLE);
					} else {
						mMessageList.setVisibility(View.VISIBLE);
						mEmptyView.setVisibility(View.INVISIBLE);
					}
				}
			};

			httpHandler.addHeader("Authorization", "Token " + this.token);
		}

		public void execute() {
			httpHandler.execute();
		}
	}

}
