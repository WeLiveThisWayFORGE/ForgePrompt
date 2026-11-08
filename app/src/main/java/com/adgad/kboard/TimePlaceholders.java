package com.adgad.kboard;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Replaces time tokens in text that is about to be inserted.
 * The saved key keeps the token. Each expansion reads the clock again.
 * Nothing is sent and nothing is loaded from the network.
 */
public final class TimePlaceholders {

    public interface Clock {
        long nowMillis();
    }

    /**
     * Largest absolute offset that is still turned into a clock time.
     * A larger value is left as typed so a huge number cannot overflow the clock.
     * 100000 minutes is a bit more than 69 days.
     */
    static final long MAX_OFFSET_MINUTES = 100_000L;

    private static final Pattern TOKEN = Pattern.compile(
            "\\{time(?:([+-])(?:(\\d{1,18})h)?(?:(\\d{1,18})m)?)?\\}");

    private TimePlaceholders() {
    }

    public static String expand(String input, Clock clock, TimeZone zone, Locale locale, boolean is24Hour) {
        if (input == null || input.indexOf("{time") < 0) {
            return input;
        }
        long now = clock.nowMillis();
        StringBuilder out = new StringBuilder(input.length());
        int index = 0;
        while (index < input.length()) {
            int start = input.indexOf("{time", index);
            if (start < 0) {
                out.append(input.substring(index));
                break;
            }
            out.append(input, index, start);
            int end = input.indexOf('}', start);
            if (end < 0) {
                out.append(input.substring(start));
                break;
            }
            String token = input.substring(start, end + 1);
            Long offsetMinutes = offsetMinutes(token);
            String formatted = offsetMinutes == null
                    ? null
                    : format(now, offsetMinutes, zone, locale, is24Hour);
            out.append(formatted == null ? token : formatted);
            index = end + 1;
        }
        return out.toString();
    }

    static Long offsetMinutes(String token) {
        Matcher matcher = TOKEN.matcher(token);
        if (!matcher.matches()) {
            return null;
        }
        String sign = matcher.group(1);
        String hours = matcher.group(2);
        String minutes = matcher.group(3);
        if (sign == null) {
            return 0L;
        }
        if (hours == null && minutes == null) {
            return null;
        }
        long total = 0;
        try {
            if (hours != null) {
                total = Math.addExact(total, Math.multiplyExact(Long.parseLong(hours), 60L));
            }
            if (minutes != null) {
                total = Math.addExact(total, Long.parseLong(minutes));
            }
        } catch (NumberFormatException | ArithmeticException ignored) {
            return null;
        }
        if (total > MAX_OFFSET_MINUTES) {
            return null;
        }
        return "-".equals(sign) ? -total : total;
    }

    static String format(long nowMillis, long offsetMinutes, TimeZone zone, Locale locale, boolean is24Hour) {
        if (offsetMinutes > MAX_OFFSET_MINUTES || offsetMinutes < -MAX_OFFSET_MINUTES) {
            return null;
        }
        try {
            Math.addExact(nowMillis, Math.multiplyExact(offsetMinutes, 60_000L));
            Calendar calendar = Calendar.getInstance(zone, locale);
            calendar.setTimeInMillis(nowMillis);
            calendar.add(Calendar.MINUTE, (int) offsetMinutes);
            int year = calendar.get(Calendar.YEAR);
            if (year < 1 || year > 9999) {
                return null;
            }
            SimpleDateFormat format = new SimpleDateFormat(timePattern(locale, is24Hour), locale);
            format.setTimeZone(zone);
            return format.format(calendar.getTime()).replace('\u202F', ' ').replace('\u00A0', ' ');
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    static String timePattern(Locale locale, boolean is24Hour) {
        DateFormat base = DateFormat.getTimeInstance(DateFormat.SHORT, locale);
        String pattern = base instanceof SimpleDateFormat
                ? ((SimpleDateFormat) base).toPattern()
                : (is24Hour ? "HH:mm" : "h:mm a");
        if (is24Hour) {
            pattern = pattern.replaceAll("[hHkK]+", "HH");
            pattern = pattern.replaceAll("[\\s\\u00A0\\u202F]*a", "");
            return pattern.trim();
        }
        pattern = pattern.replaceAll("[HkK]+", "h");
        if (!pattern.contains("a")) {
            pattern = pattern + " a";
        }
        return pattern;
    }
}
