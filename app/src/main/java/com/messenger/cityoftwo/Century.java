package com.messenger.cityoftwo;

import android.content.Context;
import android.graphics.Typeface;

/**
 * Created by Aayush on 3/19/2016.
 */
public class Century {

    private static Century instance;
    private static Typeface typeface;

    public static Century getInstance(Context context) {
        synchronized (Century.class) {
            if (instance == null) {
                instance = new Century();
                typeface = Typeface.createFromAsset(context.getAssets(), "c_gothic.ttf");
            }
            return instance;
        }
    }

    public Typeface getTypeFace() {
        return typeface;
    }
}
