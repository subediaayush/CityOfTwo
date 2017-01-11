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
	Contact from;
	String text;
	long time;

	public Message(String id, Contact from, String text, long time) {
		this.id = id;
		this.from = from;
		this.text = text;
		this.time = time;
	}

	public Message(String message) {
		try {
			JSONObject j = new JSONObject(message);
			id = j.getString("id");
			from = new Contact(j.getString("from"));
			text = j.getString("text");
			time = j.getLong("time");
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	protected Message(Parcel in) {
		id = in.readString();
		from = in.readParcelable(Contact.class.getClassLoader());
		text = in.readString();
		time = in.readLong();
	}

	@Override
	public String toString() {
		JSONObject j = new JSONObject();
		try {
			j.put("id", id);
			j.put("from", from.toString());
			j.put("text", text);
			j.put("time", time);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return j.toString();
	}

	public String getId() {
		return id;
	}

	public Contact getFrom() {
		return from;
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
		dest.writeParcelable(from, flags);
		dest.writeString(text);
		dest.writeLong(time);
	}
}
