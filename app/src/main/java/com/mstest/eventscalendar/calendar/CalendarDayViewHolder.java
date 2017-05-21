package com.mstest.eventscalendar.calendar;

import android.content.Context;
import android.content.res.ColorStateList;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.mstest.eventscalendar.R;
import com.mstest.eventscalendar.model.CalendarDay;
import com.mstest.eventscalendar.utils.CalendarUtils;
import com.mstest.eventscalendar.utils.TimeUtils;

/**
 * Created by silverauto on 2017/5/13.
 */

public class CalendarDayViewHolder extends RecyclerView.ViewHolder {
    private TextView mMonthTextView;
    private TextView mDayTextView;
    private View mSelectedCircle;
    private Context mContext;
    private CalendarDay mDay;
    private ColorStateList mTodayTextColor;
    private ColorStateList mDayTextColor;

    public CalendarDayViewHolder(View itemView, Context context) {
        super(itemView);
        mContext = context;
        mMonthTextView = (TextView) itemView.findViewById(R.id.month_text_view);
        mDayTextView = (TextView) itemView.findViewById(R.id.day_text_view);
        mSelectedCircle = itemView.findViewById(R.id.selected_day_circle);

        mTodayTextColor = ContextCompat.getColorStateList(context, R.color.calendar_today_text_color_selector);
        mDayTextColor = ContextCompat.getColorStateList(context, R.color.calendar_day_text_color_selector);
    }

    public void setDay(CalendarDay day) {
        mDay = day;
        boolean isSelected = day.isSelected();
        boolean isToday = CalendarUtils.isSameDay(CalendarUtils.today(), day.getCalendar());
        itemView.setBackgroundResource(isToday && !isSelected ? R.color.today_highlight_color : (day.getMonth() % 2 == 0 ? R.color.even_month_day_view_background_color : R.color.old_month_day_view_background_color));
        boolean isFirstDayOfMonth = day.getDay() == 1;
        mMonthTextView.setVisibility(isFirstDayOfMonth && !isSelected ? View.VISIBLE : View.GONE);
        if(isFirstDayOfMonth) {
            mMonthTextView.setText(TimeUtils.monthString(mContext, day.getDate()));
        }
        mDayTextView.setText(String.format(TimeUtils.getCurrentLocale(mContext), "%d",day.getDay()));
        mDayTextView.setSelected(isSelected);
        mDayTextView.setTextColor(isToday ? mTodayTextColor : mDayTextColor);
        mSelectedCircle.setVisibility(isSelected ? View.VISIBLE : View.GONE);
    }

    public CalendarDay getDay() {
        return mDay;
    }
}
