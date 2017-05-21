package com.mstest.eventscalendar.ui;

import android.view.View;

import java.util.Calendar;

/**
 * Created by silverauto on 2017/5/14.
 */

public interface OnDateChangedListener {
    void onDateSelected(View view, Calendar date);
}
