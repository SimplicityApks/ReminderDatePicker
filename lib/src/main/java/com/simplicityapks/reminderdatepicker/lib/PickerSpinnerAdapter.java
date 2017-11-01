package com.simplicityapks.reminderdatepicker.lib;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Serves as Adapter for all PickerSpinner Views and deals with an extra footer and its layout, the
 * option to add temporary selections and correctly setting the view for the TwinTextItems.
 *
 * This is the layout of the items and indexes:
 *
 * INDEX:       | ITEM returned by getView() or getItem():
 * 0            | item1
 * 1            | item2
 * 2            | item3
 * ...            ...
 * getCount()-1 | footer, if available (else last item)
 * getCount()   | temporarySelection, if available (else null)
 */
public class PickerSpinnerAdapter extends ArrayAdapter<TwinTextItem> {

    // IDs for both TextViews:
    private static final int PRIMARY_TEXT_ID = android.R.id.text1;
    private static final int SECONDARY_TEXT_ID = android.R.id.text2;

    @LayoutRes
    private int itemResource = R.layout.twin_text_item;

    // Due to only having a light spinner dropdown background even on a dark app theme, we need a
    // different resource with inverse text colors on pre Lollipop devices.
    private final boolean useAlternativeResources = Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB && isActivityUsingDarkTheme();

    @LayoutRes
    private int dropDownResource = useAlternativeResources? R.layout.twin_text_dropdown_item_dark : R.layout.twin_text_dropdown_item;

    /**
     * Resource for the last item in the Spinner, which will be inflated at the last position in dropdown/dialog.
     * Set to 0 for use of normal dropDownResource
     */
    @LayoutRes
    private int footerResource = useAlternativeResources? R.layout.twin_text_footer_dark : R.layout.twin_text_footer;

    /**
     * Temporary item which is selected immediately and not shown in the dropdown menu or dialog.
     * That is why it does not increase getCount().
     */
    private TwinTextItem temporarySelection;

    /**
     * The last item, set to null to disable
     */
    private TwinTextItem footer;

    private final LayoutInflater inflater;

    private boolean showSecodaryTextInView = false;

    /**
     * Constructs a new PickerSpinnerAdapter with these params:
     * @param context The context needed by any Adapter.
     * @param items The TwinTextItems to be shown in layout.
     * @param footer The item to be shown as footer, use TwinTextItem.Simple for easy creation.
     */
    public PickerSpinnerAdapter(Context context, List<TwinTextItem> items, TwinTextItem footer) {
        super(context, R.layout.twin_text_item, items);
        this.footer = footer;
        this.inflater = LayoutInflater.from(context);
    }

    /**
     * Constructs a new PickerSpinnerAdapter with these params:
     * @param context The context needed by any Adapter.
     * @param itemResource The resource to be inflated as layout, should contain two TextViews.
     * @param dropDownResource The dropDownResource to be inflated in dropDown.
     * @param items The TwinTextItems to be shown in layout.
     * @param footerResource The resource to be inflated for the footer.
     * @param footer The item to be shown as footer, use TwinTextItem.Simple for easy creation.
     */
    public PickerSpinnerAdapter(Context context, @LayoutRes int itemResource, @LayoutRes int dropDownResource,
                                List<TwinTextItem> items, @LayoutRes int footerResource, TwinTextItem footer) {
        super(context, itemResource, items);
        this.itemResource = itemResource;
        this.dropDownResource = dropDownResource;
        this.footerResource = footerResource;
        this.footer = footer;
        this.inflater = LayoutInflater.from(context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        if (convertView == null) {
            view = inflater.inflate(itemResource, parent, false);
        } else {
            view = convertView;
        }
        if(temporarySelection != null && position == getCount()) {
            // our inflated view acts as temporaryView:
            return setTextsAndCheck(view, temporarySelection, showSecodaryTextInView);
        } else {
            // we have a normal item, set the texts:
            return setTextsAndCheck(view, getItem(position), showSecodaryTextInView);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        // depending on the position, use super method or create our own
        // we don't need to inflate a footer view if it uses the default resource, the superclass will do it:
        if(footer == null || footerResource == 0 || position != getCount()-1) {
            // we have a normal item or a footer with same resource
            return setTextsAndCheck(inflater.inflate(dropDownResource, parent, false), getItem(position), true);
        } else {
            // if we want the footer, create it:
            return setTextsAndCheck(inflater.inflate(footerResource, parent, false), footer, true);
        }
    }

    private View setTextsAndCheck(View view, TwinTextItem item, boolean showSecondaryText) {
        if (view == null) throw new IllegalArgumentException(
                "The resource passed to constructor or setItemResource()/setFooterResource() is invalid");
        final TextView primaryText = (TextView) view.findViewById(PRIMARY_TEXT_ID);
        if (primaryText == null) throw new IllegalArgumentException(
                "The resource passed to constructor or setItemResource()/setFooterResource() does not " +
                        "contain a textview with id set to android.R.id.text1"
        );
        primaryText.setText(item.getPrimaryText());
        // show a disabled state if the item is disabled
        primaryText.setEnabled(item.isEnabled());

        final TextView secondaryText = (TextView) view.findViewById(SECONDARY_TEXT_ID);
        if (secondaryText != null) {
            if (showSecondaryText)
                // Note that we're including the secondary view in the measure even if no secondary text is there.
                // The reason is that the spinner should never change its size when an item is selected,
                // which would otherwise be possible when a temporary selection (but no other item) has a secondary text.
                secondaryText.setText(item.getSecondaryText());
            else
                secondaryText.setVisibility(View.GONE);
        }
        return view;
    }

    /**
     * Push an item to be selected, but not shown in the dropdown menu. This is similar to calling
     * setText(item.toString()) if a Spinner had such a method.
     * @param item The item to select, or null to remove any temporary selection.
     */
    public void selectTemporary(TwinTextItem item) {
        this.temporarySelection = item;
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
     * Finds a spinner item by its id value (excluding any temporary selection).
     * @param id The id of the item to search.
     * @return The specified TwinTextItem, or null if no item with the given id was found.
     */
    public @Nullable TwinTextItem getItemById(int id) {
        for(int index = getCount()-1; index >= 0; index--) {
            TwinTextItem item = getItem(index);
            if(item.getId() == id)
                return item;
        }
        return null;
    }

    /**
     * Finds a spinner item's position in the data set by its id value (excluding any temporary selection).
     * @param id The id of the item to search.
     * @return The position of the specified TwinTextItem, or -1 if no item with the given id was found.
     */
    public int getItemPosition(int id) {
        for(int index = getCount()-1; index >= 0; index--) {
            TwinTextItem item = getItem(index);
            if(item.getId() == id)
                return index;
        }
        return -1;
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
     * {@inheritDoc}
     */
    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEnabled(int position) {
        return getItem(position).isEnabled();
    }

    /**
     * Checks if the Spinner will show a footer, previously set using setFooter().
     * @return True if there is a footer, false otherwise.
     */
    public boolean hasFooter() {
        return this.footer != null;
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
    public void setFooterResource(@LayoutRes int footerResource) {
        this.footerResource = footerResource;
    }

    /**
     * <p>Sets the layout resource to create the view.</p>
     *
     * @param resource the layout resource defining the view, which should contain two text views.
     * @see #getDropDownView(int, android.view.View, android.view.ViewGroup)
     */
    public void setItemResource(@LayoutRes int resource) {
        this.itemResource = resource;
    }

    /**
     * <p>Sets the layout resource to create the drop down views.</p>
     *
     * @param resource the layout resource defining the drop down views, which should contain two text views.
     * @see #getDropDownView(int, android.view.View, android.view.ViewGroup)
     */
    public void setDropDownViewResource(@LayoutRes int resource) {
        this.dropDownResource = resource;
    }

    /**
     * Enables showing the secondary text in the view. The dropdown view will always include the secondary text.
     * @param enable True to enable showing it, false to disable.
     */
    public void setShowSecondaryTextInView(boolean enable) {
        if (showSecodaryTextInView != enable) {
            showSecodaryTextInView = enable;
            notifyDataSetChanged();
        }
    }

    /**
     * Checks whether showing secondary text in view is enabled for this spinner (defaults to false).
     * @return True if this PickerSpinner shows the item's secondary text in the view as well as in
     *         dropdown, false if only in dropdown.
     */
    public boolean isShowingSecondaryTextInView() {
        return this.showSecodaryTextInView;
    }

    private boolean isActivityUsingDarkTheme() {
        TypedArray themeArray = getContext().getTheme().obtainStyledAttributes(
                new int[] {android.R.attr.textColorPrimary});
        int textColor = themeArray.getColor(0, 0);
        return brightness(textColor) > 0.5f;
    }

    /**
     * Returns the brightness component of a color int. Taken from android.graphics.Color.
     *
     * @return A value between 0.0f and 1.0f
     */
    private float brightness(int color) {
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        int V = Math.max(b, Math.max(r, g));
        return (V / 255.f);
    }
}
