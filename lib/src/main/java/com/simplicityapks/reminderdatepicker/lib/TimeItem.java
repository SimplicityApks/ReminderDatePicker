package com.simplicityapks.reminderdatepicker.lib;

import android.graphics.Color;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.AlignmentSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;

import java.util.Calendar;

/**
 * Object to be inserted into the ArrayAdapter of the TimeSpinner. The time is saved as well as a label.
 */
public class TimeItem extends SpannableStringBuilder{

    private final int hour, minute;

    /**
     * Constructs a new TimeItem holding the specified time and a label to return in the toString() method.
     * @param label The String to return when toString() is called.
     * @param time The time to be returned by getTime(), as Calendar.
     */
    public TimeItem(CharSequence label, Calendar time) {
        this(label, time.get(Calendar.HOUR_OF_DAY), time.get(Calendar.MINUTE));
    }

    /**
     * Constructs a new TimeItem holding the specified time and a label to return in the toString() method.
     * @param label The String to return when toString() is called.
     * @param hour The hour of the day.
     * @param minute The minute of the hour.
     */
    public TimeItem(CharSequence label, int hour, int minute) {
        this(label, hour, minute, Color.GRAY);
    }


    /**
     * Constructs a new TimeItem holding the specified time and a label to return in the toString() method.
     * @param label The String to return when toString() is called.
     * @param hour The hour of the day.
     * @param minute The minute of the hour.
     */
    public TimeItem(CharSequence label, int hour, int minute, int timeColor) {
        super(label);
        this.hour = hour;
        this.minute = minute;
        applyTimeMarkup(timeColor);
    }

    private void applyTimeMarkup(int timeColor) {
        String timeString = this.toString();
        int timeStart = timeString.indexOf('(');
        if(timeStart<0) throw new IllegalStateException("The string "+timeString+" must contain an opening bracket for markup.");
        this.delete(timeStart, timeStart+1);

        int timeEnd = timeString.indexOf(')') - 1;
        if(timeEnd<0) throw new IllegalStateException("The string "+timeString+" must contain a closing bracket for markup.");
        this.delete(timeEnd, timeEnd+1);

        // add two spaces in between the text:
        this.insert(timeStart, " ");
        timeStart++;
        timeEnd++;

        // apply the alignment: This does not work currently...
        // TODO: find other way to handle alignment (2 TextViews?)
        /*setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_OPPOSITE), timeStart, timeEnd,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);*/

        // apply the color highlight in grey:
        setSpan(new ForegroundColorSpan(timeColor), timeStart, timeEnd,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
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
}
