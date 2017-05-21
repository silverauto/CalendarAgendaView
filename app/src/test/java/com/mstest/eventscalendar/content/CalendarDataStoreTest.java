package com.mstest.eventscalendar.content;

import android.support.v7.widget.RecyclerView;

import com.mstest.eventscalendar.BuildConfig;
import com.mstest.eventscalendar.model.CalendarData;
import com.mstest.eventscalendar.model.Event;
import com.mstest.eventscalendar.utils.CalendarUtils;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.Calendar;

import static com.mstest.eventscalendar.content.CalendarDataStore.MAX_CACHED_WEEK_DATA_SIZE;
import static com.mstest.eventscalendar.content.CalendarDataStore.PREFETCH_DATA_SIZE;
import static com.mstest.eventscalendar.content.ShadowCalendarEventLoader.MOCK_EVENT_TITLE1;
import static com.mstest.eventscalendar.content.ShadowCalendarEventLoader.MOCK_EVENT_TITLE2;
import static org.junit.Assert.assertTrue;


/**
 * ShadowCalendarEventLoader return a mock cursor with two events (MOCK_EVENT_TITLE1 and MOCK_EVENT_TITLE2)
 * for each day query
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, shadows = {ShadowCalendarEventLoader.class})
public class CalendarDataStoreTest {

    private CalendarDataStore mDataStore;
    // Initially, the data store would load nine weeks of data (this week, 4 weeks before and 4 weeks later)
    private int initialDataCount = CalendarDataStore.PREFETCH_DATA_SIZE * 2 + 7;
    private final Calendar today = CalendarUtils.today();

    @Before
    public void setUp() throws Exception {
        mDataStore = new CalendarDataStore(RuntimeEnvironment.application.getContentResolver());
    }

    @Test
    public void testInitialData() throws Exception  {
        assertTrue(mDataStore.getDayCount() == initialDataCount);
    }

    @Test
    public void testDataChangeObserver() throws Exception {

    }

    @Test
    public void testGetData() throws Exception {
        CalendarData firstData = mDataStore.getData(0);
        int dayOfWeek = today.get(Calendar.DAY_OF_WEEK);
        Calendar firstDayOfThisWeek = CalendarUtils.getDate(today, -dayOfWeek + 1);
        Calendar firstDay = CalendarUtils.getDate(firstDayOfThisWeek, -PREFETCH_DATA_SIZE);
        assertTrue(CalendarUtils.isSameDay(firstData.getCalendar(), firstDay));
        CalendarData secondData = mDataStore.getData(1);
        assertTrue(firstData.equals(secondData));
        Event event1 = secondData.getEventInDay(0);
        assertTrue(event1.getTitle().equals(MOCK_EVENT_TITLE1));
        Event event2 = secondData.getEventInDay(1);
        assertTrue(event2.getTitle().equals(MOCK_EVENT_TITLE2));

        assertTrue(mDataStore.getData(-1) == null);
        assertTrue(mDataStore.getData(mDataStore.getTotalDataCount()) == null);
    }

    @Test
    public void testGetEvent() throws Exception {
        assertTrue(mDataStore.getEvent(0) == null);
        assertTrue(mDataStore.getEvent(1).getTitle().equals(MOCK_EVENT_TITLE1));
        assertTrue(mDataStore.getEvent(2).getTitle().equals(MOCK_EVENT_TITLE2));
        assertTrue(mDataStore.getEvent(-1) == null);
        assertTrue(mDataStore.getEvent(mDataStore.getTotalDataCount()) == null);
    }

    @Test
    public void testIsHeaderData() throws Exception {
        assertTrue(!mDataStore.isHeaderData(-1));
        assertTrue(mDataStore.isHeaderData(0));
        assertTrue(!mDataStore.isHeaderData(1)); // MOCK_EVENT_TITLE1
        assertTrue(!mDataStore.isHeaderData(2)); // MOCK_EVENT_TITLE2
        assertTrue(mDataStore.isHeaderData(mDataStore.getTotalDataCount()-3));
        assertTrue(!mDataStore.isHeaderData(mDataStore.getTotalDataCount()-2));
        assertTrue(!mDataStore.isHeaderData(mDataStore.getTotalDataCount()-1));
        assertTrue(!mDataStore.isHeaderData(mDataStore.getTotalDataCount()));
    }

    @Test
    public void testTotalDataCount() throws Exception {
        assertTrue(mDataStore.getTotalDataCount() == initialDataCount * 3); // header and two events for each day
    }

    @Test
    public void testIndexOfHeaderForDate() throws Exception {
        int expectedFirstDayOfThisWeekHeaderIndex = CalendarDataStore.PREFETCH_DATA_SIZE * 3;
        Assert.assertEquals(expectedFirstDayOfThisWeekHeaderIndex, mDataStore.indexOfHeaderForDate(firstDayOfThisWeek()));
        Assert.assertEquals(0, mDataStore.indexOfHeaderForDate(firstDay()));
        int expectedLastDayHeaderIndex = mDataStore.getDayCount() * 3 - 3;
        Assert.assertEquals(expectedLastDayHeaderIndex, mDataStore.indexOfHeaderForDate(lastDay()));
        Calendar veryEarlierDate = CalendarUtils.getDate(today, -mDataStore.getDayCount());
        Assert.assertEquals(RecyclerView.NO_POSITION, mDataStore.indexOfHeaderForDate(veryEarlierDate));
        Calendar veryLaterDate = CalendarUtils.getDate(today, mDataStore.getDayCount());
        Assert.assertEquals(RecyclerView.NO_POSITION, mDataStore.indexOfHeaderForDate(veryLaterDate));
    }

    @Test
    public void testOutOfDateRange() throws Exception {
        Calendar today = CalendarUtils.today();
        assertTrue(!mDataStore.isOutOfDateRange(today));
        assertTrue(!mDataStore.isOutOfDateRange(firstDay()));
        assertTrue(!mDataStore.isOutOfDateRange(lastDay()));
        Calendar veryEarlierDate = CalendarUtils.getDate(today, -mDataStore.getDayCount());
        assertTrue(mDataStore.isOutOfDateRange(veryEarlierDate));
        Calendar veryLaterDate = CalendarUtils.getDate(today, mDataStore.getDayCount());
        assertTrue(mDataStore.isOutOfDateRange(veryLaterDate));

    }

    @Test
    public void testPrefetchEarlierData() throws Exception {
        mDataStore.prefetchEarlierData();
        Assert.assertEquals(initialDataCount + CalendarDataStore.PREFETCH_DATA_SIZE, mDataStore.getDayCount());
    }

    @Test
    public void testPrefetchLaterData() throws Exception {
        mDataStore.prefetchLaterData();
        Assert.assertEquals(initialDataCount + CalendarDataStore.PREFETCH_DATA_SIZE, mDataStore.getDayCount());
    }

    @Test
    public void testFetchMaxCachedData() throws Exception {
        for(int i = 0; i< MAX_CACHED_WEEK_DATA_SIZE; i++) {
            mDataStore.prefetchEarlierData();
        }
        for(int i = 0; i< MAX_CACHED_WEEK_DATA_SIZE; i++) {
            mDataStore.prefetchLaterData();
        }
        Assert.assertEquals(MAX_CACHED_WEEK_DATA_SIZE, mDataStore.getDayCount() / 7);
    }

    private Calendar firstDayOfThisWeek() {
        int dayOfWeek = today.get(Calendar.DAY_OF_WEEK);
        return CalendarUtils.getDate(today, -dayOfWeek + 1);
    }

    private Calendar firstDay() {
        Calendar firstDayOfThisWeek = firstDayOfThisWeek();
        return CalendarUtils.getDate(firstDayOfThisWeek, -PREFETCH_DATA_SIZE);
    }

    private Calendar lastDayOfThisWeek() {
        int dayOfWeek = today.get(Calendar.DAY_OF_WEEK);
        return CalendarUtils.getDate(today, 7 - dayOfWeek);
    }

    private Calendar lastDay() {
        Calendar lastDayOfThisWeek = lastDayOfThisWeek();
        return CalendarUtils.getDate(lastDayOfThisWeek, PREFETCH_DATA_SIZE);
    }
}