package com.simplicityapks.reminderdatepicker.lib;

import android.content.Context;
import android.util.AttributeSet;

import java.util.List;

/**
 * The right PickerSpinner of the Google Keep app, to select a time within one day.
 */
public class TimeSpinner extends PickerSpinner {

    // We only need this constructor since PickerSpinner handles the others.
    public TimeSpinner(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public List<Object> getSpinnerItems() {
        return null;
    }

    @Override
    public CharSequence getFooter() {
        return null;
    }

    @Override
    public void onFooterClick() {
        // show the TimePicker here.
    }
}
