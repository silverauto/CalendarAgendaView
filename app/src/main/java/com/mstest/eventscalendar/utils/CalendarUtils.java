package com.mstest.eventscalendar.utils;

import java.util.Calendar;
import java.util.Locale;

import static java.util.Calendar.DATE;
import static java.util.Calendar.DAY_OF_MONTH;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.YEAR;
/**
 * A utility class to manipulate the Calendar
 */

public final class CalendarUtils {

    public static Calendar today() {
        return Calendar.getInstance();
    }

    public static Calendar today(Locale locale) {
        return Calendar.getInstance(locale);
    }

    public static Calendar getDate(Calendar date, int dayOffset) {
        Calendar newDate = Calendar.getInstance();
        newDate.clear();
        newDate.set(getYear(date), getMonth(date), getDay(date));
        newDate.add(DAY_OF_MONTH, dayOffset);
        return newDate;
    }

    public static Calendar dayOfWeek(Calendar date, int dayOffset) {
        date.set(Calendar.DAY_OF_WEEK, date.getFirstDayOfWeek() + dayOffset);
        return date;
    }

    public static int getYear(Calendar calendar) {
        return calendar.get(YEAR);
    }

    public static int getMonth(Calendar calendar) {
        return calendar.get(MONTH);
    }

    public static int getDay(Calendar calendar) {
        return calendar.get(DATE);
    }

    public static int daysBetween(Calendar start, Calendar end) {
        long millisStart = start.getTimeInMillis();
        long millisEnd = end.getTimeInMillis();
        long millisDiff = millisEnd - millisStart;
        return (int) (millisDiff / (24 * 60 * 60 * 1000));
    }

    public static Calendar copyFrom(Calendar date) {
        if(date == null) {
            throw new IllegalArgumentException("The specified date should NOT be null!");
        }
        return getDate(date, 0);
    }

    public static boolean isSameDay(Calendar date1, Calendar date2) {
        if(date1 == null || date2 == null) {
            return false;
        }
        return getYear(date1) == getYear(date2) &&
                getMonth(date1) == getMonth(date2) &&
                getDay(date1) == getDay(date2);
    }
}
