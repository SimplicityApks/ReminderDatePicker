package com.simplicityapks.reminderdatepicker.lib;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import com.sleepbot.datetimepicker.time.RadialPickerLayout;
import com.sleepbot.datetimepicker.time.TimePickerDialog;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * The right PickerSpinner of the Google Keep app, to select a time within one day.
 */
public class TimeSpinner extends PickerSpinner implements AdapterView.OnItemSelectedListener {

    public static final String XML_TAG_TIMEITEM = "TimeItem";

    public static final String XML_ATTR_ABSHOUR = "absHour";
    public static final String XML_ATTR_ABSMINUTE= "absMinute";

    public static final String XML_ATTR_RELHOUR = "relHour";
    public static final String XML_ATTR_RELMINUTE = "relMinute";

    /**
     * Implement this interface if you want to be notified whenever the selected time changes.
     */
    public interface OnTimeSelectedListener {
        void onTimeSelected(int hour, int minute);
    }

    // These listeners don't have to be implemented, if null just ignore
    private OnTimeSelectedListener timeListener = null;
    private OnClickListener customTimePicker = null;

    // The default time picker dialog to show when the custom one is null:
    private TimePickerDialog timePickerDialog;
    private FragmentManager fragmentManager;

    private boolean showMoreTimeItems = false;

    // The time format used to convert Calendars into displayable Strings:
    private java.text.DateFormat timeFormat = null;

    private int lastSelectedHour = -1;
    private int lastSelectedMinute = -1;

    /**
     * Construct a new TimeSpinner with the given context's theme.
     * @param context The Context the view is running in, through which it can access the current theme, resources, etc.
     */
    public TimeSpinner(Context context){
        this(context, null, 0);
    }

    /**
     * Construct a new TimeSpinner with the given context's theme and the supplied attribute set.
     * @param context The Context the view is running in, through which it can access the current theme, resources, etc.
     * @param attrs The attributes of the XML tag that is inflating the view. May contain a flags attribute.
     */
    public TimeSpinner(Context context, AttributeSet attrs){
        this(context, attrs, 0);
    }

    /**
     * Construct a new TimeSpinner with the given context's theme, the supplied attribute set, and default style.
     * @param context The Context the view is running in, through which it can access the current theme, resources, etc.
     * @param attrs The attributes of the XML tag that is inflating the view. May contain a flags attribute.
     * @param defStyle The default style to apply to this view. If 0, no style will be applied (beyond
     *                 what is included in the theme). This may either be an attribute resource, whose
     *                 value will be retrieved from the current theme, or an explicit style resource.
     */
    public TimeSpinner(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // check if the parent activity has our timeSelectedListener, automatically enable it:
        if(context instanceof OnTimeSelectedListener)
            setOnTimeSelectedListener((OnTimeSelectedListener) context);
        setOnItemSelectedListener(this);

        initTimePickerDialog(context);

        // get the FragmentManager:
        try{
            fragmentManager = ((FragmentActivity) context).getSupportFragmentManager();
        } catch (ClassCastException e) {
            Log.d(getClass().getSimpleName(), "Can't get fragment manager from context");
        }

        if(attrs != null) {
            // get our flags from xml, if set:
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ReminderDatePicker);
            int flags = a.getInt(R.styleable.ReminderDatePicker_flags, ReminderDatePicker.MODE_GOOGLE);
            setFlags(flags);
            a.recycle();
        }
    }

    private void initTimePickerDialog(Context context) {
        final Calendar calendar = Calendar.getInstance();
        // create the dialog to show later:
        timePickerDialog = TimePickerDialog.newInstance(
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(RadialPickerLayout radialPickerLayout, int hour, int minute) {
                        setSelectedTime(hour, minute);
                    }
                },
                calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE),
                is24HourFormat(getTimeFormat()), hasVibratePermission(context));
    }

    private boolean is24HourFormat(java.text.DateFormat timeFormat) {
        String pattern;
        try {
            pattern = ((SimpleDateFormat) timeFormat).toLocalizedPattern();
        } catch (ClassCastException e) {
            // we cannot get the pattern, use the default setting for out context:
            return DateFormat.is24HourFormat(getContext());
        }
        // if pattern does not contain the 12 hour formats, we return true (regardless of any 'a' (am/pm) modifier)
        return !(pattern.contains("h") || pattern.contains("K"));
    }

    private boolean hasVibratePermission(Context context) {
        final String permission = "android.permission.VIBRATE";
        final int res = context.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    @Override
    public List<TwinTextItem> getSpinnerItems() {
        try {
            return getItemsFromXml(R.xml.time_items);
        } catch (Exception e) {
            Log.d("TimeSpinner", "Error parsing time items from xml");
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected @Nullable TwinTextItem parseItemFromXmlTag(@NonNull XmlResourceParser parser) {
        if(!parser.getName().equals(XML_TAG_TIMEITEM)) {
            Log.d("TimeSpinner", "Unknown xml tag name: " + parser.getName());
            return null;
        }

        // parse the TimeItem, possible values are
        String text = null;
        @StringRes int textResource = NO_ID, id = NO_ID;
        int hour = 0, minute = 0;
        for(int i=parser.getAttributeCount()-1; i>=0; i--) {
            String attrName = parser.getAttributeName(i);
            switch (attrName) {
                case XML_ATTR_ID:
                    id = parser.getIdAttributeResourceValue(NO_ID);
                    break;
                case XML_ATTR_TEXT:
                    text = parser.getAttributeValue(i);
                    // try to get a resource value, the string is retrieved below
                    if(text != null && text.startsWith("@"))
                        textResource = parser.getAttributeResourceValue(i, NO_ID);
                    break;

                case XML_ATTR_ABSHOUR:
                    hour = parser.getAttributeIntValue(i, -1);
                    break;
                case XML_ATTR_ABSMINUTE:
                    minute = parser.getAttributeIntValue(i, -1);
                    break;

                case XML_ATTR_RELHOUR:
                    hour += parser.getAttributeIntValue(i, 0);
                    break;
                case XML_ATTR_RELMINUTE:
                    minute += parser.getAttributeIntValue(i, 0);
                    break;
                default:
                    Log.d("TimeSpinner", "Skipping unknown attribute tag parsing xml resource: "
                            + attrName + ", maybe a typo?");
            }
        }// end for attr

        // now construct the time item from the attributes
        if(textResource != NO_ID)
            text = getResources().getString(textResource);

        // when no text is given, format the date to have at least something to show
        if(text == null || text.equals(""))
            text = formatTime(hour, minute);

        return new TimeItem(text, formatTime(hour, minute), hour, minute, id);
    }

    /**
     * Gets the currently selected time (that the Spinner is showing)
     * @return The selected time as Calendar, or null if there is none.
     */
    public Calendar getSelectedTime() {
        final Object selectedItem = getSelectedItem();
        if(!(selectedItem instanceof TimeItem))
            return null;
        return ((TimeItem) selectedItem).getTime();
    }

    /**
     * Sets the Spinner's selection as time in hour and minute. If the time was not in the possible
     * selections, a temporary item is created and passed to selectTemporary().
     * @param hour The hour to be selected.
     * @param minute The minute in the hour.
     */
    public void setSelectedTime(int hour, int minute) {
        final int count = getAdapter().getCount() - 1;
        int itemPosition = -1;
        for(int i=0; i<count; i++) {
            final TimeItem item = ((TimeItem) getAdapter().getItem(i));
            if(item.getHour() == hour && item.getMinute() == minute) {
                itemPosition = i;
                break;
            }
        }
        if(itemPosition >= 0)
            setSelection(itemPosition);
        else {
            // create a temporary TimeItem to select:
            selectTemporary(new TimeItem(formatTime(hour, minute), hour, minute, NO_ID));
        }
    }

    private String formatTime(int hour, int minute) {
        return getTimeFormat().format(new GregorianCalendar(0,0,0,hour,minute).getTime());
    }

    /**
     * Gets the time format (as java.text.DateFormat) currently used to format Calendar strings.
     * Defaults to the short time instance for your locale.
     * @return The time format, which will never be null.
     */
    public java.text.DateFormat getTimeFormat() {
        if(timeFormat == null)
            timeFormat = java.text.DateFormat.getTimeInstance(java.text.DateFormat.SHORT);
        return timeFormat;
    }

    /**
     * Sets the time format to use for formatting Calendar objects to displayable strings.
     * @param timeFormat The new time format (as java.text.DateFormat), or null to use the default format.
     */
    public void setTimeFormat(java.text.DateFormat timeFormat) {
        this.timeFormat = timeFormat;
        // update our pre-built timePickerDialog with the new timeFormat:
        initTimePickerDialog(getContext());

        // save the flags and selection first:
        final PickerSpinnerAdapter adapter = ((PickerSpinnerAdapter)getAdapter());
        final boolean moreTimeItems = isShowingMoreTimeItems();
        final boolean numbersInView = adapter.isShowingSecondaryTextInView();
        final Calendar selection = getSelectedTime();
        // we need to restore differently if we have a temporary selection:
        final boolean temporarySelected = getSelectedItemPosition() == adapter.getCount();

        // to rebuild the spinner items, we need to recreate our adapter:
        initAdapter(getContext());

        // force restore flags and selection to the new Adapter:
        setShowNumbersInView(numbersInView);
        this.showMoreTimeItems = false;
        if(temporarySelected) {
            // for some reason these calls have to be exactly in this order!
            setSelectedTime(selection.get(Calendar.HOUR_OF_DAY), selection.get(Calendar.MINUTE));
            setShowMoreTimeItems(moreTimeItems);
        } else {
            // this way it works when a date from the array is selected (like the default)
            setShowMoreTimeItems(moreTimeItems);
            setSelectedTime(selection.get(Calendar.HOUR_OF_DAY), selection.get(Calendar.MINUTE));
        }
    }

    /**
     * Implement this interface if you want to be notified whenever the selected time changes.
     */
    public void setOnTimeSelectedListener(OnTimeSelectedListener listener) {
        this.timeListener = listener;
    }

    /**
     * Gets the default {@link TimePickerDialog} that is shown when the footer is clicked.
     * @return The dialog, or null if a custom time picker has been set and the default one is thus unused.
     */
    public @Nullable TimePickerDialog getTimePickerDialog() {
        if(customTimePicker != null)
            return null;
        return timePickerDialog;
    }

    /**
     * Sets a custom listener whose onClick method will be called to create and handle the custom time picker.
     * You should call {@link #setSelectedTime} when the custom picker is finished.
     * @param launchPicker An {@link android.view.View.OnClickListener} whose onClick method will be
     *                     called to show the custom time picker, or null to use the default picker.
     */
    public void setCustomTimePicker(@Nullable OnClickListener launchPicker) {
        this.customTimePicker = launchPicker;
    }

    /**
     * Checks whether the spinner is showing all time items, including noon and late night.
     * @return True if FLAG_MORE_TIME has been set or {@link #setShowMoreTimeItems(boolean)} was called, false otherwise.
     */
    public boolean isShowingMoreTimeItems() {
        return this.showMoreTimeItems;
    }

    /**
     * Toggles showing more time items. If enabled, a noon and a late night time item are shown.
     * @param enable True to enable, false to disable more time items.
     */
    public void setShowMoreTimeItems(boolean enable) {
        if(enable && !showMoreTimeItems) {
            // create the noon and late night item:
            final Resources res = getResources();
            // switch the afternoon item to 2pm:
            insertAdapterItem(new TimeItem(res.getString(R.string.time_afternoon_2), formatTime(14, 0), 14, 0, R.id.time_afternoon_2), 2);
            removeAdapterItemById(R.id.time_afternoon);
            // noon item:
            insertAdapterItem(new TimeItem(res.getString(R.string.time_noon), formatTime(12, 0), 12, 0, R.id.time_noon), 1);
            // late night item:
            addAdapterItem(new TimeItem(res.getString(R.string.time_late_night), formatTime(23, 0), 23, 0, R.id.time_late_night));
        }
        else if(!enable && showMoreTimeItems) {
            // switch back the afternoon item:
            insertAdapterItem(new TimeItem(getResources().getString(R.string.time_afternoon), formatTime(13, 0), 13, 0, R.id.time_afternoon), 3);
            removeAdapterItemById(R.id.time_afternoon_2);
            removeAdapterItemById(R.id.time_noon);
            removeAdapterItemById(R.id.time_late_night);
        }
        showMoreTimeItems = enable;
    }

    /**
     * Toggles showing numeric time in the view. Note that time will always be shown in dropdown.
     * @param enable True to enable, false to disable numeric mode.
     */
    public void setShowNumbersInView(boolean enable) {
        PickerSpinnerAdapter adapter = (PickerSpinnerAdapter) getAdapter();
        // workaround for now.
        if(enable != adapter.isShowingSecondaryTextInView() && adapter.getCount() == getSelectedItemPosition())
            setSelection(0);
        adapter.setShowSecondaryTextInView(enable);
    }

    /**
     * Set the flags to use for this time spinner.
     * @param modeOrFlags A mode of ReminderDatePicker.MODE_... or multiple ReminderDatePicker.FLAG_...
     *                    combined with the | operator.
     */
    public void setFlags(int modeOrFlags) {
        setShowMoreTimeItems((modeOrFlags & ReminderDatePicker.FLAG_MORE_TIME) != 0);
        setShowNumbersInView((modeOrFlags & ReminderDatePicker.FLAG_NUMBERS) != 0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeAdapterItemAt(int index) {
        if(index == getSelectedItemPosition()) {
            Calendar time = getSelectedTime();
            selectTemporary(new TimeItem(formatTime(time.get(Calendar.HOUR_OF_DAY), time.get(Calendar.MINUTE)), time, NO_ID));
        }
        super.removeAdapterItemAt(index);
    }

    @Override
    public CharSequence getFooter() {
        return getResources().getString(R.string.spinner_time_footer);
    }

    @Override
    public void onFooterClick() {
        if (customTimePicker == null) {
            // update the selected time in the dialog
            final Calendar time = getSelectedTime();
            timePickerDialog.setStartTime(time.get(Calendar.HOUR_OF_DAY), time.get(Calendar.MINUTE));
            timePickerDialog.show(fragmentManager, "TimePickerDialog");
        } else {
            customTimePicker.onClick(this);
        }
    }

    @Override
    protected void restoreTemporarySelection(String codeString) {
        selectTemporary(TimeItem.fromString(codeString));
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if(timeListener != null) {
            Object selectedObj = getSelectedItem();
            if(selectedObj instanceof TimeItem) {
                TimeItem selected = (TimeItem) selectedObj;
                int hour = selected.getHour();
                int minute = selected.getMinute();
                if(hour != lastSelectedHour || minute != lastSelectedMinute) {
                    timeListener.onTimeSelected(hour, minute);
                    lastSelectedHour = hour;
                    lastSelectedMinute = minute;
                }
            }
        }
    }

    // unused
    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }
}
