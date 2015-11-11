package com.hojjat.autobahntest.befrest;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketHandler;
import de.tavendo.autobahn.WebSocketException;

/**
 * Created by mblcdr on 4/22/15.
 */
public class PushService extends Service {

    private final String TAG = "BefrestPushService";
    private String connectionUrl;
    private WebSocketConnection mConnection = new WebSocketConnection();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        connectionUrl = Befrest.Util.getConnectionUri();
        connectIfNeeded();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        startService(new Intent(this, PushService.class));
    }

    private void connectIfNeeded() {
        boolean isConntectedToInternet = Befrest.Util.isConnectedToInternet(this);
        if (!mConnection.isConnected() && isConntectedToInternet)
                connect();
    }

    private void terminateConnection() {
        mConnection.disconnect();
    }

    private void connect() {
        try {
            mConnection.connect(connectionUrl, new WebSocketHandler() {
                @Override
                public void onOpen() {
                    Log.d(TAG, "Status: Connected!");
                }

                @Override
                public void onTextMessage(String payload) {
                    Log.d(TAG, "Got notif: " + payload);
                    Bundle bundle = new Bundle();
                    bundle.putString("Data", payload);
                    sendBefrestBroadcast(Befrest.Util.ACTION_PUSH_RECIEVED, bundle);
                }

                @Override
                public void onClose(int code, String reason) {
                    Log.d(TAG, "Connection lost. Code: " + code + ", Reason: " + reason);
                    // RECONNECT POLICY GOES HERE
                }
            });
        } catch (WebSocketException e) {
            e.printStackTrace();
        }
    }

    private void sendBefrestBroadcast(String action, Bundle extras){
        Intent intent = new Intent();
        intent.setAction(action);
        intent.putExtras(extras);
        sendBroadcast(intent);
    }
}
