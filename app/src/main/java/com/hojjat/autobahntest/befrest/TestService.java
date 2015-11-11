package com.hojjat.autobahntest.befrest;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import java.util.logging.Handler;

/**
 * Created by ehsan on 11/11/2015.
 */
public class TestService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("TestService", "test service running");
        Toast.makeText(this, "Broadcast will be sent in 5 seconds", Toast.LENGTH_SHORT).show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Bundle bundle = new Bundle();
                bundle.putString(Befrest.Util.KEY_MESSAGE_PASSED, "receive message!");
                sendBefrestBroadcast(Befrest.Util.ACTION_PUSH_RECIEVED, bundle);
            }
        }).start();
        return START_REDELIVER_INTENT;
    }

    private void sendBefrestBroadcast(String action, Bundle extras) {
        Intent intent = new Intent();
        intent.setAction(action);
        intent.putExtras(extras);
        sendBroadcast(intent, Befrest.Util.getBroadcastSendingPermission(this));
        Log.d("TestService", "broadcast sent");
    }
}
