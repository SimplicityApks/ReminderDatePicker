package com.simplicityapks.reminderdatepicker.lib;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * TwinTextItem to be inserted into the ArrayAdapter of the DateSpinner. The date is saved like the DatePicker.
 * The secondary text is currently not in use, so getSecondaryText() returns null.
 */
public class DateItem implements TwinTextItem{

    private final String label;
    private final int year, month, day;

    /**
     * Constructs a new DateItem holding the specified date and a label to show primarily.
     * @param label The string to return when getPrimaryText() is called.
     * @param date The date to be returned by getDate().
     */
    public DateItem(String label, Calendar date) {
        this(label, date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH));
    }

    /**
     * Constructs a new DateItem holding the specified date and a label to to show primarily.
     * @param label The string to return when getPrimaryText() is called.
     * @param year The year.
     * @param month The month of year, zero-indexed (so 11 is December).
     * @param day The day of the month.
     */
    public DateItem(String label, int year, int month, int day) {
        this.label = label;
        this.year = year;
        this.month = month;
        this.day = day;
    }

    /**
     * Gets the current date set in this DateItem.
     * @return A new GregorianCalendar containing the date.
     */
    public Calendar getDate() {
        return new GregorianCalendar(year, month, day);
    }

    /**
     * Gets the day of the month set for this TimeItem.
     * @return The day, as int.
     */
    public int getDay() {
        return this.day;
    }

    /**
     * Gets the month set for this TimeItem.
     * @return The month, as int.
     */
    public int getMonth() {
        return this.month;
    }

    /**
     * Gets the year set for this TimeItem.
     * @return The year, as int.
     */
    public int getYear() {
        return this.year;
    }

    /**
     * Deeply compares this DateItem to the specified Object. Returns true if obj is a DateItem and
     * contains the same date (ignoring the label) or is a Calendar and contains the same date
     * ignoring hour, minute and second.
     * @param obj The Object to compare this to.
     * @return true if equal, false otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        int objDay, objMonth, objYear;
        if(obj instanceof DateItem) {
            DateItem item = (DateItem) obj;
            objDay = item.getDay();
            objMonth = item.getMonth();
            objYear = item.getYear();
        }
        else if(obj instanceof Calendar) {
            Calendar cal = (Calendar) obj;
            objDay = cal.get(Calendar.DAY_OF_MONTH);
            objMonth = cal.get(Calendar.MONTH);
            objYear = cal.get(Calendar.YEAR);
        }
        else return false;
        return objDay==this.day && objMonth==this.month && objYear==this.year;
    }

    @Override
    public CharSequence getPrimaryText() {
        return label;
    }

    @Override
    public CharSequence getSecondaryText() {
        return null;
    }

    /**
     * The returned String may be passed to {@link #fromString(String)} to save and recreate this object easily.
     * @return The elements of this object separated by \n
     */
    @Override
    public String toString() {
        String sep = "\n";
        return label +sep+ year +sep+ month +sep+ day;
    }

    /**
     * Constructs a new TimeItem from a String previously gotten from the {@link #toString()} method.
     * @param code The string to parse from.
     * @return A new TimeItem, or null if there was an error.
     */
    public static DateItem fromString(String code) {
        String[] items = code.split("\n");
        if(items.length != 4) return null;
        int year, month, day;
        try {
            year = Integer.parseInt(items[1]);
            month = Integer.parseInt(items[2]);
            day = Integer.parseInt(items[3]);
        }
        catch (NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
        return new DateItem(items[0], year, month, day);
    }
}