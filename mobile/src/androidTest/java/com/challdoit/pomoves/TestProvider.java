package com.challdoit.pomoves;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.test.AndroidTestCase;
import android.util.Log;

import com.challdoit.pomoves.data.PomovesContract.EventEntry;
import com.challdoit.pomoves.data.PomovesContract.SessionEntry;
import com.challdoit.pomoves.model.EventType;

import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * Created by David on 12/3/14.
 */
public class TestProvider extends AndroidTestCase {
    private static final String LOG_TAG = TestProvider.class.getSimpleName();

    private static String testDate = "20141210";
    private static String testValidDate = "20141201";
    private static String testInvalidDate = "20151201";

    public void testDeleteAllRecords() {
        mContext.getContentResolver().delete(
                EventEntry.CONTENT_URI, null, null);
        mContext.getContentResolver().delete(
                SessionEntry.CONTENT_URI, null, null);

        Cursor cursor = mContext.getContentResolver().query(
                SessionEntry.CONTENT_URI, null, null, null, null);
        assertTrue(cursor.getCount() == 0);

        cursor.close();

        cursor = mContext.getContentResolver().query(
                EventEntry.CONTENT_URI, null, null, null, null);
        assertTrue(cursor.getCount() == 0);
    }

    public void testInsertReadProvider() {

        ContentValues values = getSessionValues();

        Uri sessionRowUri =
                mContext.getContentResolver().insert(
                        SessionEntry.CONTENT_URI, values);
        long sessionRowId = ContentUris.parseId(sessionRowUri);

        Log.d(LOG_TAG, "New row id: " + sessionRowId);

        Cursor cursor = mContext.getContentResolver().query(
                SessionEntry.buildSessionUri(sessionRowId),
                null,
                null,
                null,
                null);

        if (cursor.moveToFirst()) {
            validateCursor(values, cursor);
        } else {
            fail("No values returned :(");
        }

        cursor.close();

        Cursor sessionCursor = mContext.getContentResolver().query(
                SessionEntry.CONTENT_URI,
                null,
                null,
                null,
                null);

        assertTrue(sessionCursor.getCount() == 1);

        sessionCursor.close();

        Cursor sessionWithStartDateCursor = mContext.getContentResolver().query(
                SessionEntry.buildSessionWithStartDate(testValidDate),
                null,
                null,
                null,
                null);

        assertTrue(sessionWithStartDateCursor.getCount() == 1);

        sessionWithStartDateCursor.close();

        sessionWithStartDateCursor = mContext.getContentResolver().query(
                SessionEntry.buildSessionWithStartDate(testInvalidDate),
                null,
                null,
                null,
                null);

        assertTrue(sessionWithStartDateCursor.getCount() == 0);

        sessionWithStartDateCursor.close();

        ContentValues eventValues = getEventValues(sessionRowId);
        mContext.getContentResolver().insert(
                EventEntry.CONTENT_URI, eventValues);

        Cursor eventCursor = mContext.getContentResolver().query(
                EventEntry.buildEventForSession(sessionRowId),
                null,
                null,
                null,
                null);

        if (eventCursor.moveToFirst()) {
            validateCursor(eventValues, eventCursor);
        } else {
            fail("No values returned :(");
        }

        eventCursor.close();

        Cursor eventTypePomodoroCursor = mContext.getContentResolver().query(
                EventEntry.buildEventWithType(sessionRowId, EventType.POMODORO),
                null,
                null,
                null,
                null);

        assertTrue("Should be 1 event with type POMODORO. Found: " +
                        eventTypePomodoroCursor.getCount(),
                eventTypePomodoroCursor.getCount() == 1);


        eventTypePomodoroCursor.close();

        Cursor eventTypeShortBreakCursor = mContext.getContentResolver().query(
                EventEntry.buildEventWithType(sessionRowId, EventType.SHORT_BREAK),
                null,
                null,
                null,
                null);

        assertTrue("Should be 0 events with type SHORT_BREAK. Found: " +
                        eventTypeShortBreakCursor.getCount(),
                eventTypeShortBreakCursor.getCount() == 0);

        eventTypeShortBreakCursor.close();

    }

    public void testDeleteRecordsAtEnd(){
        testDeleteAllRecords();
    }

    public static void validateCursor(ContentValues expected, Cursor valueCursor) {
        Set<Map.Entry<String, Object>> valueSet = expected.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse(-1 == idx);

            String expectedValue = entry.getValue().toString();

            assertEquals(expectedValue, valueCursor.getString(idx));
        }
    }

    public void testGetType() {
        String type = mContext.getContentResolver().getType(SessionEntry.CONTENT_URI);
        assertEquals(SessionEntry.CONTENT_TYPE, type);

        long testSessionId = 1;
        type = mContext.getContentResolver().getType(
                SessionEntry.buildSessionUri(testSessionId));
        assertEquals(SessionEntry.CONTENT_ITEM_TYPE, type);

        String testStartDate = new Date().toString();
        type = mContext.getContentResolver().getType(
                SessionEntry.buildSessionWithStartDate(testStartDate));
        assertEquals(SessionEntry.CONTENT_TYPE, type);

        type = mContext.getContentResolver().getType(
                EventEntry.buildEventForSession(testSessionId));
        assertEquals(EventEntry.CONTENT_TYPE, type);

        type = mContext.getContentResolver().getType(
                EventEntry.buildEventWithType(testSessionId, EventType.POMODORO));
        assertEquals(EventEntry.CONTENT_TYPE, type);

    }

    private ContentValues getSessionValues() {
        String testStats = "some stats";
        int testDuration = 5;

        ContentValues values = new ContentValues();
        values.put(SessionEntry.COLUMN_DATE_TEXT, testDate);
        values.put(SessionEntry.COLUMN_STATS, testStats);
        values.put(SessionEntry.COLUMN_DURATION, testDuration);
        return values;
    }

    private ContentValues getEventValues(long sessionId) {
        int testType = EventType.POMODORO;
        long currentTime = System.currentTimeMillis();
        String testStart = new Date(currentTime - 25 * 60 * 1000).toString();
        String testEnd = new Date(currentTime).toString();
        String testData = "some data";

        ContentValues values = new ContentValues();
        values.put(EventEntry.COLUMN_SESSION_ID, sessionId);
        values.put(EventEntry.COLUMN_TYPE, testType);
        values.put(EventEntry.COLUMN_START_TEXT, testStart);
        values.put(EventEntry.COLUMN_END_TEXT, testEnd);
        values.put(EventEntry.COLUMN_DATA, testData);
        return values;
    }

    public void testUpdateSession() {
        testDeleteAllRecords();

        ContentValues values = getSessionValues();

        Uri sessionUri = mContext.getContentResolver().insert(
                SessionEntry.CONTENT_URI, values);
        long sessionRowId = ContentUris.parseId(sessionUri);

        assertTrue(sessionRowId != -1);

        int testDuration = 10;

        ContentValues values2 = new ContentValues(values);
        values2.put(SessionEntry._ID, sessionRowId);
        values2.put(SessionEntry.COLUMN_DURATION, 10);

        int count = mContext.getContentResolver().update(
                SessionEntry.CONTENT_URI,
                values2,
                SessionEntry._ID + "=?",
                new String[]{Long.toString(sessionRowId)});

        assertTrue(count == 1);

        Cursor cursor = mContext.getContentResolver().query(
                SessionEntry.buildSessionUri(sessionRowId),
                null,
                null,
                null,
                null);

        if (cursor.moveToFirst())
            validateCursor(values2, cursor);
        else
            fail("No values returned :(");

        cursor.close();
    }

}