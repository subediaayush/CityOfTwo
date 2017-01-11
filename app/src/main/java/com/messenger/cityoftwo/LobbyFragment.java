package com.messenger.cityoftwo;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.LabeledIntent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
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

import java.util.ArrayList;
import java.util.List;

import static com.messenger.cityoftwo.CityOfTwo.KEY_CODE;
import static com.messenger.cityoftwo.CityOfTwo.KEY_CREDITS;
import static com.messenger.cityoftwo.CityOfTwo.KEY_DISTANCE;
import static com.messenger.cityoftwo.CityOfTwo.KEY_MATCH_FEMALE;
import static com.messenger.cityoftwo.CityOfTwo.KEY_MATCH_MALE;
import static com.messenger.cityoftwo.CityOfTwo.KEY_MAX_AGE;
import static com.messenger.cityoftwo.CityOfTwo.KEY_MIN_AGE;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link LobbyEventListener} interface
 * to handle interaction events.
 * Use the {@link LobbyFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LobbyFragment extends Fragment {

	private static final String ARG_TOKEN = "mToken";
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

	public LobbyFragment() {
		// Required empty public constructor
	}

	/**
	 * Use this factory method to create a new instance of
	 * this fragment using the provided parameters.
	 *
	 * @param token User Token.
	 * @return A new instance of fragment LobbyFragment.
	 */
	// TODO: Rename and change types and number of parameters
	public static LobbyFragment newInstance(String token) {
		LobbyFragment fragment = new LobbyFragment();
		Bundle args = new Bundle();

		args.putString(ARG_TOKEN, token);

		fragment.setArguments(args);
		return fragment;
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
//		mErrorDescription = (TextView) view.findViewById(R.code.status_error_description);
		mStatusContainer = view.findViewById(R.id.lobby_status);

		mMatchesContainer = view.findViewById(R.id.matches_container);
		mMatches = (ViewGroup) view.findViewById(R.id.matches);
		mMatchesPlaceholder = view.findViewById(R.id.matches_placeholder);
		mMatchesCounter = (TextView) view.findViewById(R.id.matches_counter);
		mSurpriseButton = (Button) view.findViewById(R.id.matches_surprise);
		mRescanButton = (Button) view.findViewById(R.id.matches_rescan);

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

		mSurpriseButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startRandomConversation();
			}
		});

		mRescanButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				waitForServer();
				initStatusView();
				initMatchesView();
			}
		});

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

	private void changeFilters() {
		FiltersFragment filtersFragment = FiltersFragment.newInstance();
		filtersFragment.setOnDialogEventListener(new FiltersFragment.OnDialogEventListener() {
			@Override
			public void OnFiltersApply(JSONObject filters) {
				sendFilters(filters);
			}

			@Override
			public void OnCoyRudyShared() {
				referCoyRudy();
			}
		});

		filtersFragment.show(getChildFragmentManager(), "Filter");
	}

	public void sendFilters(final JSONObject filters) {
		final SharedPreferences sp = getContext().getSharedPreferences(
				CityOfTwo.PACKAGE_NAME,
				Context.MODE_PRIVATE
		);

		try {
			filters.put(KEY_CREDITS, 200);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		String send_message = getString(R.string.url_send_filter);

		String[] Path = {CityOfTwo.API, send_message};

		HttpHandler filterHttpHandler = new HttpHandler(CityOfTwo.HOST, Path, HttpHandler.POST, filters) {

			@Override
			protected void onSuccess(String response) {
				try {
					JSONObject Response = new JSONObject(response);

					Boolean status = Response.getBoolean("parsadi");

					if (!status) onFailure(getResponseStatus());
					else {
						Integer minimumAge = filters.getInt(KEY_MIN_AGE),
								maximumAge = filters.getInt(KEY_MAX_AGE),
								maximumDistance = filters.getInt(KEY_DISTANCE);
						Boolean matchFemale = filters.getBoolean(KEY_MATCH_FEMALE),
								matchMale = filters.getBoolean(KEY_MATCH_MALE);

						Integer credits = Response.getInt(KEY_CREDITS);

						sp.edit().putInt(CityOfTwo.KEY_MIN_AGE, minimumAge)
								.putInt(CityOfTwo.KEY_MAX_AGE, maximumAge)
								.putInt(CityOfTwo.KEY_DISTANCE, maximumDistance)
								.putBoolean(CityOfTwo.KEY_MATCH_MALE, matchMale)
								.putBoolean(CityOfTwo.KEY_MATCH_FEMALE, matchFemale)
								.putBoolean(CityOfTwo.KEY_FILTERS_APPLIED, true)
								.apply();

						new SecurePreferences(getContext(), CityOfTwo.PACKAGE_NAME)
								.edit().putInt(CityOfTwo.KEY_CREDITS, credits).apply();

						initFiltersView();
					}
				} catch (Exception e) {
					onFailure(getResponseStatus());
					e.printStackTrace();
				}
			}

			@Override
			protected void onFailure(Integer status) {
				final Snackbar s = Snackbar.make(getView(),
						"Could not apply filters",
						Snackbar.LENGTH_SHORT
				);

				s.setAction("Try Again", new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						sendFilters(filters);
					}
				});

				View view = s.getView();
				view.setBackgroundColor(ContextCompat.getColor(
						getContext(),
						R.color.colorSnackBarError
				));

				view.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						s.dismiss();
					}
				});

				s.setActionTextColor(ContextCompat.getColor(
						getContext(),
						R.color.colorSnackBarText
				));

				s.show();

			}
		};

		String token = "Token " + mToken;

		filterHttpHandler.addHeader("Authorization", token);
		filterHttpHandler.execute();

	}

	public void referCoyRudy() {
		AlertDialog.Builder adb = new AlertDialog.Builder(getContext(), R.style.AppTheme_Dialog);
		adb.setTitle("Refer CoyRudy");

		View referCoyRudyView = LayoutInflater.from(getContext())
				.inflate(R.layout.layout_share_coyrudy, null);
		TextView linkTextView = (TextView) referCoyRudyView.findViewById(R.id.coyrudy_link);

		String uniqueCode = new SecurePreferences(getContext(), CityOfTwo.SECURED_PREFERENCE)
				.getString(KEY_CODE, "");

		final String shareText = getString(R.string.url_share_coyrudy) + uniqueCode;

		linkTextView.setText(shareText);

		adb.setView(referCoyRudyView);
		adb.setPositiveButton("Share", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				shareCoyRudy(shareText);
			}
		});
		adb.setNegativeButton("Copy Link", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Context context = getContext();
				android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context
						.getSystemService(Context.CLIPBOARD_SERVICE);
				android.content.ClipData clip = android.content.ClipData
						.newPlainText("Unique URL", shareText);
				clipboard.setPrimaryClip(clip);
			}
		});
		adb.show();

	}

	private void shareCoyRudy(String uniqueUrl) {

		List<String> targetedApps = new ArrayList<>();

		targetedApps.add("com.facebook.katana");
		targetedApps.add("com.facebook.orca");
		targetedApps.add("com.twitter.android");
		targetedApps.add("com.google.android.apps.plus");

		List<LabeledIntent> shareIntentList = new ArrayList<>();
		Intent shareIntent = new Intent(Intent.ACTION_SEND);
		shareIntent.setType("text/plain");

		List<ResolveInfo> resolveInfoList = getContext().getPackageManager().queryIntentActivities(shareIntent, 0);

		if (!resolveInfoList.isEmpty()) {
			for (ResolveInfo resolveInfo : resolveInfoList) {
				String packageName = resolveInfo.activityInfo.packageName;
				if (targetedApps.contains(packageName)) {
					Intent targetedShareIntent = new Intent(Intent.ACTION_SEND);
					targetedShareIntent.setComponent(new ComponentName(packageName, resolveInfo.activityInfo.name));
					targetedShareIntent.setType("text/plain");
					targetedShareIntent.putExtra(Intent.EXTRA_SUBJECT, "CoyRudy!");
					targetedShareIntent.putExtra(Intent.EXTRA_TEXT, uniqueUrl);

					shareIntentList.add(new LabeledIntent(
							targetedShareIntent,
							packageName,
							resolveInfo.loadLabel(getContext().getPackageManager()),
							resolveInfo.icon
					));
				}
			}
		}
		if (shareIntentList.isEmpty()) {
			final Snackbar s = Snackbar.make(
					getView(),
					"Link copied to clipboard",
					Snackbar.LENGTH_SHORT
			);


			View view = s.getView();
			view.setBackgroundColor(ContextCompat.getColor(
					getContext(),
					R.color.colorSnackBarDefault
			));

			view.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					s.dismiss();
				}
			});

			s.setActionTextColor(ContextCompat.getColor(
					getContext(),
					R.color.colorSnackBarText
			));

			s.show();

			Context context = getContext();
			android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context
					.getSystemService(Context.CLIPBOARD_SERVICE);
			android.content.ClipData clip = android.content.ClipData
					.newPlainText("Unique URL", uniqueUrl);
			clipboard.setPrimaryClip(clip);


			return;
		}

		// convert shareIntentList to array
		LabeledIntent[] extraIntents = shareIntentList.toArray(new LabeledIntent[shareIntentList.size()]);

		Intent chooserIntent = Intent.createChooser(new Intent(), "Select app to share");
		chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, extraIntents);
		startActivity(chooserIntent);
	}


	private void resetFilters() {
		new AlertDialog.Builder(getContext())
				.setTitle("Reset Filters")
				.setMessage("Are you sure you want to reset filters")
				.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						JSONObject filters = new JSONObject();
						try {
							filters.put(CityOfTwo.KEY_MIN_AGE, CityOfTwo.MINIMUM_AGE);
							filters.put(CityOfTwo.KEY_MAX_AGE, CityOfTwo.MAXIMUM_AGE);
							filters.put(CityOfTwo.KEY_DISTANCE, CityOfTwo.MAXIMUM_DISTANCE);
							filters.put(CityOfTwo.KEY_MATCH_MALE, true);
							filters.put(CityOfTwo.KEY_MATCH_FEMALE, true);
						} catch (JSONException e) {
							e.printStackTrace();
						}
						sendFilters(filters);
					}
				})
				.setNegativeButton("No", null)
				.show();
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
		mMatches.removeAllViews();

		mMatchesContainer.setVisibility(View.GONE);
		mMatchesPlaceholder.setVisibility(View.VISIBLE);

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
