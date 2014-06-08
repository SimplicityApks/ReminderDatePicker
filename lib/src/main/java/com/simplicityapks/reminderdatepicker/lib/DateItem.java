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

    public DateItem(String label, Calendar date) {
        this(label, date.get(Calendar.DAY_OF_MONTH), date.get(Calendar.MONTH), date.get(Calendar.YEAR));
    }

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
