package com.hojjat.autobahntest.befrest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by ehsan on 11/11/2015.
 */
public abstract class BefrestPushBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("BefrestReceiver", "onreceive");
        String intentAction = intent.getAction();
        if(intentAction.equals(Befrest.Util.ACTION_PUSH_RECIEVED))
            onPushReceived(context, intent.getStringExtra("Data"));
        Bundle extras = intent.getExtras();
    }

    abstract public void onPushReceived(Context context, String message);
}