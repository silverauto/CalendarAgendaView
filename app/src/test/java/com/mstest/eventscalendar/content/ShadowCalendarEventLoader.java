package com.mstest.eventscalendar.content;

import com.mstest.eventscalendar.test.MockCursor;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

@Implements(value = CalendarEventLoader.class, inheritImplementationMethods = true)
public class ShadowCalendarEventLoader {
    @RealObject
    CalendarEventLoader realObject;

    public static final String MOCK_EVENT_TITLE1 = "Event1";
    public static final String MOCK_EVENT_TITLE2 = "Event2";

    @Implementation
    public void queryEvents(long startTime, long endTime) {
        MockCursor cursor = new MockCursor();
        long ten_am = startTime + 3600000 * 10;
        cursor.addRow(new Object[]{1L, MOCK_EVENT_TITLE1, ten_am, ten_am + 3600000, 0}); // 10am ~ 11am
        long eight_pm = startTime + 3600000 * 20;
        cursor.addRow(new Object[]{1L, MOCK_EVENT_TITLE2, eight_pm, eight_pm + 3600000, 0}); // 8pm ~ 9pm
        realObject.onQueryComplete(0, startTime, cursor);
    }
}
