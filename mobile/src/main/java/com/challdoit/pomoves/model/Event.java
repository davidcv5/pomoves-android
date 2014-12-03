package com.challdoit.pomoves.model;

import java.util.Date;

/**
 * Created by David on 12/3/14.
 */
public class Event {

    private int eventType;
    private Date startDate;
    private Date endDate;

    public Event(int eventType) {
        this.eventType = eventType;
        startDate = new Date();
    }

    public int getEventType() {
        return eventType;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public long duration() {
        return endDate.getTime() - startDate.getTime();
    }

    private void stop() {
        endDate = new Date();
    }
}
