/******************************************************************************
 * Copyright 2015-2016 Oddrun
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.oddrun.befrest;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import com.oddrun.befrest.connectivity.WebSocketConnection;
import com.oddrun.befrest.connectivity.WebSocketConnectionHandler;
import com.oddrun.befrest.connectivity.WebSocketException;


/**
 * Created by mblcdr on 4/22/15.
 */
public class PushService extends Service {

    private final String TAG = getClass().getSimpleName();
    private final String CONNECTION_NOT_RESPONDING = "CONNECTION_NOT_RESPONDING";
    private final int PING_TIMEOUT = 20 * 1000;
    private final int PING_SEND_INTERVAL = 60 * 1000;
    private final int RECONNECT_INTENT_REQUEST_CODE = 0;

    private String connectionUrl;
    private AlarmManager alarmManager;
    private PendingIntent reconnectPendingIntent;
    private WebSocketConnection mConnection = new WebSocketConnection();
    private BroadcastReceiver broadCastReceiverConnectionStateChange;
    private WebSocketConnectionHandler handler = new WebSocketConnectionHandler() {
        @Override
        public void onOpen() {
            Log.d(TAG, "Befrest Connected");
            sendPing();
        }

        @Override
        public void onTextMessage(String message) {
            if (Befrest.DEBUG) Log.d(TAG, "Got notif: " + message);
            if (message.equals("=PONG")) {
                onPong();
            } else {
                Bundle bundle = new Bundle(1);
                bundle.putString(Befrest.Util.KEY_MESSAGE_PASSED, message);
                sendBefrestBroadcast(Befrest.ACTION_PUSH_RECIEVED, bundle);
            }
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

    private Runnable sendPingRunnable = new Runnable() {
        @Override
        public void run() {
            sendPing();
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
        broadCastReceiverConnectionStateChange = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                connectIfNeeded();
            }
        };
        registerReceiver(broadCastReceiverConnectionStateChange, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Befrest.DEBUG) Log.d(TAG, "onStartCommand");
        connectionUrl = Befrest.Util.getConnectionUri();
        if (intent != null && intent.getBooleanExtra(CONNECTION_NOT_RESPONDING, false)) {
            terminateConnection();
            if (Befrest.DEBUG) Log.d(TAG, CONNECTION_NOT_RESPONDING + "received");
        }
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
        if (mConnection == null) mConnection = new WebSocketConnection();
        boolean isConntectedToInternet = Befrest.Util.isConnectedToInternet(this);
        if (!mConnection.isConnected() && isConntectedToInternet)
            connect();
    }

    private void reconnectIfNeeded() {
        if (mConnection == null) mConnection = new WebSocketConnection();
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

    private void sendPing() {
        if (Befrest.DEBUG) Log.d(TAG, "sendPing");
        if (!Befrest.Util.isConnectedToInternet(this)) return;
        Befrest.sendMessage("PONG"); //PONG base64
        waitForAPong();
    }

    private void onPong() {
        if (Befrest.DEBUG) Log.d(TAG, "onPong");
        if (alarmManager == null) alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        PendingIntent pi = getReconnectPendingIntent();
        alarmManager.cancel(pi);
        pi.cancel();
        Handler handler = new Handler();
        handler.postDelayed(sendPingRunnable, PING_SEND_INTERVAL);
    }

    private void waitForAPong() {
        if (alarmManager == null) alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + PING_TIMEOUT, getReconnectPendingIntent());
    }

    private PendingIntent getReconnectPendingIntent() {
        if (reconnectPendingIntent == null) {
            Intent i = new Intent(this, PushService.class).putExtra(CONNECTION_NOT_RESPONDING, true);
            reconnectPendingIntent = PendingIntent.getService(this, RECONNECT_INTENT_REQUEST_CODE, i, 0);
        }
        return reconnectPendingIntent;
    }
}