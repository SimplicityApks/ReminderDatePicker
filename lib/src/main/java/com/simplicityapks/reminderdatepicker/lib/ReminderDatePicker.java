package com.simplicityapks.reminderdatepicker.lib;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;

/**
 * Base view class to be inflated via xml or constructor.
 */
public class ReminderDatePicker extends LinearLayout implements AdapterView.OnItemSelectedListener{

    private DateSpinner dateSpinner;

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
        LayoutInflater.from(context).inflate(R.layout.reminder_date_picker, this);
        dateSpinner = (DateSpinner) findViewById(R.id.date_spinner);
        dateSpinner.setOnItemSelectedListener(this);
        // check if the parent activity has our dateSelectedListener, automatically enable it:
        if(context instanceof OnDateSelectedListener)
            setOnDateSelectedListener((OnDateSelectedListener) context);
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
        if(listener == null) return;
        // TODO: get date and call
    }

    // unused
    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }
}