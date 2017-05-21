package com.mstest.eventscalendar.model;

import com.mstest.eventscalendar.utils.CalendarUtils;

import java.util.Calendar;
import java.util.Date;

public class CalendarDay {
    private Calendar mCalendar;
    private Date mDate;
    private int mMonth;
    private int mDay;
    private boolean mIsSelected;

    public static CalendarDay from(Calendar calendar) {
        if (calendar == null) {
            return null;
        }
        return new CalendarDay(calendar);
    }

    private CalendarDay(Calendar calendar) {
        mCalendar = CalendarUtils.copyFrom(calendar);
        mMonth = CalendarUtils.getMonth(mCalendar);
        mDay = CalendarUtils.getDay(mCalendar);
        mIsSelected = false;
    }

    public int getMonth() {
        return mMonth;
    }

    public int getDay() {
        return mDay;
    }

    public Calendar getCalendar() {
        return mCalendar;
    }

    public Date getDate() {
        if(mDate == null) {
            mDate = mCalendar.getTime();
        }
        return mDate;
    }

    public void setSelected(boolean selected) {
        mIsSelected = selected;
    }

    public boolean isSelected() {
        return mIsSelected;
    }
}
