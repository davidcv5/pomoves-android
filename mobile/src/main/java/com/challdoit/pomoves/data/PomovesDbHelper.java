package com.challdoit.pomoves.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.challdoit.pomoves.data.PomovesContract.EventEntry;
import com.challdoit.pomoves.data.PomovesContract.SessionEntry;

/**
 * Created by David on 12/3/14.
 */
public class PomovesDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "pomoves.db";

    public PomovesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        final String SQL_CREATE_SESSION_TABLE =
                "CREATE TABLE " + SessionEntry.TABLE_NAME + " (" +
                        SessionEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        SessionEntry.COLUMN_DATE_TEXT + " TEXT NOT NULL, " +
                        SessionEntry.COLUMN_DURATION + " INTEGER, " +
                        SessionEntry.COLUMN_STATS + " TEXT " +
                        ");";

        final String SQL_CREATE_EVENT_TABLE =
                "CREATE TABLE " + EventEntry.TABLE_NAME + " (" +
                        EventEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        EventEntry.COLUMN_SESSION_ID + " INTEGER NOT NULL, " +
                        EventEntry.COLUMN_TYPE + " INTEGER NOT NULL, " +
                        EventEntry.COLUMN_START_TEXT + " TEXT NOT NULL, " +
                        EventEntry.COLUMN_END_TEXT + " TEXT NOT NULL, " +
                        EventEntry.COLUMN_DATA + " TEXT, " +
                        " FOREIGN KEY (" + EventEntry.COLUMN_SESSION_ID + ") REFERENCES " +
                        SessionEntry.TABLE_NAME + " (" + SessionEntry._ID + ")" +
                        ");";

        db.execSQL(SQL_CREATE_SESSION_TABLE);
        db.execSQL(SQL_CREATE_EVENT_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + SessionEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + EventEntry.TABLE_NAME);
        onCreate(db);
    }
}
