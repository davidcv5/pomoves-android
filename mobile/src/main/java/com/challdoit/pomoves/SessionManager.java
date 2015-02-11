package com.challdoit.pomoves;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.support.v4.content.LocalBroadcastManager;
import android.text.format.DateUtils;

import com.challdoit.pomoves.data.PomovesContract;
import com.challdoit.pomoves.model.Event;
import com.challdoit.pomoves.model.EventHelper;
import com.challdoit.pomoves.model.Session;
import com.challdoit.pomoves.model.SessionHelper;
import com.challdoit.pomoves.util.AccountUtils;
import com.challdoit.pomoves.util.FitUtils;
import com.challdoit.pomoves.util.PrefUtils;

import java.util.Calendar;
import java.util.Date;

import static com.challdoit.pomoves.util.LogUtils.LOGI;

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
    public static final String ACTION_EVENT = "com.challdoit.pomoves.ACTION_EVENT";
    public static final String ACTION_STOP = "com.challdoit.pomoves.ACTION_STOP";
    public static final String ACTION_NEXT = "com.challdoit.pomoves.ACTION_NEXT";
    private static final String PREF_CURRENT_STEPS = "SessionManager.currentSteps";

    private static SessionManager sSessionManager;
    private Context mAppContext;
    private SharedPreferences mPrefs;
    private long mCurrentSessionId;
    private long mCurrentEventId;
    private int mPomodoroCount;
    private Session mSession;
    private long mCurrentEndTime;
    private FitUtils mFitUtils;
    private int mCurrentSteps;

    private SessionManager(Context appContext) {
        mAppContext = appContext;
        mPrefs = mAppContext.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
        mCurrentSessionId = mPrefs.getLong(PREF_CURRENT_SESSION_ID, -1);

        long existingSessionId = mCurrentSessionId;

        mSession = getCurrentSession();

        if (existingSessionId == mSession.getId()) {

            mCurrentEventId = mPrefs.getLong(PREF_CURRENT_EVENT_ID, -1);
            mPomodoroCount = mPrefs.getInt(PREF_POMODORO_COUNT, 0);
            mCurrentEndTime = mPrefs.getLong(PREF_CURRENT_END_TIME, 0);
            mCurrentSteps = mPrefs.getInt(PREF_CURRENT_STEPS, 0);
        }

        //populateDebugData(appContext);
    }

    public void clearSession() {
        mPrefs.edit().clear().apply();
        mCurrentSessionId = -1;
        mCurrentEventId = -1;
        mPomodoroCount = 0;
        mCurrentEndTime = 0;
        mSession = null;
    }

    private void populateDebugData(Context appContext) {
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
                s.getStats().info = "Session with ID: " + i;
                SessionHelper.insert(mAppContext, s);
                mCurrentEventId = s.getId();
            }

            cal.set(Calendar.YEAR, 2015);
            cal.set(Calendar.MONTH, 1);
            for (int i = 1; i <= 4; i++) {
                cal.set(Calendar.DAY_OF_MONTH, i);
                Session s = new Session();
                s.setDate(cal.getTime());
                s.getStats().info = "Session with ID: " + i;
                SessionHelper.insert(mAppContext, s);
                mCurrentEventId = s.getId();
            }
        }
    }

    public static SessionManager get(Context context) {
        if (sSessionManager == null)
            sSessionManager = new SessionManager(context.getApplicationContext());
        return sSessionManager;
    }

    private PendingIntent getSessionPendingIntent(boolean shouldCreate) {
        Intent broadcast = new Intent(ACTION_EVENT);
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

    public void setCurrentSessionId(long sessionId) {
        mCurrentSessionId = sessionId;
        mPrefs.edit().putLong(PREF_CURRENT_SESSION_ID, sessionId)
                .apply();
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
        startEvent(Event.POMODORO);
    }

    private Session createNewSession() {
        mSession = new Session();
        SessionHelper.insert(mAppContext, mSession);
        setCurrentSessionId(mSession.getId());
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

        LOGI(TAG, String.format("Starting Event: %s, Duration: %s",
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
        LOGI(TAG, "Current Event Type: " + Event.getName(mAppContext, getCurrentEventType()));

        if (eventType == Event.SHORT_BREAK || eventType == Event.LONG_BREAK)
            getFitUtils().startSession(mCurrentEventId);

        notifyChange();
    }

    private FitUtils getFitUtils() {
        if (mFitUtils == null)
            mFitUtils = new FitUtils(mAppContext,
                    AccountUtils.getActiveAccountName(mAppContext));
        return mFitUtils;
    }

    public void stopEvent() {
        stopEvent(false);
    }

    public void stopEvent(boolean stoppedManually) {
        cancelIntentIfRunning();

        int currentEventType = getCurrentEventType();

        if (currentEventType == Event.SHORT_BREAK || currentEventType == Event.LONG_BREAK) {
            getFitUtils().stopSession(mCurrentEventId);
            LOGI(TAG, "Stop event - loading: " + mCurrentEventId);
            Event event = EventHelper.load(mAppContext, mCurrentEventId);
            event.getData().steps = mCurrentSteps;
            EventHelper.update(mAppContext, event);
            getFitUtils().saveSession(event);
            resetSteps();
        }

        LOGI(TAG, String.format("Stopping Event: %s, Count: %s",
                Event.getName(mAppContext, currentEventType),
                mPomodoroCount));
        if (stoppedManually) {
            EventHelper.delete(mAppContext, mCurrentEventId);
        }
        if (currentEventType == Event.POMODORO &&
                !stoppedManually) {
            mPomodoroCount++;
            getCurrentSession().getStats().pomoCount += 1;
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

        SessionHelper.update(mAppContext, getCurrentSession());
        if (stoppedManually)
            notifyChange();
    }

    private void notifyChange() {
        LocalBroadcastManager.getInstance(mAppContext).sendBroadcast(
                new Intent(SessionManager.ACTION_EVENT));
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
        switch (eventType) {
            case Event.POMODORO:
                return PrefUtils.getPomodoroDuration(mAppContext);
            case Event.SHORT_BREAK:
                return PrefUtils.getShortBreakDuration(mAppContext);
            case Event.LONG_BREAK:
                return PrefUtils.getLongBreakDuration(mAppContext);
        }

        return 0;
    }

    public int getPomodoroCount() {
        return mPomodoroCount;
    }

    public long getCurrentEndTime() {
        return mCurrentEndTime;
    }

    public void setCurrentEndTime(long currentEndTime) {
        mCurrentEndTime = currentEndTime;
        mPrefs.edit().putLong(PREF_CURRENT_END_TIME, currentEndTime).apply();
    }

    public void resetSteps() {
        addCurrentSteps(-mCurrentSteps);
    }

    public void addCurrentSteps(int steps) {
        mCurrentSteps += steps;
        mPrefs.edit().putInt(PREF_CURRENT_STEPS, mCurrentSteps).apply();
    }

    public void updateSteps(int steps) {
        LOGI(TAG, "Updating session - steps: " + steps);
        mSession.getStats().stepCount += steps;
        SessionHelper.update(mAppContext, mSession);
        addCurrentSteps(steps);
    }
}
