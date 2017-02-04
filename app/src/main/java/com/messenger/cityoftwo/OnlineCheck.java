package com.messenger.cityoftwo;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Build;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Aayush on 1/26/2017.
 */
public abstract class OnlineCheck {
	private static final String FRIEND_ID = "friend_id";
	String mToken;
	private HttpHandler handler;

	public OnlineCheck(Context context, final Contact contact, String token, final boolean showDialog) {
		String host = CityOfTwo.HOST;

		mToken = token;

		String[] path = new String[]{
				CityOfTwo.API,
				context.getString(R.string.url_start_conversation)
		};

		final ProgressDialog p;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
			p = new ProgressDialog(context, R.style.AppTheme_Dialog);
		else
			p = new ProgressDialog(context);

		if (showDialog) {
			p.setMessage("Connecting with " + contact.nickName);
			p.setCancelable(false);
			p.show();
		}

		handler = new HttpHandler(host, path, HttpHandler.GET, FRIEND_ID, String.valueOf(contact.id)) {
			@Override
			protected void onSuccess(String response) {
				try {
					JSONObject j = new JSONObject(response);
					Boolean success = j.getBoolean("parsadi");

					if (success) {
						Boolean online = j.getBoolean(CityOfTwo.KEY_IS_AVAILABLE);

						if (online) {
							int requestId = j.getInt(CityOfTwo.KEY_REQUEST_ID);
							isOnline(contact, requestId);
						}
						else isOffline(contact);
					} else {
						onError(new Exception("Cannot connect to the host"));
					}
				} catch (JSONException e) {
					e.printStackTrace();
					onError(e);
				}
			}

			@Override
			protected void onFailure(Integer status) {
				onError(new Exception("Cannot connect to the host"));
			}

			@Override
			protected void onPostExecute() {
				if (showDialog) p.cancel();
			}
		};

		String header = "Token " + mToken;
		handler.addHeader("Authorization", header);
	}

	public void execute() {
		handler.execute();
	}

	abstract void isOnline(Contact contact, int requestId);

	abstract void isOffline(Contact contact);

	abstract void onError(Exception e);
}
