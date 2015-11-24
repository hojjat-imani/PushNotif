package com.oddrun.befrest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by ehsan on 11/24/2015.
 */
public final class BefrestConnectivityChangeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, PushService.class).putExtra(PushService.CONNECTIVITY_STATE_CHANGED, true));
    }
}