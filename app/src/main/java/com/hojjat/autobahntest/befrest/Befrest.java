package com.hojjat.autobahntest.befrest;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.Locale;

/**
 * Created by ehsan on 11/10/2015.
 */
public class Befrest {
    private static Context context;
    private static int UID;
    private static String AUTH;
    private static long CHID;
    private static Class<? extends BefrestCallbacks> callbacks;

    public static void initialize(Context context, int APP_ID, String AUTH, long USER_ID, Class<? extends BefrestCallbacks> callbacks){
        Befrest.UID = APP_ID;
        Befrest.AUTH = AUTH;
        Befrest.CHID = USER_ID;
        Befrest.callbacks = callbacks;
        storeConstants();
        startPushService();

    }

    private static void storeConstants(){
        //write cons to prefs
    }

    private static void startPushService(){
        //start service
    }

    private static void retrieveConstants(){
        //read cons from prefs
    }

    private static void onDeviceBootCompleted(){
        retrieveConstants();
        startPushService();
    }

    static class Util {
        protected static final String U_ID = "UID";
        protected static final String CH_ID = "CH_ID";
        protected static final String AUTH = "AUTH";
        protected static final String CONNECTION_URL = "CONNECTION_URL";
        protected static final String BEFREST_PREFRENCES = "BEFREST_PREFRENCES";

        protected static boolean isConnectedToInternet(Context context) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        }

        protected static String getConnectionUri(long CH_ID, int U_ID, String AUTH){
            return String.format(Locale.US, "ws://gw.bef.rest:8000/sub?chid=%d&uid=%d&auth=%s", CH_ID, U_ID, AUTH);
        }
    }
}
