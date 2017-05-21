package com.mstest.eventscalendar.agenda;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.mstest.eventscalendar.BuildConfig;
import com.mstest.eventscalendar.R;
import com.mstest.eventscalendar.content.CalendarDataStore;
import com.mstest.eventscalendar.model.CalendarData;
import com.mstest.eventscalendar.model.Event;
import com.mstest.eventscalendar.ui.OnDateChangedListener;
import com.mstest.eventscalendar.utils.CalendarUtils;
import com.mstest.eventscalendar.utils.TimeUtils;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.util.ReflectionHelpers;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.mstest.eventscalendar.utils.CalendarUtils.today;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.robolectric.shadow.api.Shadow.directlyOn;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, shadows = {})
public class AgendaViewTest {
    private ActivityController<TestActivity> mActivityController;
    private AgendaViewTest.TestActivity mActivity;
    private AgendaView mAgendaView;
    private AgendaAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;

    @Before
    public void setUp() throws Exception {
        mActivityController = Robolectric.buildActivity(TestActivity.class);
        mActivityController.create().start().visible().resume();
        mActivity = mActivityController.get();
        mAgendaView = (AgendaView) mActivity.findViewById(R.id.agenda_view);
        mAdapter = (AgendaAdapter) mAgendaView.getAdapter();
        mLayoutManager = (LinearLayoutManager) mAgendaView.getLayoutManager();
    }

    @After
    public void tearDown() {
        mActivityController.pause().stop().destroy();
    }

    @Test
    public void testInitialSelectedDate() throws Exception {
        // The selected date is the first visible date
        CalendarData todayData = mAdapter.getItem(mLayoutManager.findFirstVisibleItemPosition());
        assertTrue(CalendarUtils.isSameDay(todayData.getCalendar(), today()));
    }

    @Test
    public void testSelectedDate() throws Exception {
        Calendar oldFirstVisibleDay = getCurrentFirstVisibleDay();
        Calendar veryEarlierDay = CalendarUtils.getDate(CalendarUtils.today(), -365);
        mAgendaView.setSelectedDate(veryEarlierDay);
        Calendar newFirstVisibleDay = getCurrentFirstVisibleDay();
        assertTrue(CalendarUtils.isSameDay(oldFirstVisibleDay, newFirstVisibleDay));

        Calendar selectedDay = mAdapter.getItem(AgendaView.PREFETCH_BOUNDARY).getCalendar();
        mAgendaView.setSelectedDate(selectedDay);
        newFirstVisibleDay = getCurrentFirstVisibleDay();
        assertTrue(CalendarUtils.isSameDay(selectedDay, newFirstVisibleDay));
    }

    @Test
    public void testBindViewHolder() {
        List<CalendarData> dataSet = new ArrayList<>();
        Calendar today = CalendarUtils.today();
        CalendarData todayData = CalendarData.from(today);
        dataSet.add(todayData);
        Calendar tomorrow = CalendarUtils.getDate(today, 1);
        long tomorrowInMillis = tomorrow.getTimeInMillis();
        CalendarData tomorrowData = CalendarData.from(tomorrow);
        // one hour event interval
        Event event = new Event(tomorrowInMillis, tomorrowInMillis + 3600000, "Tomorrow", false);
        tomorrowData.addEvent(event);
        dataSet.add(tomorrowData);
        setMockDataSet(dataSet);
        mAdapter.notifyDataSetChanged();
        mAgendaView.setSelectedDate(today);

        int firstVisiblePosition = mLayoutManager.findFirstVisibleItemPosition();
        AgendaViewHolder todayHeaderViewHolder = createAndBindViewHolder(firstVisiblePosition);
        assertTrue(todayHeaderViewHolder instanceof AgendaHeaderViewHolder);
        Assert.assertEquals(TimeUtils.longDateString(RuntimeEnvironment.application, today.getTime()), ((TextView) todayHeaderViewHolder.itemView).getText());
        AgendaViewHolder todayEventViewHolder = createAndBindViewHolder(firstVisiblePosition + 1);
        assertTrue(todayEventViewHolder instanceof AgendaNoEventViewHolder);
        String noEventStr = mActivity.getResources().getString(R.string.no_event);
        Assert.assertEquals(noEventStr, ((TextView)todayEventViewHolder.itemView).getText());

        AgendaViewHolder tomorrowHeaderViewHolder = createAndBindViewHolder(firstVisiblePosition + 2);
        assertTrue(tomorrowHeaderViewHolder instanceof AgendaHeaderViewHolder);
        Assert.assertEquals(TimeUtils.longDateString(RuntimeEnvironment.application, today.getTime()), ((TextView) todayHeaderViewHolder.itemView).getText());
        AgendaViewHolder tomorrowEventViewHolder = createAndBindViewHolder(firstVisiblePosition + 3);
        assertTrue(tomorrowEventViewHolder instanceof AgendaEventViewHolder);
        TextView eventTitleTextView = (TextView) tomorrowEventViewHolder.itemView.findViewById(R.id.eventTitle);
        Assert.assertEquals(event.getTitle(), eventTitleTextView.getText());
        String eventTimeStr = TimeUtils.timeString(mActivity, event.getStartTime());
        TextView eventTimeTextView = (TextView) tomorrowEventViewHolder.itemView.findViewById(R.id.eventTime);
        Assert.assertEquals(eventTimeStr, eventTimeTextView.getText());
        String eventDurationStr = String.format(mActivity.getString(R.string.duration_hours_format), 1);
        TextView eventDurationTextView = (TextView) tomorrowEventViewHolder.itemView.findViewById(R.id.eventDuration);
        Assert.assertEquals(eventDurationStr, eventDurationTextView.getText());
    }

    @Test
    public void testCheckIfLoadMoreData() throws Exception {
        Calendar firstDay = getCurrentFirstDay();
        Calendar lastDay = getCurrentLastDay();
        Calendar today = CalendarUtils.today();
        mAgendaView.checkIfLoadMoreData(today);
        Calendar newFirstDay = getCurrentFirstDay();
        Calendar newLastDay = getCurrentLastDay();
        assertTrue(CalendarUtils.isSameDay(firstDay, newFirstDay));
        assertTrue(CalendarUtils.isSameDay(lastDay, newLastDay));

        firstDay = getCurrentFirstDay();
        mAgendaView.checkIfLoadMoreData(CalendarUtils.getDate(firstDay, -1));
        newFirstDay = getCurrentFirstDay();
        Calendar expectedFirstDay = CalendarUtils.getDate(firstDay, -CalendarDataStore.PREFETCH_DATA_SIZE);
        assertTrue(CalendarUtils.isSameDay(expectedFirstDay, newFirstDay));

        lastDay = getCurrentLastDay();
        mAgendaView.checkIfLoadMoreData(CalendarUtils.getDate(lastDay, 1));
        Calendar expectedLastDay = CalendarUtils.getDate(lastDay, CalendarDataStore.PREFETCH_DATA_SIZE);
        newLastDay = getCurrentLastDay();
        assertTrue(CalendarUtils.isSameDay(expectedLastDay, newLastDay));
    }

    @Test
    public void testPrefetchData() {
        smoothScrollToPosition(mLayoutManager.findFirstVisibleItemPosition());
        Calendar firstDay = getCurrentFirstDay();
        smoothScrollToPosition(AgendaView.PREFETCH_BOUNDARY-1);
        Calendar newFirstDay = getCurrentFirstDay();
        Calendar expectedFirstDay = CalendarUtils.getDate(firstDay, -CalendarDataStore.PREFETCH_DATA_SIZE);
        assertTrue(CalendarUtils.isSameDay(expectedFirstDay, newFirstDay));

        Calendar lastDay = getCurrentLastDay();
        smoothScrollToPosition(mAdapter.getItemCount() - AgendaView.PREFETCH_BOUNDARY + 1);
        Calendar newLastDay = getCurrentLastDay();
        Calendar expectedLastDay = CalendarUtils.getDate(lastDay, CalendarDataStore.PREFETCH_DATA_SIZE);
        assertTrue(CalendarUtils.isSameDay(expectedLastDay, newLastDay));
    }

    @Test
    public void setOnDateChangedListener() throws Exception {
        OnDateChangedListener listener = mock(OnDateChangedListener.class);
        mAgendaView.setOnDateChangedListener(listener);
        Calendar yesterday = CalendarUtils.getDate(CalendarUtils.today(), -1);
        mAgendaView.setSelectedDate(yesterday);
        verify(listener).onDateSelected(mAgendaView, yesterday);
    }

    private Calendar getCurrentFirstDay() {
        return mAdapter.getItem(0).getCalendar();
    }

    private Calendar getCurrentLastDay() {
        return mAdapter.getItem(mAdapter.getItemCount()-1).getCalendar();
    }

    private Calendar getCurrentFirstVisibleDay() {
        return mAdapter.getItem(mLayoutManager.findFirstVisibleItemPosition()).getCalendar();
    }

    private void smoothScrollToPosition(int position) {
        directlyOn(mAgendaView, RecyclerView.class, "dispatchOnScrollStateChanged",
                ReflectionHelpers.ClassParameter.from(int.class, RecyclerView.SCROLL_STATE_SETTLING));
        boolean isScrollUp = position < mLayoutManager.findFirstVisibleItemPosition();
        mAgendaView.scrollToPosition(position);
        directlyOn(mAgendaView, RecyclerView.class, "dispatchOnScrolled",
                ReflectionHelpers.ClassParameter.from(int.class, 0),
                ReflectionHelpers.ClassParameter.from(int.class, isScrollUp ? -1 : 1));
        directlyOn(mAgendaView, RecyclerView.class, "dispatchOnScrollStateChanged",
                ReflectionHelpers.ClassParameter.from(int.class, RecyclerView.SCROLL_STATE_IDLE));
    }

    private AgendaViewHolder createAndBindViewHolder(int position) {
        AgendaViewHolder viewHolder = mAdapter.createViewHolder(mAgendaView,
                mAdapter.getItemViewType(position));
        mAdapter.bindViewHolder(viewHolder, position);
        return viewHolder;
    }

    private void setMockDataSet(List<CalendarData> dataSet) {
        CalendarDataStore dataStore = new CalendarDataStore(RuntimeEnvironment.application.getContentResolver());
        mAdapter.setDataStore(dataStore);
        try {
            Field dataSetField = dataStore.getClass().getDeclaredField("mDataSet");
            dataSetField.setAccessible(true);
            dataSetField.set(dataStore, dataSet);

            Field totalDataCountField = dataStore.getClass().getDeclaredField("mTotalDataCount");
            totalDataCountField.setAccessible(true);
            int totalCount = 0;
            for(CalendarData data : dataSet) {
                totalCount += data.numberOfEvents() + 1;
            }
            totalDataCountField.set(dataStore, totalCount);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("Registered")
    static class TestActivity extends AppCompatActivity {
        @Override
        protected void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            AgendaView agendaView = new AgendaView(this);
            agendaView.setId(R.id.agenda_view);
            agendaView.setLayoutParams(new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            setContentView(agendaView);
        }
    }
}