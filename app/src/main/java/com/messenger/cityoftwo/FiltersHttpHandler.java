package com.messenger.cityoftwo;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;

import static android.content.Context.MODE_PRIVATE;
import static com.messenger.cityoftwo.CityOfTwo.KEY_CREDITS;
import static com.messenger.cityoftwo.CityOfTwo.KEY_DISTANCE;
import static com.messenger.cityoftwo.CityOfTwo.KEY_MATCH_FEMALE;
import static com.messenger.cityoftwo.CityOfTwo.KEY_MATCH_MALE;
import static com.messenger.cityoftwo.CityOfTwo.KEY_MAX_AGE;
import static com.messenger.cityoftwo.CityOfTwo.KEY_MIN_AGE;

/**
 * Created by Aayush on 1/17/2017.
 */
public abstract class FiltersHttpHandler {

	Context mContext;
	JSONObject mFilters;

	private HttpHandler httpHandler;

	public FiltersHttpHandler(Context context, JSONObject filters) {
		if (filters == null) filters = new JSONObject();

		mContext = context;
		mFilters = filters;

		String url = mContext.getString(R.string.url_send_filter);

		String[] Path = {CityOfTwo.API, url};

		httpHandler = new HttpHandler(CityOfTwo.HOST, Path, HttpHandler.POST, mFilters) {
			@Override
			protected void onSuccess(String response) {
				handleSuccess(response);
			}

			@Override
			protected void onFailure(Integer status) {
				onFiltersFailed(mFilters);

			}
		};
	}

	private void handleSuccess(String response) {
		try {
			JSONObject Response = new JSONObject(response);

			Boolean status = Response.getBoolean("parsadi");

			SharedPreferences sp = mContext.getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE);

			if (!status) onFiltersFailed(mFilters);
			else {
				Integer minimumAge = mFilters.getInt(KEY_MIN_AGE),
						maximumAge = mFilters.getInt(KEY_MAX_AGE),
						maximumDistance = mFilters.getInt(KEY_DISTANCE);
				Boolean matchFemale = mFilters.getBoolean(KEY_MATCH_FEMALE),
						matchMale = mFilters.getBoolean(KEY_MATCH_MALE);

				Integer credits = Response.getInt(KEY_CREDITS);

				sp.edit().putInt(CityOfTwo.KEY_MIN_AGE, minimumAge)
						.putInt(CityOfTwo.KEY_MAX_AGE, maximumAge)
						.putInt(CityOfTwo.KEY_DISTANCE, maximumDistance)
						.putBoolean(CityOfTwo.KEY_MATCH_MALE, matchMale)
						.putBoolean(CityOfTwo.KEY_MATCH_FEMALE, matchFemale)
						.putBoolean(CityOfTwo.KEY_FILTERS_APPLIED, true)
						.apply();

				mFilters.put(KEY_CREDITS, credits);

				new SecurePreferences(mContext, CityOfTwo.PACKAGE_NAME)
						.edit().putInt(CityOfTwo.KEY_CREDITS, credits).apply();

				onFiltersApplied(mFilters);
			}
		} catch (Exception e) {
			onFiltersFailed(mFilters);
			e.printStackTrace();
		}
	}

	public void resetFilters() {
		try {
			mFilters.put(KEY_MIN_AGE, CityOfTwo.MINIMUM_AGE);
			mFilters.put(KEY_MAX_AGE, CityOfTwo.MAXIMUM_AGE);
			mFilters.put(KEY_DISTANCE, CityOfTwo.MAXIMUM_DISTANCE);
			mFilters.put(KEY_MATCH_MALE, true);
			mFilters.put(KEY_MATCH_FEMALE, true);
		} catch (JSONException ignored) {
		}

		String url = mContext.getString(R.string.url_send_filter);

		String[] Path = {CityOfTwo.API, url};

		new HttpHandler(CityOfTwo.HOST, Path, HttpHandler.POST, mFilters) {
			@Override
			protected void onSuccess(String response) {
				handleSuccess(response);
			}

			@Override
			protected void onFailure(Integer status) {
				onFiltersFailed(mFilters);

			}
		}.execute();
	}

	protected abstract void onFiltersApplied(JSONObject filters);

	protected abstract void onFiltersFailed(JSONObject filters);

	public void execute() {
		httpHandler.execute();
	}
}
