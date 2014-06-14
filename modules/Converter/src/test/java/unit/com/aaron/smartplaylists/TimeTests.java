package com.aaron.smartplaylists;

import org.junit.Test;

import java.text.ParseException;

import static com.aaron.smartplaylists.Time.TimeUnit.DAYS;
import static com.aaron.smartplaylists.Time.TimeUnit.HOURS;
import static com.aaron.smartplaylists.Time.TimeUnit.MINUTES;
import static com.aaron.smartplaylists.Time.TimeUnit.SECONDS;
import static com.aaron.smartplaylists.Time.TimeUnit.WEEKS;
import static junit.framework.Assert.assertEquals;

/**
 * unit tests for the Time class and TimeUnit enum
 */
public class TimeTests {
    @Test
    public void testGetLargestUnit() {
        final Time t1 = new Time(2, WEEKS);

        final Pair<Integer, Time.TimeUnit> t1InWeeks = t1.getLargestUnit(WEEKS);
        final Pair<Integer, Time.TimeUnit> t1InDays = t1.getLargestUnit(DAYS);
        final Pair<Integer, Time.TimeUnit> t1InHours = t1.getLargestUnit(HOURS);

        assertEquals(2, (int) t1InWeeks.getFirst());
        assertEquals(WEEKS, t1InWeeks.getSecond());
        assertEquals(14, (int) t1InDays.getFirst());
        assertEquals(DAYS, t1InDays.getSecond());
        assertEquals(336, (int) t1InHours.getFirst());
        assertEquals(HOURS, t1InHours.getSecond());

        final Time t2 = new Time();
        t2.setWeeks(0).setDays(3).setHours(5).setMinutes(0).setSeconds(0);

        final Pair<Integer, Time.TimeUnit> t2InWeeks = t2.getLargestUnit(WEEKS);
        final Pair<Integer, Time.TimeUnit> t2InDays = t2.getLargestUnit(DAYS);
        final Pair<Integer, Time.TimeUnit> t2InHours = t2.getLargestUnit(HOURS);

        assertEquals(77, (int) t2InWeeks.getFirst());
        assertEquals(HOURS, t2InWeeks.getSecond());
        assertEquals(77, (int) t2InDays.getFirst());
        assertEquals(HOURS, t2InDays.getSecond());
        assertEquals(77, (int) t2InHours.getFirst());
        assertEquals(HOURS, t2InHours.getSecond());
    }

    @Test
    public void testTimeUnitMin() {
        assertEquals(SECONDS, Time.TimeUnit.min(SECONDS, MINUTES));
        assertEquals(MINUTES, Time.TimeUnit.min(MINUTES, HOURS));
        assertEquals(HOURS, Time.TimeUnit.min(HOURS, DAYS));
        assertEquals(DAYS, Time.TimeUnit.min(DAYS, WEEKS));
    }

    @Test
    public void testTimeParseGood() throws ParseException {
        final Time t1 = Time.parse("1:00");
        assertEquals(0, t1.getWeeks());
        assertEquals(0, t1.getDays());
        assertEquals(0, t1.getHours());
        assertEquals(1, t1.getMinutes());
        assertEquals(0, t1.getSeconds());

        final Time t2 = Time.parse("2:34:56");
        assertEquals(0, t2.getWeeks());
        assertEquals(0, t2.getDays());
        assertEquals(2, t2.getHours());
        assertEquals(34, t2.getMinutes());
        assertEquals(56, t2.getSeconds());

        final Time t3 = Time.parse("1 week");
        assertEquals(1, t3.getWeeks());
        assertEquals(0, t3.getDays());
        assertEquals(0, t3.getHours());
        assertEquals(0, t3.getMinutes());
        assertEquals(0, t3.getSeconds());
    }

    @Test(expected = ParseException.class)
    public void testTimeParseTooManyHourDigits() throws ParseException {
        Time.parse("100:12:45");
    }

    @Test(expected = ParseException.class)
    public void testTimeParseTooManyMinuteDigits() throws ParseException {
        Time.parse("1:123:45");
    }

    @Test(expected = ParseException.class)
    public void testTimeParseTooManySecondDigits() throws ParseException {
        Time.parse("1:13:450");
    }

    @Test(expected = ParseException.class)
    public void testTimeParseMisspelledDays() throws ParseException {
        Time.parse("2 daays");
    }

    @Test(expected = ParseException.class)
    public void testTimeParseMisspelledHours() throws ParseException {
        Time.parse("3 hoors");
    }

    @Test(expected = ParseException.class)
    public void testTimeParseUnsupportedUnit() throws ParseException {
        Time.parse("4 months");
    }
}
