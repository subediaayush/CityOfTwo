package com.messenger.cityoftwo;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Comparator;

import static com.messenger.cityoftwo.CityOfTwo.FLAG_END;
import static com.messenger.cityoftwo.CityOfTwo.FLAG_START;

/**
 * Created by Aayush on 1/15/2016.
 */
public class Conversation implements Parcelable {


	public static final Comparator<Conversation> FLAGS_COMPARATOR = new Comparator<Conversation>() {
		@Override
		public int compare(Conversation o1, Conversation o2) {
			if (o1.getFlags().equals(o2.getFlags())) return 0;

			if (o1.containsFlag(FLAG_START) || o2.containsFlag(FLAG_END)) return -1;
			if (o2.containsFlag(FLAG_START) || o1.containsFlag(FLAG_END)) return 1;

			return 0;
		}
	};
	public static final Comparator<Conversation> TIME_COMPARATOR = new Comparator<Conversation>() {
		@Override
		public int compare(Conversation o1, Conversation o2) {
			return (int) Math.signum(o1.getTime() - o2.getTime());
		}
	};
	public static final Comparator<Conversation> CONVERSATION_COMPARATOR = new Comparator<Conversation>() {
		@Override
		public int compare(Conversation lhs, Conversation rhs) {
			int res = FLAGS_COMPARATOR.compare(lhs, rhs);

			return res == 0 ? TIME_COMPARATOR.compare(lhs, rhs) : res;
		}
	};
	public static final Creator<Conversation> CREATOR = new Creator<Conversation>() {
		@Override
		public Conversation createFromParcel(Parcel in) {
			return new Conversation(in);
		}

		@Override
		public Conversation[] newArray(int size) {
			return new Conversation[size];
		}
	};
	private Integer flags;
	private String text;
	private long time;

	/**
	 * @param text
	 * @param flags
	 * @param time
	 */
	public Conversation(String text, Integer flags, long time) {
		this.text = text;
		this.flags = flags;
		this.time = time;
	}

	public Conversation(String text, Integer flags) {
		this(text, flags, System.currentTimeMillis());
	}

	public Conversation(String conversation) {
		String messageText;
		Integer messageType;
		Long messageTime;

		try {
			JSONObject j = new JSONObject(conversation);
			messageText = j.getString("data");
			messageType = j.getInt("flags");
			messageTime = j.getLong("time");
		} catch (JSONException e) {
			e.printStackTrace();
			messageText = conversation;
			messageType = 0;
			messageTime = System.currentTimeMillis();
		}

		this.text = messageText;
		this.flags = messageType;
		this.time = messageTime;
	}

	protected Conversation(Parcel in) {
		text = in.readString();
		time = in.readLong();
		flags = in.readInt();
	}

	public Integer getFlags() {
		return flags;
	}

	public String getText() {
		return text;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public void addFlag(int flag) {
		this.flags |= flag;
	}

	public void setFlag(int flag) {
		this.flags = flag;
	}

	public void removeFlag(int flag) {
		this.flags &= ~flag;
	}

	public boolean containsFlag(int flag) {
		return (flags & flag) == flag;
	}

	public void resetFlag() {
		this.flags = 0;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) return false;

		if (!Conversation.class.isAssignableFrom(o.getClass())) return false;

		Conversation c = (Conversation) o;
		return (
				getText().equals(c.getText()) &&
						getFlags().equals(c.getFlags()) &&
						getTime() == c.getTime()
		);
	}

	@Override
	public String toString() {
		String output;
		try {
			JSONObject j = new JSONObject();
			j.put("data", text);
			j.put("flags", flags);
			j.put("time", time);

			output = j.toString();
		} catch (JSONException e) {
			throw new ClassCastException("Could not cast Conversation object to string.");
		}

		return output;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {

		dest.writeString(this.text);
		dest.writeLong(this.time);
		dest.writeInt(this.flags);
	}
}
