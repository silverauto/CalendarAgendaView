package com.mstest.eventscalendar;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.mstest.eventscalendar.ui.OnDateChangedListener;
import com.mstest.eventscalendar.utils.CalendarUtils;
import com.mstest.eventscalendar.utils.TimeUtils;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    private Toolbar mToolBar;
    private CalendarAgendaView mCalendarAgendaView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mToolBar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolBar);
        setupTitle();
        setupCalendarAgendaView();
    }

    private void setupTitle() {
        mToolBar.setTitleTextColor(Color.WHITE);
        getSupportActionBar().setTitle(TimeUtils.yearMonthString(this, CalendarUtils.today().getTime()));
    }

    private void setupCalendarAgendaView() {
        mCalendarAgendaView = (CalendarAgendaView) findViewById(R.id.calendar_agenda_view);
        mCalendarAgendaView.setOnDateChangedListener(new OnDateChangedListener() {
            @Override
            public void onDateSelected(View view, Calendar date) {
                getSupportActionBar().setTitle(TimeUtils.yearMonthString(MainActivity.this, date.getTime()));
            }
        });
    }
}
