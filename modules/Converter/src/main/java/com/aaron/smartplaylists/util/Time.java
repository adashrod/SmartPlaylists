package com.aaron.smartplaylists.util;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A Time object represents a period of time, i.e. a length of time without context of the beginning or end of the period.
 */
public class Time {
    private int weeks;
    private int days;
    private int hours;
    private int minutes;
    private int seconds;

    private static final Pattern clockFormat = Pattern.compile("(\\d{1,2})(:\\d\\d){1,2}");
    private static final Pattern wordFormat = Pattern.compile("^\\s*\\d+\\s*(week|day|hour|minute|second)s?\\s*$", Pattern.CASE_INSENSITIVE);

    public Time() {
        weeks = days = hours = minutes = seconds = 0;
    }

    public Time(final int hours, final int minutes, final int seconds) {
        this.hours = hours;
        this.minutes = minutes;
        this.seconds = seconds;
        normalize();
    }

    public Time(final int number, final TimeUnit timeUnit) {
        weeks = 0;
        days = 0;
        hours = 0;
        minutes = 0;
        seconds = 0;
        if (timeUnit == TimeUnit.SECONDS) {
            seconds = number;
        } else if (timeUnit == TimeUnit.MINUTES) {
            minutes = number;
        } else if (timeUnit == TimeUnit.HOURS) {
            hours = number;
        } else if (timeUnit == TimeUnit.DAYS) {
            days = number;
        } else if (timeUnit == TimeUnit.WEEKS) {
            weeks = number;
        }
        normalize();
    }

    public int getWeeks() {
        return weeks;
    }

    public Time setWeeks(final int weeks) {
        if (weeks >= 0) {
            this.weeks = weeks;
        }
        return this;
    }

    public int getDays() {
        return days;
    }

    public Time setDays(final int days) {
        if (days >= 0) {
            this.days = days;
            normalize();
        }
        return this;
    }

    public int getHours() {
        return hours;
    }

    public Time setHours(final int hours) {
        if (hours >= 0) {
            this.hours = hours;
            normalize();
        }
        return this;
    }

    public int getMinutes() {
        return minutes;
    }

    public Time setMinutes(final int minutes) {
        if (minutes >= 0) {
            this.minutes = minutes;
            normalize();
        }
        return this;
    }

    public int getSeconds() {
        return seconds;
    }

    public Time setSeconds(final int seconds) {
        if (seconds >= 0) {
            this.seconds = seconds;
            normalize();
        }
        return this;
    }

    private static Time parseAsClock(final String timeString) {
        final String[] stringParts = timeString.split(":");
        final int hours;
        final int minutes;
        final int seconds;
        if (stringParts.length == 2) {
            hours = 0;
            minutes = Integer.parseInt(stringParts[0]);
            seconds = Integer.parseInt(stringParts[1]);
        } else {
            hours = Integer.parseInt(stringParts[0]);
            minutes = Integer.parseInt(stringParts[1]);
            seconds = Integer.parseInt(stringParts[2]);
        }
        return new Time(hours, minutes, seconds);
    }

    private static Time parseAsWords(final String timeString) {
        final String[] stringParts = timeString.split("\\s");
        return new Time(Integer.parseInt(stringParts[0]), TimeUnit.parseTimeUnit(stringParts[1]));
    }

    /**
     * Parses a string that represents a time. Inputs can be in a clock format (1:00, 2:30:00, 0:45) or as a number and
     * unit of time (10 seconds, 1 hour, 2 weeks)
     * @param timeString a period of time as a string
     * @return a Time object
     * @throws ParseException error parsing the input
     */
    public static Time parse(final String timeString) throws ParseException {
        if (clockFormat.matcher(timeString).matches()) {
            return parseAsClock(timeString);
        } else if (wordFormat.matcher(timeString).matches()) {
            return parseAsWords(timeString);
        } else {
            int errorOffset = -1;
            if (timeString.contains(":")) { // assume that the intended input format was "(hh:)mm:ss"
                int digitCount = 0; // count the digits for each token, i.e. between colons
                int colonCount = 0; // count the total number of colons
                for (int i = 0; i < timeString.length(); i++) {
                    final char c = timeString.charAt(i);
                    if (Character.isDigit(c)) {
                        digitCount++;
                    } else if (c == ':') {
                        colonCount++;
                        if (colonCount >= 2 && digitCount < 2) {
                            // the second colon was encountered too soon/too many colons
                            errorOffset = i;
                            break;
                        }
                        digitCount = 0;
                    } else {
                        // invalid char
                        errorOffset = i;
                        break;
                    }
                    if (digitCount > 2 || colonCount > 2) {
                        // too many colons/too many digits in a token
                        errorOffset = i;
                        break;
                    }
                }
                if (errorOffset == -1 && digitCount != 2) {
                    // incorrect number of digits in the last token, should always be 2
                    errorOffset = timeString.length();
                }
            } else { // assume that the intended format was "n unit"
                final Pattern pattern = Pattern.compile("(\\s*\\d+\\s*)(\\w*)");
                final Matcher matcher = pattern.matcher(timeString);
                if (matcher.matches()) {
                    errorOffset = matcher.start(matcher.groupCount());
                } else {
                    errorOffset = 0;
                }
            }
            throw new ParseException(String.format("Failed to parse the string \"%s\" as a time", timeString), errorOffset);
        }
    }

    /**
     * Returns the Time object as two pieces of data:
     * - a number
     * - the largest unit that the Time object comprises
     * The largestAllowed parameter determines the largest unit type that will be returned. The TimeUnit return can be
     * smaller than largestAllowed if necessary to prevent loss of data. Note Example 2 below where all return values
     * are in hours. This is because returning <3, DAYS> would obscure the remaining 5 hours.
     * Example 1
     * A Time object t is "2 weeks"
     * t.getLargestUnit(TimeUnit.WEEKS) == <2, WEEKS>
     * t.getLargestUnit(TimeUnit.DAYS) == <14, DAYS>
     * t.getLargestUnit(TimeUnit.HOURS) == <336, HOURS>
     *
     * Example 2
     * t is 3 days + 5 hours
     * t.getLargestUnit(TimeUnit.WEEKS) == <77, HOURS>
     * t.getLargestUnit(TimeUnit.DAYS) == <77, HOURS>
     * t.getLargestUnit(TimeUnit.HOURS) == <77, HOURS>
     * @param largestAllowed the largest unit type that will be returned, regardless of how much time is in the object
     * @return a pair with an Integer and a TimeUnit
     */
    public Pair<Integer, TimeUnit> getLargestUnit(final TimeUnit largestAllowed) {
        final boolean hasDays = days > 0;
        final boolean hasHours = hours > 0;
        final boolean hasMinutes = minutes > 0;
        final boolean hasSeconds = seconds > 0;

        // highestResolutionNecessary is the most granular unit (seconds being the highest resolution) needed to
        // represent the time without truncating any data.
        final TimeUnit highestResolutionNecessary;
        if (hasSeconds) {
            highestResolutionNecessary = TimeUnit.SECONDS;
        } else if (hasMinutes) {
            highestResolutionNecessary = TimeUnit.MINUTES;
        } else if (hasHours) {
            highestResolutionNecessary = TimeUnit.HOURS;
        } else if (hasDays) {
            highestResolutionNecessary = TimeUnit.DAYS;
        } else {
            highestResolutionNecessary = TimeUnit.WEEKS;
        }

        deNormalize(TimeUnit.min(largestAllowed, highestResolutionNecessary));
        if (weeks != 0) {
            return new Pair<>(weeks, TimeUnit.WEEKS);
        } else if (days != 0) {
            return new Pair<>(days, TimeUnit.DAYS);
        } else if (hours != 0) {
            return new Pair<>(hours, TimeUnit.HOURS);
        } else if (minutes != 0) {
            return new Pair<>(minutes, TimeUnit.MINUTES);
        } else {
            return new Pair<>(seconds, TimeUnit.SECONDS);
        }
    }

    private Time normalize() {
        if (seconds >= 60) {
            minutes += seconds / 60;
            seconds %= 60;
        }
        if (minutes >= 60) {
            hours += minutes / 60;
            minutes %= 60;
        }
        if (hours >= 24) {
            days += hours / 24;
            hours %= 24;
        }
        if (days >= 7) {
            weeks += days / 7;
            days %= 7;
        }
        return this;
    }

    /**
     * Modifies this to be in a de-normalized state. All units greater than largestAllowed will be zeroed and their
     * values put into the largestAllowed unit. E.g. if this == 2 days, after deNormalize(HOURS), this == 48 hours
     * @param largestAllowed the largest TimeUnit that won't be zeroed out
     * @return self
     */
    private Time deNormalize(final TimeUnit largestAllowed) {
        if (largestAllowed == TimeUnit.DAYS) {
            days += weeks * 7;
            weeks = 0;
        } else if (largestAllowed == TimeUnit.HOURS) {
            days += weeks * 7;
            hours += days * 24;
            weeks = 0;
            days = 0;
        } else if (largestAllowed == TimeUnit.MINUTES) {
            days += weeks * 7;
            hours += days * 24;
            minutes += hours * 60;
            weeks = 0;
            days = 0;
            hours = 0;
        } else if (largestAllowed == TimeUnit.SECONDS) {
            days += weeks * 7;
            hours += days * 24;
            minutes += hours * 60;
            seconds += minutes * 60;
            weeks = 0;
            days = 0;
            hours = 0;
            minutes = 0;
        }
        return this;
    }

    @Override
    public String toString() {
        normalize();
        return (hours == 0 ? "" : (hours < 10 ? "0" + hours : hours) + ":") +
            (minutes < 10 ? "0" + minutes : minutes) + ":" +
            (seconds < 10 ? "0" + seconds : seconds);
    }

    /**
     * A TimeUnit is a unit used for measuring time (seconds, minutes, hours, etc.).
     */
    public static enum TimeUnit {
        // it's important that the smaller units have smaller rank values, so if any more are added, be sure that
        // smaller times have smaller ranks so that min() works properly
        SECONDS(0),
        MINUTES(1),
        HOURS(2),
        DAYS(3),
        WEEKS(4);

        private final int rank;
        private final static Map<String, TimeUnit> TIME_UNIT_MAP = new HashMap<>();

        static {
            TIME_UNIT_MAP.put("second", SECONDS);
            TIME_UNIT_MAP.put("seconds", SECONDS);
            TIME_UNIT_MAP.put("minute", MINUTES);
            TIME_UNIT_MAP.put("minutes", MINUTES);
            TIME_UNIT_MAP.put("hour", HOURS);
            TIME_UNIT_MAP.put("hours", HOURS);
            TIME_UNIT_MAP.put("day", DAYS);
            TIME_UNIT_MAP.put("days", DAYS);
            TIME_UNIT_MAP.put("week", WEEKS);
            TIME_UNIT_MAP.put("weeks", WEEKS);
        }

        private TimeUnit(final int rank) {
            this.rank = rank;
        }

        public static TimeUnit parseTimeUnit(final String timeString) {
            return TIME_UNIT_MAP.get(timeString.toLowerCase());
        }

        public static TimeUnit min(final TimeUnit a, final TimeUnit b) {
            return a.rank < b.rank ? a : b;
        }
    }
}
