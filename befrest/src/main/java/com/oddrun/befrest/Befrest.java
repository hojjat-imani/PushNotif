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

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by ehsan on 11/10/2015.
 */
public class Befrest {
    static String TAG = "Befrest";
    protected static final boolean DEBUG = true;
    public static final String ACTION_PUSH_RECIEVED = "com.oddrun.befrest.broadcasts.PUSH_RECEIVED";

    private static Context context;
    private static int U_ID;
    private static String AUTH;
    private static long CH_ID;

    public static void initialize(Context context, int APP_ID, String AUTH, long USER_ID) {
        Befrest.context = context;
        Befrest.U_ID = APP_ID;
        Befrest.AUTH = AUTH;
        Befrest.CH_ID = USER_ID;
        runPushService();
    }

    private static boolean stopFormerServiceIfExist() {
        return Util.stopPushService(context);
    }

    public static void reInitialize(Context context, int APP_ID, String AUTH, long USER_ID) {
        /* only a wrapper of initialize for clarity.
        initialize can be used for re initializing
        */
        initialize(context, APP_ID, AUTH, USER_ID);
    }

    private static void runPushService() {
        stopFormerServiceIfExist();
        Util.startPushService(context);
    }

    protected static class Util {
        protected static final String ACTION_UNAUTHORIZED = "com.oddrun.befrest.broadcasts.UNAUTHORIZED";
        protected static final String KEY_MESSAGE_PASSED = "KEY_MESSAGE_PASSED";
        private static final String BROADCAST_SENDING_PERMISSION_POSTFIX = ".permission.PUSH_SERVICE";


        protected static boolean isConnectedToInternet(Context context) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        }

        protected static String getConnectionUri() {
            return String.format(Locale.US, "ws://gw.bef.rest:8000/sub?chid=%d&uid=%d&auth=%s", CH_ID, U_ID, AUTH);
        }

        protected static String getBroadcastSendingPermission(Context context) {
            return context.getApplicationContext().getPackageName() + BROADCAST_SENDING_PERMISSION_POSTFIX;
        }

        protected static String decodeBase64(String s) {
            try {
                return new String(Base64.decode(s, Base64.DEFAULT), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return "";
        }

        protected static String encodeBase64(String s) {
            try {
                return Base64.encodeToString(s.getBytes("UTF-8"), Base64.DEFAULT);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return "";
        }

        public static void startPushService(Context context) {
            if (DEBUG) Log.d(TAG, "starting PushService");
            context.startService(new Intent(context, PushService.class));
        }

        public static boolean stopPushService(Context context) {
            if (DEBUG) Log.d(TAG, "stopping PushService");
            return context.stopService(new Intent(context, PushService.class));
        }
    }

    public static void sendMessage(String msg) {
        new SendMessage().execute(msg);
    }

    private static class SendMessage extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://gw.bef.rest:8000/pub?chid=" + CH_ID + "&uid=" + U_ID + "&auth=" + AUTH);

            try {
                // Add your data
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
                nameValuePairs.add(new BasicNameValuePair("", params[0]));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

//                 Execute HTTP Post Request
                HttpResponse response = httpclient.execute(httppost);

                Log.d(TAG, "response " + response.getStatusLine());
            } catch (ClientProtocolException e) {
                // TODO Auto-generated catch block
            } catch (IOException e) {
                // TODO Auto-generated catch block
            }
            return "Executed";
        }
    }
}