package com.mstest.eventscalendar;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.mstest.eventscalendar.agenda.AgendaView;
import com.mstest.eventscalendar.calendar.CalendarMonthView;
import com.mstest.eventscalendar.calendar.CalendarView;
import com.mstest.eventscalendar.ui.OnDateChangedListener;
import com.mstest.eventscalendar.ui.OnUserTouchScrollListener;

import java.util.Calendar;

public class CalendarAgendaView extends LinearLayout {
    private OnDateChangedListener mDateChangedListener;
    private AgendaView mAgendaView;
    private CalendarView mCalendarView;
    private CalendarAgendaCoordinator mCalendarAgendaCoordinator;

    public CalendarAgendaView(Context context) {
        super(context);
        init();
    }

    public CalendarAgendaView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CalendarAgendaView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void init() {
        super.setOrientation(VERTICAL);
        LayoutInflater.from(getContext()).inflate(R.layout.calendar_agenda_view, this, true);
        mAgendaView = (AgendaView) findViewById(R.id.agenda_view);
        mCalendarView = (CalendarView) findViewById(R.id.calendar_view);
        mCalendarAgendaCoordinator = new CalendarAgendaCoordinator();
        mCalendarAgendaCoordinator.setDateChangedListener(mDateChangedListener);
        mCalendarAgendaCoordinator.coordinate(mCalendarView, mAgendaView);
    }

    public void setOrientation(int orientation) {
        // no op
    }

    public void setOnDateChangedListener(OnDateChangedListener listener) {
        mDateChangedListener = listener;
        if(mCalendarAgendaCoordinator != null) {
            mCalendarAgendaCoordinator.setDateChangedListener(mDateChangedListener);
        }
    }

    public static class CalendarAgendaCoordinator {
        private CalendarView mCalendarView;
        private AgendaView mAgendaView;
        private OnDateChangedListener mDateChangedListener;

        private OnDateChangedListener mOnDateChangedListener = new OnDateChangedListener() {
            public void onDateSelected(View view, Calendar date) {
                if(view == mAgendaView) {
                    mCalendarView.setSelectedDate(date);
                } else {
                    mAgendaView.setSelectedDate(date);
                }

                if(mDateChangedListener != null) {
                    mDateChangedListener.onDateSelected(view, date);
                }
            }
        };

        private OnUserTouchScrollListener mOnUserTouchScrollListener = new OnUserTouchScrollListener() {
            @Override
            public void onUserTouchScrolled(RecyclerView recyclerView) {
                mCalendarView.expandCalendarView(recyclerView != mAgendaView);
            }
        };

        private CalendarMonthView.OnDayLoadedListener mOnDayLoadedListener = new CalendarMonthView.OnDayLoadedListener() {
            @Override
            public void onDayLoaded(Calendar date) {
                mAgendaView.checkIfLoadMoreData(date);
            }
        };

        void coordinate(CalendarView calendarView, AgendaView agendaView) {
            mCalendarView = calendarView;
            mCalendarView.setOnDateChangedListener(mOnDateChangedListener);
            mCalendarView.setOnUserTouchScrollListener(mOnUserTouchScrollListener);
            mCalendarView.setOnDayLoadedListener(mOnDayLoadedListener);
            mAgendaView = agendaView;
            mAgendaView.setOnDateChangedListener(mOnDateChangedListener);
            mAgendaView.setOnUserTouchScrollListener(mOnUserTouchScrollListener);
        }

        void setDateChangedListener(OnDateChangedListener listener) {
            mDateChangedListener = listener;
        }
    }
}
