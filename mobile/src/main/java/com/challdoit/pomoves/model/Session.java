package com.challdoit.pomoves.model;

import com.challdoit.pomoves.data.PomovesContract;

import java.util.Date;

public class Session {

    private long mId;
    private Date mDate;
    private String mStats;

    public Session() {
        mId = -1;
        mDate = new Date();
    }

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        mId = id;
    }

    public Date getDate() {
        return mDate;
    }

    public void setDate(Date date) {
        mDate = date;
    }

    public String getStats() {
        return mStats;
    }

    public void setStats(String stats) {
        this.mStats = stats;
    }

    @Override
    public String toString() {
        return String.format("ID: %s, Date: %s",
                getId(), PomovesContract.getDbDateString(getDate()));
    }
}
