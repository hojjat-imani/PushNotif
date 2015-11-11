package com.oddrun.befrest;

import android.content.Context;
import android.content.Intent;
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

    public static void initialize(Context context, int APP_ID, String AUTH, long USER_ID) {
        Befrest.context = context;
        Befrest.U_ID = APP_ID;
        Befrest.AUTH = AUTH;
        Befrest.CH_ID = USER_ID;
        startPushService();
    }

    private static boolean stopFormerServiceIfExist(Context context) {
        return context.stopService(new Intent(context, PushService.class));
    }

    public static void reInitialize(Context context, int APP_ID, String AUTH, long USER_ID) {
        /* only a wrapper of initialize for clarity
        initialize can be used for re initializing
        */
        initialize(context, APP_ID, AUTH, USER_ID);
    }

    private static void startPushService() {
        Log.d(TAG, "starting PushService");
        if (stopFormerServiceIfExist(context)) {
            //service was running and stopped
            //no need to start the service again as it starts itself in onDestroy()
        } else {
            context.startService(new Intent(context, PushService.class));
        }
    }

    static class Util {
        protected static final String ACTION_PUSH_RECIEVED = "com.oddrun.befrest.broadcasts.PUSH_RECEIVED";
        protected static final String KEY_MESSAGE_PASSED = "KEY_MESSAGE_PASSED";
        private static final String BROADCAST_SENDING_PERMISSION_POSTFIX = ".permission.PUSH_SERVICE";


        protected static boolean isConnectedToInternet(Context context) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        }

        protected static String getConnectionUri() {
            return String.format(Locale.US, "ws://gw.bef.rest:8000/sub?chid=%d&uid=%d&auth=%s", Befrest.CH_ID, Befrest.U_ID, Befrest.AUTH);
        }

        protected static String getBroadcastSendingPermission(Context context) {
            return context.getApplicationContext().getPackageName() + BROADCAST_SENDING_PERMISSION_POSTFIX;
        }
    }
}
