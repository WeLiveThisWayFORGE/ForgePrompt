package com.adgad.kboard;

/**
 * Picks black or white text for a key color, and darkens a color for the pressed state.
 */
public final class ColorContrast {

    private ColorContrast() {
    }

    public static int textColorFor(int background) {
        int red = (background >> 16) & 0xFF;
        int green = (background >> 8) & 0xFF;
        int blue = background & 0xFF;
        int brightness = (red * 299 + green * 587 + blue * 114) / 1000;
        if (brightness >= 150) {
            return 0xFF000000;
        }
        return 0xFFFFFFFF;
    }

    public static int darken(int color, float factor) {
        int alpha = (color >>> 24) & 0xFF;
        int red = clamp(Math.round(((color >> 16) & 0xFF) * factor));
        int green = clamp(Math.round(((color >> 8) & 0xFF) * factor));
        int blue = clamp(Math.round((color & 0xFF) * factor));
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }

    private static int clamp(int channel) {
        if (channel < 0) {
            return 0;
        }
        if (channel > 255) {
            return 255;
        }
        return channel;
    }
}
