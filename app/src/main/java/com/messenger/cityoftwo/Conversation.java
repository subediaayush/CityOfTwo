package com.messenger.cityoftwo;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Comparator;

/**
 * Created by Aayush on 1/15/2016.
 */
public class Conversation {


    public static final Comparator<Conversation> CONVERSATION_COMPARATOR = new Comparator<Conversation>() {
        @Override
        public int compare(Conversation lhs, Conversation rhs) {
            return (int) Math.signum(lhs.getComparableValue() - rhs.getComparableValue());
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
            messageText = j.getString("text");
            messageType = j.getInt("flags");
            messageTime = j.getLong("time");
        } catch (JSONException e) {
            messageText = conversation;
            messageType = 0;
            messageTime = System.currentTimeMillis();
        }

        this.text = messageText;
        this.flags = messageType;
        this.time = messageTime;
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
            j.put("text", text);
            j.put("flags", flags);
            j.put("time", time);

            output = j.toString();
        } catch (JSONException e) {
            throw new ClassCastException("Could not cast Conversation object to string.");
        }

        return output;
    }

    private long getComparableValue() {
        if ((getFlags() & CityOfTwo.FLAG_START) == CityOfTwo.FLAG_START)
            return Long.MIN_VALUE / 2;
        if ((getFlags() & CityOfTwo.FLAG_END) == CityOfTwo.FLAG_END)
            return Long.MAX_VALUE / 2;
        if ((getFlags() & CityOfTwo.FLAG_AD) == CityOfTwo.FLAG_AD)
            return Long.MAX_VALUE / 2 - 1;
        if ((getFlags() & CityOfTwo.FLAG_INDICATOR) == CityOfTwo.FLAG_INDICATOR)
            return Long.MAX_VALUE / 2 - 2;
        return getTime();
    }
}
