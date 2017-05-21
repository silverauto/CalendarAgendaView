package com.mstest.eventscalendar.model;

/**
 * Created by silverauto on 2017/5/8.
 */

public class Event {
    private String mTitle;
    private long mStartTime;
    private long mDurationInMillis;
    private boolean mIsAllDay;

    public Event(long startTime, long endTime, String title, boolean isAllDay) {
        mStartTime = startTime;
        mDurationInMillis = endTime - startTime;
        mTitle = title;
        mIsAllDay = isAllDay;
    }

    public String getTitle() {
        return mTitle;
    }

    public long getStartTime() {
        return mStartTime;
    }

    public long getDurationInMillis() {
        return mDurationInMillis;
    }

    public boolean isAllDay() {
        return mIsAllDay;
    }
}
