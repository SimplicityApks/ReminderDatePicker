package com.simplicityapks.reminderdatepicker.lib;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import com.fourmob.datetimepicker.date.DatePickerDialog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * The left PickerSpinner in the Google Keep app, to select a date.
 */
public class DateSpinner extends PickerSpinner implements AdapterView.OnItemSelectedListener {

    // These listeners don't have to be implemented, if null just ignore
    private OnDateSelectedListener dateListener = null;
    private OnClickListener customDatePicker = null;

    // The default DatePicker dialog to show if customDatePicker has not been set
    private final DatePickerDialog datePickerDialog;
    private FragmentManager fragmentManager;

    public DateSpinner(Context context){
        this(context, null, 0);
    }

    public DateSpinner(Context context, AttributeSet attrs){
        this(context, attrs, 0);
    }

    public DateSpinner(Context context, AttributeSet attrs, int defStyle){
        super(context, attrs, defStyle);
        // check if the parent activity has our dateSelectedListener, automatically enable it:
        if(context instanceof OnDateSelectedListener)
            setOnDateSelectedListener((OnDateSelectedListener) context);
        setOnItemSelectedListener(this);

        final Calendar calendar = Calendar.getInstance();
        // create the dialog:
        datePickerDialog = DatePickerDialog.newInstance(
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePickerDialog datePickerDialog, int year, int month, int day) {
                        setSelectedDate(new GregorianCalendar(year, month, day));
                    }
                },
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH), hasVibratePermission(context));

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
        final Calendar date = Calendar.getInstance();
        ArrayList<TwinTextItem> items = new ArrayList<TwinTextItem>(3);
        // today item:
        items.add(new DateItem(res.getString(R.string.date_today), date));
        // tomorrow item:
        date.add(Calendar.DAY_OF_YEAR, 1);
        items.add(new DateItem(res.getString(R.string.date_tomorrow), date));
        // next weekday item:
        date.add(Calendar.DAY_OF_YEAR, 6);
        items.add(new DateItem(getNextWeekDay(date.get(Calendar.DAY_OF_WEEK)), date));
        return items;
    }

    private String getNextWeekDay(int weekDay) {
        // zero-indexed, because the weekday array is zero-indexed
        weekDay -= 1;
        return getResources().getStringArray(R.array.next_weekdays) [weekDay];
    }

    /**
     * Gets the currently selected date (that the Spinner is showing)
     * @return The selected date as Calendar, or null if there is none.
     */
    public Calendar getSelectedDate() {
        final DateItem selectedItem = (DateItem) getSelectedItem();
        if(selectedItem == null)
            return null;
        else
            return selectedItem.getDate();
    }

    /**
     * Sets the Spinner's selection as date. If the date was not in the possible selections, a temporary
     * item is created and passed to selectTemporary().
     * @param date The date to be selected.
     */
    public void setSelectedDate(Calendar date) {
        final int count = getAdapter().getCount() - 1;
        int itemPosition = -1;
        for(int i=0; i<count; i++) {
            if(getAdapter().getItem(i).equals(date)) { // because DateItem deeply compares to calendar
                itemPosition = i;
                break;
            }
        }
        if(itemPosition >= 0)
            setSelection(itemPosition);
        else {
            final int MILLIS_IN_DAY = 1000*60*60*24;
            final int dateDifference = ((int) date.getTimeInMillis()/MILLIS_IN_DAY)
                    - ((int)Calendar.getInstance().getTimeInMillis()/MILLIS_IN_DAY);
            if(dateDifference>0 && dateDifference<=7) { // if the date is within the next week:
                // we need to construct a temporary DateItem to select:
                final int day = date.get(Calendar.DAY_OF_WEEK);
                selectTemporary(new DateItem(getNextWeekDay(day), date));
            } else {
                // we need to show the date as a full text, using the current DateFormat:
                selectTemporary(new DateItem(getDateFormat().format(date.getTime()), date));
            }
        }
    }

    private java.text.DateFormat savedFormat;
    private java.text.DateFormat getDateFormat() {
        if(savedFormat == null)
            savedFormat = android.text.format.DateFormat.getDateFormat(getContext().getApplicationContext());
        return savedFormat;
    }

    /**
     * Implement this interface if you want to be notified whenever the selected date changes.
     */
    public void setOnDateSelectedListener(OnDateSelectedListener listener) {
        this.dateListener = listener;
    }

    /**
     * Sets a custom listener whose onClick method will be called to create and handle the custom date picker.
     * You should call {@link #setSelectedDate} when the custom picker is finished.
     * @param launchPicker An {@link android.view.View.OnClickListener} whose onClick method will be
     *                     called to show the custom date picker.
     */
    public void setCustomDatePicker(OnClickListener launchPicker) {
        this.customDatePicker = launchPicker;
    }


    /**
     * Set the flags to use for this date spinner.
     * @param modeOrFlags Either a mode of ReminderDatePicker.MODE_... or multiple ReminderDatePicker.FLAG_...
     *                    combined with the | operator.
     */
    public void setFlags(int modeOrFlags) {
        if ((modeOrFlags & ReminderDatePicker.FLAG_PAST) == ReminderDatePicker.FLAG_PAST) {

        }
    }

    @Override
    public CharSequence getFooter() {
        return getResources().getString(R.string.spinner_date_footer);
    }

    @Override
    public void onFooterClick() {
        if (customDatePicker == null) {
            datePickerDialog.show(fragmentManager, "DatePickerDialog");
        } else {
            customDatePicker.onClick(this);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if(dateListener != null)
            dateListener.onDateSelected(getSelectedDate());
    }

    // unused
    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }
}
