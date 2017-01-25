package com.messenger.cityoftwo;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link LobbyEventListener} interface
 * to handle interaction events.
 * Use the {@link LobbyFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LobbyFragment extends Fragment {

	public static final String ARG_TOKEN = "token";
	private static final String TAG = "LobbyFragment";
	private String mToken;

	private TextView mStatusDescription;
	private ImageView mErrorIndicator;
	private ImageView mSuccessIndicator;
	private ProgressBar mProgressIndicator;
	private TextView mErrorDescription;
	private View mStatusContainer;

	private View mMatchesContainer;
	private ViewGroup mMatches;
	private View mMatchesPlaceholder;
	private TextView mMatchesCounter;
	private Button mSurpriseButton;
	private Button mRescanButton;

	private TextView mFilterDistance;
	private TextView mFilterGender;
	private TextView mFilterAge;
	private Button mFilterReset;
	private Button mFilterChange;

	private Boolean mErrorEncountered = false;


	private LobbyEventListener mListener;
	private View mMatchesCardContainer;

	public LobbyFragment() {
		// Required empty public constructor
	}

	/**
	 * Use this factory method to create a new instance of
	 * this fragment using the provided parameters.
	 *
	 * @return A new instance of fragment LobbyFragment.
	 */
	// TODO: Rename and change types and number of parameters
	public static LobbyFragment newInstance() {
		return new LobbyFragment();
	}

	// TODO: Rename method, update argument and hook method into UI event
	public void onButtonPressed(Uri uri) {
		if (mListener != null) {
			mListener.onFragmentInteraction(uri);
		}
	}

//	@Override
//	public void onAttach(Context context) {
//		super.onAttach(context);
//		if (context instanceof LobbyEventListener) {
//			mListener = (LobbyEventListener) context;
//		} else {
//			throw new RuntimeException(context.toString()
//					+ " must implement LobbyEventListener");
//		}
//	}

	private void changeFilters() {
		Intent filterIntent = new Intent(getContext(), FilterActivity.class);

		startActivityForResult(filterIntent, CityOfTwo.ACTIVITY_FILTER);
	}

	private void resetFilters() {
		final ProgressDialog p = new ProgressDialog(getContext(), R.style.AppTheme_Dialog);
		p.setTitle("Applying Filters");
		p.setMessage("Please wait while we apply your filters");
		p.setCancelable(false);
		p.show();

		new FiltersHttpHandler(getContext(), null) {

			@Override
			protected void onFiltersApplied(JSONObject filters) {
				p.cancel();
				initFiltersView();
				waitForServer();
			}

			@Override
			protected void onFiltersFailed(JSONObject filters) {
				p.cancel();
				handleFilterResetFailure();
			}
		}.resetFilters();
	}

	private void handleFilterResetFailure() {
		new AlertDialog.Builder(getContext(), R.style.AppTheme_Dialog)
				.setTitle("Error")
				.setMessage("There was an error while applying filters." +
						" Do you want to try again?")
				.setNegativeButton("Cancel", null)
				.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						resetFilters();
					}
				}).show();
	}

	private void startRandomConversation() {

	}

	private void initFiltersView() {
		SharedPreferences sp = getContext().getSharedPreferences(
				CityOfTwo.PACKAGE_NAME,
				Context.MODE_PRIVATE
		);

		int minAge = sp.getInt(CityOfTwo.KEY_MIN_AGE, CityOfTwo.MINIMUM_AGE);
		int maxAge = sp.getInt(CityOfTwo.KEY_MAX_AGE, CityOfTwo.MAXIMUM_AGE);
		int distance = sp.getInt(CityOfTwo.KEY_DISTANCE, CityOfTwo.MAXIMUM_DISTANCE);
		boolean matchMale = sp.getBoolean(CityOfTwo.KEY_MATCH_MALE, true);
		boolean matchFemale = sp.getBoolean(CityOfTwo.KEY_MATCH_FEMALE, true);

		try {
			JSONObject j = new JSONObject();
			j.put("min_age", minAge);
			j.put("max_age", maxAge);
			j.put("distance", distance);
			j.put("match_male", matchMale);
			j.put("match_female", matchFemale);
			Log.i(TAG, j.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		mFilterDistance.setText((distance == CityOfTwo.MAXIMUM_DISTANCE)
				? "Anywhere"
				: "Within " + distance + " kilometers"
		);

		mFilterGender.setText((matchMale && matchFemale)
				? "Anyone"
				: "Only " + (matchMale ? "men" : "women")
		);

		mFilterAge.setText((minAge == CityOfTwo.MINIMUM_AGE && maxAge == CityOfTwo.MAXIMUM_AGE)
				? "Any age"
				: (minAge == maxAge)
				? minAge + " years old" : "From " + minAge + " to " + maxAge + " years old"
		);

		Log.i(TAG, "Filters flushed");
	}

	private void initMatchesView() {
		Bundle contactsArgs = new Bundle();
		contactsArgs.putString(ContactsFragment.ARG_TOKEN, mToken);
		contactsArgs.putInt(ContactsFragment.ARG_SEARCH_MODE, ContactsFragment.SEARCH_MODE_MATCHES);

		ContactsFragment contactsFragment = ContactsFragment.newInstance();
		contactsFragment.setArguments(contactsArgs);

		getChildFragmentManager().beginTransaction()
				.replace(R.id.matches_container, contactsFragment)
				.commit();

		contactsFragment.setListener(new ContactsFragment.ContactsFragmentListener() {
			@Override
			public void onContactSelected(Contact contact, int position) {

			}

			@Override
			public void onContactsLoaded(int number) {
				showSearchSuccess();
				if (number == 0){
					mStatusDescription.setText("No matches found");
				} else {
					mMatchesCardContainer.setVisibility(View.VISIBLE);
					mStatusDescription.setText("Matches loaded");
				}
			}

			@Override
			public void onContactLoadError() {
				showSearchFailure();
			}
		});
	}

	private void waitForServer() {
		mStatusDescription.setText("Setting up your profile");

		new LobbyHttpHandler(mToken).execute();
	}

	private void initStatusView() {
		mErrorIndicator.setVisibility(View.INVISIBLE);
		mSuccessIndicator.setVisibility(View.INVISIBLE);
		mProgressIndicator.setVisibility(View.VISIBLE);

//		mErrorDescription.setVisibility(View.GONE);
	}

	private void showSearchSuccess() {
		mProgressIndicator.setVisibility(View.VISIBLE);

		mSuccessIndicator.setVisibility(View.GONE);
		mErrorIndicator.setVisibility(View.GONE);
//		mErrorDescription.setVisibility(View.GONE);

		mStatusDescription.setText("Finding a match");
	}

	private void showSearchFailure() {
		mErrorEncountered = true;

		mErrorIndicator.setVisibility(View.VISIBLE);

		mProgressIndicator.setVisibility(View.GONE);
		mSuccessIndicator.setVisibility(View.GONE);

		mStatusDescription.setText("There was an error. Tap here to try again.");
//		mErrorDescription.setVisibility(View.VISIBLE);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		initFiltersView();

		waitForServer();
	}

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
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.fragment_lobby, container, false);

		mStatusDescription = (TextView) view.findViewById(R.id.status_description);
		mErrorIndicator = (ImageView) view.findViewById(R.id.status_error);
		mSuccessIndicator = (ImageView) view.findViewById(R.id.status_success);
		mProgressIndicator = (ProgressBar) view.findViewById(R.id.status_progress);
//		mErrorDescription = (TextView) view.findViewById(R.id.status_error_description);
		mStatusContainer = view.findViewById(R.id.lobby_status);

		mMatchesContainer = view.findViewById(R.id.matches_container);
//		mMatches = (ViewGroup) view.findViewById(R.id.matches);
		mMatchesCardContainer = view.findViewById(R.id.matches_card_container);
		mMatchesCounter = (TextView) view.findViewById(R.id.matches_counter);
//		mSurpriseButton = (Button) view.findViewById(R.id.matches_surprise);
//		mRescanButton = (Button) view.findViewById(R.id.matches_rescan);

		mFilterDistance = (TextView) view.findViewById(R.id.filter_distance);
		mFilterGender = (TextView) view.findViewById(R.id.filter_gender);
		mFilterAge = (TextView) view.findViewById(R.id.filter_age);
		mFilterReset = (Button) view.findViewById(R.id.filter_reset);
		mFilterChange = (Button) view.findViewById(R.id.filter_change);

		return view;
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		initStatusView();
		initMatchesView();
		initFiltersView();

		mStatusContainer.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mErrorEncountered) {
					mErrorEncountered = false;

					waitForServer();
					initStatusView();
					initMatchesView();
				}
			}
		});

//		mSurpriseButton.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				startRandomConversation();
//			}
//		});

//		mRescanButton.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				waitForServer();
//				initStatusView();
//				initMatchesView();
//			}
//		});

		mFilterReset.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				resetFilters();
			}
		});

		mFilterChange.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				changeFilters();
			}
		});

		waitForServer();

		Log.i(TAG, "View Created");
	}

	/**
	 * This interface must be implemented by activities that contain this
	 * fragment to allow an interaction in this fragment to be communicated
	 * to the activity and potentially other fragments contained in that
	 * activity.
	 */
	public interface LobbyEventListener {
		// TODO: Update argument type and name
		void onFragmentInteraction(Uri uri);
	}

	private class LobbyHttpHandler {

		HttpHandler httpHandler;
		String token;

		public LobbyHttpHandler(String token) {
			this.token = token;

			String[] path = {
					CityOfTwo.API,
					getString(R.string.url_send_to_lobby)
			};

			httpHandler = new HttpHandler(
					CityOfTwo.HOST,
					path,
					HttpHandler.POST
			) {
				@Override
				protected void onSuccess(String response) {
					try {
						JSONObject j = new JSONObject(response);
						Boolean success = j.getBoolean("parsadi");

						if (success) {
							showSearchSuccess();
							initMatchesView();
						} else {
							showSearchFailure();
						}
					} catch (JSONException e) {
						showSearchFailure();
					}
				}

				@Override
				protected void onFailure(Integer status) {
					showSearchFailure();
				}
			};

			httpHandler.addHeader("Authorization", "Token " + token);
		}

		public void execute() {
			httpHandler.execute();
		}
	}

}
