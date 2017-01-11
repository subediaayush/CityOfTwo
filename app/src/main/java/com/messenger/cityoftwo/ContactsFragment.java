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
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link ContactsAdapter.ContactsEventListener}
 * interface.
 */
public class ContactsFragment extends Fragment implements ContactsAdapter.ContactsEventListener {

	private static final String ARG_TOKEN = "token";

	private static final String KEY_CONTACTS = "contacts";
	private static final String KEY_TOKEN = "token";

	private String mToken;

	private ArrayList<Contact> mContacts;

	private RecyclerView mContactList;
	private TextView mEmptyView;
	private ProgressBar mLoadingView;

	private ContactsAdapter mContactAdapter;

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public ContactsFragment() {
	}

	public static ContactsFragment newInstance(String token) {
		ContactsFragment fragment = new ContactsFragment();
		Bundle args = new Bundle();

		args.putString(ARG_TOKEN, token);
		fragment.setArguments(args);
		return fragment;
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
			mToken = getArguments().getString(ARG_TOKEN);
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
				mEmptyView.setVisibility(View.INVISIBLE);
				mLoadingView.setVisibility(View.VISIBLE);
				mContactList.setVisibility(View.INVISIBLE);

				reloadInfo();
			}
		});


		if (savedInstanceState != null) {
			mContacts = savedInstanceState.getParcelableArrayList(KEY_CONTACTS);
			mToken = savedInstanceState.getString(KEY_TOKEN);

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
		mContactAdapter = new ContactsAdapter(context);
		mContactList.setLayoutManager(new LinearLayoutManager(context));
		mContactList.setAdapter(mContactAdapter);

		mContactAdapter.setEventListener(this);

		return view;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putParcelableArrayList(KEY_CONTACTS, mContacts);
		outState.putString(KEY_TOKEN, mToken);
	}

	@Override
	public void onDetach() {
		super.onDetach();
	}

	private void reloadInfo() {
		new ContactsHttpHandler(mToken).execute();
	}

	@Override
	public void onProfileViewed(int position) {

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
							contacts.add(new Contact(j.getString(i)));
						}
						mContactAdapter.setDataset(contacts);
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
					if (mContactAdapter.getItemCount() == 0) {
						mContactList.setVisibility(View.INVISIBLE);
						mEmptyView.setVisibility(View.VISIBLE);
					} else {
						mContactList.setVisibility(View.VISIBLE);
						mEmptyView.setVisibility(View.INVISIBLE);
					}
				}
			};

			httpHandler.addHeader("Authorization", "Token " + token);
		}

		public void execute() {
			httpHandler.execute();
		}
	}
}
