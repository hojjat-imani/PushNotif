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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by ehsan on 11/11/2015.
 */
public abstract class BefrestPushBroadcastReceiver extends BroadcastReceiver {
    public final String TAG = getClass().getSimpleName();

    @Override
    public final void onReceive(Context context, Intent intent) {
        String intentAction = intent.getAction();
        if (Befrest.DEBUG) Log.d(TAG, "BroadCastReceived : " + intentAction);
        switch (intentAction) {
            case Befrest.ACTION_PUSH_RECIEVED:
                onPushReceived(context, intent.getStringExtra(Befrest.Util.KEY_MESSAGE_PASSED));
                break;
            case Befrest.Util.ACTION_UNAUTHORIZED:
                onAuthorizeProblem(context);
                break;
            case ConnectivityManager.CONNECTIVITY_ACTION:
                startOrStopPushService(context);
        }
    }

    private void startOrStopPushService(Context context) {
        if (Befrest.Util.isConnectedToInternet(context))
            Befrest.Util.startPushService(context);
//        else Befrest.Util.stopPushService(context);
    }

    abstract public void onPushReceived(Context context, String message);

    public void onAuthorizeProblem(Context context) {
        Log.d(TAG, "Befrest : Authorization Problem!");
    }
}