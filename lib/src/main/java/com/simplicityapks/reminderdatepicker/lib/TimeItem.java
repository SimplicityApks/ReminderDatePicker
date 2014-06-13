package com.simplicityapks.reminderdatepicker.lib;

import java.util.Calendar;

/**
 * Object to be inserted into the ArrayAdapter of the TimeSpinner. The time is saved as well as a label.
 */
public class TimeItem {

    private final String label;
    private final int hour, minute;

    /**
     * Constructs a new TimeItem holding the specified time and a label to return in the toString() method.
     * @param label The string to return when toString() is called.
     * @param time The time to be returned by getTime(), as Calendar.
     */
    public TimeItem(String label, Calendar time) {
        this(label, time.get(Calendar.HOUR_OF_DAY), time.get(Calendar.MINUTE));
    }

    /**
     * Constructs a new TimeItem holding the specified time and a label to return in the toString() method.
     * @param label The string to return when toString() is called.
     * @param hour The hour of the day.
     * @param minute The minute of the hour.
     */
    public TimeItem(String label, int hour, int minute) {
        this.label = label;
        this.hour = hour;
        this.minute = minute;
    }

    /**
     * Gets the current time set in this TimeItem.
     * @return A new Calendar containing the time.
     */
    public Calendar getTime() {
        Calendar result = Calendar.getInstance();
        result.set(Calendar.HOUR_OF_DAY, hour);
        result.set(Calendar.MINUTE, minute);
        return result;
    }

    @Override
    public String toString() {
        return label;
    }
}
