package com.challdoit.pomoves.model;

import android.content.ContentValues;

import com.challdoit.pomoves.data.PomovesContract;

import java.util.Date;

/**
 * Created by David on 12/3/14.
 */
public class Event {

    private long mId;
    private long mSessionId;
    private int mEventType;
    private Date mStartDate;
    private Date mEndDate;
    private String mData;

    public Event() {
    }

    public Event(long sessionId, int eventType) {
        this.mId = -1;
        this.mSessionId = sessionId;
        this.mEventType = eventType;
        mStartDate = new Date();
    }

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        mId = id;
    }

    public long getSessionId() {
        return mSessionId;
    }

    public void setSessionId(long sessionId) {
        this.mSessionId = sessionId;
    }

    public int getEventType() {
        return mEventType;
    }

    public void setEventType(int eventType) {
        this.mEventType = eventType;
    }

    public Date getStartDate() {
        return mStartDate;
    }

    public void setStartDate(Date startDate) {
        this.mStartDate = startDate;
    }

    public Date getEndDate() {
        return mEndDate;
    }

    public void setEndDate(Date endDate) {
        this.mEndDate = endDate;
    }

    public String getData() {
        return mData;
    }

    public void setData(String data) {
        mData = data;
    }

    public ContentValues getContentValues() {
        ContentValues values = new ContentValues();
        values.put(PomovesContract.EventEntry.COLUMN_SESSION_ID, mSessionId);
        values.put(PomovesContract.EventEntry.COLUMN_TYPE, mEventType);
        values.put(PomovesContract.EventEntry.COLUMN_START_TEXT,
                PomovesContract.getDbDateString(mStartDate));
        values.put(PomovesContract.EventEntry.COLUMN_END_TEXT,
                PomovesContract.getDbDateString(mEndDate));
        values.put(PomovesContract.EventEntry.COLUMN_DATA, mData);
        return values;
    }

    public static final class EventType {
        public static final int POMODORO = 1;
        public static final int SHORT_BREAK = 2;
        public static final int LONG_BREAK = 3;
    }

}
