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

public class RequestFragment extends Fragment {
	private static final String ARG_TOKEN = "token";

	private static final String KEY_REQUESTS = "requests";
	private static final String KEY_TOKEN = "token";

	private String mToken;

//	private RequestsEventListener mListener;

	private ArrayList<Message> mRequests;

	private RecyclerView mRequestList;
	private TextView mEmptyView;
	private ProgressBar mLoadingView;
	private RequestAdapter mRequestAdapter;

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public RequestFragment() {
	}

	public static RequestFragment newInstance(String token) {
		RequestFragment fragment = new RequestFragment();
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
		View view = inflater.inflate(R.layout.fragment_request, container, false);

		mRequestList = (RecyclerView) view.findViewById(R.id.request_list);
		mEmptyView = (TextView) view.findViewById(R.id.empty_view);
		mLoadingView = (ProgressBar) view.findViewById(R.id.loading_view);

		mEmptyView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mEmptyView.setVisibility(View.INVISIBLE);
				mLoadingView.setVisibility(View.VISIBLE);
				mRequestList.setVisibility(View.INVISIBLE);

				reloadInfo();
			}
		});


		if (savedInstanceState != null) {
			mRequests = savedInstanceState.getParcelableArrayList(KEY_REQUESTS);
			mToken = savedInstanceState.getString(KEY_TOKEN);

			mLoadingView.setVisibility(View.INVISIBLE);
			if (mRequests.isEmpty()) {
				mRequestList.setVisibility(View.INVISIBLE);
				mEmptyView.setVisibility(View.VISIBLE);
			} else {
				mRequestList.setVisibility(View.VISIBLE);
				mEmptyView.setVisibility(View.INVISIBLE);
			}
		} else {
			mRequestList.setVisibility(View.INVISIBLE);
			mEmptyView.setVisibility(View.INVISIBLE);
			mLoadingView.setVisibility(View.VISIBLE);

			reloadInfo();
		}

		Context context = view.getContext();
		mRequestAdapter = new RequestAdapter(context);
		mRequestList.setLayoutManager(new LinearLayoutManager(context));
		mRequestList.setAdapter(mRequestAdapter);

		return view;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putParcelableArrayList(KEY_REQUESTS, mRequests);
		outState.putString(KEY_TOKEN, mToken);
	}

	@Override
	public void onDetach() {
		super.onDetach();
//		mListener = null;
	}

	private void reloadInfo() {
		new RequestHttpHandler(mToken).execute();
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
//	public interface RequestsEventListener {
	// TODO: Update argument type and name
//		void onListFragmentInteraction(DummyContent.DummyItem item);
//	}

	private class RequestHttpHandler {
		private HttpHandler httpHandler;
		private String token;

		private RequestHttpHandler(String token) {
			this.token = token;

			String[] path = {
					CityOfTwo.API,
					getString(R.string.url_get_requests)
			};

			httpHandler = new HttpHandler(
					CityOfTwo.HOST,
					path
			) {
				@Override
				protected void onSuccess(String response) {
					try {
						ArrayList<Message> contactsmessages = new ArrayList<>();
						JSONArray j = (new JSONObject(response)).getJSONArray(KEY_REQUESTS);
//						JSONArray j = new JSONArray((new JSONObject(response)).getString(KEY_CONTACTS));
						for (int i = 0; i < j.length(); i++) {
							contactsmessages.add(new Message(j.getString(i)));
						}
						mRequestAdapter.setDataset(contactsmessages);
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
					if (mRequestAdapter.getItemCount() == 0) {
						mRequestList.setVisibility(View.INVISIBLE);
						mEmptyView.setVisibility(View.VISIBLE);
					} else {
						mRequestList.setVisibility(View.VISIBLE);
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
