package com.simplicityapks.reminderdatepicker.lib;

import android.content.Context;
import android.util.AttributeSet;

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
        return null; // TODO: get the right strings from Google Keep and construct the ArrayList
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
