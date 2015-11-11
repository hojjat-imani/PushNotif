package com.hojjat.autobahntest.app;

import android.app.Application;
import android.util.Log;

import com.hojjat.autobahntest.befrest.Befrest;

/**
 * Created by ehsan on 11/11/2015.
 */
public class ApplicationConfig extends Application {
    static final int APP_ID = 0;
    static final int USER_ID = 0;
    static final String AUTH = "";

    private final String TAG = getClass().getSimpleName();
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "initializing Befrest");
        Befrest.initialize(this, APP_ID, AUTH, USER_ID);
    }
}