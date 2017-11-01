package com.simplicityapks.reminderdatepicker.lib;

import java.util.Calendar;

/**
 * Implement this interface if you want to be notified whenever the selected date changes.
 */
public interface OnDateSelectedListener {
    /**
     * Called whenever a new date is selected in the Picker calling this.
     * @param date The new selected date, as Calendar.
     */
    public void onDateSelected(Calendar date);
}
