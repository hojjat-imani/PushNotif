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
import android.content.Intent;
import android.os.Bundle;
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

    private static final String TAG = PushService.class.getSimpleName();

    //commands
    protected static final String RESTART = "RESTART";
    protected static final String CONNECTIVITY_STATE_CHANGED = "CONNECTIVITY_STATE_CHANGED";
    protected static final String CONNECT = "CONNECT";
    private static final String RETRY = "RETRY";
    private static final String SEND_PING = "SEND_PING";
    private static final String NOT_ASSIGNED = "NOT_ASSIGNED";


    //constants
    private final int PING_TIMEOUT = 10 * 1000;
    private final int PING_SEND_INTERVAL = 30 * 1000;
    private int PING_ID = 0;

    private boolean retryInProgress;
    private boolean restartInProgress;

    private boolean connecting;

    private PendingIntent retryPendingIntend;
    private PendingIntent restartPendingIntend;
    private PendingIntent sendPingPendingIntent;

    private AlarmManager alarmManager;
    private WebSocketConnection mConnection;
    private WebSocketConnectionHandler handler = new WebSocketConnectionHandler() {
        @Override
        public void onOpen() {
            Log.d(TAG, "Befrest Connected");
            connecting = false;
            sendPing();
        }

        @Override
        public void onTextMessage(String message) {
            if (Befrest.DEBUG) Log.d(TAG, "Got notif: " + message);
            if (message.startsWith("=PONG")) {
                onPong(Integer.parseInt("" + message.charAt(message.length() - 1)));
            } else {
                Bundle bundle = new Bundle(1);
                bundle.putString(Befrest.Util.KEY_MESSAGE_PASSED, message);
                sendBefrestBroadcast(Befrest.ACTION_PUSH_RECIEVED, bundle);
            }
        }

        @Override
        public void onClose(int code, String reason) {
            Log.d(TAG, "Connection lost. Code: " + code + ", Reason: " + reason);
            connecting = false;
            switch (code) {
                case CLOSE_UNAUTHORIZED:
                    sendBefrestBroadcast(Befrest.Util.ACTION_UNAUTHORIZED);
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
                    terminateConnection();
                    schaduleReconnect();
            }
        }
    };

    private void schaduleReconnect() {
        if (retryInProgress || restartInProgress || !Befrest.Util.isConnectedToInternet(this))
            return; //a retry or restart is already in progress or close was due to internet connection lost
        if (alarmManager == null) alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        cancelFuturePing();
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + getReconnectInterval(), getRetryPendingIntent());
        retryInProgress = true;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate()");
        mConnection = new WebSocketConnection();
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String command = getCommand(intent);
        if (Befrest.DEBUG) Log.d(TAG, "onStartCommand(" + command + ")");
        switch (command) {
            case RESTART:
                mConnection.disconnect();
                connectIfNeeded();
                break;
            case RETRY:
                retryInProgress = false;
                reconnectIfNeeded();
                break;
            case SEND_PING:
                sendPing();
                break;
            case CONNECTIVITY_STATE_CHANGED:
            case CONNECT:
            case NOT_ASSIGNED:
                connectIfNeeded();
                break;
        }
        return START_STICKY;
    }

    private String getCommand(Intent intent) {
        if (intent != null && intent.getBooleanExtra(CONNECTIVITY_STATE_CHANGED, false))
            return CONNECTIVITY_STATE_CHANGED;
        if (intent != null && intent.getBooleanExtra(CONNECT, false))
            return CONNECT;
        if (intent != null && intent.getBooleanExtra(RETRY, false))
            return RETRY;
        if (intent != null && intent.getBooleanExtra(SEND_PING, false))
            return SEND_PING;
        return NOT_ASSIGNED;
    }

    @Override
    public void onDestroy() {
        if (Befrest.DEBUG) Log.d(TAG, "onDestroy()");
        terminateConnection();
        super.onDestroy();
    }

    private void terminateConnection() {
        if (Befrest.DEBUG) Log.d(TAG, "terminateConnection()");
        mConnection.disconnect();
        cancelALLPendingIntents();
    }

    private void connectIfNeeded() {
        if (Befrest.DEBUG) Log.d(TAG, "connectIfNeeded()");
        if (!mConnection.isConnected() && Befrest.Util.isConnectedToInternet(this) && !connecting)
            try {
                connecting = true;
                if (Befrest.DEBUG) Log.d(TAG, "connecting ...");
                mConnection.connect(Befrest.Util.getConnectionUri(), handler);
            } catch (WebSocketException e) {
                e.printStackTrace();
            }
    }

    private void reconnectIfNeeded() {
        if (Befrest.DEBUG) Log.d(TAG, "reconnectIfNeeded()");
        if (mConnection == null) mConnection = new WebSocketConnection();
        if (!mConnection.isConnected() && Befrest.Util.isConnectedToInternet(this)) {
            connecting = true;
            if (Befrest.DEBUG) Log.d(TAG, "reconnecting ...");
            mConnection.reconnect();
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
//        if (!Befrest.Util.isConnectedToInternet(this)) return;
        PING_ID = (PING_ID + 1) % 2;
        Befrest.sendMessage("PONG" + PING_ID); //PONG base64
        waitForAPong();
    }

    private void onPong(int i) {
        if (Befrest.DEBUG) Log.d(TAG, "onPong(" + i + ") " + (i == PING_ID ? "valid" : "invalid!"));
        if (i != PING_ID) return;
        cancelFutureRestart();
        restartInProgress = false;
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + PING_SEND_INTERVAL, getSendPingPendingIntent());
    }

    private void waitForAPong() {
        if (alarmManager == null) alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + PING_TIMEOUT, getRestartPendingIntent());
        restartInProgress = true;
    }

    private void cancelALLPendingIntents() {
        cancelFuturePing();
        cancelFutureRetry();
        cancelFutureRestart();
    }

    private void cancelFuturePing() {
        if (Befrest.DEBUG) Log.d(TAG, "cancelFuturePing()");
        if (alarmManager == null) alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        PendingIntent pi = getSendPingPendingIntent();
        alarmManager.cancel(pi);
        pi.cancel();
    }

    private void cancelFutureRetry() {
        if (Befrest.DEBUG) Log.d(TAG, "cancelFutureRetry()");
        if (alarmManager == null) alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        PendingIntent pi = getRetryPendingIntent();
        alarmManager.cancel(pi);
        pi.cancel();
    }

    private void cancelFutureRestart() {
        if (Befrest.DEBUG) Log.d(TAG, "cancelFutureRestart()");
        if (alarmManager == null) alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        PendingIntent pi = getRestartPendingIntent();
        alarmManager.cancel(pi);
        pi.cancel();
    }

    private PendingIntent getRetryPendingIntent() {
        if (retryPendingIntend == null) {
            Intent i = new Intent(this, PushService.class).putExtra(RETRY, true);
            retryPendingIntend = PendingIntent.getService(this, 0, i, 0); //requestCode = 0
        }
        return retryPendingIntend;
    }

    private PendingIntent getRestartPendingIntent() {
        if (restartPendingIntend == null) {
            Intent i = new Intent(this, PushService.class).putExtra(RESTART, true);
            restartPendingIntend = PendingIntent.getService(this, 1, i, 0); // requestCode = 1
        }
        return restartPendingIntend;
    }

    private int getReconnectInterval() {
        //TODO
        return 30 * 1000;
    }

    public PendingIntent getSendPingPendingIntent() {
        if (sendPingPendingIntent == null) {
            Intent i = new Intent(this, PushService.class).putExtra(SEND_PING, true);
            sendPingPendingIntent = PendingIntent.getService(this, 2, i, 0); // requestCode = 2
        }
        return sendPingPendingIntent;
    }
}