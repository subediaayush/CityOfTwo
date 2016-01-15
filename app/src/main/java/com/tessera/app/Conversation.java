package com.tessera.app;

/**
 * Created by Aayush on 1/15/2016.
 */
public class Conversation {

    private final Type type;
    private final String text;

    public static enum Type {
        SENT,
        RECEIVED
    }

    public Conversation (String text, Type type){
        this.text = text;
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public String getText() {
        return text;
    }
}
