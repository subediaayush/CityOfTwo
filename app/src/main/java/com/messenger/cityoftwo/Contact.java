package com.messenger.cityoftwo;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Aayush on 10/7/2016.
 */

public class Contact implements Parcelable {
	public static final Creator<Contact> CREATOR = new Creator<Contact>() {
		@Override
		public Contact createFromParcel(Parcel in) {
			return new Contact(in);
		}

		@Override
		public Contact[] newArray(int size) {
			return new Contact[size];
		}
	};
	String code;
	String name;
	String nickName;
	boolean hasRevealed;
	String icon;
	String status;
	String[] topLikes;

	/*
		code:_code
		nickname:_nickname
		name:_name
		has_revealed:has_revealed
		icon:_icon_url
		status:_status_message
		top_likes:_list_top_likes[3]
		common_likes:_common_likes_counter
	 */
	int commonLikes;

	public Contact(String code, String name, String nickName, boolean hasRevealed, String icon, String status, String[] topLikes, int commonLikes) {
		this.code = code;
		this.name = name;
		this.nickName = nickName;
		this.hasRevealed = hasRevealed;
		this.icon = icon;
		this.status = status;
		this.topLikes = topLikes;
		this.commonLikes = commonLikes;
	}


	public Contact(String contact) {
		try {
			Log.i("Contact", contact);

			JSONObject j = new JSONObject(contact);

			name = j.getString("name");
			nickName = j.getString("nickname");
			hasRevealed = j.getBoolean("has_revealed");
			icon = j.getString("icon");
			status = j.getString("status");
			commonLikes = j.getInt("common_likes");

			JSONArray arr = j.getJSONArray("top_likes");
			int size = arr.length();
			topLikes = new String[size];
			for (int i = 0; i < size; i++)
				topLikes[i] = (String) arr.get(i);

			code = j.getString("code");
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	protected Contact(Parcel in) {
		code = in.readString();
		name = in.readString();
		nickName = in.readString();
		hasRevealed = in.readByte() != 0;
		icon = in.readString();
		status = in.readString();
		topLikes = in.createStringArray();
		commonLikes = in.readInt();
	}

	public String getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

	public boolean isHasRevealed() {
		return hasRevealed;
	}

	public String getIcon() {
		return icon;
	}

	public String getStatus() {
		return status;
	}

	public String[] getTopLikes() {
		return topLikes;
	}

	public int getCommonLikes() {
		return commonLikes;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {

		dest.writeString(code);
		dest.writeString(name);
		dest.writeString(nickName);
		dest.writeByte((byte) (hasRevealed ? 1 : 0));
		dest.writeString(icon);
		dest.writeString(status);
		dest.writeStringArray(topLikes);
		dest.writeInt(commonLikes);
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof Contact && getCode().equals(((Contact) o).getCode());
	}

	@Override
	public String toString() {
		JSONObject j = new JSONObject();
		try {
			j.put("name", name);
			j.put("nickname", nickName);
			j.put("has_revealed", hasRevealed);
			j.put("icon", icon);
			j.put("status", status);
			j.put("common_likes", commonLikes);
			j.put("topLikes", topLikes);
			j.put("code", code);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return j.toString();
	}
}
