package com.messenger.cityoftwo;

import android.content.Context;

import com.facebook.AccessToken;

/**
 * Created by Aayush on 7/18/2016.
 */
public abstract class FacebookSignUp {
    String token;
    HttpHandler loginHttpHandler;

    public FacebookSignUp(Context context, AccessToken accessToken) {
        this.token = accessToken.getToken();

        String[] path = {context.getString(R.string.url_signup)};
        String header = CityOfTwo.HEADER_ACCESS_TOKEN;

        loginHttpHandler = new HttpHandler(
                CityOfTwo.HOST,
                path,
                HttpHandler.POST,
                header,
                token
        ) {
            @Override
            protected void onSuccess(String response) {
                FacebookSignUp.this.onSuccess(response);
            }

            @Override
            protected void onFailure(Integer status) {
                FacebookSignUp.this.onFailure(status);
            }
        };
    }

    abstract void onSuccess(String response);

    abstract void onFailure(Integer status);

    public void execute() {
        loginHttpHandler.execute();
    }
}
