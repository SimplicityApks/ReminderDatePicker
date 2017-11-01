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
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.fourmob.datetimepicker.date.CalendarDay;
import com.fourmob.datetimepicker.date.DatePickerDialog;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * The left PickerSpinner in the Google Keep app, to select a date.
 */
public class DateSpinner extends PickerSpinner implements AdapterView.OnItemSelectedListener {

    // TODO remove when setMinDate(null) works
    private static final CalendarDay MINIMUM_POSSIBLE_DATE = new CalendarDay(1902, 1, 1);

    public static final String XML_TAG_DATEITEM = "DateItem";

    public static final String XML_ATTR_ABSDAYOFYEAR = "absDayOfYear";
    public static final String XML_ATTR_ABSDAYOFMONTH = "absDayOfMonth";
    public static final String XML_ATTR_ABSMONTH = "absMonth";
    public static final String XML_ATTR_ABSYEAR = "absYear";

    public static final String XML_ATTR_RELDAY = "relDay";
    public static final String XML_ATTR_RELMONTH = "relMonth";
    public static final String XML_ATTR_RELYEAR = "relYear";


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

    // To catch twice selecting the same date:
    private Calendar lastSelectedDate = null;

    // Min and mix date to be shown (are currently not restored during rotation as they are mostly set in the onCreate() anyway):
    private Calendar minDate = null;
    private Calendar maxDate = null;

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

        // the default min date is today:
        setMinDate(calendar);

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

    private boolean hasVibratePermission(Context context) {
        final String permission = "android.permission.VIBRATE";
        final int res = context.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    @Override
    public List<TwinTextItem> getSpinnerItems() {
        try {
            return getItemsFromXml(R.xml.date_items);
        } catch (Exception e) {
            Log.d("DateSpinner", "Error parsing date items from xml");
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected @Nullable TwinTextItem parseItemFromXmlTag(@NonNull XmlResourceParser parser) {
        if(!parser.getName().equals(XML_TAG_DATEITEM)) {
            Log.d("DateSpinner", "Unknown xml tag name: " + parser.getName());
            return null;
        }

        // parse the DateItem, possible values are
        String text = null;
        @StringRes int textResource = NO_ID, id = NO_ID;
        Calendar date = Calendar.getInstance();
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

                case XML_ATTR_ABSDAYOFYEAR:
                    final int absDayOfYear = parser.getAttributeIntValue(i, -1);
                    if(absDayOfYear > 0)
                        date.set(Calendar.DAY_OF_YEAR, absDayOfYear);
                    break;
                case XML_ATTR_ABSDAYOFMONTH:
                    final int absDayOfMonth = parser.getAttributeIntValue(i, -1);
                    if(absDayOfMonth > 0)
                        date.set(Calendar.DAY_OF_MONTH, absDayOfMonth);
                    break;
                case XML_ATTR_ABSMONTH:
                    final int absMonth = parser.getAttributeIntValue(i, -1);
                    if(absMonth >= 0)
                        date.set(Calendar.MONTH, absMonth);
                    break;
                case XML_ATTR_ABSYEAR:
                    final int absYear = parser.getAttributeIntValue(i, -1);
                    if(absYear >= 0)
                        date.set(Calendar.YEAR, absYear);
                    break;

                case XML_ATTR_RELDAY:
                    final int relDay = parser.getAttributeIntValue(i, 0);
                    date.add(Calendar.DAY_OF_YEAR, relDay);
                    break;
                case XML_ATTR_RELMONTH:
                    final int relMonth = parser.getAttributeIntValue(i, 0);
                    date.add(Calendar.MONTH, relMonth);
                    break;
                case XML_ATTR_RELYEAR:
                    final int relYear = parser.getAttributeIntValue(i, 0);
                    date.add(Calendar.YEAR, relYear);
                    break;
                default:
                    Log.d("DateSpinner", "Skipping unknown attribute tag parsing xml resource: "
                            + attrName + ", maybe a typo?");
            }
        }// end for attr

        // now construct the date item from the attributes

        // check if we got a textResource earlier and parse that string together with the weekday
        if(textResource != NO_ID)
            text = getWeekDay(date.get(Calendar.DAY_OF_WEEK), textResource);

        // when no text is given, format the date to have at least something to show
        if(text == null || text.equals(""))
            text = formatDate(date);

        return new DateItem(text, date, id);
    }

    private String getWeekDay(int weekDay, @StringRes int stringRes) {
        if(weekDays == null) weekDays = new DateFormatSymbols().getWeekdays();
        // use a separate string for Saturday and Sunday because of gender variation in Portuguese
        if(weekDay==7 || weekDay==1) {
            if(stringRes == R.string.date_next_weekday)
                stringRes = R.string.date_next_weekday_weekend;
            else if(stringRes == R.string.date_last_weekday)
                stringRes = R.string.date_last_weekday_weekend;
        }
        String result = getResources().getString(stringRes, weekDays[weekDay]);
        // in some translations (French for instance), the weekday is the first word but is not capitalized, so we'll do that
        return Character.toUpperCase(result.charAt(0)) + result.substring(1);
    }

    /**
     * Gets the currently selected date (that the Spinner is showing)
     * @return The selected date as Calendar, or null if there is none.
     */
    public Calendar getSelectedDate() {
        final Object selectedItem = getSelectedItem();
        if(!(selectedItem instanceof DateItem))
            return null;
        return ((DateItem) selectedItem).getDate();
    }

    /**
     * Sets the Spinner's selection as date. If the date was not in the possible selections, a temporary
     * item is created and passed to selectTemporary().
     * @param date The date to be selected.
     */
    public void setSelectedDate(@NonNull Calendar date) {
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
                selectTemporary(new DateItem(getWeekDay(day, R.string.date_only_weekday), formatSecondaryDate(date), date, NO_ID));
            } else {
                // show the date as a full text, using the current DateFormat:
                selectTemporary(new DateItem(formatDate(date), date, NO_ID));
            }
        }
        else {
            // show the date as a full text, using the current DateFormat:
            selectTemporary(new DateItem(formatDate(date), date, NO_ID));
        }
    }

    private String formatDate(@NonNull Calendar date) {
        if(customDateFormat == null)
            return DateUtils.formatDateTime(getContext(), date.getTimeInMillis(), DateUtils.FORMAT_SHOW_DATE);
        else
            return customDateFormat.format(date.getTime());
    }

    // only to be used when FLAG_NUMBERS and FLAG_WEEKDAY_NAMES have been set
    private String formatSecondaryDate(@NonNull Calendar date) {
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
            int monthPosition = getAdapterItemPosition(4);
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
     * Sets the minimum allowed date.
     * Spinner items and dates in the date picker before the given date will get disabled.
     * @param minDate The minimum date, or null to clear the previous min date.
     */
    public void setMinDate(@Nullable Calendar minDate) {
        this.minDate = minDate;
        // update the date picker (even if it is not used right now)
        if(minDate == null)
            datePickerDialog.setMinDate(MINIMUM_POSSIBLE_DATE);
        else if(maxDate != null && compareCalendarDates(minDate, maxDate) > 0)
            throw new IllegalArgumentException("Minimum date must be before maximum date!");
        else
            datePickerDialog.setMinDate(new CalendarDay(minDate));
        updateEnabledItems();
    }

    /**
     * Gets the current minimum allowed date.
     * @return The minimum date, or null if there is none.
     */
    public @Nullable Calendar getMinDate() {
        return minDate;
    }

    /**
     * Sets the maximum allowed date.
     * Spinner items and dates in the date picker after the given date will get disabled.
     * @param maxDate The maximum date, or null to clear the previous max date.
     */
    public void setMaxDate(@Nullable Calendar maxDate) {
        this.maxDate = maxDate;
        // update the date picker (even if it is not used right now)
        if(maxDate == null)
            datePickerDialog.setMaxDate(null);
        else if(minDate != null && compareCalendarDates(minDate, maxDate) > 0)
            throw new IllegalArgumentException("Maximum date must be after minimum date!");
        else
            datePickerDialog.setMaxDate(new CalendarDay(maxDate));
        updateEnabledItems();
    }

    /**
     * Gets the current maximum allowed date.
     * @return The maximum date, or null if there is none.
     */
    public @Nullable Calendar getMaxDate() {
        return maxDate;
    }


    /**
     * Loops through the Spinner items and disables all that are not within the min/max date range.
     */
    private void updateEnabledItems() {
        PickerSpinnerAdapter adapter = (PickerSpinnerAdapter) getAdapter();
        // if the current item is out of range, we have no choice but to reset it
        if(!isInDateRange(getSelectedDate())) {
            final Calendar today = Calendar.getInstance();
            if(isInDateRange(today))
                setSelectedDate(today);
            else
                // if today itself is not a valid date, we will just use the minimum date (which is always set here)
                setSelectedDate(minDate);
        }

        for(int position = getLastItemPosition(); position >= 0; position--) {
            DateItem item = (DateItem) adapter.getItem(position);
            if(isInDateRange(item.getDate()))
                item.setEnabled(true);
            else
                item.setEnabled(false);
        }
    }

    private boolean isInDateRange(@NonNull Calendar date) {
        return (minDate == null || compareCalendarDates(minDate, date) <= 0) // later than minDate
                && (maxDate == null || compareCalendarDates(maxDate, date) >= 0); // before maxDate
    }

    /**
     * Compares the two given Calendar objects, only counting the date, not time.
     * @return -1 if first comes before second, 0 if both are the same day, 1 if second is before first.
     */
    static int compareCalendarDates(@NonNull Calendar first, @NonNull Calendar second) {
        final int firstYear = first.get(Calendar.YEAR);
        final int secondYear = second.get(Calendar.YEAR);
        final int firstDay = first.get(Calendar.DAY_OF_YEAR);
        final int secondDay = second.get(Calendar.DAY_OF_YEAR);
        if(firstYear == secondYear) {
            if(firstDay == secondDay)
                return 0;
            else if(firstDay < secondDay)
                return -1;
            else
                return 1;
        }
        else if(firstYear < secondYear)
            return -1;
        else
            return 1;
    }

    /**
     * Implement this interface if you want to be notified whenever the selected date changes.
     */
    public void setOnDateSelectedListener(OnDateSelectedListener listener) {
        this.dateListener = listener;
    }

    /**
     * Gets the default {@link DatePickerDialog} that is shown when the footer is clicked.
     * @return The dialog, or null if a custom date picker has been set and the default one is thus unused.
     */
    public @Nullable DatePickerDialog getDatePickerDialog() {
        if(customDatePicker != null)
            return null;
        return datePickerDialog;
    }

    /**
     * Sets a custom listener whose onClick method will be called to create and handle the custom date picker.
     * You should call {@link #setSelectedDate} when the custom picker is finished.
     * @param launchPicker An {@link android.view.View.OnClickListener} whose onClick method will be
     *                     called to show the custom date picker, or null to use the default picker.
     */
    public void setCustomDatePicker(@Nullable OnClickListener launchPicker) {
        this.customDatePicker = launchPicker;
    }

    /**
     * Toggles showing the past items. Past mode shows the yesterday and last weekday item.
     * @param enable True to enable, false to disable past mode.
     */
    public void setShowPastItems(boolean enable) {
        if(enable && !showPastItems) {
            // first reset the minimum date if necessary:
            if(getMinDate() != null && compareCalendarDates(getMinDate(), Calendar.getInstance()) == 0)
                setMinDate(null);

            // create the yesterday and last Monday item:
            final Resources res = getResources();
            final Calendar date = Calendar.getInstance();
            // yesterday:
            date.add(Calendar.DAY_OF_YEAR, -1);
            insertAdapterItem(new DateItem(res.getString(R.string.date_yesterday), date, R.id.date_yesterday), 0);
            // last weekday item:
            date.add(Calendar.DAY_OF_YEAR, -6);
            int weekday = date.get(Calendar.DAY_OF_WEEK);
            insertAdapterItem(new DateItem(getWeekDay(weekday, R.string.date_last_weekday),
                    date, R.id.date_last_week), 0);
        }
        else if(!enable && showPastItems) {
            // delete the yesterday and last weekday items:
            removeAdapterItemById(R.id.date_last_week);
            removeAdapterItemById(R.id.date_yesterday);

            // we set the minimum date to today as we don't allow past items
            setMinDate(Calendar.getInstance());
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
            addAdapterItem(new DateItem(formatDate(date), date, R.id.date_month));
        }
        else if(!enable && showMonthItem) {
            removeAdapterItemById(R.id.date_month);
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
        if (enable != adapter.isShowingSecondaryTextInView() && adapter.getCount() == getSelectedItemPosition())
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeAdapterItemAt(int index) {
        if(index == getSelectedItemPosition()) {
            Calendar date = getSelectedDate();
            selectTemporary(new DateItem(formatDate(date), date, NO_ID));
        }
        super.removeAdapterItemAt(index);
    }

    @Override
    public CharSequence getFooter() {
        return getResources().getString(R.string.spinner_date_footer);
    }

    @Override
    public void onFooterClick() {
        if (customDatePicker == null) {
            // update the selected date in the dialog
            final Calendar date = getSelectedDate();
            datePickerDialog.onDateSelected(
                    date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH));
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
        if(dateListener != null) {
            // catch selecting same date twice
            Calendar date = getSelectedDate();
            if(date != null && !date.equals(lastSelectedDate)) {
                dateListener.onDateSelected(date);
                lastSelectedDate = date;
            }
        }
    }

    // unused
    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }
}
