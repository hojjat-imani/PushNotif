package com.hojjat.autobahntest.befrest;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
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
        connectionUrl = intent.getStringExtra(Befrest.Util.CONNECTION_URL);
        connectIfNeeded();
        return START_STICKY;
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

}
