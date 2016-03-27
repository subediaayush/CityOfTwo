package com.messenger.cityoftwo;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Aayush on 1/15/2016.
 */
public class Conversation {


    private final Integer type;
    private final String text;
    private final Date time;

    /**
     * @param text
     * @param type
     * @param time
     */
    public Conversation(String text, Integer type, Date time) {
        this.text = text;
        this.type = type;
        this.time = time;
    }

    public Conversation(String text, Integer type) {
        this.text = text;
        this.type = type;
        this.time = Calendar.getInstance().getTime();
    }

    public Conversation(String conversation) {
        String text;
        Integer type;
        Long time;
        try {
            JSONObject j = new JSONObject(conversation);
            text = j.getString("text");
            type = j.getInt("type");
            time = j.getLong("time");
        } catch (JSONException e) {
            throw new ClassCastException("Could not cast string to class Conversation.");
        }

        this.text = text;
        this.type = type;
        this.time = new Date(time);
    }

    public Integer getType() {
        return type;
    }

    public String getText() {
        return text;
    }

    public Date getTime() {
        return time;
    }

    @Override
    public String toString() {
        String output;
        try {
            JSONObject j = new JSONObject();
            j.put("text", text);
            j.put("type", type);
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
                        getType().equals(c.getType()) &&
                        getTime().getTime() == c.getTime().getTime()
        );
    }
}
