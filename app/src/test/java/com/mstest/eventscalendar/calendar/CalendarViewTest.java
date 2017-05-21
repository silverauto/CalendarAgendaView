package com.mstest.eventscalendar.calendar;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.mstest.eventscalendar.BuildConfig;
import com.mstest.eventscalendar.R;
import com.mstest.eventscalendar.ui.OnDateChangedListener;
import com.mstest.eventscalendar.utils.AnimationUtils;
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
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, shadows = {})
public class CalendarViewTest {
    private ActivityController<CalendarViewTest.TestActivity> mActivityController;
    private CalendarViewTest.TestActivity mActivity;
    private CalendarView mCalendarView;


    @Before
    public void setUp() throws Exception {
        mActivityController = Robolectric.buildActivity(CalendarViewTest.TestActivity.class);
        mActivityController.create().start().visible().resume();
        mActivity = mActivityController.get();
        mCalendarView = (CalendarView) mActivity.findViewById(R.id.calendar_view);
    }

    @After
    public void tearDown() throws Exception {
        mActivityController.pause().stop().destroy();
    }

    @Test
    public void setOnDateChangedListener() throws Exception {
        OnDateChangedListener listener = mock(OnDateChangedListener.class);
        mCalendarView.setOnDateChangedListener(listener);
        Calendar yesterday = CalendarUtils.getDate(CalendarUtils.today(), -1);
        mCalendarView.setSelectedDate(yesterday);
        verify(listener).onDateSelected(mCalendarView.findViewById(R.id.month_view), yesterday);
    }

    @Test
    public void testExpandCalendarView() throws Exception {
        View monthView = mCalendarView.getChildAt(1);
        int dayCellHeight = mActivity.getResources().getDimensionPixelOffset(R.dimen.calendar_view_cell_height);
        int initialMonthViewHeight = monthView.getHeight();
        Robolectric.getForegroundThreadScheduler().pause();
        mCalendarView.expandCalendarView(true);
        Robolectric.getForegroundThreadScheduler().advanceBy(AnimationUtils.DEFAULT_DURATION, TimeUnit.MILLISECONDS);
        Robolectric.getForegroundThreadScheduler().unPause();
        int expectedExpandedHeight = dayCellHeight * CalendarView.EXPAND_ROW_COUNT;
        Assert.assertEquals(expectedExpandedHeight, monthView.getHeight());

        // Expand the calendar view when the view has been expanded
        Robolectric.getForegroundThreadScheduler().pause();
        mCalendarView.expandCalendarView(true);
        Robolectric.getForegroundThreadScheduler().advanceBy(AnimationUtils.DEFAULT_DURATION, TimeUnit.MILLISECONDS);
        Robolectric.getForegroundThreadScheduler().unPause();
        Assert.assertEquals(expectedExpandedHeight, monthView.getHeight());

        // Collapse the calendar view
        Robolectric.getForegroundThreadScheduler().pause();
        mCalendarView.expandCalendarView(false);
        Robolectric.getForegroundThreadScheduler().advanceBy(AnimationUtils.DEFAULT_DURATION, TimeUnit.MILLISECONDS);
        Robolectric.getForegroundThreadScheduler().unPause();
        Assert.assertEquals(initialMonthViewHeight, monthView.getHeight());
    }

    @SuppressLint("Registered")
    static class TestActivity extends AppCompatActivity {
        @Override
        protected void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            CalendarView calendarView = new CalendarView(this);
            calendarView.setId(R.id.calendar_view);
            calendarView.setLayoutParams(new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            setContentView(calendarView);
        }
    }
}