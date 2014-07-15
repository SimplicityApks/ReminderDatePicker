package com.simplicityapks.reminderdatepicker.lib;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import com.sleepbot.datetimepicker.time.RadialPickerLayout;
import com.sleepbot.datetimepicker.time.TimePickerDialog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * The right PickerSpinner of the Google Keep app, to select a time within one day.
 */
public class TimeSpinner extends PickerSpinner implements AdapterView.OnItemSelectedListener {

    /**
     * Implement this interface if you want to be notified whenever the selected time changes.
     */
    public interface OnTimeSelectedListener {
        public void onTimeSelected(int hour, int minute);
    }

    // These listeners don't have to be implemented, if null just ignore
    private OnTimeSelectedListener timeListener = null;
    private OnClickListener customTimePicker = null;

    // The default time picker dialog to show when the custom one is null:
    private final TimePickerDialog timePickerDialog;
    private FragmentManager fragmentManager;

    public TimeSpinner(Context context){
        this(context, null, 0);
    }

    public TimeSpinner(Context context, AttributeSet attrs){
        this(context, attrs, 0);
    }

    public TimeSpinner(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // check if the parent activity has our timeSelectedListener, automatically enable it:
        if(context instanceof OnTimeSelectedListener)
            setOnTimeSelectedListener((OnTimeSelectedListener) context);
        setOnItemSelectedListener(this);

        final Calendar calendar = Calendar.getInstance();
        // create the dialog to show later:
        timePickerDialog = TimePickerDialog.newInstance(
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(RadialPickerLayout radialPickerLayout, int hour, int minute) {
                        setSelectedTime(hour, minute);
                    }
                },
                calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE),
                DateFormat.is24HourFormat(context), hasVibratePermission(context));

        // get the FragmentManager:
        try{
            fragmentManager = ((FragmentActivity) context).getSupportFragmentManager();
        } catch (ClassCastException e) {
            Log.d(getClass().getSimpleName(), "Can't get fragment manager from context");
        }
    }

    private boolean hasVibratePermission(Context context) {
        final String permission = "android.permission.VIBRATE";
        final int res = context.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    @Override
    public List<TwinTextItem> getSpinnerItems() {
        final Resources res = getResources();
        ArrayList<TwinTextItem> items = new ArrayList<TwinTextItem>(4);
        // Morning item:
        items.add(new TimeItem(res.getString(R.string.time_morning), 9, 0));
        // Afternoon item:
        items.add(new TimeItem(res.getString(R.string.time_afternoon), 13, 0));
        // Evening item:
        items.add(new TimeItem(res.getString(R.string.time_evening), 17, 0));
        // Night item:
        items.add(new TimeItem(res.getString(R.string.time_night), 20, 0));
        return items;
    }

    /**
     * Gets the currently selected time (that the Spinner is showing)
     * @return The selected time as Calendar, or null if there is none.
     */
    public Calendar getSelectedTime() {
        final TimeItem selectedItem = (TimeItem) getSelectedItem();
        if(selectedItem == null)
            return null;
        else
            return selectedItem.getTime();
    }

    /**
     * Sets the Spinner's selection as time in hour and minute. If the time was not in the possible
     * selections, a temporary item is created and passed to selectTemporary().
     * @param hour The hour to be selected.
     * @param minute The minute in the hour.
     */
    public void setSelectedTime(int hour, int minute) {
        final int count = getAdapter().getCount() - 1;
        int itemPosition = -1;
        for(int i=0; i<count; i++) {
            final TimeItem item = ((TimeItem) getAdapter().getItem(i));
            if(item.getHour() == hour && item.getMinute() == minute) {
                itemPosition = i;
                break;
            }
        }
        if(itemPosition >= 0)
            setSelection(itemPosition);
        else {
            // create a temporary TimeItem to select:
            selectTemporary(new TimeItem(hour+":"+minute, hour, minute));
        }
    }

    /**
     * Implement this interface if you want to be notified whenever the selected time changes.
     */
    public void setOnTimeSelectedListener(OnTimeSelectedListener listener) {
        this.timeListener = listener;
    }

    /**
     * Sets a custom listener whose onClick method will be called to create and handle the custom time picker.
     * You should call {@link #setSelectedTime} when the custom picker is finished.
     * @param launchPicker An {@link android.view.View.OnClickListener} whose onClick method will be
     *                     called to show the custom time picker.
     */
    public void setCustomTimePicker(OnClickListener launchPicker) {
        this.customTimePicker = launchPicker;
    }

    /**
     * Sets to show numeric time in the view. Note that time will always be shown in dropdown.
     * @param enable True to enable, false to disable numeric mode.
     */
    public void setShowNumbersInView(boolean enable) {
        ((PickerSpinnerAdapter) getAdapter()).setShowSecodaryTextInView(enable);
    }

    /**
     * Set the flags to use for this time spinner.
     * @param modeOrFlags A mode of ReminderDatePicker.MODE_... or multiple ReminderDatePicker.FLAG_...
     *                    combined with the | operator.
     */
    public void setFlags(int modeOrFlags) {
        setShowNumbersInView((modeOrFlags & ReminderDatePicker.FLAG_NUMBERS) == ReminderDatePicker.FLAG_NUMBERS);
    }

    @Override
    public CharSequence getFooter() {
        return getResources().getString(R.string.spinner_time_footer);
    }

    @Override
    public void onFooterClick() {
        if (customTimePicker == null) {
            timePickerDialog.show(fragmentManager, "TimePickerDialog");
        } else {
            customTimePicker.onClick(this);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if(timeListener != null) {
            TimeItem selected = (TimeItem) getItemAtPosition(position);
            if(selected != null)
                timeListener.onTimeSelected(selected.getHour(), selected.getMinute());
        }
    }

    // unused
    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }
}
