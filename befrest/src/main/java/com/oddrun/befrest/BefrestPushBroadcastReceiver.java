package com.oddrun.befrest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by ehsan on 11/11/2015.
 */
public abstract class BefrestPushBroadcastReceiver extends BroadcastReceiver {
    public final String TAG = getClass().getSimpleName();

    @Override
    public final void onReceive(Context context, Intent intent) {
        String intentAction = intent.getAction();
        if (Befrest.DEBUG) Log.d(TAG, "BroadCastReceived : " + intentAction);
        switch (intentAction) {
            case Befrest.ACTION_PUSH_RECIEVED:
                onPushReceived(context, intent.getStringExtra(Befrest.Util.KEY_MESSAGE_PASSED));
                break;
            case Befrest.Util.ACTION_UNAUTHORIZED:
                onAuthorizeProblem(context);
                break;
            case ConnectivityManager.CONNECTIVITY_ACTION:
                startOrStopPushService(context);
        }
    }

    private void startOrStopPushService(Context context) {
        if (Befrest.Util.isConnectedToInternet(context))
            Befrest.Util.startPushService(context);
//        else Befrest.Util.stopPushService(context);
    }

    abstract public void onPushReceived(Context context, String message);

    public void onAuthorizeProblem(Context context) {
        Log.d(TAG, "Befrest : Authorization Problem!");
    }
}