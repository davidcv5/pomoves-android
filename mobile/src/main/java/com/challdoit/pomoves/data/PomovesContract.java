package com.challdoit.pomoves.data;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by David on 12/3/14.
 */
public class PomovesContract {

    public static final String CONTENT_AUTHORITY = "com.challdoit.pomoves";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_SESSION = "session";
    public static final String PATH_EVENT = "event";

    public static final String DATE_FORMAT = "yyyyMMdd";
    public static final String DATETIME_FORMAT = "yyyyMMdd:hhmmss";

    public static final class SessionEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_SESSION).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_SESSION;
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_SESSION;

        public static final String TABLE_NAME = "session";

        public static final String COLUMN_DATE_TEXT = "date";
        public static final String COLUMN_DURATION = "duration";
        public static final String COLUMN_STATS = "stats";

        public static Uri buildSessionUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildSessionWithStartDate(String startDate) {
            return CONTENT_URI.buildUpon()
                    .appendQueryParameter(COLUMN_DATE_TEXT, startDate).build();
        }

        public static String getStartDateFromUri(Uri uri) {
            return uri.getQueryParameter(COLUMN_DATE_TEXT);
        }

    }

    public static final class EventEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_EVENT).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_EVENT;
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_EVENT;


        public static final String TABLE_NAME = "event";

        public static final String COLUMN_SESSION_ID = "session_id";

        public static final String COLUMN_TYPE = "type";
        public static final String COLUMN_START_TEXT = "start";
        public static final String COLUMN_END_TEXT = "end";
        public static final String COLUMN_DATA = "data";

        public static Uri buildEventUri(long _id) {
            return ContentUris.withAppendedId(CONTENT_URI, _id);
        }

        public static Uri buildEventForSession(long sessionId) {
            return CONTENT_URI.buildUpon()
                    .appendPath(Long.toString(sessionId)).build();
        }

        public static Uri buildEventWithType(long sessionId, int type) {
            return CONTENT_URI.buildUpon()
                    .appendPath(Long.toString(sessionId))
                    .appendQueryParameter(COLUMN_TYPE, Integer.toString(type))
                    .build();
        }

        public static final long getSessionFromUri(Uri uri) {
            return Long.parseLong(uri.getPathSegments().get(1));
        }

        public static final int getTypeFromUri(Uri uri) {
            String type = uri.getQueryParameter(COLUMN_TYPE);

            if (type != null)
                return Integer.parseInt(uri.getQueryParameter(COLUMN_TYPE));

            return -1;
        }
    }

//    public static final class DetailEntry implements BaseColumns {
//
//        public static final String TABLE_NAME = "detail";
//
//        public static final String COLUMN_EVENT_ID = "event_id";
//
//        public static final String COLUMN_TYPE = "type";
//        public static final String COLUMN_DATA = "data";
//
//    }

    public static String getDbDateString(Date date){
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        return sdf.format(date);
    }
}
