package com.mstest.eventscalendar.agenda;

import android.view.View;
import android.widget.TextView;

import com.mstest.eventscalendar.R;
import com.mstest.eventscalendar.model.Event;
import com.mstest.eventscalendar.utils.CalendarUtils;
import com.mstest.eventscalendar.utils.TimeUtils;

public class AgendaEventViewHolder extends AgendaViewHolder {
    private TextView mEventTime;
    private TextView mEventDuration;
    private TextView mEventTitle;

    public AgendaEventViewHolder(View itemView) {
        super(itemView);
        mEventTime = (TextView) itemView.findViewById(R.id.eventTime);
        mEventDuration = (TextView) itemView.findViewById(R.id.eventDuration);
        mEventTitle = (TextView) itemView.findViewById(R.id.eventTitle);
    }

    public void setEvent(Event event) {
        long now = CalendarUtils.today().getTimeInMillis();
        boolean isEventOccurred = now >= event.getStartTime() && now < event.getStartTime() + event.getDurationInMillis();
        mEventTime.setSelected(isEventOccurred);
        mEventTime.setText(TimeUtils.timeString(getContext(), event.getStartTime()));
        mEventTitle.setText(event.getTitle());
        long numberOfMinutes = TimeUtils.numberOfMinutes(event.getDurationInMillis());
        int hours = (int) numberOfMinutes / 60;
        int minutes = (int) numberOfMinutes % 60;
        String duration;
        if(hours > 0) {
            if(minutes == 0) {
                duration = String.format(getContext().getString(R.string.duration_hours_format), hours);
            } else {
                duration = String.format(getContext().getString(R.string.duration_hours_and_minutes_format), hours, minutes);
            }
        } else {
            duration = String.format(getContext().getString(R.string.duration_minutes_format), minutes);
        }
        mEventDuration.setText(duration);

    }
}
