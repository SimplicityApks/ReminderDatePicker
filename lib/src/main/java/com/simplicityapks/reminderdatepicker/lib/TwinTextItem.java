package com.simplicityapks.reminderdatepicker.lib;

/**
 * Base interface for list items used by a PickerSpinnerAdapter. Enables having both primary and secondary text.
 */
public interface TwinTextItem {

    /**
     * Base class for fast creating of TwinTextItems.
     */
    public class Simple implements TwinTextItem{
        private final CharSequence primary, secondary;

        /**
         * Constructs a new simple TwinTextItem.
         * @param primaryText The text to be shown primarily.
         * @param secondaryText The text to be shown secondarily.
         */
        public Simple(CharSequence primaryText, CharSequence secondaryText) {
            primary = primaryText;
            secondary = secondaryText;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public CharSequence getPrimaryText() {
            return primary;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public CharSequence getSecondaryText() {
            return secondary;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isEnabled() {
            return true;
        }
    }

    /**
     * Gets the text to be shown primarily.
     * @return The text (probably as String).
     */
    public CharSequence getPrimaryText();

    /**
     * Gets the text to be shown secondarily.
     * @return The text (probably as String).
     */
    public CharSequence getSecondaryText();

    /**
     * Whether this item is enabled. Return false to show this spinner item in a disabled state with grey text.
     * @return true to enable, false to disable this item.
     */
    public boolean isEnabled();
}
