package com.simplicityapks.reminderdatepicker.lib;

import android.content.Context;
import android.util.AttributeSet;

/**
 * The left Spinner in the Google Keep app, to select a date.
 */
public class DateButton extends PopupButton {

    // We only need this constructor since the PickerSpinner handles the others.
    public DateButton(Context context, AttributeSet attrs, int defStyle){
        super(context);
    }

    @Override
    public String getLastItem() {
        return null;
    }

    @Override
    public void onLastItemClick() {
        // show the additional picker dialog.
    }
}
