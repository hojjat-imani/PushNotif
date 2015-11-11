package com.oddrun.befrest;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import java.nio.channels.SocketChannel;

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
                public void onTextMessage(String message) {
                    Log.d(TAG, "Got notif: " + message);
                    Bundle bundle = new Bundle();
                    bundle.putString(Befrest.Util.KEY_MESSAGE_PASSED, message);
                    sendBefrestBroadcast(Befrest.Util.ACTION_PUSH_RECIEVED, bundle);
                }

                @Override
                public void onClose(int code, String reason) {
                    Log.d(TAG, "Connection lost. Code: " + code + ", Reason: " + reason);
                    // RECONNECT POLICY GOES HERE
                    //we must try to connect if the problem is from internet connection, server ,...
                    switch (code){
                        case CLOSE_CANNOT_CONNECT:
                        case CLOSE_CONNECTION_LOST:
                        case CLOSE_INTERNAL_ERROR:
                        case CLOSE_NORMAL:
                        case CLOSE_PROTOCOL_ERROR:
                    }
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
        Log.d(TAG, "broadcast sent - permission : " + Befrest.Util.getBroadcastSendingPermission(this));
        sendBroadcast(intent, Befrest.Util.getBroadcastSendingPermission(this));
    }
}
