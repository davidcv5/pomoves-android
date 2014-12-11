package com.challdoit.pomoves;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.util.Log;

import com.challdoit.pomoves.data.PomovesContract.SessionEntry;
import com.challdoit.pomoves.data.PomovesContract.EventEntry;
import com.challdoit.pomoves.data.PomovesDbHelper;
import com.challdoit.pomoves.model.EventType;

import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * Created by David on 12/3/14.
 */
public class TestDb extends AndroidTestCase {
    private static final String LOG_TAG = TestDb.class.getSimpleName();

    public void testCreateDb() throws Throwable {
        mContext.deleteDatabase(PomovesDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new PomovesDbHelper(mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());
        db.close();
    }

    public void testInsertReadDb() {
        PomovesDbHelper dbHelper = new PomovesDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = getSessionValues();

        long sessionRowId;
        sessionRowId = db.insert(SessionEntry.TABLE_NAME, null, values);

        assertTrue(sessionRowId != -1);
        Log.d(LOG_TAG, "New row id: " + sessionRowId);


        Cursor cursor = db.query(
                SessionEntry.TABLE_NAME,
                null,
                null,
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

        ContentValues eventValues = getEventValues(sessionRowId);
        db.insert(EventEntry.TABLE_NAME, null, eventValues);

        Cursor eventCursor = db.query(
                EventEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null);

        if (eventCursor.moveToFirst()) {
            validateCursor(eventValues, eventCursor);
        } else {
            fail("No values returned :(");
        }
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

    private ContentValues getSessionValues() {
        String testDate = new Date().toString();
        String testDetails = "some details";
        int testDuration = 5;

        ContentValues values = new ContentValues();
        values.put(SessionEntry.COLUMN_DATE_TEXT, testDate);
        values.put(SessionEntry.COLUMN_STATS, testDetails);
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
}
