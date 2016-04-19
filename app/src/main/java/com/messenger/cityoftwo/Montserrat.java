package com.messenger.cityoftwo;

import android.content.Context;
import android.graphics.Typeface;

/**
 * Created by Aayush on 3/19/2016.
 */
public class Montserrat {

    private static Montserrat instance;
    private static Typeface typeface;

    public static Montserrat getInstance(Context context) {
        synchronized (Montserrat.class) {
            if (instance == null) {
                instance = new Montserrat();
                typeface = Typeface.createFromAsset(context.getAssets(), "montserrat_alternates.ttf");
            }
            return instance;
        }
    }

    public Typeface getTypeFace() {
        return typeface;
    }
}
