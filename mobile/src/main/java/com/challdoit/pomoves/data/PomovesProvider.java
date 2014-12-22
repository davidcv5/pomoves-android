package com.challdoit.pomoves.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.challdoit.pomoves.model.Event;
import com.challdoit.pomoves.model.Session;

public class PomovesProvider extends ContentProvider {

    private static final int SESSION = 100;
    private static final int SESSION_ID = 101;
    private static final int EVENT = 301;
    private static final int EVENT_FOR_SESSION = 302;

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private PomovesDbHelper mOpenHelper;

    static String sSessionWithStartDate =
            PomovesContract.SessionEntry.TABLE_NAME +
                    "." + PomovesContract.SessionEntry.COLUMN_DATE_TEXT + ">= ?";

    static String sEventSelection =
            PomovesContract.EventEntry.TABLE_NAME +
                    "." + PomovesContract.EventEntry.COLUMN_SESSION_ID + "= ?";

    static String sEventTypeSelection = sEventSelection + " AND " +
            PomovesContract.EventEntry.TABLE_NAME +
            "." + PomovesContract.EventEntry.COLUMN_TYPE + "=?";

    private Cursor getSession(Uri uri, String[] projection, String sortOrder) {
        Cursor cursor;
        String startDate =
                PomovesContract.SessionEntry.getStartDateFromUri(uri);

        String selection = null;
        String[] selectionArgs = null;

        if (startDate != null) {
            selection = sSessionWithStartDate;
            selectionArgs = new String[]{startDate};
        }


        cursor = mOpenHelper.getReadableDatabase().query(
                PomovesContract.SessionEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);
        return cursor;
    }

    private Cursor getEventForSession(Uri uri, String[] projection, String sortOrder) {
        Cursor cursor;
        long sessionId = PomovesContract.EventEntry.getSessionFromUri(uri);
        int type = PomovesContract.EventEntry.getTypeFromUri(uri);

        String selection;
        String[] selectionArgs;

        if (type < 0) {
            selection = sEventSelection;
            selectionArgs = new String[]{Long.toString(sessionId)};
        } else {
            selection = sEventTypeSelection;
            selectionArgs = new String[]{
                    Long.toString(sessionId),
                    Integer.toString(type)};

        }

        cursor = mOpenHelper.getReadableDatabase().query(
                PomovesContract.EventEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);
        return cursor;
    }

    private static UriMatcher buildUriMatcher() {

        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = PomovesContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, PomovesContract.PATH_SESSION, SESSION);
        matcher.addURI(authority, PomovesContract.PATH_SESSION + "/*", SESSION_ID);
        matcher.addURI(authority, PomovesContract.PATH_EVENT, EVENT);
        matcher.addURI(authority, PomovesContract.PATH_EVENT + "/*", EVENT_FOR_SESSION);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new PomovesDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor result;

        switch (sUriMatcher.match(uri)) {
            case SESSION_ID: {
                result = mOpenHelper.getReadableDatabase().query(
                        PomovesContract.SessionEntry.TABLE_NAME,
                        projection,
                        PomovesContract.SessionEntry._ID + "='"
                                + ContentUris.parseId(uri) + "'",
                        null,
                        null,
                        null,
                        sortOrder);
                break;
            }
            case SESSION: {
                result = getSession(uri, projection, sortOrder);
                break;
            }
            case EVENT: {
                result = mOpenHelper.getReadableDatabase().query(
                        PomovesContract.EventEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            case EVENT_FOR_SESSION: {
                result = getEventForSession(uri, projection, sortOrder);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        result.setNotificationUri(
                getContext().getContentResolver(), uri);

        return result;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case SESSION_ID:
                return PomovesContract.SessionEntry.CONTENT_ITEM_TYPE;
            case SESSION:
                return PomovesContract.SessionEntry.CONTENT_TYPE;
            case EVENT:
                return PomovesContract.EventEntry.CONTENT_TYPE;
            case EVENT_FOR_SESSION:
                return PomovesContract.EventEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        Uri returnUri;

        switch (sUriMatcher.match(uri)) {
            case SESSION: {
                long _id = db.insert(PomovesContract.SessionEntry.TABLE_NAME,
                        null, values);
                if (_id >= 0)
                    returnUri = PomovesContract.SessionEntry.buildSessionUri(_id);
                else
                    throw new SQLException("Failed to insert row into " + uri);
                break;
            }
            case EVENT: {
                long _id = db.insert(PomovesContract.EventEntry.TABLE_NAME,
                        null, values);
                if (_id >= 0)
                    returnUri = PomovesContract.EventEntry.buildEventUri(_id);
                else
                    throw new SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        getContext().getContentResolver().notifyChange(returnUri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int rowsDeleted;

        switch (sUriMatcher.match(uri)) {
            case SESSION:
                rowsDeleted = db.delete(
                        PomovesContract.SessionEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case EVENT:
                rowsDeleted = db.delete(
                        PomovesContract.EventEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (selection == null || rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int rowsUpdated;

        switch (sUriMatcher.match(uri)) {
            case SESSION:
                rowsUpdated = db.update(
                        PomovesContract.SessionEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case EVENT:
                rowsUpdated = db.update(
                        PomovesContract.EventEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }

    public static class EventCursor extends CursorWrapper {
        public EventCursor(Cursor cursor) {
            super(cursor);
        }

        public Event getEvent() {
            if (isBeforeFirst() || isAfterLast())
                return null;

            Event event = new Event();
            event.setId(getLong(getColumnIndex(PomovesContract.EventEntry._ID)));
            event.setSessionId(
                    getLong(getColumnIndex(PomovesContract.EventEntry.COLUMN_SESSION_ID)));
            event.setEventType(getInt(getColumnIndex(PomovesContract.EventEntry.COLUMN_TYPE)));
            event.setStartDate(PomovesContract.getDateTimeFromDb(
                    getString(getColumnIndex(PomovesContract.EventEntry.COLUMN_START_TEXT))));
            event.setEndDate(PomovesContract.getDateTimeFromDb(
                    getString(getColumnIndex(PomovesContract.EventEntry.COLUMN_END_TEXT))));

            return event;
        }
    }

    public static class SessionCursor extends CursorWrapper {
        public SessionCursor(Cursor cursor) {
            super(cursor);
        }

        public Session getSession() {
            if (isBeforeFirst() || isAfterLast())
                return null;
            Session session = new Session();
            session.setId(getLong(getColumnIndex(PomovesContract.SessionEntry._ID)));
            session.setDate(PomovesContract.getDateFromDb(
                    getString(getColumnIndex(PomovesContract.SessionEntry.COLUMN_DATE_TEXT))));
            session.setStats(getString(getColumnIndex(PomovesContract.SessionEntry.COLUMN_STATS)));

            return session;
        }
    }

}
