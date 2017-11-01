package com.simplicityapks.reminderdatepicker.lib;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * TwinTextItem to be inserted into the ArrayAdapter of the DateSpinner. The date is saved like the DatePicker.
 * The secondary text is currently not in use, so getSecondaryText() returns null.
 */
public class DateItem implements TwinTextItem{

    private final String label, dateNumbers;
    private final int year, month, day, id;
    private boolean enabled = true;

    /**
     * Constructs a new DateItem holding the specified date and a label to show primarily.
     * @param label The string to return when getPrimaryText() is called.
     * @param date The date to be returned by getDate().
     * @param id The identifier to find this item with.
     */
    public DateItem(String label, Calendar date, int id) {
        this(label, date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH), id);
    }

    /**
     * Constructs a new DateItem holding the specified date and a label to to show primarily.
     * @param label The string to return when getPrimaryText() is called.
     * @param year The year.
     * @param month The month of year, zero-indexed (so 11 is December).
     * @param day The day of the month.
     * @param id The identifier to find this item with.
     */
    public DateItem(String label, int year, int month, int day, int id) {
        this.label = label;
        this.year = year;
        this.month = month;
        this.day = day;
        this.id = id;
        this.dateNumbers = null;
    }

    /**
     * Constructs a new DateItem holding the specified date and a label to show primarily, as well as
     * a dateString to show secondary.
     * @param label The string to return when getPrimaryText() is called.
     * @param dateString The String to return when getSecondaryText() is called.
     * @param date The date to be returned by getDate().
     * @param id The identifier to find this item with.
     */
    public DateItem(String label, String dateString, Calendar date, int id) {
        this(label, dateString, date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH), id);
    }

    /**
     * Constructs a new DateItem holding the specified date and a label to to show primarily, as well as
     * a dateString to show secondary.
     * @param label The string to return when getPrimaryText() is called.
     * @param dateString The String to return when getSecondaryText() is called.
     * @param year The year.
     * @param month The month of year, zero-indexed (so 11 is December).
     * @param day The day of the month.
     * @param id The identifier to find this item with.
     */
    public DateItem(String label, String dateString, int year, int month, int day, int id) {
        this.label = label;
        this.year = year;
        this.month = month;
        this.day = day;
        this.id = id;
        this.dateNumbers = dateString;
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
        return dateNumbers;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Enable or disable this spinner item.
     * @param enable true to enable, false to disable this item.
     */
    public void setEnabled(boolean enable) {
        this.enabled = enable;
    }

    /**
     * The returned String may be passed to {@link #fromString(String)} to save and recreate this object easily.
     * @return The elements of this object separated by \n
     */
    @Override
    public String toString() {
        String sep = "\n";
        return nullToEmpty(label) +sep+ nullToEmpty(dateNumbers) +sep+ year +sep+ month +sep+ day +sep+ id;
    }

    /**
     * Constructs a new TimeItem from a String previously gotten from the {@link #toString()} method.
     * @param code The string to parse from.
     * @return A new TimeItem, or null if there was an error.
     */
    public static DateItem fromString(String code) {
        String[] items = code.split("\n");
        if(items.length != 6) return null;
        int year, month, day, id;
        try {
            year = Integer.parseInt(items[2]);
            month = Integer.parseInt(items[3]);
            day = Integer.parseInt(items[4]);
            id = Integer.parseInt(items[5]);
        }
        catch (NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
        return new DateItem(emptyToNull(items[0]), emptyToNull(items[1]), year, month, day, id);
    }

    /**
     * Makes sure s is not null, but the empty string instead. Otherwise just return s.
     */
    private static String nullToEmpty(String s) {
        return s==null? "" : s;
    }

    /**
     * Makes sure s is not an empty string, but null instead. Otherwise just return s.
     */
    private static String emptyToNull(String s) {
        return "".equals(s)? null : s;
    }
}