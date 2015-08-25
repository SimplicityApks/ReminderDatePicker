package com.simplicityapks.reminderdatepicker.lib;

/**
 * Base interface for list items used by a PickerSpinnerAdapter. Enables having both primary and secondary text.
 */
public interface TwinTextItem {

    /**
     * Base class for fast creating of TwinTextItems.
     */
    class Simple implements TwinTextItem{
        private final CharSequence primary, secondary;
        private final int id;

        /**
         * Constructs a new simple TwinTextItem.
         * @param primaryText The text to be shown primarily.
         * @param secondaryText The text to be shown secondarily.
         */
        public Simple(CharSequence primaryText, CharSequence secondaryText) {
            this(primaryText, secondaryText, 0);
        }

        /**
         * Constructs a new simple TwinTextItem.
         * @param primaryText The text to be shown primarily.
         * @param secondaryText The text to be shown secondarily.
         * @param itemId The id value to find this item with.
         */
        public Simple(CharSequence primaryText, CharSequence secondaryText, int itemId) {
            primary = primaryText;
            secondary = secondaryText;
            id = itemId;
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
        public int getId() {
            return id;
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
     * Returns the identifier with which the item can be found and removed from the adapter.
     * Does not have to be unique for each item if you don't use runtime item modifications.
     */
    int getId();

    /**
     * Gets the text to be shown primarily.
     * @return The text (probably as String).
     */
    CharSequence getPrimaryText();

    /**
     * Gets the text to be shown secondarily.
     * @return The text (probably as String).
     */
    CharSequence getSecondaryText();

    /**
     * Whether this item is enabled. Return false to show this spinner item in a disabled state with grey text.
     * @return true to enable, false to disable this item.
     */
    boolean isEnabled();
}
