package com.challdoit.pomoves;

import android.content.ContentValues;
import android.database.Cursor;
import android.test.AndroidTestCase;

import com.challdoit.pomoves.data.PomovesContract;
import com.challdoit.pomoves.data.PomovesDbHelper;
import com.challdoit.pomoves.model.Event;
import com.challdoit.pomoves.model.Session;
import com.challdoit.pomoves.model.SessionHelper;

public class TestSessionManager extends AndroidTestCase {

    private SessionManager manager;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        manager = SessionManager.get(mContext);
    }

    public void testDeleteAll() {

        mContext.getContentResolver().delete(
                PomovesContract.EventEntry.CONTENT_URI,
                null,
                null);

        mContext.getContentResolver().delete(
                PomovesContract.SessionEntry.CONTENT_URI,
                null,
                null);

        Cursor cursor = mContext.getContentResolver().query(
                PomovesContract.SessionEntry.CONTENT_URI, null, null, null, null);
        assertTrue(cursor.getCount() == 0);

        cursor.close();

        Cursor eventCursor = mContext.getContentResolver().query(
                PomovesContract.EventEntry.CONTENT_URI, null, null, null, null);
        assertTrue(eventCursor.getCount() == 0);

        eventCursor.close();
    }

    public void testClearPreferences() {
        manager.clearSession();
    }

    public void testSessionLifecycle() {

        Session session = manager.startSession(); // Event 1

        long currentSessionId = manager.getCurrentSessionId();

        assertTrue(currentSessionId > 0);

        assertTrue(manager.isTrackingSession(session));

        assertTrue(manager.isTrackingSession());

        assertEquals(Event.POMODORO, manager.getCurrentEventType());

        manager.stopEvent(); // Event 2

        assertEquals(Event.SHORT_BREAK, manager.getCurrentEventType());

        manager.stopEvent(); // Event 3

        assertEquals(Event.POMODORO, manager.getCurrentEventType());

        manager.stopEvent(); // Event 4

        assertEquals(Event.SHORT_BREAK, manager.getCurrentEventType());

        manager.stopEvent(); // Event 5

        assertEquals(Event.POMODORO, manager.getCurrentEventType());

        manager.stopEvent(true); // Event 4

        Cursor eventCursor = mContext.getContentResolver().query(
                PomovesContract.EventEntry.CONTENT_URI,
                new String[]{PomovesContract.EventEntry._ID},
                null,
                null,
                null);

        assertEquals(4, eventCursor.getCount());

        manager.startSession(); // Event 5

        assertEquals(Event.POMODORO, manager.getCurrentEventType());

        manager.stopEvent(); // Event 6

        assertEquals(Event.SHORT_BREAK, manager.getCurrentEventType());

        manager.stopEvent(); // Event 7

        assertEquals(Event.POMODORO, manager.getCurrentEventType());

        manager.stopEvent(); // Event 8

        assertEquals(Event.LONG_BREAK, manager.getCurrentEventType());

        manager.stopEvent(true); // Event 9

        assertEquals(Event.POMODORO, manager.getCurrentEventType());

        assertFalse(manager.isTrackingSession());

        Cursor cursor = mContext.getContentResolver().query(
                PomovesContract.SessionEntry.CONTENT_URI,
                new String[]{PomovesContract.SessionEntry._ID},
                null,
                null,
                null);

        assertEquals(1, cursor.getCount());

        testClearPreferences();
    }

    public void testStopSessionManually() {

        manager.startSession();

        assertEquals(Event.POMODORO, manager.getCurrentEventType());

        manager.stopEvent();

        assertEquals(Event.SHORT_BREAK, manager.getCurrentEventType());

        manager.stopEvent(true);

        assertEquals(Event.POMODORO, manager.getCurrentEventType());

        testClearPreferences();
    }

//    public void testResumeSessionFromPomodoro(){
//
//    }
//
//    public void testResumeSessionFromBreak(){
//
//    }
//
//    public void testResumeSessionFromLongPause(){
//
//    }


}
