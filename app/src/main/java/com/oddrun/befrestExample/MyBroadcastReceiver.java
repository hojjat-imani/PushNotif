package com.oddrun.befrestExample;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.oddrun.befrest.BefrestPushBroadcastReceiver;


/**
 * Created by ehsan on 11/11/2015.
 */
public class MyBroadcastReceiver extends BefrestPushBroadcastReceiver {
    @Override
    public void onPushReceived(Context context, String message) {
        Log.d("MReceiver", "onPUshReceive");
    }

    @Override
    public void onAuthorizeProblem(Context context) {
        Log.d("MReceiver", "onAuthorizeProblem");
        Toast.makeText(context, "UnAuthorized!!!", Toast.LENGTH_SHORT).show();
    }
}