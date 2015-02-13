package com.challdoit.pomoves.model;

import android.content.Context;

import com.challdoit.pomoves.R;
import com.google.gson.Gson;

import java.util.Date;

public class Event {

    private long mId;
    private long mSessionId;
    private int mEventType;
    private Date mStartDate;
    private Date mEndDate;
    private Data mData;

    private Gson gson;

    public static final int POMODORO = 1;
    public static final int SHORT_BREAK = 2;
    public static final int LONG_BREAK = 3;

    public Event() {
        gson = new Gson();
    }

    public Event(long sessionId, int eventType) {
        this();
        this.mId = -1;
        this.mSessionId = sessionId;
        this.mEventType = eventType;
        mStartDate = new Date();
        mEndDate = mStartDate;
        mData = new Data();
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

    public Data getData() {
        return mData;
    }

    public void setDataFromJson(String stats) {
        this.mData = gson.fromJson(stats, Data.class);
    }

    public String getDataJson() {
        return gson.toJson(mData, Data.class);
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

    public class Data {
        public int steps;

        public Data() {

        }
    }

    @Override
    public String toString() {
        return String.format("ID: %s, Type: %s", mId, mEventType);
    }
}
