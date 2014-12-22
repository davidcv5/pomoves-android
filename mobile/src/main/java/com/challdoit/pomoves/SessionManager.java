package com.challdoit.pomoves;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.text.format.DateUtils;
import android.util.Log;

import com.challdoit.pomoves.model.Event;
import com.challdoit.pomoves.model.Session;
import com.challdoit.pomoves.model.SessionHelper;

public class SessionManager {

    private static final String TAG = SessionManager.class.getSimpleName();

    private static final int SECOND = 1000;
    private static final int MINUTE = 60 * SECOND;
    private static final String PREFS_FILE = "sessions";
    private static final String PREF_CURRENT_SESSION_ID = "SessionManager.currentSessionId";
    private static final String PREF_CURRENT_EVENT_TYPE = "SessionManager.eventType";
    private static final String PREF_CURRENT_EVENT_START_TIME = "SessionManager.startTime";
    private static final String PREF_POMODORO_COUNT = "SessionManager.pomodoroCount";
    public static final String ACTION_SESSION = "com.challdoit.pomoves.ACTION_LOCATION";

    private static SessionManager sSessionManager;
    private Context mAppContext;
    private SharedPreferences mPrefs;
    private long mCurrentSessionId;
    private long mCurrentSessionStarted;

    private SessionManager(Context appContext) {
        mAppContext = appContext;
        mPrefs = mAppContext.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
        mCurrentSessionId = mPrefs.getLong(PREF_CURRENT_SESSION_ID, -1);
        mCurrentSessionStarted = mPrefs.getLong(PREF_CURRENT_EVENT_START_TIME, -1);
    }

    public static SessionManager get(Context context) {
        if (sSessionManager == null)
            sSessionManager = new SessionManager(context.getApplicationContext());
        return sSessionManager;
    }

    private PendingIntent getSessionPendingIntent(boolean shouldCreate) {
        Intent broadcast = new Intent(ACTION_SESSION);
        int flags = shouldCreate ? 0 : PendingIntent.FLAG_NO_CREATE;
        return PendingIntent.getBroadcast(mAppContext, 0, broadcast, flags);
    }

    public boolean isTrackingSession() {
        return getSessionPendingIntent(false) != null;
    }

    public boolean isTrackingSession(Session session) {
        return session != null && session.getId() == mCurrentSessionId;
    }

    public long getCurrentSessionId() {
        return mCurrentSessionId;
    }

    public Session startSession() {
        Session session = null;
        if (getCurrentSessionId() > 0)
            session = SessionHelper.load(mAppContext, getCurrentSessionId());
        if (session == null || !DateUtils.isToday(session.getDate().getTime()))
            session = createNewSession();
        startTrackingSession(session);
        return session;
    }

    private void startTrackingSession(Session session) {
        mCurrentSessionId = session.getId();
        mPrefs.edit().putLong(PREF_CURRENT_SESSION_ID, mCurrentSessionId)
                .apply();
        startEvent(Event.POMODORO);
    }

    private Session createNewSession() {
        Session session = new Session();
        SessionHelper.insert(mAppContext, session);
        return session;
    }

    public void endSession() {
        // TODO: end and save session
    }

    public void updateSession() {
        // TODO: recalculate and save session data
    }

    public void startEvent(int eventType) {
        Log.d(TAG, "Starting Event: " + Event.getName(mAppContext, eventType));
        int duration = getEventDuration(eventType) * SECOND;

        cancelIntentIfRunning();

        PendingIntent timer = getSessionPendingIntent(true);
        AlarmManager alarmManager =
                (AlarmManager) mAppContext.getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() + duration,
                    timer);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() + duration,
                    timer);
        }

        mPrefs.edit().putInt(PREF_CURRENT_EVENT_TYPE, eventType).apply();
    }

    public void stopEvent(boolean stoppedManually) {
        cancelIntentIfRunning();

        int pomodoroCount = mPrefs.getInt(PREF_POMODORO_COUNT, 0);
        int currentEventType = getCurrentEventType();
        Log.d(TAG, "Stopping Event: " + Event.getName(mAppContext, currentEventType));
        if (currentEventType == Event.POMODORO &&
                !stoppedManually) {
            pomodoroCount++;
            if (pomodoroCount < 4) {
                startEvent(Event.SHORT_BREAK);
                mPrefs.edit().putInt(PREF_POMODORO_COUNT, pomodoroCount).apply();
            } else {
                startEvent(Event.LONG_BREAK);
                mPrefs.edit().putInt(PREF_POMODORO_COUNT, 0).apply();
            }
        } else if (currentEventType != Event.POMODORO) {
            if (stoppedManually) {
                mPrefs.edit().putInt(PREF_CURRENT_EVENT_TYPE,
                        Event.POMODORO).apply();
            } else {
                startEvent(Event.POMODORO);
            }
        }

    }

    private void cancelIntentIfRunning() {
        PendingIntent timer = getSessionPendingIntent(false);
        if (timer != null)
            timer.cancel();
    }

    public int getCurrentEventType() {
        return mPrefs.getInt(PREF_CURRENT_EVENT_TYPE, -1);
    }

    private int getEventDuration(int eventType) {
        switch (eventType) {
            case Event.POMODORO:
                return mPrefs.getInt(mAppContext.getString(R.string.PREF_POMODORO_DURATION), 10);
            case Event.SHORT_BREAK:
                return mPrefs.getInt(mAppContext.getString(R.string.PREF_SHORT_BREAK_DURATION), 10);
            case Event.LONG_BREAK:
                return mPrefs.getInt(mAppContext.getString(R.string.PREF_LONG_BREAK_DURATION), 10);
        }

        return 0;
    }

    public void clearPreferences() {
        mPrefs.edit().clear().apply();
    }
}
