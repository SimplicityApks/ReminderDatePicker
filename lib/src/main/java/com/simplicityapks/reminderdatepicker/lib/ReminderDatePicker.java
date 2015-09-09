package com.simplicityapks.reminderdatepicker.lib;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.sleepbot.datetimepicker.time.TimePickerDialog;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * A Google Keep like date and time picker for reminders, to be inflated via xml or constructor.
 * Holds both DateSpinner and TimeSpinner and takes care of handling selection layout changes.
 *
 * Refer to <a href="https://github.com/SimplicityApks/ReminderDatePicker">the project's github page</a> for official documentation.
 */
public class ReminderDatePicker extends LinearLayout implements AdapterView.OnItemSelectedListener{

    /**
     * Mode for {@link #setFlags(int)}. Base mode, same items as in the Google Keep app.
     */
    public static final int MODE_GOOGLE = 0;         // 000000

    /**
     * Mode for {@link #setFlags(int)}. Include all possible items and show numbers in the time spinner.
     */
    public static final int MODE_EVERYTHING = 31;    // 011111

    /**
     * Flag for {@link #setFlags(int)}. Include a yesterday and last weekday item.
     */
    public static final int FLAG_PAST = 1;           // 000001

    /**
     * Flag for {@link #setFlags(int)}. Include a month item exactly one month from today.
     */
    public static final int FLAG_MONTH = 2;          // 000010

    /**
     * Flag for {@link #setFlags(int)}. Include a noon and late night item in the time spinner.
     */
    public static final int FLAG_MORE_TIME = 4;      // 000100

    /**
     * Flag for {@link #setFlags(int)}. Show numeric time in the time spinner view and in the date
     * spinner view when a day within the next week is shown with FLAG_WEEKDAY_NAMES. Note that time
     * will always be shown in dropdown.
     */
    public static final int FLAG_NUMBERS = 8;        // 001000

    /**
     * Flag for {@link #setFlags(int)}. Show the weekday name when a date within the next week is
     * selected instead of the standard date format.
     */
    public static final int FLAG_WEEKDAY_NAMES = 16; // 010000

    /**
     * Flag for {@link #setFlags(int)}. Hide the time picker and show a button to show it.
     */
    public static final int FLAG_HIDE_TIME = 32;     // 100000

    // has FLAG_HIDE_TIME been set?
    private boolean shouldHideTime = false;

    private DateSpinner dateSpinner;
    private TimeSpinner timeSpinner;

    // This listener doesn't have to be implemented, if it is null just ignore it
    private OnDateSelectedListener listener = null;

    // To catch twice selecting the same date:
    private Calendar lastSelectedDate = null;

    // To keep track whether we need to selectDefaultDate in onAttachToWindow():
    private boolean shouldSelectDefault = true;

    /**
     * Construct a new ReminderDatePicker with the given context's theme but without any flags.
     * @param context The Context the view is running in, through which it can access the current theme, resources, etc.
     */
    public ReminderDatePicker(Context context) {
        this(context, null);
    }

    /**
     * Construct a new ReminderDatePicker with the given context's theme and the supplied attribute set.
     * @param context The Context the view is running in, through which it can access the current theme, resources, etc.
     * @param attrs The attributes of the XML tag that is inflating the view. May contain a flags attribute.
     */
    public ReminderDatePicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    /**
     * Construct a new ReminderDatePicker with the given context's theme and the supplied attribute set.
     * @param context The Context the view is running in, through which it can access the current theme, resources, etc.
     * @param attrs The attributes of the XML tag that is inflating the view. May contain a flags attribute.
     * @param defStyle The default style to apply to this view. If 0, no style will be applied (beyond
     *                 what is included in the theme). This may either be an attribute resource, whose
     *                 value will be retrieved from the current theme, or an explicit style resource.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public ReminderDatePicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
        // Additional styling work is done here
    }

    private void init(Context context, AttributeSet attrs) {
        View.inflate(context, R.layout.reminder_date_picker, this);
        dateSpinner = (DateSpinner) findViewById(R.id.date_spinner);
        dateSpinner.setOnItemSelectedListener(this);

        timeSpinner = (TimeSpinner) findViewById(R.id.time_spinner);
        timeSpinner.setOnItemSelectedListener(this);
        // check if the parent activity has our dateSelectedListener, automatically enable it:
        if(context instanceof OnDateSelectedListener)
            setOnDateSelectedListener((OnDateSelectedListener) context);

        // set gravity, for the timeButton when th eTimeSpinner is hidden:
        setGravity(Gravity.CENTER_VERTICAL);

        if(attrs != null) {
            // get our flags from xml, if set:
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ReminderDatePicker);
            int flags = a.getInt(R.styleable.ReminderDatePicker_flags, MODE_GOOGLE);
            setFlags(flags);
            a.recycle();
        }
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(state);
        if(state != null)
            shouldSelectDefault = false;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        // we may need to initialize the selected date
        if(shouldSelectDefault)
            selectDefaultDate();
    }

    /**
     * Selects the next best date (and time) after today.
     * Requires that the items are in ascending order (and that there is at least one item to select).
     */
    private void selectDefaultDate() {
        Calendar today = Calendar.getInstance();
        int hour = -1, minute = -1;

        // get the next possible selection
        Calendar date = getNextItemDate(today);
        // if it is the today item, we need to take a look the time
        if(date != null && DateSpinner.compareCalendarDates(date, today) == 0) {
            // same as getNextTimeDate for TimeSpinner
            final int last = timeSpinner.getLastItemPosition();
            final int searchHour = today.get(Calendar.HOUR_OF_DAY),
                    searchMinute = today.get(Calendar.MINUTE);
            for (int i=0; i<=last; i++) {
                final TimeItem time = ((TimeItem) timeSpinner.getItemAtPosition(i));
                if(time.getHour() > searchHour || (time.getHour() == searchHour && time.getMinute() >= searchMinute)) {
                    hour = time.getHour();
                    minute = time.getMinute();
                    break;
                }
            }

            // it may be too late in the evening to select the today item
            // or if FLAG_HIDE_TIME has been set, set it to tomorrow morning:
            if((hour == -1 && minute == -1) || shouldHideTime) {
                Calendar tomorrow = (Calendar) today.clone();
                tomorrow.add(Calendar.DAY_OF_YEAR, 1);
                date = getNextItemDate(tomorrow); // if this returns null it'll be set to today below
            }
        }
        if(date == null) {
            // it seems this spinner only contains past items, use the last one
            date = ((DateItem) dateSpinner.getItemAtPosition(dateSpinner.getLastItemPosition())).getDate();
        }

        if(hour == -1 && minute == -1) {
            // the date is not today, just select the earliest possible time
            final TimeItem time = ((TimeItem) timeSpinner.getItemAtPosition(0));
            hour = time.getHour();
            minute = time.getMinute();
        }

        // finally, select what we found
        date.set(Calendar.HOUR_OF_DAY, hour);
        date.set(Calendar.MINUTE, minute);
        setSelectedDate(date);
    }

    /**
     * Gets the next date item's date equal to or later than the given date in the DateSpinner.
     * Requires that the items are in ascending order.
     * @return A date from the next item in the DateSpinner, or no such date was found.
     */
    private @Nullable Calendar getNextItemDate(Calendar searchDate) {
        final int last = dateSpinner.getLastItemPosition();
        for (int i=0; i<=last; i++) {
            final Calendar date = ((DateItem) dateSpinner.getItemAtPosition(i)).getDate();
            // use the DateSpinner's compare method so hours and minutes are not considered
            if(DateSpinner.compareCalendarDates(date, searchDate) >= 0)
                return date;
        }
        // not found
        return null;
    }

    /**
     * Gets the currently selected date (that the Spinners are showing)
     * @return The selected date as Calendar, or null if there is none.
     */
    public Calendar getSelectedDate() {
        Calendar result = dateSpinner.getSelectedDate();
        Calendar time = timeSpinner.getSelectedTime();
        if(result!=null && time!=null) {
            result.set(Calendar.HOUR_OF_DAY, time.get(Calendar.HOUR_OF_DAY));
            result.set(Calendar.MINUTE, time.get(Calendar.MINUTE));
            return result;
        }
        else return null;
    }

    /**
     * Sets the Spinners' selection as date considering both time and day.
     * @param date The date to be selected.
     */
    public void setSelectedDate(Calendar date) {
        if(date!=null) {
            dateSpinner.setSelectedDate(date);
            timeSpinner.setSelectedTime(date.get(Calendar.HOUR_OF_DAY), date.get(Calendar.MINUTE));
            // a custom selection has been set, don't select the default date:
            shouldSelectDefault = false;
        }
    }

    /**
     * Sets the Spinners' date selection as integers considering only day.
     */
    public void setSelectedDate(int year, int month, int day) {
        dateSpinner.setSelectedDate(new GregorianCalendar(year, month, day));
        // a custom selection has been set, don't select the default date:
        shouldSelectDefault = false;
    }

    /**
     * Sets the Spinners' time selection as integers considering only time.
     */
    public void setSelectedTime(int hour, int minute) {
        timeSpinner.setSelectedTime(hour, minute);
        // a custom selection has been set, don't select the default date:
        shouldSelectDefault = false;
    }

    /**
     * Implement this interface if you want to be notified whenever the selected date changes.
     */
    public void setOnDateSelectedListener(OnDateSelectedListener listener) {
        this.listener = listener;
    }

    /**
     * Gets the default {@link DatePickerDialog} that is shown when the footer in the DateSpinner is clicked.
     * @return The dialog, or null if a custom date picker has been set and the default one is thus unused.
     */
    public @Nullable DatePickerDialog getDatePickerDialog() {
        return dateSpinner.getDatePickerDialog();
    }

    /**
     * Gets the default {@link TimePickerDialog} that is shown when the footer in the TimeSpinner is clicked.
     * @return The dialog, or null if a custom time picker has been set and the default one is thus unused.
     */
    public @Nullable TimePickerDialog getTimePickerDialog() {
        return timeSpinner.getTimePickerDialog();
    }

    /**
     * Sets a custom listener whose onClick method will be called to create and handle the custom date picker.
     * You should call {@link #setSelectedDate(int, int, int)} when the custom picker is finished.
     * @param launchPicker An {@link android.view.View.OnClickListener} whose onClick method will be
     *                     called to show the custom date picker, or null to use the default picker.
     */
    public void setCustomDatePicker(@Nullable OnClickListener launchPicker) {
        dateSpinner.setCustomDatePicker(launchPicker);
    }

    /**
     * Sets a custom listener whose onClick method will be called to create and handle the custom time picker.
     * You should call {@link #setSelectedTime} when the custom picker is finished.
     * @param launchPicker An {@link android.view.View.OnClickListener} whose onClick method will be
     *                     called to show the custom time picker, or null to use the default picker.
     */
    public void setCustomTimePicker(@Nullable OnClickListener launchPicker) {
        timeSpinner.setCustomTimePicker(launchPicker);
    }

    /**
     * Checks if the time spinner is currently invisible (with {@link #FLAG_HIDE_TIME}), so the user didn't choose a time.
     * @return True if the time is not visible, false otherwise.
     */
    public boolean isTimeHidden() {
        return timeSpinner.getVisibility() == GONE;
    }

    /**
     * Toggles hiding the Time Spinner and replaces it with a Button.
     * @param enable True to hide the Spinner, false to show it.
     * @param useDarkTheme True if a white icon shall be used, false for a dark one.
     */
    public void setHideTime(boolean enable, final boolean useDarkTheme) {
        if(enable && !shouldHideTime) {
            // hide the time spinner and show a button to show it instead
            timeSpinner.setVisibility(GONE);
            ImageButton timeButton = (ImageButton) LayoutInflater.from(getContext()).inflate(R.layout.time_button, null);
            timeButton.setImageResource(useDarkTheme ? R.drawable.ic_action_time_dark : R.drawable.ic_action_time_light);
            timeButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    setHideTime(false, useDarkTheme);
                }
            });
            this.addView(timeButton);
        } else if(!enable && shouldHideTime) {
            timeSpinner.setVisibility(VISIBLE);
            this.removeViewAt(2);
        }
        shouldHideTime = enable;
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

    /**
     * Gets the custom DateFormat currently used in the DateSpinner to format Calendar strings.
     * If {@link #setDateFormat(java.text.DateFormat)} has not been called yet, it will return null.
     * @return The time format, or null if the Spinner is using the default date format.
     */
    public java.text.DateFormat getCustomDateFormat() {
        return dateSpinner.getCustomDateFormat();
    }

    /**
     * Sets the custom date format to use for formatting Calendar objects to displayable strings in the DateSpinner.
     * @param dateFormat The new DateFormat, or null to use the default format.
     */
    public void setDateFormat(java.text.DateFormat dateFormat) {
        dateSpinner.setDateFormat(dateFormat);
    }

    /**
     * Gets the time format (as java.text.DateFormat) currently used in the TimeSpinner to format Calendar strings.
     * Defaults to the short time instance for your locale.
     * @return The time format, which will never be null.
     */
    public java.text.DateFormat getTimeFormat() {
        return timeSpinner.getTimeFormat();
    }

    /**
     * Sets the time format to use for formatting Calendar objects to displayable strings in the TimeSpinner.
     * @param timeFormat The new time format (as java.text.DateFormat), or null to use the default format.
     */
    public void setTimeFormat(java.text.DateFormat timeFormat) {
        timeSpinner.setTimeFormat(timeFormat);
    }


    /**
     * Sets the minimum allowed date for the DateSpinner.
     * Spinner items and dates in the date picker before the given date will get disabled.
     * Does not affect the TimeSpinner.
     * @param minDate The minimum date, or null to clear the previous min date.
     */
    public void setMinDate(@Nullable Calendar minDate) {
        dateSpinner.setMinDate(minDate);
    }

    /**
     * Gets the current minimum allowed date for the DateSpinner.
     * @return The minimum date, or null if there is none.
     */
    public @Nullable Calendar getMinDate() {
        return dateSpinner.getMinDate();
    }

    /**
     * Sets the maximum allowed date for the DateSpinner.
     * Spinner items and dates in the date picker before the given date will get disabled.
     * Does not affect the TimeSpinner.
     * @param maxDate The maximum date, or null to clear the previous max date.
     */
    public void setMaxDate(@Nullable Calendar maxDate) {
        dateSpinner.setMaxDate(maxDate);
    }

    /**
     * Gets the current maximum allowed date for the DateSpinner.
     * @return The maximum date, or null if there is none.
     */
    public @Nullable Calendar getMaxDate() {
        return dateSpinner.getMaxDate();
    }


    /**
     * Set the flags to use for the picker.
     * @param modeOrFlags A mode of ReminderDatePicker.MODE_... or multiple ReminderDatePicker.FLAG_...
     *                    combined with the | operator.
     */
    public void setFlags(int modeOrFlags) {
        // check each flag and pass it on if needed:
        setHideTime((modeOrFlags & FLAG_HIDE_TIME) != 0, isActivityUsingDarkTheme());
        dateSpinner.setFlags(modeOrFlags);
        timeSpinner.setFlags(modeOrFlags);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // An item has been selected in one of our child spinners, so get the selected Date and call the listeners
        if(listener != null) {
            // catch selecting same date twice
            Calendar date = getSelectedDate();
            if(date != null && !date.equals(lastSelectedDate)) {
                listener.onDateSelected(date);
                lastSelectedDate = date;
            }
        }
    }

    // unused
    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }
}
