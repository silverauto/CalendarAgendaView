package com.mstest.eventscalendar.model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * A data structure to store the events occurred in the same day
 */

public final class CalendarData {

    private List<Event> mOccurredEvents = new ArrayList<>();
    private Date mDate;
    private Calendar mCalendar;

    public static CalendarData from(Calendar calendar) {
        if (calendar == null) {
            return null;
        }
        return new CalendarData(calendar);
    }

    private CalendarData(Calendar calendar) {
        mCalendar = calendar;
    }

    public Date getDate() {
        if (mDate == null) {
            mDate = getCalendar().getTime();
        }
        return mDate;
    }

    public Calendar getCalendar() {
        return mCalendar;
    }

    public void addEvent(Event event) {
        if (event == null) {
            return;
        }
        mOccurredEvents.add(event);
    }

    public void updateEvents(List<Event> events) {
        if (events == null) {
            mOccurredEvents.clear();
            return;
        }
        mOccurredEvents = events;
    }

    public boolean hasNoEvent() {
        return mOccurredEvents.size() == 0;
    }

    public int numberOfEvents() {
        int count = mOccurredEvents.size();
        return count == 0 ? 1 : count;
    }

    public Event getEventInDay(int position) {
         if(position >= mOccurredEvents.size()) {
             return null;
         }
        return mOccurredEvents.get(position);
    }
}
