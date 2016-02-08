package com.messenger.cityoftwo;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.util.Base64;
import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Aayush on 2/5/2016.
 */
public class TesseraApplication extends Application {

    public static final String ACCESS_TOKEN = "ACCESS_TOKEN",
            PROFILE = "PROFILE";
    public static final int LOGIN = 1, TEST = 2;

    @Override
    public void onCreate() {
        super.onCreate();
        printHashKey();
    }

    private void printHashKey() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.tessera.messenger",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }
    }
}
