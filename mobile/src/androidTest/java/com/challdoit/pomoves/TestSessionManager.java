package com.challdoit.pomoves;

import android.test.AndroidTestCase;

import com.challdoit.pomoves.model.Event;
import com.challdoit.pomoves.model.Session;

public class TestSessionManager extends AndroidTestCase {

    private SessionManager manager;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        manager = SessionManager.get(mContext);
    }

    private void testClearPreferences() {
        manager.clearPreferences();
    }

    public void testStartSessionFromZero() {

        Session session = manager.startSession();

        long currentSessionId = manager.getCurrentSessionId();

        assertTrue(currentSessionId > 0);

        assertTrue(manager.isTrackingSession(session));

        testClearPreferences();

    }

    public void testSessionLifecycle() {

        manager.startSession();

        assertTrue(manager.isTrackingSession());

        assertEquals(Event.POMODORO, manager.getCurrentEventType());

        manager.stopEvent();

        assertEquals(Event.SHORT_BREAK, manager.getCurrentEventType());

        manager.stopEvent();

        assertEquals(Event.POMODORO, manager.getCurrentEventType());

        manager.stopEvent();

        assertEquals(Event.SHORT_BREAK, manager.getCurrentEventType());

        manager.stopEvent();

        assertEquals(Event.POMODORO, manager.getCurrentEventType());

        manager.stopEvent();

        assertEquals(Event.SHORT_BREAK, manager.getCurrentEventType());

        manager.stopEvent();

        assertEquals(Event.POMODORO, manager.getCurrentEventType());

        manager.stopEvent();

        assertEquals(Event.LONG_BREAK, manager.getCurrentEventType());

        manager.stopEvent(true);

        assertEquals(Event.POMODORO, manager.getCurrentEventType());

        assertFalse(manager.isTrackingSession());

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
