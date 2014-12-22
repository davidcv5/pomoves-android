package com.challdoit.pomoves.model;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.challdoit.pomoves.data.PomovesContract;
import com.challdoit.pomoves.data.PomovesContract.SessionEntry;
import com.challdoit.pomoves.data.PomovesProvider.SessionCursor;

public class SessionHelper {

    public static Session load(Context context, long id) {
        Cursor cursor = context.getContentResolver().query(
                SessionEntry.buildSessionUri(id),
                null,
                null,
                null,
                null);

        if (cursor.moveToFirst()) {
            SessionCursor sessionCursor =
                    new SessionCursor(cursor);
            return sessionCursor.getSession();
        }

        return null;
    }

    public static void insert(Context context, Session session) {
        Uri sessionUri = context.getContentResolver().insert(
                SessionEntry.CONTENT_URI,
                getContentValues(session));
        session.setId(ContentUris.parseId(sessionUri));
    }

    public static void update(Context context, Session session) {
        context.getContentResolver().update(
                SessionEntry.CONTENT_URI,
                getContentValues(session),
                SessionEntry._ID + "=?",
                new String[]{Long.toString(session.getId())});
    }

    private static ContentValues getContentValues(Session session) {
        ContentValues values = new ContentValues();
        values.put(PomovesContract.SessionEntry.COLUMN_DATE_TEXT,
                PomovesContract.getDbDateString(session.getDate()));
        values.put(PomovesContract.SessionEntry.COLUMN_STATS, session.getStats());
        return values;
    }
}
