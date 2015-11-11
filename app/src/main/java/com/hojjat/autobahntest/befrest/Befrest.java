package com.hojjat.autobahntest.befrest;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.util.Locale;

/**
 * Created by ehsan on 11/10/2015.
 */
public class Befrest {
    static String TAG = "Befrest";
    private static Context context;
    private static int U_ID;
    private static String AUTH;
    private static long CH_ID;

    public static void initialize(Context context, int APP_ID, String AUTH, long USER_ID){
        Befrest.context = context;
        Befrest.U_ID = APP_ID;
        Befrest.AUTH = AUTH;
        Befrest.CH_ID = USER_ID;
        storeConstants();
        startPushService();
    }

    private static void storeConstants(){
        //write cons to prefs
    }

    private static void startPushService(){
        //start service
        Log.d(TAG, "starting test service");
    }

    private static void retrieveConstants(){
        //read cons from prefs
    }

    private static void onDeviceBootCompleted(){
        retrieveConstants();
        startPushService();
    }

    static class Util {
        protected static final String U_ID = "U_ID";
        protected static final String CH_ID = "CH_ID";
        protected static final String AUTH = "AUTH";
        protected static final String CONNECTION_URL = "CONNECTION_URL";
        protected static final String BEFREST_PREFRENCES = "BEFREST_PREFRENCES";
        protected static final String ACTION_PUSH_RECIEVED = "com.oddrun.befrest.broadcasts.PUSH_RECEIVED";
        protected static final String KEY_MESSAGE_PASSED = "KEY_MESSAGE_PASSED";
        private static final String BROADCAST_SENDING_PERMISSION_POSTFIX =".permission.PUSH_SERVICE";


        protected static boolean isConnectedToInternet(Context context) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        }

        protected static String getConnectionUri(){
            return String.format(Locale.US, "ws://gw.bef.rest:8000/sub?chid=%d&uid=%d&auth=%s", Befrest.CH_ID, Befrest.U_ID, Befrest.U_ID);
        }

        protected static String getBroadcastSendingPermission(Context context){
            return context.getApplicationContext().getPackageName() + BROADCAST_SENDING_PERMISSION_POSTFIX;
        }
    }
}
