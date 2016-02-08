package com.messenger.cityoftwo;

import android.os.AsyncTask;
import android.util.Log;

import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Created by Aayush on 8/2/2015.
 */
public abstract class HttpHandler extends AsyncTask<Void, Void, Void> {

    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    private OkHttpClient client;
    private String Response;
    private Integer Status;
    private HttpUrl URL;
    private Boolean Success;

    public HttpHandler(String host, String[] Path, String[] qHeaders, String[] qValues) {
        this.client = new OkHttpClient();
        this.client.setConnectTimeout(10, TimeUnit.SECONDS);
        this.client.setReadTimeout(10, TimeUnit.SECONDS);

        this.Status = -1;
        this.Response = "";

        HttpUrl.Builder urlBuilder = new HttpUrl.Builder();
        try {
            urlBuilder.scheme("http")
                    .host(host);
        } catch (Exception e) {
            Log.e("Http Connection", "Error while building valid url: " + host);
        }

        if (Path.length > 0)
            for (String path : Path)
                urlBuilder.addPathSegment(path);

        if (qHeaders.length == qValues.length)
            for (int i = 0; i < qHeaders.length; i++)
                if (qValues[i] != null) urlBuilder.addQueryParameter(qHeaders[i], qValues[i]);

        HttpUrl url = urlBuilder.build();

        Log.i("Http URL", "URL set to " + url.toString());
        this.URL = url;
    }


    public HttpHandler(String host, String[] Path) {
        this(host, Path, EMPTY_STRING_ARRAY, EMPTY_STRING_ARRAY);
    }

    public HttpHandler(String host) {
        this(host, EMPTY_STRING_ARRAY);
    }

    public HttpHandler() {
        this("");
    }

    public String getResponse() {
        return Response;
    }

    public Integer getResponseStatus() {
        return Status;
    }

    public Boolean getSuccess() {
        return Success;
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            Request request = new Request.Builder().url(URL).build();

            Response response = client.newCall(request).execute();

            this.Response = response.body().string();
            this.Status = response.code();
            this.Success = response.isSuccessful();

            Log.i("Http Connection", "Call to " + URL.toString() + " completed");

        } catch (IOException e) {
            Log.i("Http Connection", "Call to " + URL.toString() + " failed with an exception: " + e.toString());
        }

        return null;
    }

    public HttpUrl getURL() {
        return URL;
    }

    public void setURL(HttpUrl URL) {
        this.URL = URL;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if (getResponseStatus() == 200){
            onSuccess(getResponse());
        } else {
            onFailure(getResponseStatus());
        }
    }

    @Override
    protected void onPreExecute() {
        onPreRun();
    }

    protected abstract void onPreRun();
    protected abstract void onSuccess(String response);
    protected abstract void onFailure(Integer status);
}
