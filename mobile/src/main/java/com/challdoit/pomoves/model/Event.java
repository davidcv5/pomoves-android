package com.challdoit.pomoves.model;

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
}
