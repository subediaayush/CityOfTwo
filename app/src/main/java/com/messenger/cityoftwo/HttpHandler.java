package com.messenger.cityoftwo;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Iterator;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.messenger.cityoftwo.CityOfTwo.StringToArray;
import static java.lang.Integer.parseInt;

/**
 * Created by Aayush on 8/2/2015.
 */
public abstract class HttpHandler extends AsyncTask<Void, Void, Void> {

    public static final int GET = 0,
            POST = 1;

    public static final MediaType MEDIA_TYPE_MARKDOWN
            = MediaType.parse("application/json");

    private static final String[] EMPTY_STRING_ARRAY = new String[0];
    private OkHttpClient client;
    private String Response;
    private Integer Status;
    private HttpUrl URL;
    private Boolean Success;
    private Integer Type;
    private JSONObject Params;
    private Request.Builder RequestBuilder;

    public HttpHandler(int type) {
        this.client = new OkHttpClient.Builder()
                .addInterceptor(new CurlLoggingInterceptor())
                .build();

        this.RequestBuilder = new Request.Builder();

        this.Status = -1;
        this.Response = "";
        this.Type = type;

    }

    public HttpHandler(String host, String[] path, Integer type, JSONObject params) {
        this(type);

        HttpUrl.Builder urlBuilder = new HttpUrl.Builder();
        try {
            String[] h = host.split(":");

            urlBuilder.scheme("http")
                    .host(h[0]);
            if (h.length > 1)
                urlBuilder.port(parseInt(h[1]));

        } catch (Exception e) {
            Log.e("Http Connection", "Error while building valid url: " + host);
        }

        if (path.length > 0)
            for (String p : path)
                urlBuilder.addPathSegment(p);

        this.Params = params;

        Iterator<?> Headers = params.keys();

        if (Type == GET) while (Headers.hasNext()) {
            try {
                String header = (String) Headers.next(),
                        value = (String) params.get(header);
                urlBuilder.addQueryParameter(header, value);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        HttpUrl url = urlBuilder.build();

        Log.i("Http URL", "URL set to " + url.toString());
        this.URL = url;
        RequestBuilder.url(URL);
    }

    public HttpHandler(String host, String[] path, JSONObject params) {
        this(host, path, GET, params);
    }

    public HttpHandler(String host, String[] path, Integer type, String[] qHeaders, String[] qValues) {
        this(host, path, type, createJSON(qHeaders, qValues));
    }

    public HttpHandler(String host, String[] path, String[] qHeaders, String[] qValues) {
        this(host, path, GET, qHeaders, qValues);
    }

    public HttpHandler(String host, String[] path, Integer type, String Header, String Value) {
        this(host, path, type, StringToArray(Header), StringToArray(Value));
    }

    public HttpHandler(String host, String[] path, String header, String value) {
        this(host, path, GET, header, value);
    }

    public HttpHandler(String host, String[] path) {
        this(host, path, EMPTY_STRING_ARRAY, EMPTY_STRING_ARRAY);
    }

    public HttpHandler(String host) {
        this(host, EMPTY_STRING_ARRAY);
    }

    private static JSONObject createJSON(String[] headers, String[] values) {
        JSONObject j = new JSONObject();
        int counter = 0;
        for (String header : headers)
            try {
                j.put(header, values[counter++]);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        return j;
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
        Request.Builder b = RequestBuilder;

        if (Type == POST) {
            String p = "{\"access_token\":\"CAAXwkyvUUdwBAAmLAJbtBxDLZBUjnfLGefZCqXI5ji93PN2eO7QotVwzV7GKD65KlRZCUcYXZB4gfhxRDSUs13QbIXOXH0Xb8ZAM9PR0AXCYLljijtDJX05ZCMObTgv2SijLfcUp9mf76JgRbaJNrTTO1vtFoJCrPYjlov51v4QTqL1YCbyHUTZC9gOoIPFmcezS0NSQBXbsg986fgNUVLTW6Mis5IKOFxysNgum17ZADwZDZD\"}";

            String param = Params.toString();
            b.post(RequestBody.create(MEDIA_TYPE_MARKDOWN, param))
                    .addHeader("content-type", "application/json")
                    .addHeader("cache-control", "no-cache");

        }

        Request request = b.build();

        try {
            Response response = client.newCall(request).execute();

            this.Response = response.body().string();
            this.Status = response.code();
            this.Success = response.isSuccessful();

            Log.i("Http Connection", "Call to " + URL.toString() + " completed");
            Log.i("Http Response", response.message());
        } catch (IOException e) {
            Log.i("Http Connection", "Call to " + URL.toString() + " failed with an exception: " + e.toString());
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("Http Connection", "Call to " + URL.toString() + " failed");
        }

        return null;
    }

    public HttpUrl getURL() {
        return URL;
    }

    public void setURL(HttpUrl URL) {
        this.URL = URL;
    }

    public void addHeader(String header, String value) {
        RequestBuilder.addHeader(header, value);
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if (getResponseStatus() != 404) {
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
