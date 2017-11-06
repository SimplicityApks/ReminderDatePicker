package com.simplicityapks.reminderdatepicker.lib;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.XmlRes;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SpinnerAdapter;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Base class for both DateSpinner and TimeSpinner.
 * This is a Spinner with the following additional, optional features:
 *
 * 1. A custom last list item (footer), which won't get selected on click. Instead, onFooterClick() will be called.
 * 2. Items with secondary text, due to integration with {@link com.simplicityapks.reminderdatepicker.lib.PickerSpinnerAdapter}
 * 3. Select items which are not currently in the spinner items (use {@link #selectTemporary(TwinTextItem)}.
 * 4. Dynamic and easy modifying the spinner items without having to worry about selection changes (use the ...AdapterItem...() methods)
 */
public abstract class PickerSpinner extends android.support.v7.widget.AppCompatSpinner {

    public static final String XML_ATTR_ID = "id";
    public static final String XML_ATTR_TEXT = "text";

    // Indicates that the onItemSelectedListener callback should not be passed to the listener.
    private final ArrayList<Integer> interceptSelectionCallbacks = new ArrayList<>();
    // Indicates that the selection should be restored after initialization (setSelection has not been called externally)
    private boolean restoreTemporarySelection = false;
    // Indicates that the temporary item should be reselected after an item is removed
    private boolean reselectTemporaryItem = false;

    /**
     * Construct a new PickerSpinner with the given context's theme.
     * @param context The Context the view is running in, through which it can access the current theme, resources, etc.
     */
    public PickerSpinner(Context context) {
        this(context, null);
    }

    /**
     * Construct a new PickerSpinner with the given context's theme and the supplied attribute set.
     * @param context The Context the view is running in, through which it can access the current theme, resources, etc.
     * @param attrs The attributes of the XML tag that is inflating the view.
     */
    public PickerSpinner(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * Construct a new PickerSpinner with the given context's theme, the supplied attribute set, and default style.
     * @param context The Context the view is running in, through which it can access the current theme, resources, etc.
     * @param attrs The attributes of the XML tag that is inflating the view.
     * @param defStyle The default style to apply to this view. If 0, no style will be applied (beyond
     *                 what is included in the theme). This may either be an attribute resource, whose
     *                 value will be retrieved from the current theme, or an explicit style resource.
     */
    public PickerSpinner(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);
        initAdapter(context);
    }

    protected void initAdapter(Context context) {
        CharSequence footer = getFooter();
        TwinTextItem footerItem = footer == null? null : new TwinTextItem.Simple(footer, null);
        // create our simple adapter with default layouts and set it here:
        PickerSpinnerAdapter adapter = new PickerSpinnerAdapter(context, getSpinnerItems(), footerItem);
        setAdapter(adapter);
    }

    @NonNull
    @Override
    public Parcelable onSaveInstanceState() {
        // our temporary selection will not be saved
        if(getSelectedItemPosition() == getAdapter().getCount()) {
            Bundle state = new Bundle();
            state.putParcelable("superState", super.onSaveInstanceState());
            // save the TwinTextItem using its toString() method
            state.putString("temporaryItem", getSelectedItem().toString());
            return state;
        }
        else return super.onSaveInstanceState();
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if(state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            super.onRestoreInstanceState(bundle.getParcelable("superState"));
            final String tempItem = bundle.getString("temporaryItem");
            restoreTemporarySelection(tempItem);
        }
        else super.onRestoreInstanceState(state);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        // When going from state gone to visible with a temporary item selected, but the array has
        // changed (by toggling FLAG_MORE_TIME), the position is somehow reset by the system, so we
        // need to reselect the temporary item (even if it was already selected).
        // This is merely a workaround as I can't find a better solution.
        if(visibility == VISIBLE) {
            reselectTemporaryItem = false;
            PickerSpinnerAdapter adapter = (PickerSpinnerAdapter) getAdapter();
            int count = adapter.getCount();
            // check whether we have the temporary item selected
            if(getSelectedItemPosition() == count) {
                // get the temp item from the adapter to reselect it later:
                TwinTextItem tempItem = null;
                try {
                    tempItem = adapter.getItem(count);
                } catch (IndexOutOfBoundsException e) {
                    Log.d("PickerSpinner", "SetVisibility: Couldn't get temporary item from adapter, aborting workaround");
                }
                // now reselect the temporary item
                if(tempItem != null) {
                    selectTemporary(tempItem);
                }
            }
        }

    }


    /**
     * Sets the Adapter used to provide the data which backs this Spinner. Needs to be an {@link com.simplicityapks.reminderdatepicker.lib.PickerSpinnerAdapter}
     * to be used with this class. Note that a PickerSpinner automatically creates its own adapter
     * so you should not need to call this method.
     * @param adapter The PickerSpinnerAdapter to be used.
     * @throws IllegalArgumentException If adapter is not a PickerSpinnerAdapter.
     */
    @Override
    public void setAdapter(SpinnerAdapter adapter) {
        if(adapter instanceof PickerSpinnerAdapter)
            super.setAdapter(adapter);
        else throw new IllegalArgumentException(
                "adapter must extend PickerSpinnerAdapter to be used with this class");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSelection(int position) {
        PickerSpinnerAdapter adapter = (PickerSpinnerAdapter) getAdapter();
        if(position == adapter.getCount()-1 && adapter.hasFooter())
            onFooterClick(); // the footer has been clicked, so don't update the selection
        else {
            // remove any previous temporary selection:
            ((PickerSpinnerAdapter)getAdapter()).selectTemporary(null);
            reselectTemporaryItem = false;
            restoreTemporarySelection = false;
            // check that the selection goes through:
            interceptSelectionCallbacks.clear();
            super.setSelection(position);
            super.setSelection(position, false);
        }
    }

    /**
     * Equivalent to {@link #setSelection(int)}, but without calling any onItemSelectedListeners or
     * checking for footer clicks.
     */
    private void setSelectionQuietly(int position) {
        // intercept the callback here:
        interceptSelectionCallbacks.add(position);
        superSetSelection(position);
    }

    private void superSetSelection(int position) {
        super.setSelection(position, false); // No idea why both setSelections are needed but it only works with both
        super.setSelection(position);
    }

    /**
     * Push an item to be selected, but not shown in the dropdown menu. This is similar to calling
     * setText(item.toString()) if a Spinner had such a method.
     * @param item The item to select, or null to remove any temporary selection.
     */
    public void selectTemporary(TwinTextItem item) {
        // if we just want to clear the selection:
        if(item == null) {
            setSelection(getLastItemPosition());
            // the call is passed on to the adapter in setSelection.
            return;
        }
        PickerSpinnerAdapter adapter = (PickerSpinnerAdapter) getAdapter();
        // pass on the call to the adapter (just stores the item):
        adapter.selectTemporary(item);
        final int tempItemPosition = adapter.getCount();
        if(getSelectedItemPosition() == tempItemPosition) {
            // this is quite a hack, first reset the position to 0 but intercept the callback,
            // then redo the selection:
            setSelectionQuietly(0);
        }
        super.setSelection(tempItemPosition);
        // during initialization the system might check our selected position and reset it,
        // thus we need to check after the message queue has been settled
        if (!restoreTemporarySelection) {
            restoreTemporarySelection = true;
            post(new Runnable() {
                @Override
                public void run() {
                    if (restoreTemporarySelection) {
                        restoreTemporarySelection = false;
                        reselectTemporaryItem = false;
                        final int tempItemPosition = getAdapter().getCount();
                        if (getSelectedItemPosition() != tempItemPosition)
                            superSetSelection(tempItemPosition);
                    }
                }
            });
        }
    }

    @Override
    public void setOnItemSelectedListener(final OnItemSelectedListener listener) {
        super.setOnItemSelectedListener(
                new OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if (reselectTemporaryItem) {
                            reselectTemporaryItem = false;
                            final int tempItemPosition = getAdapter().getCount();
                            if (position != tempItemPosition)
                                setSelectionQuietly(tempItemPosition);
                        }
                        if (interceptSelectionCallbacks.contains(position)) {
                            interceptSelectionCallbacks.remove((Integer) position);
                        }
                        // sometimes during rotation or initialization onItemSelected will be called with the footer selected, catch that
                        else if (!(((PickerSpinnerAdapter) getAdapter()).hasFooter()
                                && position == getLastItemPosition() + 1))
                            listener.onItemSelected(parent, view, position, id);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        listener.onNothingSelected(parent);
                    }
                }
        );
    }

    /**
     * Gets the position of the last item in the dataset, after which the footer and temporary selection have their index.
     * @return The last selectable position.
     */
    public int getLastItemPosition() {
        PickerSpinnerAdapter adapter = (PickerSpinnerAdapter) getAdapter();
        return adapter.getCount() - (adapter.hasFooter()? 2 : 1);
    }

    /**
     * Finds a spinner adapter item by its id value (excluding any temporary selection).
     * @param id The id of the item to search.
     * @return The specified TwinTextItem, or null if no item with the given id was found.
     */
    public @Nullable TwinTextItem getAdapterItemById(int id) {
        return ((PickerSpinnerAdapter) getAdapter()).getItemById(id);
    }

    /**
     * Finds a spinner item's position in the data set by its id value (excluding any temporary selection).
     * @param id The id of the item to search.
     * @return The position of the specified TwinTextItem, or -1 if no item with the given id was found.
     */
    public int getAdapterItemPosition(int id) {
        return ((PickerSpinnerAdapter) getAdapter()).getItemPosition(id);
    }

    /**
     * Adds the item to the adapter's data set and takes care of handling selection changes.
     * Always call this method instead of getAdapter().add().
     * @param item The item to insert.
     */
    public void addAdapterItem(TwinTextItem item) {
        insertAdapterItem(item, getLastItemPosition()+1);
    }

    /**
     * Inserts the item at the specified index into the adapter's data set and takes care of handling selection changes.
     * Always call this method instead of getAdapter().insert().
     * @param item The item to insert.
     * @param index The index where it'll be at.
     */
    public void insertAdapterItem(TwinTextItem item, int index) {
        int selection = getSelectedItemPosition();
        Object selectedItem = getSelectedItem();
        ((PickerSpinnerAdapter) getAdapter()).insert(item, index);
        // select the new item if there was an equal temporary item selected
        if(selectedItem.equals(item))
            setSelectionQuietly(index);
        // otherwise keep track when inserting above the selection
        else if(index <= selection)
            setSelectionQuietly(selection+1);
    }

    /**
     * Removes the specified item from the adapter and takes care of handling selection changes.
     * Always call this method instead of getAdapter().remove().
     * Note that if you remove the selected item here, it will just reselect the next one instead of
     * creating a temporary item containing the current selection.
     * @param index The index of the item to be removed.
     */
    public void removeAdapterItemAt(int index) {
        PickerSpinnerAdapter adapter = (PickerSpinnerAdapter) getAdapter();
        int count = adapter.getCount();
        int selection = getSelectedItemPosition();

        // check which item will be removed:
        if(index == count) // temporary selection
            selectTemporary(null);
        else if (index == count-1 && adapter.hasFooter()) { // footer
            if(selection == count)
                setSelectionQuietly(selection - 1);
            adapter.setFooter(null);
        } else { // a normal item
            // keep the right selection in either of these cases:
            if(index == selection) { // we delete the selected item and
                if(index == getLastItemPosition())  // it is the last real item
                    setSelection(selection - 1);
                else {
                    // we need to reselect the current item
                    // (this is not guaranteed to fire a selection callback when multiple operations
                    // modify the dataset, so it is a lot better to first select the item you want
                    // to have selected, best by overriding this method in your subclass).
                    setSelectionQuietly(index==0 && count>1? 1 : 0);
                    setSelection(selection);
                }
            }
            else if(index < selection && selection!=count) // we remove an item above it
                setSelectionQuietly(selection - 1);
            adapter.remove(adapter.getItem(index));
            if(selection == count) { // we have a temporary item selected
                reselectTemporaryItem = true;
                setSelectionQuietly(selection - 1);
            }
        }
    }

    /**
     * Removes the specified item(s) from the adapter and takes care of handling selection changes.
     * Always call this method instead of getAdapter().remove().
     * @param id The id of the item(s) to be removed. All items with this id will be removed.
     * @return True if one or more items with the specified id were found and removed, false otherwise.
     */
    public boolean removeAdapterItemById(int id) {
        PickerSpinnerAdapter adapter = (PickerSpinnerAdapter) getAdapter();
        boolean result = false;
        for(int index = adapter.getCount()-1; index >= 0; index--) {
            TwinTextItem item = adapter.getItem(index);
            if(item.getId() == id) {
                removeAdapterItemAt(index);
                result = true;
            }
        }
        return result;
    }

    /**
     * Gets the default list of items to be inflated into the Spinner, will be called once on
     * initializing the Spinner. Should use lazy initialization in inherited classes.
     * @return The List of Objects whose toString() method will be called for the items, or null.
     */
    public abstract List<TwinTextItem> getSpinnerItems();

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

    /**
     * Called to restore a previously saved temporary selection. The given codeString has been saved
     * using the toString() method on the TwinTextItem. This method should ideally only call
     * {@link #selectTemporary(TwinTextItem)} with a new TwinTextItem parsed from the codeString.
     * @param codeString The raw String saved from the item's toString() method.
     */
    protected abstract void restoreTemporarySelection(String codeString);

    /**
     *
     */
    protected ArrayList<TwinTextItem> getItemsFromXml(@XmlRes int xmlResource)
            throws XmlPullParserException, IOException {
        final Resources res = getResources();
        XmlResourceParser parser = res.getXml(xmlResource);
        ArrayList<TwinTextItem> items = new ArrayList<>();

        int eventType;
        while((eventType = parser.next()) != XmlPullParser.END_DOCUMENT) {
            if(eventType == XmlPullParser.START_TAG) {
                // call our subclass to parse the correct item
                TwinTextItem item = parseItemFromXmlTag(parser);
                if(item != null)
                    items.add(item);
            }
        }

        return items;
    }

    /**
     * Override this method in your spinner, returning your specific item parsed from the given xml parser at the current tag.
     * Do not call parser.next() in here!
     */
    protected @Nullable TwinTextItem parseItemFromXmlTag(@NonNull XmlResourceParser parser) {
        return null;
    }
}
