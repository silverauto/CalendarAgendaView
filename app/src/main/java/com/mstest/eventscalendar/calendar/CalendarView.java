package com.mstest.eventscalendar.calendar;

import android.content.Context;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mstest.eventscalendar.R;
import com.mstest.eventscalendar.ui.OnDateChangedListener;
import com.mstest.eventscalendar.ui.OnUserTouchScrollListener;
import com.mstest.eventscalendar.utils.AnimationUtils;
import com.mstest.eventscalendar.utils.CalendarUtils;
import com.mstest.eventscalendar.utils.TimeUtils;

import java.util.Calendar;

public class CalendarView extends LinearLayout {
    public final static int EXPAND_ROW_COUNT = 5;
    public final static int COLLAPSE_ROW_COUNT = 2;

    private CalendarMonthView mMonthView;
    private int mCellHeight;
    private boolean mViewExpanded;

    public CalendarView(Context context) {
        super(context);
        init(context);
    }

    public CalendarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CalendarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void init(Context context) {
        setOrientation(VERTICAL);
        mCellHeight = getResources().getDimensionPixelOffset(R.dimen.calendar_view_cell_height);
        initHeader(context);
        initMonthView(context);
    }

    public void initHeader(Context context) {
        LinearLayout headerLayout = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.calendar_header_view, this, false);
        Calendar today = CalendarUtils.today(TimeUtils.getCurrentLocale(context));
        TextView headerTextView;
        for(int i=0;i<headerLayout.getChildCount();i++) {
            headerTextView = (TextView) headerLayout.getChildAt(i);
            headerTextView.setText(TimeUtils.weekDayString(context, CalendarUtils.dayOfWeek(today, i).getTime()));
        }
        addView(headerLayout);
    }

    public void initMonthView(Context context) {
        mMonthView = new CalendarMonthView(context);
        mMonthView.setId(R.id.month_view);
        addView(mMonthView, new LinearLayoutCompat.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mCellHeight * 2));
        expandCalendarView(false);
    }

    public void setOnDateChangedListener(OnDateChangedListener listener) {
        mMonthView.setOnDateChangedListener(listener);
    }

    public void setSelectedDate(Calendar date) {
        mMonthView.setSelectedDate(date);
    }

    private int getMonthViewHeight() {
        int numberOfRows = mViewExpanded ? EXPAND_ROW_COUNT : COLLAPSE_ROW_COUNT;
        return mCellHeight * numberOfRows;
    }

    public void expandCalendarView(boolean expand) {
        if(mViewExpanded != expand) {
            mViewExpanded = expand;
            getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
            requestLayout();
            AnimationUtils.expandOrCollapseView(mMonthView, mMonthView.getHeight(), getMonthViewHeight());
        }
    }

    public void setOnUserTouchScrollListener(OnUserTouchScrollListener listener) {
        mMonthView.setOnUserTouchScrollListener(listener);
    }

    public void setOnDayLoadedListener(CalendarMonthView.OnDayLoadedListener listener) {
        mMonthView.setOnDayLoadedListener(listener);
    }
}
