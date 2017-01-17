package com.messenger.cityoftwo;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Aayush on 1/1/2017.
 */

public class Message implements Parcelable {

	public static final Creator<Message> CREATOR = new Creator<Message>() {
		@Override
		public Message createFromParcel(Parcel in) {
			return new Message(in);
		}

		@Override
		public Message[] newArray(int size) {
			return new Message[size];
		}
	};
	String id;
	boolean received;
	String text;
	long time;

	public Message(String id, boolean received, String text, long time) {
		this.id = id;
		this.received = received;
		this.text = text;
		this.time = time;
	}


	public Message(String message) {
		try {
			JSONObject j = new JSONObject(message);
			id = j.getString("id");
			received = j.getBoolean("received");
			text = j.getString("name");
			time = j.getLong("time");
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	protected Message(Parcel in) {
		id = in.readString();
		received = in.readByte() != 0;
		text = in.readString();
		time = in.readLong();
	}

	@Override
	public String toString() {
		JSONObject j = new JSONObject();
		try {
			j.put("id", id);
			j.put("received", received);
			j.put("name", text);
			j.put("time", time);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return j.toString();
	}

	public String getId() {
		return id;
	}

	public boolean getReceived() {
		return received;
	}

	public String getText() {
		return text;
	}

	public long getTime() {
		return time;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {

		dest.writeString(id);
		dest.writeByte((byte) (received ? 1 : 0));
		dest.writeString(text);
		dest.writeLong(time);
	}
}
