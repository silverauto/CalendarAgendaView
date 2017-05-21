package com.mstest.eventscalendar.test;

import android.database.CursorWrapper;
import android.database.MatrixCursor;

import com.mstest.eventscalendar.content.CalendarEventLoader;

/**
 * Created by silverauto on 2017/5/19.
 */

public class MockCursor extends CursorWrapper{

    public MockCursor() {
        super(new MatrixCursor(CalendarEventLoader.PROJECTION));
    }

    public void addRow(Object[] rowValues) {
        ((MatrixCursor) getWrappedCursor()).addRow(rowValues);
    }
}
