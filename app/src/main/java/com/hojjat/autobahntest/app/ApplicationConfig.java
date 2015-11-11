package com.hojjat.autobahntest.app;

import android.app.Application;
import android.util.Log;

import com.hojjat.autobahntest.befrest.Befrest;

/**
 * Created by ehsan on 11/11/2015.
 */
public class ApplicationConfig extends Application {
    @Override
    public void onCreate() {
        Log.d("AppConfing", "oncreate");
        super.onCreate();
        Befrest.initialize(this, 0, "", 1);
        Log.d("APPCongif", "Befrest initialized");
    }
}
