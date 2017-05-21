package com.mstest.eventscalendar.calendar;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.mstest.eventscalendar.R;
import com.mstest.eventscalendar.model.CalendarDay;
import com.mstest.eventscalendar.utils.CalendarUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.mstest.eventscalendar.content.CalendarDataStore.MAX_CACHED_WEEK_DATA_SIZE;
import static com.mstest.eventscalendar.content.CalendarDataStore.PREFETCH_DATA_SIZE;

public class CalendarMonthViewAdapter extends RecyclerView.Adapter<CalendarDayViewHolder> {

    private Context mContext;
    private List<CalendarDay> mDays;
    private int mSelectedIndex;

    CalendarMonthViewAdapter(Context context) {
        mContext = context;
        mDays = new ArrayList<>();
        loadCalendarDay();
    }

    @Override
    public CalendarDayViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new CalendarDayViewHolder(LayoutInflater.from(mContext).inflate(R.layout.calendar_day_view, parent, false), mContext);
    }

    @Override
    public void onBindViewHolder(CalendarDayViewHolder holder, int position) {
        if(position < 0 || position >= getItemCount()) {
            return;
        }
        holder.setDay(getDay(position));
    }

    @Override
    public int getItemCount() {
        return mDays.size();
    }

    public CalendarDay getDay(int position) {
        if(position < 0 || position >= getItemCount()) {
            return null;
        }
        return mDays.get(position);
    }

    private void loadCalendarDay() {
        Calendar dayCounter = CalendarUtils.copyFrom(CalendarUtils.today());
        int dayOfWeek = dayCounter.get(Calendar.DAY_OF_WEEK);
        dayCounter.add(Calendar.DATE, -dayOfWeek + 1);
        // add this week
        for(int i=0;i<7;i++) {
            mDays.add(CalendarDay.from(dayCounter));
            dayCounter.add(Calendar.DATE, 1);
        }

        // append later weeks
        for(int i=0;i<MAX_CACHED_WEEK_DATA_SIZE / 8;i++) {
            loadLaterWeeks(dayCounter);
        }

        // prepend earlier weeks
        dayCounter = CalendarUtils.copyFrom(CalendarUtils.today());
        dayCounter.add(Calendar.DATE, -dayOfWeek + 1);
        for(int i=0;i<MAX_CACHED_WEEK_DATA_SIZE / 8;i++) {
            loadEarlierWeeks(dayCounter);
        }
    }

    private void loadLaterWeeks(Calendar date) {
        for(int i=0;i<PREFETCH_DATA_SIZE;i++) {
            mDays.add(CalendarDay.from(date));
            date.add(Calendar.DATE, 1);
        }
    }

    private void loadEarlierWeeks(Calendar date) {
        for(int i=0;i<PREFETCH_DATA_SIZE;i++) {
            date.add(Calendar.DATE, -1);
            mDays.add(0, CalendarDay.from(date));
        }
    }

    int indexOfDay(Calendar day) {
        if(mDays.size() == 0) {
            return -1;
        }
        return CalendarUtils.daysBetween(mDays.get(0).getCalendar(), day);
    }

    boolean isOutOfDayRange(Calendar day) {
        int dataCount = mDays.size();
        return dataCount == 0 || day.before(mDays.get(0).getCalendar()) || day.after(mDays.get(dataCount-1));
    }

    void setDaySelected(Calendar day) {
        if(isOutOfDayRange(day)) {
            return;
        }
        int selectedIndex = indexOfDay(day);
        if(mSelectedIndex != selectedIndex && selectedIndex < mDays.size() && selectedIndex >= 0) {
            mDays.get(mSelectedIndex).setSelected(false);
            mDays.get(selectedIndex).setSelected(true);
            notifyItemChanged(selectedIndex);
            notifyItemChanged(mSelectedIndex);
            mSelectedIndex = selectedIndex;
        }
    }
}
