package com.simplicityapks.reminderdatepicker.lib;

import java.util.Calendar;

/**
 * Object to be inserted into the ArrayAdapter of the TimeSpinner. The time is saved as well as a label.
 */
public class TimeItem implements TwinTextItem{

    private final String label, digitalTime;
    private final int hour, minute;

    /**
     * Constructs a new TimeItem holding the specified time and a label to show primarily.
     * @param label The String to return when getPrimaryText() is called, but the first text in brackets is set as secondary text.
     * @param time The time to be returned by getTime(), as Calendar.
     */
    public TimeItem(String label, Calendar time) {
        this(label, time.get(Calendar.HOUR_OF_DAY), time.get(Calendar.MINUTE));
    }

    /**
     * Constructs a new TimeItem holding the specified time and a label to to show primarily.
     * @param label The String to return when getPrimaryText() is called, but the first text in brackets is set as secondary text.
     * @param hour The hour of the day.
     * @param minute The minute of the hour.
     */
    public TimeItem(String label, int hour, int minute) {
        this.hour = hour;
        this.minute = minute;

        // parse the digital time from the label and set both label and digitalTime:
        int timeStart = label.indexOf('(');
        int timeEnd = label.indexOf(')');
        if(timeStart>0 || timeEnd>0) {
            digitalTime = label.substring(timeStart+1, timeEnd);
            this.label = label.substring(0, timeStart) + label.substring(timeEnd+1);
        } else {
            // something went wrong, assume that label is only the primary text:
            digitalTime = null;
            this.label = label;
        }
    }

    /**
     * Constructs a new TimeItem holding the specified time and a label to to show primarily.
     * @param label The String to return when getPrimaryText() is called.
     * @param timeString The String to return when getSecondaryText() is called.
     * @param hour The hour of the day.
     * @param minute The minute of the hour.
     */
    public TimeItem(String label, String timeString, int hour, int minute) {
        this.label = label;
        this.digitalTime = timeString;
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
    public CharSequence getPrimaryText() {
        return label;
    }

    @Override
    public CharSequence getSecondaryText() {
        return digitalTime;
    }

    /**
     * The returned String may be passed to {@link #fromString(String)} to save and recreate this object easily.
     * @return The elements of this object separated by \n
     */
    @Override
    public String toString() {
        String sep = "\n";
        return label +sep+ digitalTime +sep+ hour +sep+ minute;
    }

    /**
     * Constructs a new TimeItem from a String previously gotten from the {@link #toString()} method.
     * @param code The string to parse from.
     * @return A new TimeItem, or null if there was an error.
     */
    public static TimeItem fromString(String code) {
        String[] items = code.split("\n");
        if(items.length != 4) return null;
        int hour, minute;
        try {
            hour = Integer.parseInt(items[2]);
            minute = Integer.parseInt(items[3]);
        }
        catch (NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
        return new TimeItem(items[0], items[1], hour, minute);
    }
}
