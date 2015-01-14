package com.challdoit.pomoves;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class PomovesReceiver extends BroadcastReceiver {

    public static final String TAG = PomovesReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "time is up!");
        SessionManager manager = SessionManager.get(context);
        if (manager != null) {
            Log.d(TAG, "stopping event");
            manager.stopEvent();
        } else {
            Log.d(TAG, "manager is null");
        }
    }
}
