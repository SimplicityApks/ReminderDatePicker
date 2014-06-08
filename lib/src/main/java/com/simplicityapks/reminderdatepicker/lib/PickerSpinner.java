package com.simplicityapks.reminderdatepicker.lib;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import java.util.List;

/**
 * Base class for both DateSpinner and TimeSpinner.
 *
 * Allows to use a custom last list item, which won't get selected on click. Instead,
 * onLastItemClick() will be called.
 */
public abstract class PickerSpinner extends Spinner {

    private final int DEFAULT_DROPDOWN_RES = android.R.layout.simple_spinner_dropdown_item;
    private final int DEFAULT_FOOTER_RES = 0; // TODO: create layout file with darker background

    public PickerSpinner(Context context) {
        this(context, null);
    }

    public PickerSpinner(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PickerSpinner(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        // create our adapter and set it here:
        setAdapter(new PickerSpinnerAdapter<Object>(context, DEFAULT_DROPDOWN_RES, getSpinnerItems(),
                DEFAULT_FOOTER_RES, getFooter()));
    }

    @Override
    public void setSelection(int position) {
        if(position == getCount()-1)
            onFooterClick(); // the footer has been clicked, so don't update the selection
        else
            super.setSelection(position);
    }

    /**
     * Gets the default list of items to be inflated into the Spinner, will be called once on
     * initializing the Spinner. Should use lazy initialization in inherited classes.
     * @return The List of Objects whose toString() method will be called for the items, or null.
     */
    public abstract List<Object> getSpinnerItems();

    /**
     * Gets the String to be shown as footer in the drop down menu.
     * @return The footer, or null to disable showing it.
     */
    public abstract String getFooter();

    /**
     * Built-in listener for clicks on the footer. Note that the footer will not replace the
     * selection and you still need a separate OnItemSelectedListener.
     */
    public abstract void onFooterClick();
}
