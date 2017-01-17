package com.messenger.cityoftwo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
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
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link ContactAdapterWrapper.ContactsEventListener}
 * interface.
 */
public class ContactsFragment extends DialogFragment implements ContactAdapterWrapper.ContactsEventListener {

	private static final String ARG_TOKEN = "token";
	private static final String ARG_MODE = "mode";
	private static final String KEY_MODE = "mode";
	private static final String KEY_CONTACTS = "contacts";
	private static final String KEY_TOKEN = "token";
	private final String ARG_CURRENT_GUEST = "current_guest";
	private String mToken;

	private ArrayList<Contact> mContacts;
	private RecyclerView mContactList;
	private TextView mEmptyView;

	private ProgressBar mLoadingView;
	private ContactAdapterWrapper mContactAdapter;

	private boolean isSectioned;

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
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_contacts_list, container, false);

		mContactList = (RecyclerView) view.findViewById(R.id.contact_list);
		mEmptyView = (TextView) view.findViewById(R.id.empty_view);
		mLoadingView = (ProgressBar) view.findViewById(R.id.loading_view);

		mEmptyView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mEmptyView.setVisibility(View.INVISIBLE);
				mLoadingView.setVisibility(View.VISIBLE);
				mContactList.setVisibility(View.INVISIBLE);

				reloadInfo();
			}
		});


		if (savedInstanceState != null) {
			mContacts = savedInstanceState.getParcelableArrayList(KEY_CONTACTS);
			mToken = savedInstanceState.getString(KEY_TOKEN);
			isSectioned = savedInstanceState.getBoolean(KEY_MODE);

			mLoadingView.setVisibility(View.INVISIBLE);
			if (mContacts.isEmpty()) {
				mContactList.setVisibility(View.INVISIBLE);
				mEmptyView.setVisibility(View.VISIBLE);
			} else {
				mContactList.setVisibility(View.VISIBLE);
				mEmptyView.setVisibility(View.INVISIBLE);
			}
		} else {
			mContactList.setVisibility(View.INVISIBLE);
			mEmptyView.setVisibility(View.INVISIBLE);
			mLoadingView.setVisibility(View.VISIBLE);

			reloadInfo();
		}

		Context context = view.getContext();
		mContactList.setLayoutManager(new LinearLayoutManager(context));
		mContactAdapter = new ContactAdapterWrapper(context, isSectioned);
		mContactList.setAdapter(mContactAdapter.getAdapter());

		return view;
	}

	@Override
	public void onDetach() {
		super.onDetach();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments() != null) {
			mToken = getArguments().getString(ARG_TOKEN, "");
			isSectioned = getArguments().getBoolean(ARG_MODE, false);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putParcelableArrayList(KEY_CONTACTS, mContacts);
		outState.putString(KEY_TOKEN, mToken);
		outState.putBoolean(KEY_MODE, isSectioned);
	}

	private void reloadInfo() {
		new ContactsHttpHandler(mToken).execute();
	}

	@Override
	public void onProfileViewed(int position) {
		Contact contact = mContactAdapter.get(position);

		Intent contactIntent = new Intent(getActivity(), ProfileActivity.class);
		contactIntent.putExtra(ARG_CURRENT_GUEST, contact);
	}

	public int getTotalContacts() {
		return mContactAdapter.getItemCount();
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
		private HttpHandler httpHandler;
		private String token;

		private ContactsHttpHandler(String token) {
			this.token = token;

			String[] path = {
					CityOfTwo.API,
					"get-contacts"
			};

			httpHandler = new HttpHandler(
					CityOfTwo.HOST,
					path
			) {
				@Override
				protected void onSuccess(String response) {
					try {
						ArrayList<Contact> contacts = new ArrayList<>();
						JSONArray j = (new JSONObject(response)).getJSONArray(KEY_CONTACTS);
//						JSONArray j = new JSONArray((new JSONObject(response)).getString(KEY_CONTACTS));
						for (int i = 0; i < j.length(); i++) {
							JSONObject j1 = new JSONObject(j.getString(i));
							j1.put(CityOfTwo.KEY_IS_FRIEND, true);
							contacts.add(new Contact(j1.toString()));
						}

						mContacts = contacts;
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
					if (getTotalContacts() == 0) {
						mContactList.setVisibility(View.INVISIBLE);
						mEmptyView.setVisibility(View.VISIBLE);
					} else {
						mContactList.setVisibility(View.VISIBLE);
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
