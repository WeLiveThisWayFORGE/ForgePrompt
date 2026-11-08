package com.adgad.kboard;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ColorAndSizeTest {

    @Test
    public void picksReadableText() {
        assertEquals(0xFF000000, ColorContrast.textColorFor(0xFFFFFFFF));
        assertEquals(0xFFFFFFFF, ColorContrast.textColorFor(0xFF000000));
        assertEquals(0xFF000000, ColorContrast.textColorFor(0xFFFFEB3B));
        assertEquals(0xFFFFFFFF, ColorContrast.textColorFor(0xFFF44336));
        assertEquals(0xFF000000, ColorContrast.textColorFor(0xFF80CBC4));
        assertEquals(0xFFFFFFFF, ColorContrast.textColorFor(0xFF616161));
    }

    @Test
    public void darkenKeepsAlphaAndLowersChannels() {
        int darker = ColorContrast.darken(0x80FF0000, 0.5f);
        assertEquals(0x80, (darker >>> 24) & 0xFF);
        assertTrue(((darker >> 16) & 0xFF) < 0xFF);
        assertEquals(0, (darker >> 8) & 0xFF);
        assertEquals(0, darker & 0xFF);
    }

    @Test
    public void mediumMatchesOriginalScale() {
        assertEquals(1f, KeySize.heightScale(KeySize.MEDIUM), 0f);
        assertEquals(1f, KeySize.textScale(KeySize.MEDIUM), 0f);
        assertEquals(14, KeySize.maxLabelChars(KeySize.MEDIUM));
        assertEquals(1f, KeySize.heightScale(null), 0f);
        assertEquals(1f, KeySize.heightScale("other"), 0f);
    }

    @Test
    public void smallIsSmallerAndLargeIsLarger() {
        assertTrue(KeySize.heightScale(KeySize.SMALL) < 1f);
        assertTrue(KeySize.textScale(KeySize.SMALL) < 1f);
        assertTrue(KeySize.maxLabelChars(KeySize.SMALL) > 14);
        assertTrue(KeySize.heightScale(KeySize.LARGE) > 1f);
        assertTrue(KeySize.textScale(KeySize.LARGE) > 1f);
        assertTrue(KeySize.maxLabelChars(KeySize.LARGE) < 14);
    }
}
