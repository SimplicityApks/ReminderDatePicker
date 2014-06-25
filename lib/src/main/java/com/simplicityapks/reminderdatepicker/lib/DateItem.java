package com.simplicityapks.reminderdatepicker.lib;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Object to be inserted into the ArrayAdapter of the DateSpinner. The date is saved like the DatePicker.
 */
public class DateItem implements TwinTextItem{

    private final String label;
    private final int day, month, year;

    /**
     * Constructs a new DateItem holding the specified date and a label to show primarily.
     * @param label The string to return when getPrimaryText() is called.
     * @param date The date to be returned by getDate().
     */
    public DateItem(String label, Calendar date) {
        this(label, date.get(Calendar.DAY_OF_MONTH), date.get(Calendar.MONTH), date.get(Calendar.YEAR));
    }

    /**
     * Constructs a new DateItem holding the specified date and a label to to show primarily.
     * @param label The string to return when getPrimaryText() is called.
     * @param day The day of the month.
     * @param month The month of year, zero-indexed (so 11 is December).
     * @param year The year.
     */
    public DateItem(String label, int day, int month, int year) {
        this.label = label;
        this.day = day;
        this.month = month;
        this.year = year;
    }

    /**
     * Gets the current date set in this DateItem.
     * @return A new GregorianCalendar containing the date.
     */
    public Calendar getDate() {
        return new GregorianCalendar(day, month, year);
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
}