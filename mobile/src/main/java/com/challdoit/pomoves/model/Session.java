package com.challdoit.pomoves.model;

import com.challdoit.pomoves.data.PomovesContract;
import com.google.gson.Gson;

import java.util.Date;

public class Session {

    private long mId;
    private Date mDate;
    private String mStatsJson;
    private Stats mStats;
    private String mUser;

    private Gson gson;

    public Session() {
        mId = -1;
        mDate = new Date();
        mStats = new Stats();
        gson = new Gson();
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

    public Stats getStats() {
        return mStats;
    }

    public void setStatsFromJson(String stats) {
        this.mStats = gson.fromJson(stats, Stats.class);
    }

    public String getStatsJson() {
        return gson.toJson(mStats, Stats.class);
    }

    public String getUser() {
        return mUser;
    }

    public void setUser(String user) {
        mUser = user;
    }

    @Override
    public String toString() {
        return String.format("ID: %s, Date: %s, User: %s",
                getId(),
                PomovesContract.getDbDateString(getDate()),
                getUser());
    }

    public class Stats {
        public int pomoCount;
        public int pomoTime;
        public int stepCount;
        public int stepTime;
        public int waterCount;
        public String info = "";

        public Stats() {
        }
    }
}
