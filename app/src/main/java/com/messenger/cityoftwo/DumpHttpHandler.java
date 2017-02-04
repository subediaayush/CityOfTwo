package com.messenger.cityoftwo;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Aayush on 1/30/2017.
 */

public abstract class DumpHttpHandler {
	HttpHandler handler;

	public DumpHttpHandler(final Context context, String token, int chatroomId) {
		String dump_chat = context.getString(R.string.url_chat_end);

		JSONObject j = new JSONObject();
		try {
			j.put(CityOfTwo.HEADER_CHATROOM_ID, chatroomId);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		String[] Path = {CityOfTwo.API, dump_chat};

		HttpHandler dumpHttpHandler = new HttpHandler(CityOfTwo.HOST, Path, HttpHandler.POST, j) {
			@Override
			protected void onPostExecute() {
				SharedPreferences sp = context.getSharedPreferences(CityOfTwo.PACKAGE_NAME, Context.MODE_PRIVATE);
				sp.edit().remove(CityOfTwo.KEY_LAST_CHATROOM).apply();

				DumpHttpHandler.this.onPostExecute();
			}
		};

		dumpHttpHandler.addHeader("Authorization", "Token " + token);

		handler = dumpHttpHandler;
	}

	public void execute() {
		handler.execute();
	}

	public abstract void onPostExecute();
}
