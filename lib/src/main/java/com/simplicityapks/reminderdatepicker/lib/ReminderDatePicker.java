package com.simplicityapks.reminderdatepicker.lib;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;

import java.util.Calendar;

/**
 * Base view class to be inflated via xml or constructor.
 */
public class ReminderDatePicker extends LinearLayout implements AdapterView.OnItemSelectedListener{

    private DateSpinner dateSpinner;
    private TimeSpinner timeSpinner;

    // This listener doesn't have to be implemented, if it is null just ignore it
    private OnDateSelectedListener listener = null;

    public ReminderDatePicker(Context context) {
        this(context, null);
    }

    public ReminderDatePicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public ReminderDatePicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
        // Additional styling work is done here
    }

    private void init(Context context) {
        View.inflate(context, R.layout.reminder_date_picker, this);
        dateSpinner = (DateSpinner) findViewById(R.id.date_spinner);
        dateSpinner.setOnItemSelectedListener(this);

        timeSpinner = (TimeSpinner) findViewById(R.id.time_spinner);
        timeSpinner.setOnItemSelectedListener(this);
        // check if the parent activity has our dateSelectedListener, automatically enable it:
        if(context instanceof OnDateSelectedListener)
            setOnDateSelectedListener((OnDateSelectedListener) context);
    }

    /**
     * Gets the currently selected date (that the Spinners are showing)
     * @return The selected date as Calendar, or null if there is none.
     */
    public Calendar getSelectedDate() {
        Calendar result = dateSpinner.getSelectedDate();
        Calendar time = timeSpinner.getSelectedTime();
        if(result!=null && time!=null) {
            result.set(Calendar.HOUR_OF_DAY, time.get(Calendar.HOUR_OF_DAY));
            result.set(Calendar.MINUTE, time.get(Calendar.MINUTE));
            return result;
        }
        else return null;
    }

    /**
     * Sets the Spinners' selection as date considering both time and day.
     * @param date The date to be selected.
     */
    public void setSelectedDate(Calendar date) {
        if(date!=null) {
            dateSpinner.setSelectedDate(date);
            timeSpinner.setSelectedTime(date.get(Calendar.HOUR_OF_DAY), date.get(Calendar.MINUTE));
        }
    }

    /**
     * Implement this interface if you want to be notified whenever the selected date changes.
     */
    public void setOnDateSelectedListener(OnDateSelectedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // An item has been selected in one of our child spinners, so get the selected Date and call the listeners
        if(listener != null) {
            listener.onDateSelected(getSelectedDate());
        }
    }

    // unused
    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }
}
