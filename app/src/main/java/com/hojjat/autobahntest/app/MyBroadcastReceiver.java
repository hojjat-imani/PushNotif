package com.hojjat.autobahntest.app;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.hojjat.autobahntest.befrest.BefrestPushBroadcastReceiver;

/**
 * Created by ehsan on 11/11/2015.
 */
public class MyBroadcastReceiver extends BefrestPushBroadcastReceiver {
    @Override
    public void onPushReceived(Context context, String message) {
        Log.d("MReceiver", "onPUshReceive");
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }
}