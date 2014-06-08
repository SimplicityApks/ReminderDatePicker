package com.simplicityapks.reminderdatepicker.lib;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

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
    private int footerResource = 0;

    /**
     * The last item, set to null to disable
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
        if(footer == null || position != getCount()-1)
            return super.getView(position, convertView, parent);

        View footerView;
        if(footerResource == 0)
            footerView = super.getView(position, convertView, parent);
        else {
            footerView = LayoutInflater.from(getContext()).inflate(footerResource, parent);
            if(footerView == null) throw new IllegalArgumentException(
                    "The footer resource passed to constructor or setFooterResource() is invalid");
        }
        final TextView textView = (TextView)footerView.findViewById(android.R.id.text1);
        if(textView == null) throw new IllegalArgumentException(
                "The footer resource passed to constructor or setFooterResource() does not contain" +
                        " a textview with id set to android.R.id.text1");
        textView.setText(footer.toString());
        return footerView;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getCount() {
        // we need one extra item which is not in the array.
        return super.getCount() + (footer==null? 0 : 1);
    }

    /**
     * Sets the text to be shown in the footer.
     * @param footer An Object whose toString() will be the footer text, or null to disable the footer.
     */
    public void setFooter(T footer) {
        this.footer = footer;
    }

    /**
     * Sets the layout resource to be inflated as footer. It should contain a TextView with id set
     * to android.R.id.text1, where the text will be added.
     * @param footerResource A valid xml layout resource, or 0 to use dropDownResource instead.
     */
    public void setFooterResource(int footerResource) {
        this.footerResource = footerResource;
    }
}
