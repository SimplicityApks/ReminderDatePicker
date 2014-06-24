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

    /**
     * Gets the hour set for this TimeItem.
     * @return The hour, as int.
     */
    public int getHour() {
        return this.hour;
    }

    /**
     * Gets the minute set for this TimeItem.
     * @return The minute, as int.
     */
    public int getMinute() {
        return this.minute;
    }

    /**
     * Deeply compares this TimeItem to the specified Object. Returns true if obj is a TimeItem and
     * contains the same date (ignoring the label) or is a Calendar and contains the same hour and minute.
     * @param obj The Object to compare this to.
     * @return true if equal, false otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        int objHour, objMinute;
        if(obj instanceof TimeItem) {
            TimeItem item = (TimeItem) obj;
            objHour = item.getHour();
            objMinute = item.getMinute();
        }
        else if(obj instanceof Calendar) {
            Calendar cal = (Calendar) obj;
            objHour = cal.get(Calendar.HOUR_OF_DAY);
            objMinute = cal.get(Calendar.MINUTE);
        }
        else return false;
        return objHour==this.hour && objMinute==this.minute;
    }

    @Override
    public String toString() {
        return label;
    }
}
