package com.challdoit.pomoves;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by David on 12/15/14.
 */
public class PomovesService extends Service {

    public static final String TAG = PomovesService.class.getSimpleName();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "Time is up!");
        SessionManager manager = SessionManager.get(this);
        manager.stopEvent(false);
    }
}
