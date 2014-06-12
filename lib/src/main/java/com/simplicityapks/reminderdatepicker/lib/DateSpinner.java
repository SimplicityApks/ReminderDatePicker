package com.simplicityapks.reminderdatepicker.lib;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * The left PickerSpinner in the Google Keep app, to select a date.
 */
public class DateSpinner extends PickerSpinner {

    // We only need this constructor since PickerSpinner handles the others.
    public DateSpinner(Context context, AttributeSet attrs, int defStyle){
        super(context);
    }

    @Override
    public List<Object> getSpinnerItems() {
        final Resources res = getResources();
        final Calendar date = Calendar.getInstance();
        ArrayList<Object> items = new ArrayList<Object>(3);
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
     * Gets the currently selected Date (that the Spinner is showing)
     * @return The selected Date as Calendar, or null if there is none.
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
            if(((DateItem) getAdapter().getItem(i)).getDate().equals(date)) {
                itemPosition = i;
                break;
            }
        }
        if(itemPosition >= 0)
            setSelection(itemPosition);
        else {
            final int dateDifference = (int) ((date.getTimeInMillis() - Calendar.getInstance().getTimeInMillis())
                    / (1000*60*60*24));
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

    @Override
    public String getFooter() {
        return getResources().getString(R.string.spinner_date_footer);
    }

    @Override
    public void onFooterClick() {
        // show the date picker
    }
}