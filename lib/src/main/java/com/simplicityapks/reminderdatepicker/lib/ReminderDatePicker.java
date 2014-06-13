package com.simplicityapks.reminderdatepicker.lib;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

/**
 * Base view class to be inflated via xml or constructor.
 */
public class ReminderDatePicker extends LinearLayout {

    private DateSpinner dateSpinner;

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
    }
}