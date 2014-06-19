package com.simplicityapks.reminderdatepicker.lib;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ArrayAdapter;
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

    private final int DEFAULT_ITEM_RES = android.R.layout.simple_spinner_item;
    private final int DEFAULT_DROPDOWN_RES = android.R.layout.simple_spinner_dropdown_item;
    private final int DEFAULT_FOOTER_RES = 0; // TODO: create layout file with darker background

    public PickerSpinner(Context context) {
        this(context, null);
    }

    public PickerSpinner(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PickerSpinner(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);

        // create our adapter and set it here:
        PickerSpinnerAdapter adapter = new PickerSpinnerAdapter(context, DEFAULT_ITEM_RES, getSpinnerItems(),
                DEFAULT_FOOTER_RES, getFooter());
        adapter.setDropDownViewResource(DEFAULT_DROPDOWN_RES);
        setAdapter(adapter);
    }

    @Override
    public void setAdapter(SpinnerAdapter adapter) {
        if(adapter instanceof PickerSpinnerAdapter)
            super.setAdapter(adapter);
        else throw new IllegalArgumentException(
                "adapter must extend PickerSpinnerAdapter to be used with this class");
    }

    @Override
    public void setSelection(int position) {
        if(position == getCount()-1)
            onFooterClick(); // the footer has been clicked, so don't update the selection
        else {
            // remove any previous temporary selection:
            ((PickerSpinnerAdapter)getAdapter()).selectTemporary(null);
            super.setSelection(position);
        }
    }

    /**
     * Push an item to be selected, but not shown in the dropdown menu. This is similar to calling
     * setText(item.toString()) if a Spinner had such a method.
     * @param item The item to select, or null to remove any temporary selection.
     */
    public void selectTemporary(Object item) {
        // pass on the call to the adapter:
        ((PickerSpinnerAdapter)getAdapter()).selectTemporary(item);
    }

    /**
     * Gets the default list of items to be inflated into the Spinner, will be called once on
     * initializing the Spinner. Should use lazy initialization in inherited classes.
     * @return The List of Objects whose toString() method will be called for the items, or null.
     */
    public abstract List<Object> getSpinnerItems();

    /**
     * Gets the CharSequence to be shown as footer in the drop down menu.
     * @return The footer, or null to disable showing it.
     */
    public abstract CharSequence getFooter();

    /**
     * Built-in listener for clicks on the footer. Note that the footer will not replace the
     * selection and you still need a separate OnItemSelectedListener.
     */
    public abstract void onFooterClick();
}
