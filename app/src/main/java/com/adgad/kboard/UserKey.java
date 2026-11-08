package com.adgad.kboard;

/**
 * One phrase or command key. {@code color} is an ARGB int, or null to keep the
 * keyboard's background color. Older saves are plain strings and load with a null color.
 */
public class UserKey {
    public String text;
    public Integer color;

    public UserKey() {
        this("", null);
    }

    public UserKey(String text) {
        this(text, null);
    }

    public UserKey(String text, Integer color) {
        this.text = text;
        this.color = color;
    }
}
