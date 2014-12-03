package com.challdoit.pomoves;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.util.Log;

import com.challdoit.pomoves.data.PomovesContract.SessionEntry;
import com.challdoit.pomoves.data.PomovesDbHelper;

import java.util.Date;

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
        String testDate = new Date().toString();
        String testDetails = "some details";
        int testDuration = 5;

        PomovesDbHelper dbHelper = new PomovesDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(SessionEntry.COLUMN_DATE_TEXT, testDate);
        values.put(SessionEntry.COLUMN_DETAILS, testDetails);
        values.put(SessionEntry.COLUMN_DURATION, testDuration);

        long sessionRowId;
        sessionRowId = db.insert(SessionEntry.TABLE_NAME, null, values);

        assertTrue(sessionRowId != -1);
        Log.d(LOG_TAG, "New row id: " + sessionRowId);

        String[] columns = {
                SessionEntry._ID,
                SessionEntry.COLUMN_DATE_TEXT,
                SessionEntry.COLUMN_DETAILS,
                SessionEntry.COLUMN_DURATION
        };

        Cursor cursor = db.query(
                SessionEntry.TABLE_NAME,
                columns,
                null,
                null,
                null,
                null,
                null);

        if(cursor.moveToFirst()){
            int dateIndex = cursor.getColumnIndex(SessionEntry.COLUMN_DATE_TEXT);
            String date = cursor.getString(dateIndex);

            int detailsIndex = cursor.getColumnIndex(SessionEntry.COLUMN_DETAILS);
            String details = cursor.getString(detailsIndex);

            int durationIndex = cursor.getColumnIndex(SessionEntry.COLUMN_DURATION);
            int duration = cursor.getInt(durationIndex);

            assertEquals(date, testDate);
            assertEquals(details, testDetails);
            assertEquals(duration, testDuration);
        }else {
            fail("No values returned :(");
        }
    }
}
