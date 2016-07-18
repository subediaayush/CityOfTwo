package com.messenger.cityoftwo;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Aayush on 1/15/2016.
 */
public class Conversation {


    private Integer flags;
    private String text;
    private Date time;

    /**
     * @param text
     * @param flags
     * @param time
     */
    public Conversation(String text, Integer flags, Date time) {
        this.text = text;
        this.flags = flags;
        this.time = time;
    }

    public Conversation(String text, Integer flags) {
        this(text, flags, Calendar.getInstance().getTime());
    }

    public Conversation(String text, Date time) {
        this(text, 0, time);
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
            messageTime = Calendar.getInstance().getTime().getTime();
        }

        this.text = messageText;
        this.flags = messageType;
        this.time = new Date(messageTime);
    }

    public Integer getFlags() {
        return flags;
    }

    public String getText() {
        return text;
    }

    public Date getTime() {
        return time;
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
    public String toString() {
        String output;
        try {
            JSONObject j = new JSONObject();
            j.put("text", text);
            j.put("flags", flags);
            j.put("time", time.getTime());

            output = j.toString();
        } catch (JSONException e) {
            throw new ClassCastException("Could not cast Conversation object to string.");
        }

        return output;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;

        if (!Conversation.class.isAssignableFrom(o.getClass())) return false;

        Conversation c = (Conversation) o;
        return (
                getText().equals(c.getText()) &&
                        getFlags().equals(c.getFlags()) &&
                        getTime().getTime() == c.getTime().getTime()
        );
    }
}
