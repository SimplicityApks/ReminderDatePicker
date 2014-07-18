package com.simplicityapks.reminderdatepicker.lib;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Base view class to be inflated via xml or constructor.
 */
public class ReminderDatePicker extends LinearLayout implements AdapterView.OnItemSelectedListener{

    public static final int MODE_GOOGLE = 0;    // 0000; the standard mode
    public static final int MODE_EVERYTHING = 7;// 0111; include all features

    /**
     * Flag for {@link #setFlags(int)}. Include a yesterday and last weekday item.
     */
    public static final int FLAG_PAST = 1;      // 0001

    /**
     * Flag for {@link #setFlags(int)}. Include a month item exactly one month from today.
     */
    public static final int FLAG_MONTH = 2;     // 0010

    /**
     * Flag for {@link #setFlags(int)}. Show numeric time in the time spinner view. Note that time
     * will always be shown in dropdown.
     */
    public static final int FLAG_NUMBERS = 4;   // 0100

    /**
     * Flag for {@link #setFlags(int)}. Hide the time picker and show a button to show it.
     */
    public static final int FLAG_HIDE_TIME = 8; // 1000

    private boolean isTimeHidden = false;

    private DateSpinner dateSpinner;
    private TimeSpinner timeSpinner;

    // This listener doesn't have to be implemented, if it is null just ignore it
    private OnDateSelectedListener listener = null;

    public ReminderDatePicker(Context context) {
        this(context, null);
    }

    public ReminderDatePicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public ReminderDatePicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
        // Additional styling work is done here
    }

    private void init(Context context, AttributeSet attrs) {
        View.inflate(context, R.layout.reminder_date_picker, this);
        dateSpinner = (DateSpinner) findViewById(R.id.date_spinner);
        dateSpinner.setOnItemSelectedListener(this);

        timeSpinner = (TimeSpinner) findViewById(R.id.time_spinner);
        timeSpinner.setOnItemSelectedListener(this);
        // check if the parent activity has our dateSelectedListener, automatically enable it:
        if(context instanceof OnDateSelectedListener)
            setOnDateSelectedListener((OnDateSelectedListener) context);

        if(attrs != null) {
            // get our flags from xml, if set:
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ReminderDatePicker);
            int flags = a.getInt(R.styleable.ReminderDatePicker_flags, MODE_GOOGLE);
            setFlags(flags);
        }
        selectDefaultDate();
    }

    private void selectDefaultDate() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        if(hour < 9) hour = 9;
        else if(hour < 13) hour = 13;
        else if(hour < 17) hour = 17;
        else if(hour < 20) hour = 20;
        else {
            hour = 9;
            calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) + 1);
        }
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        setSelectedDate(calendar);
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
     * Sets the Spinners' date selection as integers considering only day.
     */
    public void setSelectedDate(int year, int month, int day) {
        dateSpinner.setSelectedDate(new GregorianCalendar(year, month, day));
    }

    /**
     * Sets the Spinners' time selection as integers considering only time.
     */
    public void setSelectedTime(int hour, int minute) {
        timeSpinner.setSelectedTime(hour, minute);
    }

    /**
     * Implement this interface if you want to be notified whenever the selected date changes.
     */
    public void setOnDateSelectedListener(OnDateSelectedListener listener) {
        this.listener = listener;
    }

    /**
     * Sets a custom listener whose onClick method will be called to create and handle the custom date picker.
     * You should call {@link #setSelectedDate(int, int, int)} when the custom picker is finished.
     * @param launchPicker An {@link android.view.View.OnClickListener} whose onClick method will be
     *                     called to show the custom date picker.
     */
    public void setCustomDatePicker(OnClickListener launchPicker) {
        dateSpinner.setCustomDatePicker(launchPicker);
    }

    /**
     * Sets a custom listener whose onClick method will be called to create and handle the custom time picker.
     * You should call {@link #setSelectedTime} when the custom picker is finished.
     * @param launchPicker An {@link android.view.View.OnClickListener} whose onClick method will be
     *                     called to show the custom time picker.
     */
    public void setCustomTimePicker(OnClickListener launchPicker) {
        timeSpinner.setCustomTimePicker(launchPicker);
    }

    /**
     * Toggles hiding the Time Spinner and replaces it with a Button.
     * @param enable True to hide the Spinner, false to show it.
     * @param useDarkTheme True if a white icon shall be used, false for a dark one.
     */
    public void setHideTime(boolean enable, final boolean useDarkTheme) {
        if(enable && !isTimeHidden) {
            // hide the time spinner and show a button to show it instead
            timeSpinner.setVisibility(GONE);
            ImageButton timeButton = new ImageButton(getContext());
            timeButton.setImageResource(useDarkTheme? R.drawable.ic_action_time_dark : R.drawable.ic_action_time_light);
            timeButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    setHideTime(false, useDarkTheme);
                }
            });
            this.addView(timeButton);
        } else if(!enable && isTimeHidden) {
            timeSpinner.setVisibility(VISIBLE);
            this.removeViewAt(2);
        }
        isTimeHidden = enable;
    }

    /**
     * Set the flags to use for the picker.
     * @param modeOrFlags A mode of ReminderDatePicker.MODE_... or multiple ReminderDatePicker.FLAG_...
     *                    combined with the | operator.
     */
    public void setFlags(int modeOrFlags) {
        // check each flag and pass it on if needed:
        setHideTime((modeOrFlags & FLAG_HIDE_TIME) == FLAG_HIDE_TIME, false);
        dateSpinner.setFlags(modeOrFlags);
        timeSpinner.setFlags(modeOrFlags);
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
