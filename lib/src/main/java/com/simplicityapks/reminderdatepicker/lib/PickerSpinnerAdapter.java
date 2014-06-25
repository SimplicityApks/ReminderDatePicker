package com.simplicityapks.reminderdatepicker.lib;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Serves as Adapter for all PickerSpinner Views and deals with an extra footer and its layout, the
 * option to add temporary selections and correctly setting the view for the TwinTextItems.
 */
public class PickerSpinnerAdapter extends ArrayAdapter<TwinTextItem>{

    // IDs for both TextViews:
    private final int PRIMARY_TEXT_ID = android.R.id.text1;
    private final int SECONDARY_TEXT_ID = android.R.id.text2;

    /**
     * Resource for the last item in the Spinner, which will be inflated at the last position in dropdown/dialog.
     * Set to 0 for use of normal dropDownResource
     */
    private int footerResource = 0;

    /**
     * Temporary item which is selected immediately and not shown in the dropdown menu or dialog.
     * That is why it does not increase getCount().
     */
    private TwinTextItem temporarySelection;

    /**
     * The last item, set to null to disable
     */
    private TwinTextItem footer;

    /**
     * Constructs a new PickerSpinnerAdapter with these params:
     * @param context The context needed by any Adapter.
     * @param resource The resource to be inflated as layout, should contain two TextViews
     * @param footerResource The resource to be inflated for the footer.
     */
    public PickerSpinnerAdapter(Context context, int resource, int footerResource) {
        super(context, resource);
        this.footerResource = footerResource;
    }

    /**
     * Constructs a new PickerSpinnerAdapter with these params:
     * @param context The context needed by any Adapter.
     * @param resource The resource to be inflated as layout, should contain two TextViews
     * @param items The TwinTextItems to be shown in layout.
     * @param footerResource The resource to be inflated for the footer.
     * @param footer The item to be shown as footer, use TwinTextItem.Simple for easy creation.
     */
    public PickerSpinnerAdapter(Context context, int resource, List<TwinTextItem> items,
                                int footerResource, TwinTextItem footer) {
        super(context, resource, items);
        this.footerResource = footerResource;
        this.footer = footer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(0, convertView, parent);
        if(temporarySelection != null && position == getCount()) {
            // our inflated view acts as temporaryView:
            final TextView primary = (TextView) view.findViewById(PRIMARY_TEXT_ID);
            primary.setText(temporarySelection.getPrimaryText());
            final TextView secondary = (TextView) view.findViewById(SECONDARY_TEXT_ID);
            secondary.setText(temporarySelection.getSecondaryText());
            return view;
        }
        final TextView primary = (TextView) view.findViewById(PRIMARY_TEXT_ID);
        primary.setText(getItem(position).getPrimaryText());
        final TextView secondary = (TextView) view.findViewById(SECONDARY_TEXT_ID);
        secondary.setText(getItem(position).getSecondaryText());
        // depending on the position, use super method or create our own
        if(footer != null && position == getCount()-1)
            Log.d(getClass().getSimpleName(), "Strange call to getView at footer position: "+position);
        return super.getView(position, convertView, parent);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        // depending on the position, use super method or create our own
        // we don't need to inflate a footer view if it uses the default resource, the superclass will do it:
        if(footer == null || footerResource == 0 || position != getCount()-1) {
            View dropDown = super.getDropDownView(position, convertView, parent);
            final TextView primary = (TextView) dropDown.findViewById(PRIMARY_TEXT_ID);
            primary.setText(getItem(position).getPrimaryText());
            final TextView secondary = (TextView) dropDown.findViewById(SECONDARY_TEXT_ID);
            secondary.setText(getItem(position).getSecondaryText());
            return dropDown;
        }

        // if we want the footer, create it:
        View footerView = LayoutInflater.from(getContext()).inflate(footerResource, parent);
        if(footerView == null) throw new IllegalArgumentException(
                "The footer resource passed to constructor or setFooterResource() is invalid");
        final TextView primaryText = (TextView)footerView.findViewById(PRIMARY_TEXT_ID);
        if(primaryText == null) throw new IllegalArgumentException(
                "The footer resource passed to constructor or setFooterResource() does not contain" +
                        " a textview with id set to android.R.id.text1");
        primaryText.setText(footer.getPrimaryText());
        final TextView secondaryText = (TextView)footerView.findViewById(SECONDARY_TEXT_ID);
        if(secondaryText != null)
            secondaryText.setText(footer.getSecondaryText());
        return footerView;
    }

    /**
     * Push an item to be selected, but not shown in the dropdown menu. This is similar to calling
     * setText(item.toString()) if a Spinner had such a method.
     * @param item The item to select, or null to remove any temporary selection.
     */
    public void selectTemporary(TwinTextItem item) {
        this.temporarySelection = item;
        notifyDataSetChanged();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TwinTextItem getItem(int position) {
        if(temporarySelection != null && position == getCount())
            return temporarySelection;
        else if(footer != null && position == getCount()-1)
            return footer;
        else
            return super.getItem(position);
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
    public void setFooter(TwinTextItem footer) {
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
