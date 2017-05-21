package com.mstest.eventscalendar.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.text.format.DateFormat;
import android.text.format.DateUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public final class TimeUtils {
    private final static String LONG_DATE_FORMAT = "EEEE, MMMM dd, yyyy";
    private final static String WEEK_DAY_FORMAT = "EEE";
    private final static String MONTH_FORMAT = "MMM";
    private final static String TIME_FORMAT_AM_PM = "h:mm a";
    private final static String TIME_FORMAT_24_HOURS = "H:mm";

    private final static long minuteInMillis = 60000;

    public static String longDateString(Context context, Date date) {
        if(date == null) {
            return "";
        }
        SimpleDateFormat formatter = new SimpleDateFormat(LONG_DATE_FORMAT, getCurrentLocale(context));
        return formatter.format(date);
    }

    public static String weekDayString(Context context, Date date) {
        if(date == null) {
            return "";
        }
        SimpleDateFormat formatter = new SimpleDateFormat(WEEK_DAY_FORMAT, getCurrentLocale(context));
        return formatter.format(date);

    }

    public static String monthString(Context context, Date date) {
        if(date == null) {
            return "";
        }
        SimpleDateFormat formatter = new SimpleDateFormat(MONTH_FORMAT, getCurrentLocale(context));
        return formatter.format(date);
    }

    public static String yearMonthString(Context context, Date date) {
        if(date == null) {
            return "";
        }
        return DateUtils.formatDateRange(context, date.getTime(), date.getTime(),
                DateUtils.FORMAT_SHOW_DATE |
                        DateUtils.FORMAT_NO_MONTH_DAY |
                        DateUtils.FORMAT_SHOW_YEAR);
    }

    public static String timeString(Context context, long timeInMillis) {
        if(timeInMillis < 0) {
            return "";
        }
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(timeInMillis);
        boolean is24Hours = DateFormat.is24HourFormat(context);
        SimpleDateFormat formatter = new SimpleDateFormat(is24Hours ? TIME_FORMAT_24_HOURS : TIME_FORMAT_AM_PM, getCurrentLocale(context));
        return formatter.format(date.getTime());
    }

    @TargetApi(Build.VERSION_CODES.N)
    public static Locale getCurrentLocale(Context context){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            return context.getResources().getConfiguration().getLocales().get(0);
        } else{
            //noinspection deprecation
            return context.getResources().getConfiguration().locale;
        }
    }

    public static long numberOfMinutes(long timeInMillis) {
        return timeInMillis / minuteInMillis;
    }
}
