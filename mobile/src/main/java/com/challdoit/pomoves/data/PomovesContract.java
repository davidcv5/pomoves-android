package com.challdoit.pomoves.data;

import android.provider.BaseColumns;

/**
 * Created by David on 12/3/14.
 */
public class PomovesContract {

    public static final class SessionEntry implements BaseColumns {

        public static final String TABLE_NAME = "session";

        public static final String COLUMN_DATE_TEXT = "date";
        public static final String COLUMN_DURATION = "duration";
        public static final String COLUMN_DETAILS = "details";

    }

    public static final class EventEntry implements BaseColumns {

        public static final String TABLE_NAME = "event";

        public static final String COLUMN_SESSION_ID = "session_id";

        public static final String COLUMN_TYPE = "type";
        public static final String COLUMN_START_TEXT = "start";
        public static final String COLUMN_END_TEXT = "end";
    }

    public static final class DetailEntry implements BaseColumns{

        public static final String TABLE_NAME = "detail";

        public static final String COLUMN_EVENT_ID = "event_id";

        public static final String COLUMN_TYPE = "type";
        public static final String COLUMN_DATA = "data";

    }

}
