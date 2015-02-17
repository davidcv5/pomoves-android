package com.challdoit.pomoves;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.challdoit.pomoves.model.Event;
import com.challdoit.pomoves.ui.TimerActivity;
import com.challdoit.pomoves.util.PrefUtils;

import static com.challdoit.pomoves.util.LogUtils.LOGI;

public class PomovesReceiver extends BroadcastReceiver {

    private static final int NOTIFICATION_ID = 1;

    public static final String TAG = PomovesReceiver.class.getSimpleName();
    public static final String DISABLE_VIBRATION = "DISABLE_VIBRATION";
    private SessionManager manager;

    @Override
    public void onReceive(Context context, Intent intent) {

        LOGI(TAG, "time is up!");

        manager = SessionManager.get(context);

        if (manager == null) {
            cancelNotifications(context);
            return;
        }

        boolean isRunning = manager.isTrackingSession();
        boolean disableVibration = intent.getBooleanExtra(DISABLE_VIBRATION, false);

        switch (intent.getAction()) {
            case SessionManager.ACTION_STOP:
                manager.stopEvent(true);
                break;
            case SessionManager.ACTION_NEXT:
                if (isRunning)
                    manager.stopEvent(true);
                manager.startSession();
                break;
            case SessionManager.ACTION_EVENT:
                manager.stopEvent();
                break;
        }

        notifyUser(context, manager.isTrackingSession(), disableVibration);
    }

    private void cancelNotifications(Context context) {
        NotificationManager mNotifyMgr =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotifyMgr.cancel(NOTIFICATION_ID);
    }

    private void notifyUser(Context context, boolean isRunning, boolean disableVibration) {
        NotificationCompat.Builder mBuilder =
                getNotificationBuilder(context, isRunning);

        if (!disableVibration && PrefUtils.isNotificationsEnabled(context))
            mBuilder.setVibrate(new long[]{0, 500});

        PendingIntent mainPendingIntent = getMainPendingIntent(context);

        mBuilder.setContentIntent(mainPendingIntent);

        if (!isRunning) {
            mBuilder.addAction(R.drawable.ic_play,
                    "Start",
                    getActionPendingIntent(context, SessionManager.ACTION_NEXT));
            mBuilder.setOngoing(false);
        } else {
            mBuilder.addAction(R.drawable.ic_stop,
                    "Stop",
                    getActionPendingIntent(context, SessionManager.ACTION_STOP));
            if (manager.getCurrentEventType() == Event.LONG_BREAK
                    || manager.getCurrentEventType() == Event.SHORT_BREAK)
                mBuilder.addAction(R.drawable.ic_play,
                        "Skip",
                        getActionPendingIntent(context, SessionManager.ACTION_NEXT));
            mBuilder.setOngoing(true);
        }

        NotificationManager mNotifyMgr =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotifyMgr.notify(NOTIFICATION_ID, mBuilder.build());
    }

    private NotificationCompat.Builder getNotificationBuilder(
            Context context, boolean isRunning) {

        return
                new NotificationCompat.Builder(context)
                        .setCategory(NotificationCompat.CATEGORY_ALARM)
                        .setSmallIcon(R.drawable.ic_pomodoro_notification)
                        .setContentTitle(
                                isRunning ?
                                        manager.getCurrentEventType() == Event.POMODORO ?
                                                "Stay focused" :
                                                "Make the best of your break" :
                                        "Pomoves")
                        .setContentText(isRunning ?
                                "Running: " + Event.getName(context,
                                        manager.getCurrentEventType()) :
                                "Stopped");

    }

    private PendingIntent getMainPendingIntent(Context context) {
        Intent resultIntent = new Intent(context, TimerActivity.class);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return PendingIntent.getActivity(
                context,
                0,
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
    }

    private PendingIntent getActionPendingIntent(Context context, String action) {
        Intent broadcast = new Intent(action);
        broadcast.putExtra(DISABLE_VIBRATION, true);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        return PendingIntent.getBroadcast(context, 0, broadcast, flags);
    }
}
