package com.oddrun.befrestExample;

import android.app.Application;
import android.util.Base64;
import android.util.Log;


import com.oddrun.befrest.Befrest;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by ehsan on 11/11/2015.
 */
public class ApplicationConfig extends Application {
    static final int APP_ID = 2;
    static final int USER_ID = 917;
    static final String AUTH = sign();

    private static final String SHARED_KEY = "23e78b4b-079b-4556-aad0-beded33ed064";
    private static final String API_KEY = "e2a29f25-2a38-4cac-bbc5-1cec1a02fba0";

    private final String TAG = getClass().getSimpleName();
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "initializing Befrest");
        Log.d(TAG, "AUTH: " + AUTH);
        Befrest.initialize(this, APP_ID, AUTH, USER_ID);
    }

    private static String sign() {
        MessageDigest dig = null;
        try {
            dig = MessageDigest.getInstance("md5");
        }catch (Exception e){
            e.printStackTrace();
        }
            String payload = String.format("%s,%s", SHARED_KEY, generateTokenInYourServer());

        dig.reset();
        dig.update(payload.getBytes());
        byte[] digest = dig.digest();

        String b64 = Base64.encodeToString(digest, Base64.DEFAULT);
        b64 = b64.replace("+", "-").replace("=", "").replace("/", "_").replace("\n", "");
        Log.d("SIGN", b64);
        return b64;
    }

    private static String generateTokenInYourServer(){
        MessageDigest dig = null;
        try {
            dig = MessageDigest.getInstance("md5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        String payload = String.format("%s,%s,%s", API_KEY, APP_ID, USER_ID);
        dig.reset();
        dig.update(payload.getBytes());
        byte[] digest = dig.digest();

        String b64 = Base64.encodeToString(digest, Base64.DEFAULT);
        b64 = b64.replace("+", "-").replace("=", "").replace("/", "_").replace("\n", "");
        return b64;
    }
}