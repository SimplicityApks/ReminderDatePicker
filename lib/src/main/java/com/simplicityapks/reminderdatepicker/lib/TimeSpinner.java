package com.simplicityapks.reminderdatepicker.lib;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;

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

    // This listener doesn't have to be implemented, if it is null just ignore it
    private OnTimeSelectedListener timeListener = null;

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

    @Override
    public CharSequence getFooter() {
        return getResources().getString(R.string.spinner_time_footer);
    }

    @Override
    public void onFooterClick() {
        // TODO: show the TimePicker here.
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
