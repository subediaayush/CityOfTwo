package com.messenger.cityoftwo;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
public class ContactsFragment extends Fragment implements ContactAdapterWrapper.ContactsEventListener, DialogWrappableFragmentInterface
		, EmptyContentWrappableFragmentInterface, ReloadableFragment {

	public final static String ARG_SEARCH_MODE = "mode";
	public final static String ARG_CONTACTS = "contacts";
	public final static String ARG_TOKEN = "token";

	public final static int SEARCH_MODE_CONTACTS = 0b01;
	public final static int SEARCH_MODE_MATCHES = 0b10;

	public static final int MODE_ONLINE = 0;
	public static final int MODE_OFFLINE = 1;

	private static final String TAG = "ContactsFragment";

	private final String KEY_MESSAGE_RECEIVED = "message_received";
	private final String KEY_MESSAGE_SENT = "message_sent";
	private final String KEY_MESSAGES = "messages";
	private final String KEY_MATCHES = "matches";

	private String mToken;

	private ArrayList<Contact> mContacts;
	private RecyclerView mContactList;

	private HashMap<Integer, ArrayList<Conversation>> mRecentMessages;

	private ContactAdapterWrapper mContactAdapter;

	private int mSearchMode;

	private ContactsFragmentListener mListener;
	private DialogFragmentWrapper mDialogFragmentWrapper;
	private EmptyContentFragmentWrapper mEmptyContentFragmentWrapper;

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public ContactsFragment() {
	}

	public static ContactsFragment newInstance(Bundle args) {
		ContactsFragment fragment = new ContactsFragment();
		if (args != null) fragment.setArguments(args);
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
			mToken = getArguments().getString(ARG_TOKEN, "");
			mSearchMode = getArguments().getInt(ARG_SEARCH_MODE, SEARCH_MODE_CONTACTS);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_contacts_list, container, false);

		mContactList = (RecyclerView) view.findViewById(R.id.contact_list);

		if (savedInstanceState != null) {
			mContacts = savedInstanceState.getParcelableArrayList(ARG_CONTACTS);
			mToken = savedInstanceState.getString(ARG_TOKEN);
			mSearchMode = savedInstanceState.getInt(ARG_SEARCH_MODE);
		} else {
			reloadInfo();
		}

		Context context = view.getContext();
		mContactList.setLayoutManager(new LinearLayoutManager(context));
		mContactAdapter = new ContactAdapterWrapper(context, mSearchMode);
		mContactList.setAdapter(mContactAdapter.getAdapter());

		mContactAdapter.setEventListener(this);

		return view;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putParcelableArrayList(ARG_CONTACTS, mContacts);
		outState.putString(ARG_TOKEN, mToken);
		outState.putInt(ARG_SEARCH_MODE, mSearchMode);
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
		Contact contact = mContactAdapter.get(position);
		if (mListener != null) mListener.onContactSelected(contact);
	}

	public int getTotalContacts() {
		return mContactAdapter.getItemCount();
	}

	protected void reloadContact(Contact contact, int position) {
		mContactAdapter.update(contact, position);
	}

	public void setListener(ContactsFragmentListener listener) {
		this.mListener = listener;
	}

	@Override
	public void setDialogWrapper(DialogFragmentWrapper wrapper) {
		mDialogFragmentWrapper = wrapper;
	}

	public void reloadFragment() {
		reloadInfo();
	}

	@Override
	public void setContentWrapper(EmptyContentFragmentWrapper wrapper) {
		mEmptyContentFragmentWrapper = wrapper;
	}

	@Override
	public void reloadContent() {
		reloadInfo();
	}

	private void notifyObservers(boolean error) {
		if (!error) {
			int totalItems = mContacts.size();

			if (mEmptyContentFragmentWrapper != null)
				mEmptyContentFragmentWrapper.isContentEmpty(totalItems == 0);
//			mEmptyContentFragmentWrapper.isContentEmpty(totalItems != 0);
			if (mListener != null) mListener.onContactsLoaded(mContacts.size());
		}

//						if (mListener != null) mListener.onContactsLoaded(0);
		if (mDialogFragmentWrapper != null)
			mDialogFragmentWrapper.onItemsPrepared();

	}

	/**
	 * Created by Aayush on 1/23/2017.
	 */
	public interface ContactsFragmentListener {
		void onContactSelected(Contact contact);

		void onContactsLoaded(int totalContacts);

		void onContactLoadError();
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

			if ((mSearchMode & SEARCH_MODE_CONTACTS) == SEARCH_MODE_CONTACTS) {
				contactsHttpHandler = new HttpHandler(
						CityOfTwo.HOST,
						pathContacts
				) {

					@Override
					protected void onSuccess(String response) {
						addContacts(response, SEARCH_MODE_CONTACTS);
					}

					@Override
					protected void onFailure(Integer status) {
						if (mListener != null) mListener.onContactLoadError();
					}
				};
				contactsHttpHandler.addHeader("Authorization", "Token " + this.token);
			}
			if ((mSearchMode & SEARCH_MODE_MATCHES) == SEARCH_MODE_MATCHES) {
				matchesHttpHandler = new HttpHandler(
						CityOfTwo.HOST,
						pathMatches
				) {

					@Override
					protected void onSuccess(String response) {
						addContacts(response, SEARCH_MODE_MATCHES);
					}

					@Override
					protected void onFailure(Integer status) {
						if (mListener != null) mListener.onContactLoadError();
					}
				};
				matchesHttpHandler.addHeader("Authorization", "Token " + this.token);
			}
		}

		private void addContacts(String response, int searchMode) {
			try {
				ArrayList<Contact> contacts = new ArrayList<>();
				JSONArray contactListJSON;
				boolean isFriend;
				if ((searchMode & SEARCH_MODE_CONTACTS) == SEARCH_MODE_CONTACTS) {
					contactListJSON = (new JSONObject(response)).getJSONArray(KEY_MESSAGES);
					isFriend = true;
				} else {
					contactListJSON = (new JSONObject(response)).getJSONArray(KEY_MATCHES);
					isFriend = false;
				}

//						JSONArray contactListJSON = new JSONArray((new JSONObject(response)).getString(ARG_CONTACTS));
				for (int i = 0; i < contactListJSON.length(); i++) {
					JSONObject contactJSON = contactListJSON.getJSONObject(i);
					contactJSON.put(CityOfTwo.KEY_IS_FRIEND, isFriend);
					Contact c = new Contact(contactJSON.toString());

					ArrayList<Conversation> messsages = new ArrayList<>();

					JSONObject rawConversation;
					Conversation buffer;

					rawConversation = contactJSON.getJSONObject(KEY_MESSAGE_RECEIVED);

					if (rawConversation.length() > 0) {
						buffer = new Conversation(rawConversation.toString());
//							buffer.setFlag(Integer.parseInt(buffer.getFlags().toString(),2));
						buffer.removeFlag(CityOfTwo.FLAG_SENT);
						buffer.addFlag(CityOfTwo.FLAG_RECEIVED);
						messsages.add(buffer);
					}

					rawConversation = contactJSON.getJSONObject(KEY_MESSAGE_SENT);
					if (rawConversation.length() > 0) {

						buffer = new Conversation(rawConversation.toString());
//							buffer.setFlag(Integer.parseInt(buffer.getFlags().toString(),2));
						buffer.removeFlag(CityOfTwo.FLAG_RECEIVED);
						buffer.addFlag(CityOfTwo.FLAG_SENT);
						messsages.add(buffer);
					}

					c.setLastMessage(messsages);
					contacts.add(c);
				}

				if (listInitiated) {
					mContacts.addAll(contacts);
					mContactAdapter.insertAll(mContacts);
					notifyObservers(false);
				} else {
					mContacts = contacts;
					mContactAdapter.setDataset(mContacts);
					listInitiated = true;

					if (Integer.bitCount(mSearchMode) == 1) {
						notifyObservers(false);
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
				notifyObservers(true);
			}
		}

		public void execute() {
			if ((mSearchMode & SEARCH_MODE_CONTACTS) == SEARCH_MODE_CONTACTS)
				contactsHttpHandler.execute();
			if ((mSearchMode & SEARCH_MODE_MATCHES) == SEARCH_MODE_MATCHES)
				matchesHttpHandler.execute();
		}
	}
}
