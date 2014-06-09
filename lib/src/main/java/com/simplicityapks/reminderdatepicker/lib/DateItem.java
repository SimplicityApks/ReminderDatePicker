package com.simplicityapks.reminderdatepicker.lib;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Object to be inserted into the ArrayAdapter of the DateSpinner. The date is saved like the DatePicker.
 */
public class DateItem {

    private final String label;
    private final int day, month, year;

    /**
     * Constructs a new DateItem holding the specified date and a label to return in the toString() method.
     * @param label The string to return when toString() is called.
     * @param date The date to be returned by getDate().
     */
    public DateItem(String label, Calendar date) {
        this(label, date.get(Calendar.DAY_OF_MONTH), date.get(Calendar.MONTH), date.get(Calendar.YEAR));
    }

    /**
     * Constructs a new DateItem holding the specified date and a label to return in the toString() method.
     * @param label The string to return when toString() is called.
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

    @Override
    public String toString() {
        return label;
    }
}
