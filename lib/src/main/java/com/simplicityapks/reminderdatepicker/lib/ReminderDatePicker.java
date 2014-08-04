package com.simplicityapks.reminderdatepicker.lib;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;

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
    public static final int MODE_GOOGLE = 0;     // 00000

    /**
     * Mode for {@link #setFlags(int)}. Include all possible items and show numbers in the time spinner.
     */
    public static final int MODE_EVERYTHING = 15;// 01111

    /**
     * Flag for {@link #setFlags(int)}. Include a yesterday and last weekday item.
     */
    public static final int FLAG_PAST = 1;       // 00001

    /**
     * Flag for {@link #setFlags(int)}. Include a month item exactly one month from today.
     */
    public static final int FLAG_MONTH = 2;      // 00010

    /**
     * Flag for {@link #setFlags(int)}. Include a noon and late night item in the time spinner.
     */
    public static final int FLAG_MORE_TIME = 4;  // 00100

    /**
     * Flag for {@link #setFlags(int)}. Show numeric time in the time spinner view. Note that time
     * will always be shown in dropdown.
     */
    public static final int FLAG_NUMBERS = 8;    // 01000

    /**
     * Flag for {@link #setFlags(int)}. Hide the time picker and show a button to show it.
     */
    public static final int FLAG_HIDE_TIME = 16; // 10000

    // has FLAG_HIDE_TIME been set?
    private boolean shouldHideTime = false;

    private DateSpinner dateSpinner;
    private TimeSpinner timeSpinner;

    // This listener doesn't have to be implemented, if it is null just ignore it
    private OnDateSelectedListener listener = null;

    // To catch twice selecting the same date:
    private Calendar lastSelectedDate = null;

    // To keep track whether we need to selectDefaultDate in onAttachToWindow():
    private boolean restoringViewState = false;

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
        }
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(state);
        if(state != null)
            restoringViewState = true;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        // we may need to initialize the selected date
        if(!restoringViewState)
            selectDefaultDate();
    }

    private void selectDefaultDate() {
        Calendar calendar = Calendar.getInstance();
        boolean moreTime = timeSpinner.isShowingMoreTimeItems();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        // if FLAG_HIDE_TIME has been set, set it to tomorrow morning:
        if(shouldHideTime) {
            hour = 9;
            calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) + 1);
        }
        else if(hour < 9)               hour = 9;
        else if(hour < 12 && moreTime)  hour = 12;
        else if(hour < 13 && !moreTime) hour = 13;
        else if(hour < 14 && moreTime)  hour = 14;
        else if(hour < 17)              hour = 17;
        else if(hour < 20)              hour = 20;
        else if(hour < 23 && moreTime)  hour = 23;
        else {
            hour = 9;
            calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) + 1);
        }
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        setSelectedDate(calendar);
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
        }
    }

    /**
     * Sets the Spinners' date selection as integers considering only day.
     */
    public void setSelectedDate(int year, int month, int day) {
        dateSpinner.setSelectedDate(new GregorianCalendar(year, month, day));
    }

    /**
     * Sets the Spinners' time selection as integers considering only time.
     */
    public void setSelectedTime(int hour, int minute) {
        timeSpinner.setSelectedTime(hour, minute);
    }

    /**
     * Implement this interface if you want to be notified whenever the selected date changes.
     */
    public void setOnDateSelectedListener(OnDateSelectedListener listener) {
        this.listener = listener;
    }

    /**
     * Sets a custom listener whose onClick method will be called to create and handle the custom date picker.
     * You should call {@link #setSelectedDate(int, int, int)} when the custom picker is finished.
     * @param launchPicker An {@link android.view.View.OnClickListener} whose onClick method will be
     *                     called to show the custom date picker.
     */
    public void setCustomDatePicker(OnClickListener launchPicker) {
        dateSpinner.setCustomDatePicker(launchPicker);
    }

    /**
     * Sets a custom listener whose onClick method will be called to create and handle the custom time picker.
     * You should call {@link #setSelectedTime} when the custom picker is finished.
     * @param launchPicker An {@link android.view.View.OnClickListener} whose onClick method will be
     *                     called to show the custom time picker.
     */
    public void setCustomTimePicker(OnClickListener launchPicker) {
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
            if(!date.equals(lastSelectedDate))
                listener.onDateSelected(date);
            lastSelectedDate = date;
        }
    }

    // unused
    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }
}
