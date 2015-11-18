package com.oddrun.befrest;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.oddrun.befrest.connectivity.WebSocketConnection;
import com.oddrun.befrest.connectivity.WebSocketConnectionHandler;
import com.oddrun.befrest.connectivity.WebSocketException;

/**
 * Created by mblcdr on 4/22/15.
 */
public class PushService extends Service {

    private final String TAG = "BefrestPushService";
    private String connectionUrl;
    private WebSocketConnection mConnection = new WebSocketConnection();
    private BroadcastReceiver broadCastReceiverConnectionStateChange;
    private WebSocketConnectionHandler handler = new WebSocketConnectionHandler() {
        @Override
        public void onOpen() {
            Log.d(TAG, "Befrest Connected");
        }

        @Override
        public void onTextMessage(String message) {
            if (Befrest.DEBUG) Log.d(TAG, "Got notif: " + message);
            Bundle bundle = new Bundle(1);
            bundle.putString(Befrest.Util.KEY_MESSAGE_PASSED, message);
            sendBefrestBroadcast(Befrest.ACTION_PUSH_RECIEVED, bundle);
        }

        @Override
        public void onClose(int code, String reason) {
            Log.d(TAG, "Connection lost. Code: " + code + ", Reason: " + reason);
            switch (code) {
                case CLOSE_UNAUTHORIZED:
                    sendBefrestBroadcast(Befrest.Util.ACTION_UNAUTHORIZED);
                    terminateConnection();
                    stopSelf();
                    break;
                case CLOSE_RECONNECT:
                    //reconnection handled in autobahn
                    break;
                case CLOSE_CANNOT_CONNECT:
                case CLOSE_CONNECTION_LOST:
                case CLOSE_INTERNAL_ERROR:
                case CLOSE_NORMAL:
                case CLOSE_PROTOCOL_ERROR:
                case CLOSE_SERVER_ERROR:
                    reconnectIfNeeded();
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        registerConnectionStatusChangeBroadcastReceiver();
    }

    private void registerConnectionStatusChangeBroadcastReceiver() {
        broadCastReceiverConnectionStateChange = new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent) {
                connectIfNeeded();
            }
        };
        registerReceiver(broadCastReceiverConnectionStateChange, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
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
        terminateConnection();
        unregisterReceiver(broadCastReceiverConnectionStateChange);
    }

    private void connectIfNeeded() {
        boolean isConntectedToInternet = Befrest.Util.isConnectedToInternet(this);
        if (!mConnection.isConnected() && isConntectedToInternet)
            connect();
    }

    private void reconnectIfNeeded(){
        boolean isConntectedToInternet = Befrest.Util.isConnectedToInternet(this);
        if (!mConnection.isConnected() && isConntectedToInternet)
            mConnection.reconnect();
    }

    private void terminateConnection() {
        mConnection.disconnect();
        //disconnect does not work properly :|
        //connection remains open after disconnect
        //so we make it null
        mConnection = null;
    }

    private void connect() {
        try {
            mConnection.connect(connectionUrl, handler);
        } catch (WebSocketException e) {
            e.printStackTrace();
        }
    }

    private void sendBefrestBroadcast(String action, Bundle extras) {
        Intent intent = new Intent(action);
        intent.putExtras(extras);
        sendBroadcast(intent, Befrest.Util.getBroadcastSendingPermission(this));
    }

    private void sendBefrestBroadcast(String action) {
        Intent intent = new Intent(action);
        sendBroadcast(intent, Befrest.Util.getBroadcastSendingPermission(this));
    }
}
