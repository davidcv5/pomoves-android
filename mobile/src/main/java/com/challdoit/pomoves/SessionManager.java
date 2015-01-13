package com.challdoit.pomoves;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.support.v4.content.LocalBroadcastManager;
import android.text.format.DateUtils;
import android.util.Log;

import com.challdoit.pomoves.data.PomovesContract;
import com.challdoit.pomoves.model.Event;
import com.challdoit.pomoves.model.EventHelper;
import com.challdoit.pomoves.model.Session;
import com.challdoit.pomoves.model.SessionHelper;

import java.util.Calendar;
import java.util.Date;

public class SessionManager {

    private static final String TAG = SessionManager.class.getSimpleName();

    private static final int SECOND = 1000;
    private static final int MINUTE = 60 * SECOND;
    private static final String PREFS_FILE = "sessions";
    private static final String PREF_CURRENT_SESSION_ID = "SessionManager.currentSessionId";
    private static final String PREF_CURRENT_EVENT_ID = "SessionManager.currentEventId";
    private static final String PREF_CURRENT_EVENT_TYPE = "SessionManager.eventType";
    private static final String PREF_CURRENT_END_TIME = "SessionManager.currentEndTime";
    private static final String PREF_POMODORO_COUNT = "SessionManager.pomodoroCount";
    public static final String ACTION_SESSION = "com.challdoit.pomoves.ACTION_LOCATION";

    private static SessionManager sSessionManager;
    private Context mAppContext;
    private SharedPreferences mPrefs;
    private long mCurrentSessionId;
    private long mCurrentEventId;
    private int mPomodoroCount;
    private Session mSession;
    private long mCurrentEndTime;

    private SessionManager(Context appContext) {
        mAppContext = appContext;
        mPrefs = mAppContext.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
        mCurrentSessionId = mPrefs.getLong(PREF_CURRENT_SESSION_ID, -1);
        mCurrentEventId = mPrefs.getLong(PREF_CURRENT_EVENT_ID, -1);
        mPomodoroCount = mPrefs.getInt(PREF_POMODORO_COUNT, 0);
        mCurrentEndTime = mPrefs.getLong(PREF_CURRENT_END_TIME, 0);

        Cursor c = appContext.getContentResolver().query(
                PomovesContract.SessionEntry.CONTENT_URI,
                new String[]{PomovesContract.SessionEntry._ID},
                null, null, null);
        int total = c.getCount();
        if (total == 0) {
            Calendar cal = Calendar.getInstance();

            cal.set(Calendar.YEAR, 2014);
            cal.set(Calendar.MONTH, 12);
            for (int i = 1; i <= 31; i++) {
                cal.set(Calendar.DAY_OF_MONTH, i);
                Session s = new Session();
                s.setDate(cal.getTime());
                s.setStats("Session with ID: " + i);
                SessionHelper.insert(mAppContext, s);
                mCurrentEventId = s.getId();
            }

            cal.set(Calendar.YEAR, 2015);
            cal.set(Calendar.MONTH, 1);
            for (int i = 1; i <= 4; i++) {
                cal.set(Calendar.DAY_OF_MONTH, i);
                Session s = new Session();
                s.setDate(cal.getTime());
                s.setStats("Session with ID: " + i);
                SessionHelper.insert(mAppContext, s);
                mCurrentEventId = s.getId();
            }
        }

        mSession = getCurrentSession();
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

    public long getCurrentEventId() {
        return mCurrentEventId;
    }

    public Session startSession() {
        mSession = getCurrentSession();
        startTrackingSession(mSession);
        return mSession;
    }

    private Session getCurrentSession() {
        if (getCurrentSessionId() < 0 ||
                (mSession != null && !DateUtils.isToday(mSession.getDate().getTime())))
            mSession = createNewSession();
        else if (mSession == null)
            mSession = SessionHelper.load(mAppContext, getCurrentSessionId());
        return mSession;
    }

    private void startTrackingSession(Session session) {
        mCurrentSessionId = session.getId();
        mPrefs.edit().putLong(PREF_CURRENT_SESSION_ID, mCurrentSessionId)
                .apply();
        startEvent(Event.POMODORO);
    }

    private Session createNewSession() {
        mSession = new Session();
        mSession.setStats("some stats");
        SessionHelper.insert(mAppContext, mSession);
        mCurrentSessionId = mSession.getId();
        return mSession;
    }

    public void stopSession() {
        // TODO: end and save session
    }

    public void updateSession() {
        // TODO: recalculate and save session data
    }

    public void startEvent(int eventType) {
        int duration = getEventDuration(eventType) * SECOND;

        Log.d(TAG, String.format("Starting Event: %s, Duration: %s",
                Event.getName(mAppContext, eventType),
                duration));

        long now = System.currentTimeMillis();
        setCurrentEndTime(now + duration);

        PendingIntent timer = getSessionPendingIntent(true);
        AlarmManager alarmManager =
                (AlarmManager) mAppContext.getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP,
                    mCurrentEndTime,
                    timer);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP,
                    mCurrentEndTime,
                    timer);
        }

        Session session = getCurrentSession();
        mPrefs.edit().putInt(PREF_CURRENT_EVENT_TYPE, eventType).apply();
        Event event = new Event(session.getId(), eventType);
        event.setStartDate(new Date(now));
        event.setEndDate(new Date(mCurrentEndTime));
        EventHelper.insert(mAppContext, event);
        mCurrentEventId = event.getId();
        Log.d(TAG, "Current Event Type: " + Event.getName(mAppContext, getCurrentEventType()));

        notifyChange();
    }

    public void stopEvent() {
        stopEvent(false);
    }

    public void stopEvent(boolean stoppedManually) {
        cancelIntentIfRunning();

        int currentEventType = getCurrentEventType();
        Log.d(TAG, String.format("Stopping Event: %s, Count: %s",
                Event.getName(mAppContext, currentEventType),
                mPomodoroCount));
        if (stoppedManually) {
            EventHelper.delete(mAppContext, mCurrentEventId);
        }
        if (currentEventType == Event.POMODORO &&
                !stoppedManually) {
            mPomodoroCount++;
            if (mPomodoroCount < 4) {
                startEvent(Event.SHORT_BREAK);
            } else {
                startEvent(Event.LONG_BREAK);
                mPomodoroCount = 0;
            }
            mPrefs.edit().putInt(PREF_POMODORO_COUNT, mPomodoroCount).apply();
        } else if (currentEventType != Event.POMODORO) {
            if (stoppedManually) {
                mPrefs.edit().putInt(PREF_CURRENT_EVENT_TYPE,
                        Event.POMODORO).apply();
            } else {
                startEvent(Event.POMODORO);
            }
        }
        Session session = getCurrentSession();
        session.setStats(Integer.toString(getCurrentEventType()));
        SessionHelper.update(mAppContext, session);

        if (stoppedManually)
            notifyChange();
    }

    private void notifyChange() {
        LocalBroadcastManager.getInstance(mAppContext).sendBroadcast(
                new Intent(SessionManager.ACTION_SESSION));
    }

    private void cancelIntentIfRunning() {
        PendingIntent timer = getSessionPendingIntent(false);
        if (timer != null) {
            timer.cancel();
            AlarmManager alarmManager =
                    (AlarmManager) mAppContext.getSystemService(Context.ALARM_SERVICE);
            alarmManager.cancel(timer);
        }
    }

    public int getCurrentEventType() {
        return mPrefs.getInt(PREF_CURRENT_EVENT_TYPE, -1);
    }

    private int getEventDuration(int eventType) {
        int defaultDuration = 10;
        switch (eventType) {
            case Event.POMODORO:
                return mPrefs.getInt(mAppContext.getString(R.string.PREF_POMODORO_DURATION), defaultDuration);
            case Event.SHORT_BREAK:
                return mPrefs.getInt(mAppContext.getString(R.string.PREF_SHORT_BREAK_DURATION), defaultDuration);
            case Event.LONG_BREAK:
                return mPrefs.getInt(mAppContext.getString(R.string.PREF_LONG_BREAK_DURATION), defaultDuration);
        }

        return 0;
    }

    @SuppressLint("CommitPrefEdits")
    public void clearPreferences() {
        mPrefs.edit().clear().commit();
    }

    public int getPomodoroCount() {
        return mPomodoroCount;
    }

    public long getCurrentEndTime() {
        return mCurrentEndTime;
    }

    public void setCurrentEndTime(long currentEndTime) {
        mCurrentEndTime = currentEndTime;
        mPrefs.edit().putLong(PREF_CURRENT_END_TIME, currentEndTime);
    }
}
