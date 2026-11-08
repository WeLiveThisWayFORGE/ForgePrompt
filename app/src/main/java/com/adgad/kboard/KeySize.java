package com.adgad.kboard;

/**
 * Key size preference. Medium matches the original kboard key height and text size.
 * The keyboard grid stays four keys across (or one, in single-row mode); size changes
 * height and the label size.
 */
public final class KeySize {

    public static final String PREF = "keysize";
    public static final String SMALL = "small";
    public static final String MEDIUM = "medium";
    public static final String LARGE = "large";

    private KeySize() {
    }

    public static float heightScale(String value) {
        if (SMALL.equals(value)) {
            return 0.75f;
        }
        if (LARGE.equals(value)) {
            return 1.3f;
        }
        return 1f;
    }

    public static float textScale(String value) {
        if (SMALL.equals(value)) {
            return 0.85f;
        }
        if (LARGE.equals(value)) {
            return 1.2f;
        }
        return 1f;
    }

    /**
     * Characters shown before the label is ellipsized. Key width does not change, so larger
     * text gets a shorter limit and smaller text a slightly longer one. Medium is the original 14.
     */
    public static int maxLabelChars(String value) {
        if (SMALL.equals(value)) {
            return 16;
        }
        if (LARGE.equals(value)) {
            return 12;
        }
        return 14;
    }
}
