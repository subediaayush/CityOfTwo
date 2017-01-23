package com.messenger.cityoftwo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
public class ContactsFragment extends DialogFragment implements ContactAdapterWrapper.ContactsEventListener {

	public final static String ARG_SEARCH_MODE = "mode";
	public final static String ARG_CONTACTS = "contacts";
	public final static String ARG_TOKEN = "token";

	public final static int SEARCH_MODE_CONTACTS = 0b01;
	public final static int SEARCH_MODE_MATCHES = 0b10;

	private static final String TAG = "ContactsFragment";

	private final String KEY_MESSAGE_RECEIVED = "message_received";
	private final String KEY_MESSAGE_SENT = "message_sent";
	private final String KEY_MESSAGES = "messages";

	private String mToken;

	private ArrayList<Contact> mContacts;
	private RecyclerView mContactList;
	private TextView mEmptyView;

	private HashMap<Integer, ArrayList<Conversation>> mRecentMessages;

	private ProgressBar mLoadingView;
	private ContactAdapterWrapper mContactAdapter;

	private int searchMode;

	private ContactsFragmentListener mListener;

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public ContactsFragment() {
	}

	public static ContactsFragment newInstance() {
		return new ContactsFragment();
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
	public void onDetach() {
		super.onDetach();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments() != null) {
			mToken = getArguments().getString(ARG_TOKEN, "");
			searchMode = getArguments().getInt(ARG_SEARCH_MODE, SEARCH_MODE_CONTACTS);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putParcelableArrayList(ARG_CONTACTS, mContacts);
		outState.putString(ARG_TOKEN, mToken);
		outState.putInt(ARG_SEARCH_MODE, searchMode);
	}

	private void reloadInfo() {
		new ContactsHttpHandler(mToken).execute();
	}

	@Override
	public void onProfileViewed(int position) {
		Contact contact = mContactAdapter.get(position);

		Intent contactIntent = new Intent(getActivity(), ProfileActivity.class);

		contactIntent.putExtra(
				ProfileActivity.ARG_PROFILE_MODE,
				ChatAdapter.MODE_CHAT
		);

		if (mListener != null) mListener.onContactSelected(contact);

		contactIntent.putExtra(ProfileActivity.ARG_CURRENT_GUEST, contact);

		startActivityForResult(contactIntent, CityOfTwo.ACTIVITY_PROFILE);
	}

	public int getTotalContacts() {
		return mContactAdapter.getItemCount();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == CityOfTwo.ACTIVITY_PROFILE) {
			Contact c = data.getParcelableExtra(ProfileActivity.ARG_CURRENT_GUEST);
			mContactAdapter.update(c);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_contacts_list, container, false);

		mContactList = (RecyclerView) view.findViewById(R.id.contact_list);
		mEmptyView = (TextView) view.findViewById(R.id.empty_view);
		mLoadingView = (ProgressBar) view.findViewById(R.id.loading_view);

		mEmptyView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mEmptyView.setVisibility(View.GONE);
				mLoadingView.setVisibility(View.VISIBLE);
				mContactList.setVisibility(View.INVISIBLE);

				reloadInfo();
			}
		});


		if (savedInstanceState != null) {
			mContacts = savedInstanceState.getParcelableArrayList(ARG_CONTACTS);
			mToken = savedInstanceState.getString(ARG_TOKEN);
			searchMode = savedInstanceState.getInt(ARG_SEARCH_MODE);


			mLoadingView.setVisibility(View.GONE);
			if (mContacts.isEmpty()) {
				mContactList.setVisibility(View.INVISIBLE);
				mEmptyView.setVisibility(View.VISIBLE);
			} else {
				mContactList.setVisibility(View.VISIBLE);
				mEmptyView.setVisibility(View.GONE);
			}
		} else {
			mContactList.setVisibility(View.INVISIBLE);
			mEmptyView.setVisibility(View.GONE);
			mLoadingView.setVisibility(View.VISIBLE);

			reloadInfo();
		}

		Context context = view.getContext();
		mContactList.setLayoutManager(new LinearLayoutManager(context));
		mContactAdapter = new ContactAdapterWrapper(context, searchMode);
		mContactList.setAdapter(mContactAdapter.getAdapter());

		mContactAdapter.setEventListener(this);

		return view;
	}

	public void setListener(ContactsFragmentListener listener) {
		this.mListener = mListener;
	}

	/**
	 * Created by Aayush on 1/23/2017.
	 */
	public static interface ContactsFragmentListener {
		void onContactSelected(Contact contact);
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
	private class ContactsHttpHandler {
		private HttpHandler contactsHttpHandler;
		private HttpHandler matchesHttpHandler;

		private String token;
		private boolean listInitiated = false;

		private ContactsHttpHandler(String token) {
			this.token = token;

			String[] pathContacts = {
					CityOfTwo.API,
					getString(R.string.url_get_messages)
			};

			String[] pathMatches = {
					CityOfTwo.API,
					getString(R.string.url_get_matches)
			};

			if ((searchMode & SEARCH_MODE_CONTACTS) == SEARCH_MODE_CONTACTS)
				contactsHttpHandler = new HttpHandler(
						CityOfTwo.HOST,
						pathContacts
				) {

					@Override
					protected void onSuccess(String response) {
						addContacts(response, true);
						refreshContactList();
					}

					@Override
					protected void onFailure(Integer status) {
						refreshContactList();
					}


				};

			if ((searchMode & SEARCH_MODE_MATCHES) == SEARCH_MODE_MATCHES)
				matchesHttpHandler = new HttpHandler(
						CityOfTwo.HOST,
						pathMatches
				) {

					@Override
					protected void onSuccess(String response) {
						addContacts(response, false);
						refreshContactList();
					}

					@Override
					protected void onFailure(Integer status) {
						refreshContactList();
					}


				};

			contactsHttpHandler.addHeader("Authorization", "Token " + this.token);
		}

		private void addContacts(String response, boolean isFriend) {
			try {
				ArrayList<Contact> contacts = new ArrayList<>();
				JSONArray contactListJSON = (new JSONObject(response)).getJSONArray(KEY_MESSAGES);
//						JSONArray contactListJSON = new JSONArray((new JSONObject(response)).getString(ARG_CONTACTS));
				for (int i = 0; i < contactListJSON.length(); i++) {
					JSONObject contactJSON = contactListJSON.getJSONObject(i);
					contactJSON.put(CityOfTwo.KEY_IS_FRIEND, isFriend);
					Contact c = new Contact(contactJSON.toString());

					ArrayList<Conversation> messsages = new ArrayList<>();

					JSONObject rawConversation;
					Conversation buffer;

					rawConversation = contactJSON.getJSONObject(KEY_MESSAGE_RECEIVED);
					Log.i(TAG, rawConversation.toString());

					buffer = new Conversation(rawConversation.toString());
//							buffer.setFlag(Integer.parseInt(buffer.getFlags().toString(),2));
					buffer.removeFlag(CityOfTwo.FLAG_SENT);
					buffer.addFlag(CityOfTwo.FLAG_RECEIVED);
					messsages.add(buffer);

					rawConversation = contactJSON.getJSONObject(KEY_MESSAGE_SENT);
					Log.i(TAG, rawConversation.toString());

					buffer = new Conversation(rawConversation.toString());
//							buffer.setFlag(Integer.parseInt(buffer.getFlags().toString(),2));
					buffer.removeFlag(CityOfTwo.FLAG_RECEIVED);
					buffer.addFlag(CityOfTwo.FLAG_SENT);
					messsages.add(buffer);

					c.setLastMessage(messsages);
					contacts.add(c);
				}

				if (listInitiated) {
					mContacts.addAll(contacts);
					mContactAdapter.insertAll(mContacts);
				} else {
					mContacts = contacts;
					mContactAdapter.setDataset(mContacts);
					listInitiated = true;
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		private void refreshContactList() {
			mLoadingView.setVisibility(View.GONE);
			if (getTotalContacts() == 0) {
				mContactList.setVisibility(View.INVISIBLE);
				mEmptyView.setVisibility(View.VISIBLE);
			} else {
				mContactList.setVisibility(View.VISIBLE);
				mEmptyView.setVisibility(View.GONE);
			}
		}

		public void execute() {
			contactsHttpHandler.execute();
		}
	}
}
