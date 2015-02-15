package com.simplicityapks.reminderdatepicker.lib;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.support.annotation.StringRes;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import com.fourmob.datetimepicker.date.DatePickerDialog;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * The left PickerSpinner in the Google Keep app, to select a date.
 */
public class DateSpinner extends PickerSpinner implements AdapterView.OnItemSelectedListener {

    // These listeners don't have to be implemented, if null just ignore
    private OnDateSelectedListener dateListener = null;
    private OnClickListener customDatePicker = null;

    // The default DatePicker dialog to show if customDatePicker has not been set
    private final DatePickerDialog datePickerDialog;
    private FragmentManager fragmentManager;

    private boolean showPastItems = false;
    private boolean showMonthItem = false;
    private boolean showWeekdayNames = false;
    private boolean showNumbersInView = false;

    private String[] weekDays = null;

    // The custom DateFormat used to convert Calendars into displayable Strings:
    private java.text.DateFormat customDateFormat = null;
    private java.text.DateFormat secondaryDateFormat = null;

    /**
     * Construct a new DateSpinner with the given context's theme.
     * @param context The Context the view is running in, through which it can access the current theme, resources, etc.
     */
    public DateSpinner(Context context){
        this(context, null, 0);
    }

    /**
     * Construct a new DateSpinner with the given context's theme and the supplied attribute set.
     * @param context The Context the view is running in, through which it can access the current theme, resources, etc.
     * @param attrs The attributes of the XML tag that is inflating the view. May contain a flags attribute.
     */
    public DateSpinner(Context context, AttributeSet attrs){
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
    public DateSpinner(Context context, AttributeSet attrs, int defStyle){
        super(context, attrs, defStyle);
        // check if the parent activity has our dateSelectedListener, automatically enable it:
        if(context instanceof OnDateSelectedListener)
            setOnDateSelectedListener((OnDateSelectedListener) context);
        setOnItemSelectedListener(this);

        final Calendar calendar = Calendar.getInstance();
        // create the dialog:
        datePickerDialog = DatePickerDialog.newInstance(
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePickerDialog datePickerDialog, int year, int month, int day) {
                        setSelectedDate(new GregorianCalendar(year, month, day));
                    }
                },
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH), hasVibratePermission(context));

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
        }
    }

    private boolean hasVibratePermission(Context context) {
        final String permission = "android.permission.VIBRATE";
        final int res = context.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    @Override
    public List<TwinTextItem> getSpinnerItems() {
        final Resources res = getResources();
        final Calendar date = Calendar.getInstance();
        ArrayList<TwinTextItem> items = new ArrayList<TwinTextItem>(3);
        // today item:
        items.add(new DateItem(res.getString(R.string.date_today), date));
        // tomorrow item:
        date.add(Calendar.DAY_OF_YEAR, 1);
        items.add(new DateItem(res.getString(R.string.date_tomorrow), date));
        // next weekday item:
        date.add(Calendar.DAY_OF_YEAR, 6);
        int weekday = date.get(Calendar.DAY_OF_WEEK);
        items.add(new DateItem(getWeekDay(weekday,
                // have a separate string for Saturday and Sunday because of gender variation in Portuguese
                weekday==7 || weekday==1? R.string.date_next_weekday : R.string.date_next_weekday_weekend), date));
        return items;
    }

    private String getWeekDay(int weekDay, @StringRes int stringRes) {
        if(weekDays == null) weekDays = new DateFormatSymbols().getWeekdays();
        // in some translations (French for instance), the weekday is the first word but is not capitalized, so we'll do that
        String result = getResources().getString(stringRes, weekDays[weekDay]);
        return Character.toUpperCase(result.charAt(0)) + result.substring(1);
    }

    /**
     * Gets the currently selected date (that the Spinner is showing)
     * @return The selected date as Calendar, or null if there is none.
     */
    public Calendar getSelectedDate() {
        final DateItem selectedItem = (DateItem) getSelectedItem();
        if(selectedItem == null)
            return null;
        else
            return selectedItem.getDate();
    }

    /**
     * Sets the Spinner's selection as date. If the date was not in the possible selections, a temporary
     * item is created and passed to selectTemporary().
     * @param date The date to be selected.
     */
    public void setSelectedDate(Calendar date) {
        final int count = getAdapter().getCount() - 1;
        int itemPosition = -1;
        for(int i=0; i<count; i++) {
            if(getAdapter().getItem(i).equals(date)) { // because DateItem deeply compares to calendar
                itemPosition = i;
                break;
            }
        }
        if(itemPosition >= 0)
            setSelection(itemPosition);
        else if(showWeekdayNames) {
            final long MILLIS_IN_DAY = 1000*60*60*24;
            final long dateDifference = (date.getTimeInMillis()/MILLIS_IN_DAY)
                    - (Calendar.getInstance().getTimeInMillis()/MILLIS_IN_DAY);
            if(dateDifference>0 && dateDifference<7) { // if the date is within the next week:
                // construct a temporary DateItem to select:
                final int day = date.get(Calendar.DAY_OF_WEEK);

                // Because these items are always temporarily selected, we can safely assume that
                // they will never appear in the spinner dropdown. When a FLAG_NUMBERS is set, we
                // want these items to have the date as secondary text in a short format.
                selectTemporary(new DateItem(getWeekDay(day, R.string.date_only_weekday), formatSecondaryDate(date), date));
            } else {
                // show the date as a full text, using the current DateFormat:
                selectTemporary(new DateItem(formatDate(date), date));
            }
        }
        else {
            // show the date as a full text, using the current DateFormat:
            selectTemporary(new DateItem(formatDate(date), date));
        }
    }

    private String formatDate(Calendar date) {
        if(customDateFormat == null)
            return DateUtils.formatDateTime(getContext(), date.getTimeInMillis(), DateUtils.FORMAT_SHOW_DATE);
        else
            return customDateFormat.format(date.getTime());
    }

    // only to be used when FLAG_NUMBERS and FLAG_WEEKDAY_NAMES have been set
    private String formatSecondaryDate(Calendar date) {
        if(secondaryDateFormat == null)
            return DateUtils.formatDateTime(getContext(), date.getTimeInMillis(),
                    DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NUMERIC_DATE);
        else
            return secondaryDateFormat.format(date.getTime());
    }

    /**
     * Gets the custom DateFormat currently used to format Calendar strings.
     * If {@link #setDateFormat(java.text.DateFormat)} has not been called yet, it will return null.
     * @return The date format, or null if the Spinner is using the default date format.
     */
    public java.text.DateFormat getCustomDateFormat() {
        return customDateFormat;
    }

    /**
     * Sets the custom date format to use for formatting Calendar objects to displayable strings.
     * @param dateFormat The new DateFormat, or null to use the default format.
     */
    public void setDateFormat(java.text.DateFormat dateFormat) {
        setDateFormat(dateFormat, null);
    }

    /**
     * Sets the custom date format to use for formatting Calendar objects to displayable strings.
     * @param dateFormat The new DateFormat, or null to use the default format.
     * @param numbersDateFormat The DateFormat for formatting the secondary date when both FLAG_NUMBERS
     *                          and FLAG_WEEKDAY_NAMES are set, or null to use the default format.
     */
    public void setDateFormat(java.text.DateFormat dateFormat, java.text.DateFormat numbersDateFormat) {
        this.customDateFormat = dateFormat;
        this.secondaryDateFormat = numbersDateFormat;
        // update the spinner with the new date format:

        // the only spinner item that will be affected is the month item, so just toggle the flag twice
        // instead of rebuilding the whole adapter
        if(showMonthItem) {
            int monthPosition = getLastItemPosition();
            boolean reselectMonthItem = getSelectedItemPosition() == monthPosition;
            setShowMonthItem(false);
            setShowMonthItem(true);
            if(reselectMonthItem) setSelection(monthPosition);
        }

        // if we have a temporary date item selected, update that as well
        if(getSelectedItemPosition() == getAdapter().getCount())
            setSelectedDate(getSelectedDate());
    }

    /**
     * Implement this interface if you want to be notified whenever the selected date changes.
     */
    public void setOnDateSelectedListener(OnDateSelectedListener listener) {
        this.dateListener = listener;
    }

    /**
     * Sets a custom listener whose onClick method will be called to create and handle the custom date picker.
     * You should call {@link #setSelectedDate} when the custom picker is finished.
     * @param launchPicker An {@link android.view.View.OnClickListener} whose onClick method will be
     *                     called to show the custom date picker.
     */
    public void setCustomDatePicker(OnClickListener launchPicker) {
        this.customDatePicker = launchPicker;
    }

    /**
     * Toggles showing the past items. Past mode shows the yesterday and last weekday item.
     * @param enable True to enable, false to disable past mode.
     */
    public void setShowPastItems(boolean enable) {
        if(enable && !showPastItems) {
            // create the yesterday and last Monday item:
            final Resources res = getResources();
            final Calendar date = Calendar.getInstance();
            // yesterday:
            date.add(Calendar.DAY_OF_YEAR, -1);
            insertAdapterItem(new DateItem(res.getString(R.string.date_yesterday), date), 0);
            // last weekday item:
            date.add(Calendar.DAY_OF_YEAR, -6);
            int weekday = date.get(Calendar.DAY_OF_WEEK);
            insertAdapterItem(new DateItem(getWeekDay(weekday,
            // have a separate string for Saturday and Sunday because of gender variation in Portuguese
                    weekday==7 || weekday==1? R.string.date_last_weekday : R.string.date_last_weekday_weekend),
                    date), 0);
        }
        else if(!enable && showPastItems) {
            // delete the yesterday and last weekday items:
            removeAdapterItemAt(1);
            removeAdapterItemAt(0);
        }
        showPastItems = enable;
    }

    /**
     * Toggles showing the month item. Month mode an item in exactly one month from now.
     * @param enable True to enable, false to disable month mode.
     */
    public void setShowMonthItem(boolean enable) {
        if(enable && !showMonthItem) {
            // create the in 1 month item
            final Calendar date = Calendar.getInstance();
            date.add(Calendar.MONTH, 1);
            addAdapterItem(new DateItem(formatDate(date), date));
        }
        else if(!enable && showMonthItem) {
            removeAdapterItemAt(getLastItemPosition());
        }
        showMonthItem = enable;
    }

    /**
     * Toggles showing the weekday names instead of dates for the next week. Turning this on will
     * display e.g. "Sunday" for the day after tomorrow, otherwise it'll be January 1.
     * @param enable True to enable, false to disable weekday names.
     */
    public void setShowWeekdayNames(boolean enable) {
        if(showWeekdayNames != enable) {
            showWeekdayNames = enable;
            // if FLAG_NUMBERS has been set, toggle the secondary text in the adapter
            if(showNumbersInView)
                setShowNumbersInViewInt(enable);
            // reselect the current item so it will use the new setting:
            setSelectedDate(getSelectedDate());
        }
    }

    /**
     * Toggles showing numeric dates for the weekday items in the spinner view. This will only apply
     * when a day within the next week is selected and FLAG_WEEKDAY_NAMES has been set, not in the dropdown.
     * @param enable True to enable, false to disable numeric mode.
     */
    public void setShowNumbersInView(boolean enable) {
        showNumbersInView = enable;
        // only enable the adapter when FLAG_WEEKDAY_NAMES has been set as well
        if(!enable || showWeekdayNames)
            setShowNumbersInViewInt(enable);
    }

    private void setShowNumbersInViewInt(boolean enable) {
        PickerSpinnerAdapter adapter = (PickerSpinnerAdapter) getAdapter();
        // workaround for now. See GitHub issue #2
        if (enable != adapter.isShowingSecondaryTextInView() && getCount() == getSelectedItemPosition())
            setSelection(0);
        adapter.setShowSecondaryTextInView(enable);
    }

    /**
     * Set the flags to use for this date spinner.
     * @param modeOrFlags A mode of ReminderDatePicker.MODE_... or multiple ReminderDatePicker.FLAG_...
     *                    combined with the | operator.
     */
    public void setFlags(int modeOrFlags) {
        setShowPastItems((modeOrFlags & ReminderDatePicker.FLAG_PAST) != 0);
        setShowMonthItem((modeOrFlags & ReminderDatePicker.FLAG_MONTH) != 0);
        setShowWeekdayNames((modeOrFlags & ReminderDatePicker.FLAG_WEEKDAY_NAMES) != 0);
        setShowNumbersInView((modeOrFlags & ReminderDatePicker.FLAG_NUMBERS) != 0);
    }

    @Override
    public CharSequence getFooter() {
        return getResources().getString(R.string.spinner_date_footer);
    }

    @Override
    public void onFooterClick() {
        if (customDatePicker == null) {
            datePickerDialog.show(fragmentManager, "DatePickerDialog");
        } else {
            customDatePicker.onClick(this);
        }
    }

    @Override
    protected void restoreTemporarySelection(String codeString) {
        selectTemporary(DateItem.fromString(codeString));
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if(dateListener != null)
            dateListener.onDateSelected(getSelectedDate());
    }

    // unused
    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }
}
