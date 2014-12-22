package com.challdoit.pomoves.model;

import android.content.Context;

import com.challdoit.pomoves.R;

import java.util.Date;

public class Event {

    private long mId;
    private long mSessionId;
    private int mEventType;
    private Date mStartDate;
    private Date mEndDate;
    private String mData;

    public static final int POMODORO = 1;
    public static final int SHORT_BREAK = 2;
    public static final int LONG_BREAK = 3;

    public Event() {
    }

    public Event(long sessionId, int eventType) {
        this.mId = -1;
        this.mSessionId = sessionId;
        this.mEventType = eventType;
        mStartDate = new Date();
        mEndDate = mStartDate;
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

    public static String getName(Context context, int eventType) {
        switch (eventType) {
            case POMODORO:
                return context.getString(R.string.POMODORO);
            case SHORT_BREAK:
                return context.getString(R.string.SHORT_BREAK);
            case LONG_BREAK:
                return context.getString(R.string.LONG_BREAK);
        }

        return context.getString(R.string.UNKNOWN);
    }
}
