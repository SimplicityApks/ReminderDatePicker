package com.simplicityapks.reminderdatepicker.lib;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.SpinnerAdapter;

import java.util.List;

/**
 * Serves as Adapter for all PickerSpinner Views and deals with the extra footer and its layout.
 */
public class PickerSpinnerAdapter<T> extends ArrayAdapter
        implements SpinnerAdapter, ListAdapter{ // TODO: check if instead should use ArrayAdapter<Object>

    /**
     * Resource for the last item in the Spinner, which will be inflated at the last position in dropdown/dialog.
     * Set to 0 for use of normal dropDownResource
     */
    private int footerResource;

    /**
     * The last item, set to null to disable custom layouts
     */
    private T footer;

    public PickerSpinnerAdapter(Context context, int dropDownResource, int footerResource) {
        super(context, dropDownResource);
        this.footerResource = footerResource;
    }

    public PickerSpinnerAdapter(Context context, int dropDownResource, List<T> objects,
                                int footerResource, T footer) {
        super(context, dropDownResource, objects);
        this.footerResource = footerResource;
        this.footer = footer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // depending on the position, use super method or create our own
        if(position != getCount()-1)
            return super.getView(position, convertView, parent);
        View lastView = null;
        if(footerResource == 0)
            lastView = super.getView(position, convertView, parent);

        return lastView;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getCount() {
        // we need one extra item which is not in the array.
        return super.getCount() + (footer==null? 0 : 1);
    }

    public void setfooter(T footer) {
        this.footer = footer;
    }
}
