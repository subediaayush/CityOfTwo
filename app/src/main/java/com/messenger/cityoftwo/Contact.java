package com.messenger.cityoftwo;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Comparator;

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
	public static final Comparator<Contact> FRIEND_COMPARATOR = new Comparator<Contact>() {
		@Override
		public int compare(Contact lhs, Contact rhs) {
			if (lhs.isFriend == rhs.isFriend) return 0;

			if (lhs.isFriend) return -1;
			else return 1;
		}
	};
	public static final Comparator<Contact> MESSAGE_COMPARATOR = new Comparator<Contact>() {
		@Override
		public int compare(Contact lhs, Contact rhs) {
			// Declare equal if both contacts have or do not have any message
			if ((lhs.lastMessage == null) == (rhs.lastMessage == null)) return 0;

			if (lhs.lastMessage == null) return 1;
			else return -1;
		}
	};
	public static final Comparator<Contact> NAME_COMPARATOR = new Comparator<Contact>() {
		@Override
		public int compare(Contact lhs, Contact rhs) {
			return lhs.name.compareTo(rhs.name);
		}
	};
	public boolean isFriend;
	String code;
	String name;
	String nickName;
	boolean hasRevealed;
	String status;
	String[] topLikes;
	Conversation lastMessage;
	int commonLikes;

	public Contact(String code, String name, String nickName, boolean hasRevealed, String icon, String status, String[] topLikes, int commonLikes, boolean isFriend, Conversation lastMessage) {
		this.code = code;
		this.name = name;
		this.nickName = nickName;
		this.hasRevealed = hasRevealed;
		this.status = status;
		this.topLikes = topLikes;
		this.commonLikes = commonLikes;
		this.isFriend = isFriend;
		this.lastMessage = lastMessage;
	}

	public Contact(String contact) {
		try {
			Log.i("Contact", contact);

			JSONObject j = new JSONObject(contact);

			name = j.getString("name");
			nickName = j.getString("nickname");
			hasRevealed = j.getBoolean("has_revealed");
			status = j.getString("status");
			lastMessage = new Conversation(j.getString("message"));
			commonLikes = j.getInt("common_likes");
			isFriend = j.getBoolean("is_friend");

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
		isFriend = in.readByte() != 0;
		code = in.readString();
		name = in.readString();
		nickName = in.readString();
		hasRevealed = in.readByte() != 0;
		status = in.readString();
		topLikes = in.createStringArray();
		lastMessage = in.readParcelable(Conversation.class.getClassLoader());
		commonLikes = in.readInt();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeByte((byte) (isFriend ? 1 : 0));
		dest.writeString(code);
		dest.writeString(name);
		dest.writeString(nickName);
		dest.writeByte((byte) (hasRevealed ? 1 : 0));
		dest.writeString(status);
		dest.writeStringArray(topLikes);
		dest.writeParcelable(lastMessage, flags);
		dest.writeInt(commonLikes);
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
			j.put("status", status);
			j.put("message", lastMessage);
			j.put("common_likes", commonLikes);
			j.put("topLikes", topLikes);
			j.put("is_friend", isFriend);
			j.put("code", code);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return j.toString();
	}
}
