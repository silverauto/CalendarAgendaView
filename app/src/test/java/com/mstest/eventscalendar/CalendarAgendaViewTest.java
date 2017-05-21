package com.mstest.eventscalendar;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.mstest.eventscalendar.agenda.AgendaAdapter;
import com.mstest.eventscalendar.agenda.AgendaView;
import com.mstest.eventscalendar.calendar.CalendarMonthView;
import com.mstest.eventscalendar.calendar.CalendarMonthViewAdapter;
import com.mstest.eventscalendar.calendar.CalendarView;
import com.mstest.eventscalendar.ui.OnDateChangedListener;
import com.mstest.eventscalendar.utils.CalendarUtils;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

import java.util.Calendar;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, shadows = {})
public class CalendarAgendaViewTest {
    private ActivityController<CalendarAgendaViewTest.TestActivity> mActivityController;
    private CalendarAgendaView mCalendarAgendaView;
    private CalendarView mCalendarView;
    private CalendarMonthView mMonthView;
    private AgendaView mAgendaView;

    @Before
    public void setUp() throws Exception {
        mActivityController = Robolectric.buildActivity(CalendarAgendaViewTest.TestActivity.class);
        mActivityController.create().start().visible().resume();
        TestActivity activity = mActivityController.get();
        mCalendarAgendaView = (CalendarAgendaView) activity.findViewById(R.id.calendar_agenda_view);
        mCalendarView = (CalendarView) mCalendarAgendaView.findViewById(R.id.calendar_view);
        mMonthView = (CalendarMonthView) mCalendarView.findViewById(R.id.month_view);
        mAgendaView = (AgendaView) mCalendarAgendaView.findViewById(R.id.agenda_view);
    }

    @After
    public void tearDown() throws Exception {
        mActivityController.pause().stop().destroy();
    }

    @Test
    public void testSetOrientation() throws Exception {
        mCalendarAgendaView.setOrientation(LinearLayout.HORIZONTAL);
        Assert.assertEquals(mCalendarAgendaView.getOrientation(), LinearLayout.VERTICAL);
    }

    @Test
    public void setOnDateChangedListener() throws Exception {
        OnDateChangedListener listener = mock(OnDateChangedListener.class);
        mCalendarAgendaView.setOnDateChangedListener(listener);
        Calendar today = CalendarUtils.today();
        int dayOfWeek = today.get(Calendar.DAY_OF_WEEK);
        Calendar lastDayOfLastWeek = CalendarUtils.getDate(today, -dayOfWeek);
        mAgendaView.setSelectedDate(lastDayOfLastWeek);
        verify(listener).onDateSelected(mAgendaView, lastDayOfLastWeek);
        CalendarMonthViewAdapter monthAdapter = (CalendarMonthViewAdapter) mMonthView.getAdapter();
        GridLayoutManager monthLayoutManager = (GridLayoutManager) mMonthView.getLayoutManager();
        Calendar firstVisibleDayOfMonthView = monthAdapter.getDay(monthLayoutManager.findFirstVisibleItemPosition()).getCalendar();
        Calendar firstDayOfLastWeek = CalendarUtils.getDate(lastDayOfLastWeek, -6);
        Assert.assertTrue(CalendarUtils.isSameDay(firstDayOfLastWeek, firstVisibleDayOfMonthView));


        Calendar tomorrow = CalendarUtils.getDate(today, 1);
        mCalendarView.setSelectedDate(tomorrow);
        verify(listener).onDateSelected(mMonthView, tomorrow);
        AgendaAdapter agendaAdapter = (AgendaAdapter) mAgendaView.getAdapter();
        LinearLayoutManager agendaLayoutManager = (LinearLayoutManager) mAgendaView.getLayoutManager();
        Calendar firstVisibleDayOfAgendaView = agendaAdapter.getItem(agendaLayoutManager.findFirstVisibleItemPosition()).getCalendar();
        Assert.assertTrue(CalendarUtils.isSameDay(tomorrow, firstVisibleDayOfAgendaView));
    }

    @SuppressLint("Registered")
    static class TestActivity extends AppCompatActivity {
        @Override
        protected void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            CalendarAgendaView calendarAgendaView = new CalendarAgendaView(this);
            calendarAgendaView.setId(R.id.calendar_agenda_view);
            calendarAgendaView.setLayoutParams(new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            setContentView(calendarAgendaView);
        }
    }
}