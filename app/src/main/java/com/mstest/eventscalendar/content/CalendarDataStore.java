package com.mstest.eventscalendar.content;

import android.content.ContentResolver;
import android.support.annotation.IntDef;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;

import com.mstest.eventscalendar.model.CalendarData;
import com.mstest.eventscalendar.model.Event;
import com.mstest.eventscalendar.utils.CalendarUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


/**
 * This class is used to manage the calendar data.
 * (The days and events on the calendar view and agenda view)
 *
 */

public class CalendarDataStore {
    public final static int PREFETCH_DATA_SIZE = 28;  // 4 weeks
    public final static int MAX_CACHED_WEEK_DATA_SIZE = 57;

    // All of days in the calendar
    private List<CalendarData> mDataSet = new ArrayList<>();
    // The actual number of rows displayed in the AgendaView
    private int mTotalDataCount;
    private DataChangeObserver mDataChangeObserver;
    private CalendarEventLoader mCalendarEventLoader;

    public interface DataChangeObserver {
        void onChanged(@DataChangeType int changeType, int start, int count);
    }

    public static final int DATA_INSERT = 1;
    public static final int DATA_REMOVE = 2;
    public static final int DATA_UPDATE = 3;
    @IntDef({DATA_INSERT, DATA_REMOVE, DATA_UPDATE})
    public @interface DataChangeType {
    }

    private CalendarEventLoader.EventQueryObserver mEventQueryObserver = new CalendarEventLoader.EventQueryObserver() {
        @Override
        public void onQueryCompleted(Calendar startDate, List<Event> events) {
            handleEventQueryCompleted(startDate, events);
        }
    };

    public CalendarDataStore(ContentResolver contentResolver) {
        mCalendarEventLoader = new CalendarEventLoader(contentResolver);
        mCalendarEventLoader.setEventQueryObserver(mEventQueryObserver);
        initializeData();
    }

    private void initializeData() {
        Calendar dayCounter = CalendarUtils.copyFrom(CalendarUtils.today());
        int dayOfWeek = dayCounter.get(Calendar.DAY_OF_WEEK);
        // add this week
        dayCounter.add(Calendar.DATE, -dayOfWeek);
        fetchLaterData(dayCounter, 7);
        // append later weeks
        dayCounter.add(Calendar.DATE, 7);
        fetchLaterData(dayCounter, PREFETCH_DATA_SIZE);

        dayCounter = CalendarUtils.copyFrom(CalendarUtils.today());
        // prepend earlier weeks
        dayCounter.add(Calendar.DATE, -dayOfWeek + 1);
        fetchEarlierData(dayCounter, PREFETCH_DATA_SIZE);
    }

    public void setDataChangeObserver(DataChangeObserver observer) {
        mDataChangeObserver = observer;
    }

    public CalendarData getData(int position) {
        if (position < 0 || position >= getTotalDataCount()) {
            return null;
        }

        int index = 0;
        for(CalendarData calendarData : mDataSet) {
            if(position >= index && position <= index + calendarData.numberOfEvents()) {
                // header, no event, and events occurred in the same day share the same CalendarData
                return calendarData;
            }
            index += calendarData.numberOfEvents() + 1;
        }
        return null;
    }

    public Event getEvent(int position) {
        if (position < 0 || position >= getTotalDataCount() || isHeaderData(position)) {
            return null;
        }
        int index = 0;
        for(CalendarData calendarData : mDataSet) {
            if(position < index || position > index + calendarData.numberOfEvents()) {
                index += calendarData.numberOfEvents() + 1;
            } else if(calendarData.hasNoEvent()) {
                // for no event item
                return null;
            } else {
                return calendarData.getEventInDay(position - index - 1);
            }
        }
        return null;
    }

    public boolean isHeaderData(int position) {
        int index = 0;
        for(CalendarData calendarData : mDataSet) {
            if(position == index) {
                return true;
            }
            index += calendarData.numberOfEvents() + 1;
        }
        return false;
    }

    public int getTotalDataCount() {
        return mTotalDataCount;
    }

    public int getDayCount() {
        return mDataSet.size();
    }

    public int indexOfHeaderForDate(Calendar date) {
        if(isOutOfDateRange(date)) {
            return RecyclerView.NO_POSITION;
        }
        int index = 0;
        for(CalendarData calendarData : mDataSet) {
            if(CalendarUtils.isSameDay(date, calendarData.getCalendar())) {
                return index;
            }
            index += calendarData.numberOfEvents() + 1;
        }

        return CalendarUtils.daysBetween(mDataSet.get(0).getCalendar(), date);
    }

    public boolean isOutOfDateRange(Calendar date) {
        int dataCount = mDataSet.size();
        return dataCount == 0 || date.before(mDataSet.get(0).getCalendar()) || date.after(mDataSet.get(dataCount-1).getCalendar());
    }

    public void prefetchEarlierData() {
        int dataCount = mDataSet.size();
        if(dataCount == 0) {
            return;
        }
        Calendar currentFirstDay = mDataSet.get(0).getCalendar();
        Calendar firstDayOfThisWeek = CalendarUtils.today();
        int dayOfWeek = firstDayOfThisWeek.get(Calendar.DAY_OF_WEEK);
        firstDayOfThisWeek.add(Calendar.DATE, -dayOfWeek + 1);
        int numberOfWeeksBeforeThisWeek = (CalendarUtils.daysBetween(currentFirstDay, firstDayOfThisWeek) + 1) / 7;
        if(numberOfWeeksBeforeThisWeek + PREFETCH_DATA_SIZE/7 > MAX_CACHED_WEEK_DATA_SIZE / 2) {
            return;
        }
        Calendar dayCounter = CalendarUtils.copyFrom(currentFirstDay);
        fetchEarlierData(dayCounter, PREFETCH_DATA_SIZE);
    }

    private void fetchEarlierData(Calendar date, int maxPrefetchSize) {
        Calendar prefetchDateCounter = CalendarUtils.copyFrom(date);
        int prefetchSize = Math.min(PREFETCH_DATA_SIZE, maxPrefetchSize);
        for(int i=1;i<=prefetchSize;i++) {
            CalendarData data = CalendarData.from(CalendarUtils.getDate(prefetchDateCounter, -i));
            mDataSet.add(0, data);
            // add data for header and no event item
            mTotalDataCount += 2;
            long startTimeInDay = data.getCalendar().getTimeInMillis();
            // query the data according the specified date
            mCalendarEventLoader.queryEvents(startTimeInDay, startTimeInDay + DateUtils.DAY_IN_MILLIS);
        }
        notifyDataChanged(DATA_INSERT, 0, prefetchSize * 2);
    }

    public void prefetchLaterData() {
        int dataCount = mDataSet.size();
        if(dataCount == 0) {
            return;
        }
        Calendar currentLastDay = mDataSet.get(dataCount-1).getCalendar();
        Calendar lastDayOfThisWeek = CalendarUtils.today();
        int dayOfWeek = lastDayOfThisWeek.get(Calendar.DAY_OF_WEEK);
        lastDayOfThisWeek.add(Calendar.DATE, 7 - dayOfWeek);
        int numberOfWeeksAfterThisWeek = (CalendarUtils.daysBetween(lastDayOfThisWeek, currentLastDay) + 1) / 7;
        if(numberOfWeeksAfterThisWeek + PREFETCH_DATA_SIZE/7 > MAX_CACHED_WEEK_DATA_SIZE / 2) {
            return;
        }
        Calendar dayCounter = CalendarUtils.copyFrom(currentLastDay);
        fetchLaterData(dayCounter, PREFETCH_DATA_SIZE);
    }

    private void fetchLaterData(Calendar date, int maxPrefetchSize) {
        int dataCountBeforeInserted = mTotalDataCount;
        Calendar prefetchDateCounter = CalendarUtils.copyFrom(date);
        int prefetchSize = Math.min(PREFETCH_DATA_SIZE, maxPrefetchSize);
        for(int i=1;i<=prefetchSize;i++) {
            CalendarData data = CalendarData.from(CalendarUtils.getDate(prefetchDateCounter, i));
            mDataSet.add(mDataSet.size(), data);
            mTotalDataCount += 2;
            long startTimeInDay = data.getCalendar().getTimeInMillis();
            mCalendarEventLoader.queryEvents(startTimeInDay, startTimeInDay + DateUtils.DAY_IN_MILLIS);
        }
        notifyDataChanged(DATA_INSERT, dataCountBeforeInserted, mTotalDataCount - dataCountBeforeInserted);
    }

    private void handleEventQueryCompleted(Calendar startDate, List<Event> events) {
        int start = 0;
        int end = mDataSet.size()-1;
        int mid;
        CalendarData data;
        // Do the binary search to find the corresponding day on which these events occur
        while(start <= end) {
            mid = (start+end)/2;
            data = mDataSet.get(mid);
            if(CalendarUtils.isSameDay(data.getCalendar(), startDate)) {
                int oldEventCount = data.numberOfEvents();
                int newEventCount = events.size() == 0 ? 1 : events.size();
                int updateCount = Math.min(oldEventCount, newEventCount);
                int indexOfData = indexOfHeaderForDate(data.getCalendar()); // header position
                mTotalDataCount += newEventCount - oldEventCount;
                data.updateEvents(events);
                notifyDataChanged(DATA_UPDATE, indexOfData + 1, updateCount);
                if(oldEventCount < newEventCount) {
                    notifyDataChanged(DATA_INSERT, indexOfData + 1 + updateCount, newEventCount - oldEventCount);
                } else {
                    notifyDataChanged(DATA_REMOVE, indexOfData + 1 + updateCount, oldEventCount - newEventCount);
                }
                return;
            } else if(startDate.before(data.getCalendar())) {
                end = mid - 1;
            } else {
                start = mid + 1;
            }
        }
    }

    private void notifyDataChanged(@DataChangeType int changeType, int start, int count) {
        if(mDataChangeObserver != null) {
            mDataChangeObserver.onChanged(changeType, start, count);
        }
    }
}
