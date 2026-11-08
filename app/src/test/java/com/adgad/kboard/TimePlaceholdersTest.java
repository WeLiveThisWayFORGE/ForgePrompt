package com.adgad.kboard;

import org.junit.Test;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class TimePlaceholdersTest {

    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");
    private static final Locale US = Locale.US;

    @Test
    public void propertyPhraseUsesLocalClockFiveMinutesAhead() {
        String saved = "I'm unable to access your property. If you get this message before {time+5m} and can provide access, please respond.";
        String expanded = TimePlaceholders.expand(saved, clock(atUtc(2026, 11, 8, 23, 58)), UTC, US, false);

        assertEquals(
                "I'm unable to access your property. If you get this message before 12:03 AM and can provide access, please respond.",
                expanded);
        assertFalse(expanded.contains("{time"));
    }

    @Test
    public void midnightBoundaryAheadAndBehind() {
        assertEquals("12:03 AM", expand("{time+5m}", atUtc(2026, 11, 8, 23, 58), false));
        assertEquals("00:03", expand("{time+5m}", atUtc(2026, 11, 8, 23, 58), true));
        assertEquals("11:57 PM", expand("{time-5m}", atUtc(2026, 11, 9, 0, 2), false));
        assertEquals("23:57", expand("{time-5m}", atUtc(2026, 11, 9, 0, 2), true));
    }

    @Test
    public void anyPositiveWholeMinuteOffsetIncludingDateRollover() {
        long at2340 = atUtc(2026, 11, 8, 23, 40);
        assertEquals("23:45", expand("{time+5m}", at2340, true));
        assertEquals("23:50", expand("{time+10m}", at2340, true));
        assertEquals("00:00", expand("{time+20m}", at2340, true));
        assertEquals("00:10", expand("{time+30m}", at2340, true));
        assertEquals(
                "23:45 / 23:50 / 00:00 / 00:10",
                expand("{time+5m} / {time+10m} / {time+20m} / {time+30m}", at2340, true));
        assertEquals("23:58", expand("{time+1440m}", atUtc(2026, 11, 8, 23, 58), true));
        assertEquals("01:20", expand("{time+90m}", atUtc(2026, 11, 8, 23, 50), true));
        assertEquals("01:40", expand("{time+100000m}", atUtc(2026, 11, 8, 15, 0), true));
    }

    @Test
    public void minuteHourAndCombinedOffsets() {
        long at1500 = atUtc(2026, 11, 8, 15, 0);
        assertEquals("3:00 PM", expand("{time}", at1500, false));
        assertEquals("15:00", expand("{time}", at1500, true));
        assertEquals("3:00 PM", expand("{time+0m}", at1500, false));
        assertEquals("4:30 PM", expand("{time+90m}", at1500, false));
        assertEquals("16:30", expand("{time+1h30m}", at1500, true));
        assertEquals("13:45", expand("{time-1h15m}", at1500, true));
        assertEquals("00:05", expand("{time+1h5m}", atUtc(2026, 11, 8, 23, 0), true));
    }

    @Test
    public void deviceHourCycleOverridesLocaleDefault() {
        long afternoon = atUtc(2026, 11, 8, 15, 5);
        assertEquals("15:05", expand("{time}", afternoon, Locale.GERMANY, true));
        assertEquals("3:05 PM", expand("{time}", afternoon, Locale.GERMANY, false));
        assertEquals("3:05 PM", expand("{time}", afternoon, Locale.US, false));
        assertEquals("15:05", expand("{time}", afternoon, Locale.US, true));
    }

    @Test
    public void eachExpansionReadsTheClockAgain() {
        long[] now = {atUtc(2026, 11, 8, 10, 0)};
        int[] reads = {0};
        TimePlaceholders.Clock clock = new TimePlaceholders.Clock() {
            @Override
            public long nowMillis() {
                reads[0]++;
                return now[0];
            }
        };
        String first = TimePlaceholders.expand("{time} and {time+5m}", clock, UTC, US, true);
        assertEquals(1, reads[0]);
        assertEquals("10:00 and 10:05", first);

        now[0] = atUtc(2026, 11, 8, 10, 1);
        String second = TimePlaceholders.expand("{time} and {time+5m}", clock, UTC, US, true);
        assertEquals(2, reads[0]);
        assertEquals("10:01 and 10:06", second);
        assertFalse(first.equals(second));
    }

    @Test
    public void unknownAndMalformedTokensStayLiteral() {
        long now = atUtc(2026, 11, 8, 15, 0);
        String[] literals = {
                "{send}",
                "{time+}",
                "{time+5}",
                "{time+5x}",
                "{time+5m }",
                "{time +5m}",
                "{time+m}",
                "{time+5hm}",
                "{time+5m2h}",
                "{Time+5m}",
                "{time+100001m}",
                "{time+1000000m}",
                "{time+9999999999999999999m}",
                "{time+999999999999999999999999m}",
                "{nottime}",
                "before {time+5m more} after",
                "{time+5m"
        };
        for (String literal : literals) {
            assertEquals(literal, expand(literal, now, true));
        }
        assertEquals("see {time+} at 15:07", expand("see {time+} at {time+7m}", now, true));
    }

    @Test
    public void nullAndPlainTextAreUntouched() {
        assertNull(TimePlaceholders.expand(null, clock(0), UTC, US, true));
        String plain = "Good morning, no token here.";
        assertSame(plain, TimePlaceholders.expand(plain, clock(0), UTC, US, true));
    }

    @Test
    public void savedExportKeepsTheTokenAndColor() {
        String template = "I'm unable to access your property. If you get this message before {time+5m} and can provide access, please respond.";
        UserKey key = new UserKey(template, 0xFF009688);
        String json = KeyStore.toJson(Arrays.asList(key));
        UserKey loaded = KeyStore.fromJson(json).get(0);

        assertEquals(template, loaded.text);
        assertEquals(Integer.valueOf(0xFF009688), loaded.color);
        assertEquals("12:03 AM", expand("{time+5m}", atUtc(2026, 11, 8, 23, 58), false));
    }

    private static String expand(String input, long now, boolean is24Hour) {
        return expand(input, now, US, is24Hour);
    }

    private static String expand(String input, long now, Locale locale, boolean is24Hour) {
        return TimePlaceholders.expand(input, clock(now), UTC, locale, is24Hour);
    }

    private static TimePlaceholders.Clock clock(final long now) {
        return new TimePlaceholders.Clock() {
            @Override
            public long nowMillis() {
                return now;
            }
        };
    }

    private static long atUtc(int year, int month, int day, int hour, int minute) {
        Calendar calendar = Calendar.getInstance(UTC, US);
        calendar.clear();
        calendar.set(year, month - 1, day, hour, minute, 0);
        return calendar.getTimeInMillis();
    }
}
