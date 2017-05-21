package com.mstest.eventscalendar.agenda;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;

import com.mstest.eventscalendar.content.CalendarDataStore;
import com.mstest.eventscalendar.ui.DividerItemDecorationItem;
import com.mstest.eventscalendar.ui.OnDateChangedListener;
import com.mstest.eventscalendar.ui.OnUserTouchScrollListener;
import com.mstest.eventscalendar.ui.RecyclerViewGestureListener;
import com.mstest.eventscalendar.utils.CalendarUtils;

import java.util.Calendar;

public class AgendaView extends RecyclerView {
    public final static int PREFETCH_BOUNDARY = 15;
    private Calendar mSelectedDate;

    private LinearLayoutManager mLayoutManager;
    private CalendarDataStore mDataStore;
    private OnDateChangedListener mOnDateChangedListener;
    private OnUserTouchScrollListener mOnUserTouchScrollListener;

    private int mOldFirstVisiblePosition = NO_POSITION;
    private int mOldLastVisiblePosition = NO_POSITION;

    private RecyclerViewGestureListener mGestureListener = new RecyclerViewGestureListener(this) {

        @Override
        public void onItemClick(ViewHolder viewHolder) {

        }

        @Override
        public void onScrolled() {
            if(mOnUserTouchScrollListener != null) {
                mOnUserTouchScrollListener.onUserTouchScrolled(getRecyclerView());
            }
        }
    };

    public AgendaView(Context context) {
        super(context);
        init(context);
    }

    public AgendaView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public AgendaView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        mDataStore = new CalendarDataStore(context.getContentResolver());
        AgendaAdapter adapter = new AgendaAdapter(context, this);
        adapter.setDataStore(mDataStore);
        setAdapter(adapter);
        setHasFixedSize(false);
        setOverScrollMode(View.OVER_SCROLL_NEVER);
        setItemAnimator(null);
        addItemDecoration(new DividerItemDecorationItem(getContext()));
        mLayoutManager = new LinearLayoutManager(getContext(), VERTICAL, false);
        setLayoutManager(mLayoutManager);
        moveToTodayEvent();
        addOnItemTouchListener(mGestureListener);
    }

    public void moveToTodayEvent() {
        // wait for the first layout
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if(getWidth() != 0 && getHeight() != 0) {
                    getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    setSelectedDate(CalendarUtils.today());
                }
            }
        });
    }

    @Override
    public void onScrolled(int dx, int dy) {
        if (dy != 0) {
            loadMoreDataIfNeeded(dy < 0);
            notifyDateChanged();
        }
    }

    public void setSelectedDate(Calendar date) {
        if(getWidth() != 0 && getHeight() != 0 && !CalendarUtils.isSameDay(mSelectedDate, date) &&
                date != null && !mDataStore.isOutOfDateRange(date)) {
             Calendar firstVisibleDate = mDataStore.getData(mLayoutManager.findFirstVisibleItemPosition()).getCalendar();

            if(!CalendarUtils.isSameDay(firstVisibleDate, date)) {
                mLayoutManager.scrollToPositionWithOffset(mDataStore.indexOfHeaderForDate(date), 0);
            }
            mSelectedDate = date;
            if(mOnDateChangedListener != null) {
                mOnDateChangedListener.onDateSelected(this, mSelectedDate);
            }
        }
    }

    public void checkIfLoadMoreData(Calendar date) {
        int firstVisiblePosition = mLayoutManager.findFirstVisibleItemPosition();
        int lastVisiblePosition = mLayoutManager.findLastVisibleItemPosition();
        if(firstVisiblePosition == NO_POSITION || lastVisiblePosition == NO_POSITION) {
            return;
        }
        if(date.before(mDataStore.getData(firstVisiblePosition).getCalendar())) {
            mDataStore.prefetchEarlierData();
        } else if(date.after(mDataStore.getData(lastVisiblePosition).getCalendar())) {
            mDataStore.prefetchLaterData();
        }
    }

    private void loadMoreDataIfNeeded(boolean isScrollUp) {
        int firstVisiblePosition = mLayoutManager.findFirstVisibleItemPosition();
        int lastVisiblePosition = mLayoutManager.findLastVisibleItemPosition();
        if(isScrollUp && mOldFirstVisiblePosition > PREFETCH_BOUNDARY && firstVisiblePosition <= PREFETCH_BOUNDARY) {
            mDataStore.prefetchEarlierData();
        } else if(!isScrollUp && mOldLastVisiblePosition < mDataStore.getTotalDataCount() - PREFETCH_BOUNDARY && lastVisiblePosition >= mDataStore.getTotalDataCount() - PREFETCH_BOUNDARY) {
            mDataStore.prefetchLaterData();
        }
        mOldFirstVisiblePosition = firstVisiblePosition;
        mOldLastVisiblePosition = lastVisiblePosition;
    }

    private void notifyDateChanged() {
        int position = mLayoutManager.findFirstVisibleItemPosition();
        if (position <= NO_POSITION) {
            return;
        }
        Calendar selectedDate = mDataStore.getData(position).getCalendar();
        if(selectedDate != mSelectedDate) {
            if(mOnDateChangedListener != null) {
                mOnDateChangedListener.onDateSelected(this, selectedDate);
            }
            mSelectedDate = selectedDate;
        }
    }

    public void setOnDateChangedListener(OnDateChangedListener listener) {
        mOnDateChangedListener = listener;
    }

    public void setOnUserTouchScrollListener(OnUserTouchScrollListener listener) {
        mOnUserTouchScrollListener = listener;
    }
}
