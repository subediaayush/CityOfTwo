package com.messenger.cityoftwo;

/**
 * Created by Aayush on 1/15/2016.
 */
public class Conversation {

    public static final Integer SENT = 0,
            RECEIVED = 1;

    private final Integer type;
    private final String text;

    /**
     * @param text
     * @param type
     */
    public Conversation(String text, Integer type) {
        this.text = text;
        this.type = type;
    }

    public Integer getType() {
        return type;
    }

    public String getText() {
        return text;
    }

}
