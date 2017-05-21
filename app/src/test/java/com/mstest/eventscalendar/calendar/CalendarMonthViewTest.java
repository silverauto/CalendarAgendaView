package com.mstest.eventscalendar.calendar;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.mstest.eventscalendar.BuildConfig;
import com.mstest.eventscalendar.R;
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
import org.robolectric.shadows.ShadowDrawable;
import org.robolectric.util.ReflectionHelpers;

import java.util.Calendar;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.shadow.api.Shadow.directlyOn;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, shadows = {})
public class CalendarMonthViewTest {
    private static final int INITIAL_DISPLAYED_ROW_COUNT = CalendarView.COLLAPSE_ROW_COUNT;
    private ActivityController<CalendarMonthViewTest.TestActivity> mActivityController;
    private CalendarMonthView mMonthView;
    private CalendarMonthViewAdapter mAdapter;
    private GridLayoutManager mLayoutManager;

    @Before
    public void setUp() throws Exception {
        mActivityController = Robolectric.buildActivity(CalendarMonthViewTest.TestActivity.class);
        mActivityController.create().start().visible().resume();
        CalendarMonthViewTest.TestActivity activity = mActivityController.get();
        mMonthView = (CalendarMonthView) activity.findViewById(R.id.calendar_view);
        mAdapter = (CalendarMonthViewAdapter) mMonthView.getAdapter();
        mLayoutManager = (GridLayoutManager) mMonthView.getLayoutManager();
    }

    @After
    public void tearDown() throws Exception {
        mActivityController.pause().stop().destroy();
    }

    @Test
    public void testInitialUI() throws Exception {
        // show two weeks
        Assert.assertEquals(7 * INITIAL_DISPLAYED_ROW_COUNT, mMonthView.getChildCount());

        // Initially, today is selected
        Calendar today = CalendarUtils.today();
        CalendarDayViewHolder todayViewHolder = createAndBindViewHolder(mAdapter.indexOfDay(today));
        ShadowDrawable shadowDrawable = shadowOf(todayViewHolder.itemView.getBackground());
        int expectedBackgroundResId = todayViewHolder.getDay().getMonth() % 2 == 0 ? R.color.even_month_day_view_background_color : R.color.old_month_day_view_background_color;
        Assert.assertEquals(expectedBackgroundResId, shadowDrawable.getCreatedFromResId());
        View selectedCircle = todayViewHolder.itemView.findViewById(R.id.selected_day_circle);
        assertTrue(selectedCircle.getVisibility() == View.VISIBLE);
    }

    @Test
    public void testInitialSelectedDate() throws Exception {
        // The first visible row (week) should include the initial selected date (today)
        Calendar today = CalendarUtils.today();
        int dayOfWeek = today.get(Calendar.DAY_OF_WEEK);
        Calendar firstDayOfSelectedWeek = CalendarUtils.getDate(today, -dayOfWeek+1);
        assertTrue(CalendarUtils.isSameDay(firstDayOfSelectedWeek, getCurrentFirstVisibleDay()));
    }

    @Test
    public void testSelectedDate() throws Exception {
        Calendar initialFirstVisibleDay = getCurrentFirstVisibleDay();
        Calendar dayWithinVisibleDayRange = CalendarUtils.getDate(CalendarUtils.today(), 1);
        mMonthView.setSelectedDate(dayWithinVisibleDayRange);
        Calendar newFirstVisibleDay = getCurrentFirstVisibleDay();
        assertTrue(CalendarUtils.isSameDay(initialFirstVisibleDay, newFirstVisibleDay));

        Calendar veryEarlierDay = CalendarUtils.getDate(CalendarUtils.today(), 365);
        mMonthView.setSelectedDate(veryEarlierDay);
        newFirstVisibleDay = getCurrentFirstVisibleDay();
        assertTrue(CalendarUtils.isSameDay(initialFirstVisibleDay, newFirstVisibleDay));

        Calendar firstDay = getFirstDay();
        int dayOfWeek = firstDay.get(Calendar.DAY_OF_WEEK);
        Calendar firstDayOfSelectedWeek = CalendarUtils.getDate(firstDay, -dayOfWeek+1);
        mMonthView.setSelectedDate(firstDay);
        newFirstVisibleDay = getCurrentFirstVisibleDay();
        assertTrue(CalendarUtils.isSameDay(firstDayOfSelectedWeek, newFirstVisibleDay));

        Calendar lastDay = getLastDay();
        dayOfWeek = lastDay.get(Calendar.DAY_OF_WEEK);
        // Since the selected day is in the last week, the first visible week should be the last second week.
        Calendar firstDayOfLastSecondWeek = CalendarUtils.getDate(lastDay, -dayOfWeek * 2 + 1);
        mMonthView.setSelectedDate(lastDay);
        newFirstVisibleDay = getCurrentFirstVisibleDay();
        assertTrue(CalendarUtils.isSameDay(firstDayOfLastSecondWeek, newFirstVisibleDay));
    }

    @Test
    public void testBindViewHolder() {
        Calendar tomorrow = CalendarUtils.getDate(CalendarUtils.today(), 1);
        mMonthView.setSelectedDate(tomorrow);

        Calendar today = CalendarUtils.today();
        CalendarDayViewHolder todayViewHolder = createAndBindViewHolder(mAdapter.indexOfDay(today));
        ShadowDrawable shadowDrawable = shadowOf(todayViewHolder.itemView.getBackground());
        Assert.assertEquals(R.color.today_highlight_color, shadowDrawable.getCreatedFromResId());
        View selectedCircle = todayViewHolder.itemView.findViewById(R.id.selected_day_circle);
        assertTrue(selectedCircle.getVisibility() == View.GONE);

        CalendarDayViewHolder tomorrowViewHolder = createAndBindViewHolder(mAdapter.indexOfDay(tomorrow));
        int expectedBackgroundResId = tomorrowViewHolder.getDay().getMonth() % 2 == 0 ? R.color.even_month_day_view_background_color : R.color.old_month_day_view_background_color;
        shadowDrawable = shadowOf(tomorrowViewHolder.itemView.getBackground());
        Assert.assertEquals(expectedBackgroundResId, shadowDrawable.getCreatedFromResId());
        selectedCircle = tomorrowViewHolder.itemView.findViewById(R.id.selected_day_circle);
        assertTrue(selectedCircle.getVisibility() == View.VISIBLE);
    }

    @Test
    public void setOnDateChangedListener() throws Exception {
        OnDateChangedListener listener = mock(OnDateChangedListener.class);
        mMonthView.setOnDateChangedListener(listener);
        Calendar veryEarlierDay = CalendarUtils.getDate(CalendarUtils.today(), -365);
        mMonthView.setSelectedDate(veryEarlierDay);
        verify(listener, never()).onDateSelected(any(View.class), any(Calendar.class));

        Calendar yesterday = CalendarUtils.getDate(CalendarUtils.today(), -1);
        mMonthView.setSelectedDate(yesterday);
        verify(listener).onDateSelected(mMonthView, yesterday);
    }

    @Test
    public void testOnDayLoadedListener() throws Exception {
        CalendarMonthView.OnDayLoadedListener listener = mock(CalendarMonthView.OnDayLoadedListener.class);
        mMonthView.setOnDayLoadedListener(listener);
        Calendar today = CalendarUtils.today();
        int dayOfWeek = today.get(Calendar.DAY_OF_WEEK);
        Calendar lastDayOfLastWeek = CalendarUtils.getDate(today, -dayOfWeek);
        smoothScrollToPosition(mAdapter.indexOfDay(lastDayOfLastWeek), true);
        Calendar firstDayOfLastWeek = CalendarUtils.getDate(today, -dayOfWeek - 6);
        verify(listener).onDayLoaded(firstDayOfLastWeek);
    }

    private Calendar getFirstDay() {
        return mAdapter.getDay(0).getCalendar();
    }

    private Calendar getLastDay() {
        return mAdapter.getDay(mAdapter.getItemCount()-1).getCalendar();
    }

    private Calendar getCurrentFirstVisibleDay() {
        return mAdapter.getDay(mLayoutManager.findFirstVisibleItemPosition()).getCalendar();
    }

    private void smoothScrollToPosition(int position, boolean isScrollUp) {
        directlyOn(mMonthView, RecyclerView.class, "dispatchOnScrollStateChanged",
                ReflectionHelpers.ClassParameter.from(int.class, RecyclerView.SCROLL_STATE_SETTLING));
        mMonthView.scrollToPosition(position);
        directlyOn(mMonthView, RecyclerView.class, "dispatchOnScrolled",
                ReflectionHelpers.ClassParameter.from(int.class, 0),
                ReflectionHelpers.ClassParameter.from(int.class, isScrollUp ? -1 : 1));
        directlyOn(mMonthView, RecyclerView.class, "dispatchOnScrollStateChanged",
                ReflectionHelpers.ClassParameter.from(int.class, RecyclerView.SCROLL_STATE_IDLE));
    }

    private CalendarDayViewHolder createAndBindViewHolder(int position) {
        CalendarDayViewHolder viewHolder = mAdapter.createViewHolder(mMonthView,
                mAdapter.getItemViewType(position));
        mAdapter.bindViewHolder(viewHolder, position);
        return viewHolder;
    }

    @SuppressLint("Registered")
    static class TestActivity extends AppCompatActivity {
        @Override
        protected void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            CalendarMonthView monthView = new CalendarMonthView(this);
            monthView.setId(R.id.calendar_view);
            int dayCellHeight = getResources().getDimensionPixelOffset(R.dimen.calendar_view_cell_height);
            monthView.setLayoutParams(new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, dayCellHeight * INITIAL_DISPLAYED_ROW_COUNT));
            setContentView(monthView);
        }
    }
}