package com.mstest.eventscalendar.agenda;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.mstest.eventscalendar.utils.CalendarUtils;
import com.mstest.eventscalendar.utils.TimeUtils;

import java.util.Calendar;
import java.util.Date;

public class AgendaHeaderViewHolder extends AgendaViewHolder {
    TextView mDateView;

    public AgendaHeaderViewHolder(View itemView) {
        super(itemView);
        mDateView = (TextView) itemView;
    }

    public void setDate(Context context, Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        boolean isToday = CalendarUtils.isSameDay(CalendarUtils.today(), calendar);
        mDateView.setSelected(isToday);
        String formattedDate = TimeUtils.longDateString(context, date);
        mDateView.setText(formattedDate);
    }
}
