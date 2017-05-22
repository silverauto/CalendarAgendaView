package com.mstest.eventscalendar.calendar;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;

import com.github.rubensousa.gravitysnaphelper.GravitySnapHelper;
import com.mstest.eventscalendar.ui.DividerItemDecorationItem;
import com.mstest.eventscalendar.ui.OnDateChangedListener;
import com.mstest.eventscalendar.ui.OnUserTouchScrollListener;
import com.mstest.eventscalendar.ui.RecyclerViewGestureListener;
import com.mstest.eventscalendar.utils.CalendarUtils;

import java.util.Calendar;

/**
 * A grid of day views. Each row is a week display the days from Sunday to Saturday.
 */

public class CalendarMonthView extends RecyclerView {
    private CalendarMonthViewAdapter mAdapter;
    private GridLayoutManager mLayoutManager;
    private OnDateChangedListener mOnDateChangedListener;
    private OnUserTouchScrollListener mOnUserTouchScrollListener;
    private Calendar mSelectedDate;
    private OnDayLoadedListener mOnDayLoadedListener;
    private Calendar mFirstLoadedDay;
    private Calendar mLastLoadedDay;

    public interface OnDayLoadedListener {
        void onDayLoaded(Calendar date);
    }

    private RecyclerViewGestureListener mGestureListener = new RecyclerViewGestureListener(this) {
        @Override
        public void onItemClick(RecyclerView.ViewHolder viewHolder) {
            if(viewHolder != null) {
                setSelectedDate(((CalendarDayViewHolder)viewHolder).getDay().getCalendar());
            }
        }

        @Override
        public void onScrolled() {
            if(mOnUserTouchScrollListener != null) {
                mOnUserTouchScrollListener.onUserTouchScrolled(getRecyclerView());
            }
        }
    };

    public CalendarMonthView(Context context) {
        super(context);
        init();
    }

    public CalendarMonthView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CalendarMonthView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        setOverScrollMode(View.OVER_SCROLL_NEVER);
        mAdapter = new CalendarMonthViewAdapter(getContext());
        setAdapter(mAdapter);
        setHasFixedSize(true);
        mLayoutManager = new GridLayoutManager(getContext(), 7, 1, false);
        setLayoutManager(mLayoutManager);
        addItemDecoration(new DividerItemDecorationItem(getContext()));
        addOnItemTouchListener(mGestureListener);
        setItemAnimator(null);
        LinearSnapHelper linearSnapHelper = new GravitySnapHelper(Gravity.TOP);
        linearSnapHelper.attachToRecyclerView(this);
        this.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                getViewTreeObserver().removeOnPreDrawListener(this);
                setSelectedDate(CalendarUtils.today());
                return false;
            }
        });
    }

    public void setSelectedDate(Calendar date) {
        if(!CalendarUtils.isSameDay(mSelectedDate, date) && date != null && !mAdapter.isOutOfDayRange(date)) {
            Calendar firstVisibleDate = mAdapter.getDay(mLayoutManager.findFirstVisibleItemPosition()).getCalendar();
            Calendar lastVisibleDate = mAdapter.getDay(mLayoutManager.findLastVisibleItemPosition()).getCalendar();
            if(date.before(firstVisibleDate) || date.after(lastVisibleDate)) {
                mLayoutManager.scrollToPositionWithOffset(mAdapter.indexOfDay(date), 0);
            }
            mAdapter.setDaySelected(date);
            mSelectedDate = date;
            if(mOnDateChangedListener != null) {
                mOnDateChangedListener.onDateSelected(this, mSelectedDate);
            }

            if(mFirstLoadedDay == null || mLastLoadedDay == null) {
                // set the first
                mFirstLoadedDay = date;
                mLastLoadedDay = date;
            }
        }

    }

    public void setOnDateChangedListener(OnDateChangedListener listener) {
        mOnDateChangedListener = listener;
    }

    public void setOnUserTouchScrollListener(OnUserTouchScrollListener listener) {
        mOnUserTouchScrollListener = listener;
    }

    public void setOnDayLoadedListener(OnDayLoadedListener listener) {
        mOnDayLoadedListener = listener;
    }

    @Override
    public void onScrolled(int dx, int dy) {
        if(mOnDayLoadedListener != null) {
            int firstPosition = mLayoutManager.findFirstVisibleItemPosition();
            int lastPosition = mLayoutManager.findLastVisibleItemPosition();
            if(firstPosition == NO_POSITION || lastPosition == NO_POSITION ||
                    mFirstLoadedDay == null || mLastLoadedDay == null) {
                return;
            }

            Calendar firstVisibleDay = mAdapter.getDay(firstPosition).getCalendar();
            Calendar lastVisibleDay = mAdapter.getDay(lastPosition).getCalendar();

            if (firstVisibleDay.before(mFirstLoadedDay)) {
                mOnDayLoadedListener.onDayLoaded(firstVisibleDay);
                mFirstLoadedDay = firstVisibleDay;
            } else if(lastVisibleDay.after(mLastLoadedDay)) {
                mOnDayLoadedListener.onDayLoaded(lastVisibleDay);
                mLastLoadedDay = lastVisibleDay;
            }
        }
    }
}
