package com.oddrun.befrest;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import java.util.Locale;

/**
 * Created by ehsan on 11/10/2015.
 */
public class Befrest {
    static String TAG = "Befrest";
    protected static final boolean DEBUG = true;

    private static Context context;
    private static int U_ID;
    private static String AUTH;
    private static long CH_ID;

    public static void initialize(Context context, int APP_ID, String AUTH, long USER_ID) {
        Befrest.context = context;
        Befrest.U_ID = APP_ID;
        Befrest.AUTH = AUTH;
        Befrest.CH_ID = USER_ID;
        runPushService();
    }

    private static boolean stopFormerServiceIfExist(){
        return Util.stopPushService(context);
    }

    public static void reInitialize(Context context, int APP_ID, String AUTH, long USER_ID) {
        /* only a wrapper of initialize for clarity.
        initialize can be used for re initializing
        */
        initialize(context, APP_ID, AUTH, USER_ID);
    }

    private static void runPushService() {
        stopFormerServiceIfExist();
        Util.startPushService(context);
    }

    static class Util {
        protected static final String ACTION_PUSH_RECIEVED = "com.oddrun.befrest.broadcasts.PUSH_RECEIVED";
        protected static final String KEY_MESSAGE_PASSED = "KEY_MESSAGE_PASSED";
        private static final String BROADCAST_SENDING_PERMISSION_POSTFIX = ".permission.PUSH_SERVICE";


        protected static boolean isConnectedToInternet(Context context) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
            if (DEBUG) Log.d(TAG, "isConnectedToInternet : " + isConnected);
            return isConnected;
        }

        protected static String getConnectionUri() {
            return String.format(Locale.US, "ws://gw.bef.rest:8000/sub?chid=%d&uid=%d&auth=%s", Befrest.CH_ID, Befrest.U_ID, Befrest.AUTH);
        }

        protected static String getBroadcastSendingPermission(Context context) {
            return context.getApplicationContext().getPackageName() + BROADCAST_SENDING_PERMISSION_POSTFIX;
        }

        public static void startPushService(Context context) {
            if (DEBUG) Log.d(TAG, "starting PushService");
            context.startService(new Intent(context, PushService.class));
        }

        public static boolean stopPushService(Context context) {
            if (DEBUG) Log.d(TAG, "stopping PushService");
            return context.stopService(new Intent(context, PushService.class));
        }
    }
}
