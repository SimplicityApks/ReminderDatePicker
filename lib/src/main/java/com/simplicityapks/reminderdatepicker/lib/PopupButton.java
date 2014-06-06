package com.simplicityapks.reminderdatepicker.lib;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;

/**
 * Base class for both DateButton and TimeButton. This base class takes care of inflating the popup
 * and managing selection.
 *
 * Allows to use a custom last list item, which won't get selected on click. Instead,
 * onLastItemClick() will be called.
 */
public abstract class PopupButton extends Button{
    public PopupButton(Context context) {
        this(context, null);
    }

    public PopupButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PopupButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

    }


    public abstract String getLastItem();

    public abstract void onLastItemClick();
}
