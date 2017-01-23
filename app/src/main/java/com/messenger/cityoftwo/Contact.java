package com.messenger.cityoftwo;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
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

	public static final Comparator<Contact> NAME_COMPARATOR = new Comparator<Contact>() {
		@Override
		public int compare(Contact lhs, Contact rhs) {
			return lhs.name.compareTo(rhs.name);
		}
	};

	public static final Comparator<Contact> MESSAGE_COMPARATOR = new Comparator<Contact>() {
		@Override
		public int compare(Contact lhs, Contact rhs) {
			// Declare equal if both contacts have or do not have any message
			if ((lhs.lastMessages.isEmpty() == rhs.lastMessages.isEmpty())) return 0;

			if (lhs.lastMessages.isEmpty()) return 1;
			else return -1;
		}
	};

	public boolean isFriend;
	Integer id;
	String fid;
	String name;
	String nickName;
	boolean hasRevealed;
	String status;
	String[] topLikes;
	ArrayList<Conversation> lastMessages;
	int commonLikes;

	public Contact(Integer code, String fid, String name, String nickName, boolean hasRevealed, String status, String[] topLikes, int commonLikes, boolean isFriend) {
		this.id = code;
		this.fid = fid;
		this.name = name;
		this.nickName = nickName;
		this.hasRevealed = hasRevealed;
		this.status = status;
		this.topLikes = topLikes;
		this.commonLikes = commonLikes;
		this.isFriend = isFriend;

		this.lastMessages = new ArrayList<>();
	}

	public Contact(String contact) {
		try {
			Log.i("Contact", contact);

			JSONObject j = new JSONObject(contact);

			name = j.getString("name");
			nickName = j.getString("nickname");
			hasRevealed = j.getBoolean("has_revealed");
			status = j.getString("status");
			commonLikes = j.getInt("common_likes");
			isFriend = j.getBoolean("is_friend");

			JSONArray arr = j.getJSONArray("top_likes");
			int size = arr.length();
			topLikes = new String[size];

			for (int i = 0; i < size; i++)
				topLikes[i] = (String) arr.get(i);

			lastMessages = new ArrayList<>();
			if (j.has("last_messages")) {
				JSONArray rawMessages = j.getJSONArray("last_messages");
				for (int i = 0; i < rawMessages.length(); i++)
					lastMessages.add(new Conversation(rawMessages.getString(i)));
			}

			id = j.getInt("id");
			fid = j.getString("fbid");
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	protected Contact(Parcel in) {
		isFriend = in.readByte() != 0;
		id = in.readInt();
		fid = in.readString();
		name = in.readString();
		nickName = in.readString();
		hasRevealed = in.readByte() != 0;
		status = in.readString();
		topLikes = in.createStringArray();

		lastMessages = new ArrayList<>();
		in.readTypedList(lastMessages, Conversation.CREATOR);

		commonLikes = in.readInt();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeByte((byte) (isFriend ? 1 : 0));
		dest.writeInt(id);
		dest.writeString(fid);
		dest.writeString(name);
		dest.writeString(nickName);
		dest.writeByte((byte) (hasRevealed ? 1 : 0));
		dest.writeString(status);
		dest.writeStringArray(topLikes);

		dest.writeTypedList(lastMessages);

		dest.writeInt(commonLikes);
	}

	public Integer getId() {
		return id;
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
		return o instanceof Contact && getId().equals(((Contact) o).getId());
	}

	@Override
	public String toString() {
		JSONObject j = new JSONObject();
		try {
			j.put("name", name);
			j.put("nickname", nickName);
			j.put("has_revealed", hasRevealed);
			j.put("status", status);
			j.put("common_likes", commonLikes);
			j.put("topLikes", topLikes);
			j.put("last_messages", new JSONArray(lastMessages));
			j.put("is_friend", isFriend);
			j.put("id", id);
			j.put("fbid", fid);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return j.toString();
	}


	public void setLastMessage(ArrayList<Conversation> messages) {
		lastMessages = messages;
	}
}
