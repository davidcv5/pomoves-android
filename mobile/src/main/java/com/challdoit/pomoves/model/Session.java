package com.challdoit.pomoves.model;

import android.content.ContentValues;

import com.challdoit.pomoves.data.PomovesContract;

import java.util.Date;

/**
 * Created by admin on 12/12/14.
 */
public class Session {

    private long mId;
    private Date mStartDate;
    private String mStats;

    public Session() {
        mId = -1;
        mStartDate = new Date();
    }

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        mId = id;
    }

    public Date getStartDate() {
        return mStartDate;
    }

    public void setStartDate(Date startDate) {
        mStartDate = startDate;
    }

    public String getStats() {
        return mStats;
    }

    public void setStats(String stats) {
        this.mStats = stats;
    }

    public int getDurationSeconds(long endMillis) {
        return (int) ((endMillis - mStartDate.getTime()) / 1000);
    }

    public ContentValues getContentValues() {
        ContentValues values = new ContentValues();
        values.put(PomovesContract.SessionEntry.COLUMN_DATE_TEXT,
                PomovesContract.getDbDateString(mStartDate));
        values.put(PomovesContract.SessionEntry.COLUMN_STATS, mStats);
        return values;
    }
}
