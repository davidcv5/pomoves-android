package com.challdoit.pomoves.model;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.challdoit.pomoves.data.PomovesContract;
import com.challdoit.pomoves.provider.PomovesProvider;

public class EventHelper {

    public static Event load(Context context, long id) {
        Cursor cursor = context.getContentResolver().query(
                PomovesContract.EventEntry.buildEventUri(id),
                null,
                null,
                null,
                null);

        if (cursor.moveToFirst()) {
            PomovesProvider.EventCursor eventCursor =
                    new PomovesProvider.EventCursor(cursor);
            return eventCursor.getEvent();
        }

        return null;
    }

    public static void insert(Context context, Event event) {
        Uri eventUri = context.getContentResolver().insert(
                PomovesContract.EventEntry.CONTENT_URI,
                getContentValues(event));
        event.setId(ContentUris.parseId(eventUri));
    }

    public static void update(Context context, Event event) {
        context.getContentResolver().update(
                PomovesContract.EventEntry.CONTENT_URI,
                getContentValues(event),
                PomovesContract.SessionEntry._ID + "=?",
                new String[]{Long.toString(event.getId())});
    }


    private static ContentValues getContentValues(Event event) {
        ContentValues values = new ContentValues();
        values.put(PomovesContract.EventEntry.COLUMN_SESSION_ID, event.getSessionId());
        values.put(PomovesContract.EventEntry.COLUMN_TYPE, event.getEventType());
        values.put(PomovesContract.EventEntry.COLUMN_START_TEXT,
                PomovesContract.getDbDateTimeString(event.getStartDate()));
        values.put(PomovesContract.EventEntry.COLUMN_END_TEXT,
                PomovesContract.getDbDateTimeString(event.getEndDate()));
        values.put(PomovesContract.EventEntry.COLUMN_DATA, event.getData());
        return values;
    }

    public static void delete(Context context, long currentEventId) {
        if (currentEventId > 0) {
            context.getContentResolver().delete(
                    PomovesContract.EventEntry.CONTENT_URI,
                    PomovesContract.EventEntry._ID + "=?",
                    new String[]{Long.toString(currentEventId)});
        }
    }
}
