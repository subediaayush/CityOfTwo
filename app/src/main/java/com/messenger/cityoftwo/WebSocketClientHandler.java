package com.messenger.cityoftwo;

import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

/**
 * Created by Aayush on 1/31/2016.
 */
public class WebSocketClientHandler extends WebSocketClient {
    public WebSocketClientHandler(URI serverURI) {
        super(serverURI);
        Log.i("Websocket Client", "Connected to " + serverURI);
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        Log.i("Websocket Client", "Connection Established - " + handshakedata.toString());
    }

    @Override
    public void onMessage(String message) {
        Log.i("Websocket Client", "Message Received: " + message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        Log.i("Websocket Client", "Connection Closed");

    }

    @Override
    public void onError(Exception ex) {
        Log.i("Websocket Client", "Error - " + ex.toString());
    }
}
