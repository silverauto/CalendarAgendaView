package com.mstest.eventscalendar.content;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.CalendarContract;

import com.mstest.eventscalendar.model.Event;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Asynchronously query the calendar events
 */

public class CalendarEventLoader extends AsyncQueryHandler {
    private static final int PROJECTION_INDEX_TITLE = 1;
    private static final int PROJECTION_INDEX_DTSTART = 2;
    private static final int PROJECTION_INDEX_DTEND = 3;
    private static final int PROJECTION_INDEX_ALLDAY = 4;
    public final static String[] PROJECTION = new String[]{
            CalendarContract.Events._ID,
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DTSTART,
            CalendarContract.Events.DTEND,
            CalendarContract.Events.ALL_DAY
    };

    private final static String ORDER_BY = CalendarContract.Events.DTSTART + " ASC";

    private final static String START_DATE_WITHIN_RANGE = "(" +
            CalendarContract.Events.DTSTART + ">=? AND " +
            CalendarContract.Events.DTSTART + "<?)";
    private final static String SELECTION = START_DATE_WITHIN_RANGE;

    private EventQueryObserver mEventQueryObserver;

    public interface EventQueryObserver {
        void onQueryCompleted(Calendar startDate, List<Event> events);
    }

    public CalendarEventLoader(ContentResolver contentResolver) {
        super(contentResolver);
    }

    public void queryEvents(long startTime, long endTime) {
        String[] selectionArgs = {String.valueOf(startTime), String.valueOf(endTime)};
        startQuery(0, startTime, CalendarContract.Events.CONTENT_URI,
                PROJECTION, SELECTION, selectionArgs, ORDER_BY);
    }

    public void setEventQueryObserver(EventQueryObserver observer) {
        mEventQueryObserver = observer;
    }

    @Override
    protected final void onQueryComplete(int token, Object cookie, Cursor cursor) {
        if(mEventQueryObserver != null && cursor != null) {
            Calendar startDate = Calendar.getInstance();
            startDate.setTimeInMillis(((Long)cookie).longValue());
            mEventQueryObserver.onQueryCompleted(startDate, buildEvents(cursor));
        }
    }

    private List<Event> buildEvents(Cursor cursor) {
        List<Event> events = new ArrayList<>();
        if(cursor == null) {
            return events;
        }
        while(cursor.moveToNext()) {
            events.add(new Event(cursor.getLong(PROJECTION_INDEX_DTSTART),
                    cursor.getLong(PROJECTION_INDEX_DTEND),
                    cursor.getString(PROJECTION_INDEX_TITLE),
                    cursor.getInt(PROJECTION_INDEX_ALLDAY) == 1));
        }
        cursor.close();
        return events;
    }
}
