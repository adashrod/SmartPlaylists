package com.adashrod.smartplaylists.converters;

import com.adashrod.smartplaylists.playlists.AgnosticSmartPlaylist;
import com.adashrod.smartplaylists.playlists.MetadataField;
import com.adashrod.smartplaylists.playlists.Operator;
import com.adashrod.smartplaylists.playlists.Order;
import com.adashrod.smartplaylists.playlists.PlaylistType;
import com.adashrod.smartplaylists.playlists.Rule;
import com.adashrod.smartplaylists.playlists.XbmcSmartPlaylist;
import com.adashrod.timeperiod.TimePeriod;
import com.adashrod.timeperiod.TimePeriodFormat;
import com.adashrod.timeperiod.TimeUnit;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import javafx.util.Pair;
import org.apache.log4j.Logger;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * todo: replace getMatchAll* and getOrder* with BiMaps
 * The meat of XBMC playlist conversion. Since v11 and v12 XBMC playlists have an identical java API, the only
 * difference needed to handle the two separately is casting them when passing to functions in this class.
 */
public class XbmcPlaylistConverterTools {
    private static final Logger logger = Logger.getLogger(XbmcPlaylistConverterTools.class);

    private static final BiMap<String, PlaylistType> PLAYLIST_TYPE_MAP = HashBiMap.create();
    private static final BiMap<String, MetadataField> STRING_FIELD_MAP = HashBiMap.create();
    private static final BiMap<String, MetadataField> NUMBER_FIELD_MAP = HashBiMap.create();
    private static final BiMap<String, MetadataField> DATE_FIELD_MAP = HashBiMap.create();
    private static final Collection<BiMap<String, MetadataField>> FIELD_MAPS = new HashSet<>();
    private static final BiMap<String, Operator> STRING_OPERATOR_MAP = HashBiMap.create();
    private static final BiMap<String, Operator> NUMBER_OPERATOR_MAP = HashBiMap.create();
    private static final BiMap<String, Operator> DATE_OPERATOR_MAP = HashBiMap.create();
    private static final Map<TimeUnit, String> TIME_UNIT_MAP = new HashMap<>();
    private static final Set<String> STRING_FIELD_KEYS;
    private static final Set<String> NUMBER_FIELD_KEYS;
    private static final Set<String> DATE_FIELD_KEYS;
    private static final Set<MetadataField> STRING_FIELD_VALUES;
    private static final Set<MetadataField> NUMBER_FIELD_VALUES;
    private static final Set<MetadataField> DATE_FIELD_VALUES;
    private static final String DATE_FORMAT_STRING = "yyyy-MM-dd";
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat(DATE_FORMAT_STRING);
    private static final TimePeriodFormat TIME_FORMAT_HMS = new TimePeriodFormat("hh:mm:ss").setMaxUnit(TimeUnit.HOUR);
    private static final TimePeriodFormat TIME_FORMAT_MS = new TimePeriodFormat("mm:ss");

    private static final boolean defaultMatchAll = false;
    private static final MetadataField defaultOrderByField = MetadataField.TITLE;
    private static final boolean defaultOrderIsAscending = true;
    private static final int defaultLimit = 0; // no limit

    static {
        PLAYLIST_TYPE_MAP.put("songs", PlaylistType.MUSIC);
        PLAYLIST_TYPE_MAP.put("artists", PlaylistType.ARTISTS);
        PLAYLIST_TYPE_MAP.put("albums", PlaylistType.ALBUMS);
        PLAYLIST_TYPE_MAP.put("mixed", PlaylistType.MIXED);
        STRING_FIELD_MAP.put("artist", MetadataField.ARTIST);
        STRING_FIELD_MAP.put("albumartist", MetadataField.ALBUM_ARTIST);
        STRING_FIELD_MAP.put("title", MetadataField.TITLE);
        STRING_FIELD_MAP.put("album", MetadataField.ALBUM);
        STRING_FIELD_MAP.put("genre", MetadataField.GENRE);
        STRING_FIELD_MAP.put("path", MetadataField.PATH);
        STRING_FIELD_MAP.put("filename", MetadataField.FILE_NAME);
        STRING_FIELD_MAP.put("comment", MetadataField.COMMENT);
        STRING_FIELD_MAP.put("playlist", MetadataField.PLAYLIST);
        NUMBER_FIELD_MAP.put("year", MetadataField.YEAR);
        NUMBER_FIELD_MAP.put("tracknumber", MetadataField.TRACK_NUMBER);
        NUMBER_FIELD_MAP.put("time", MetadataField.DURATION);
        NUMBER_FIELD_MAP.put("playcount", MetadataField.PLAY_COUNT);
        NUMBER_FIELD_MAP.put("rating", MetadataField.RATING);
        DATE_FIELD_MAP.put("lastplayed", MetadataField.LAST_PLAYED);
        FIELD_MAPS.add(STRING_FIELD_MAP);
        FIELD_MAPS.add(NUMBER_FIELD_MAP);
        FIELD_MAPS.add(DATE_FIELD_MAP);
        STRING_OPERATOR_MAP.put("is", Operator.IS);
        STRING_OPERATOR_MAP.put("isnot", Operator.IS_NOT);
        STRING_OPERATOR_MAP.put("startswith", Operator.STARTS_WITH);
        STRING_OPERATOR_MAP.put("endswith", Operator.ENDS_WITH);
        STRING_OPERATOR_MAP.put("contains", Operator.CONTAINS);
        STRING_OPERATOR_MAP.put("doesnotcontain", Operator.DOES_NOT_CONTAIN);
        NUMBER_OPERATOR_MAP.put("is", Operator.IS);
        NUMBER_OPERATOR_MAP.put("isnot", Operator.IS_NOT);
        NUMBER_OPERATOR_MAP.put("lessthan", Operator.LESS_THAN);
        NUMBER_OPERATOR_MAP.put("greaterthan", Operator.GREATER_THAN);
        DATE_OPERATOR_MAP.put("before", Operator.BEFORE);
        DATE_OPERATOR_MAP.put("after", Operator.AFTER);
        DATE_OPERATOR_MAP.put("inthelast", Operator.IN_THE_LAST);
        DATE_OPERATOR_MAP.put("notinthelast", Operator.NOT_IN_THE_LAST);

        TIME_UNIT_MAP.put(TimeUnit.SECOND, "seconds");
        TIME_UNIT_MAP.put(TimeUnit.MINUTE, "minutes");
        TIME_UNIT_MAP.put(TimeUnit.HOUR, "hours");
        TIME_UNIT_MAP.put(TimeUnit.DAY, "days");
        TIME_UNIT_MAP.put(TimeUnit.WEEK, "weeks");

        STRING_FIELD_KEYS = STRING_FIELD_MAP.keySet();
        NUMBER_FIELD_KEYS = NUMBER_FIELD_MAP.keySet();
        DATE_FIELD_KEYS = DATE_FIELD_MAP.keySet();
        STRING_FIELD_VALUES = STRING_FIELD_MAP.values();
        NUMBER_FIELD_VALUES = NUMBER_FIELD_MAP.values();
        DATE_FIELD_VALUES = DATE_FIELD_MAP.values();
    }

    private static boolean getMatchAllFromXbmc(final String xbmcMatch) {
        return xbmcMatch.equals("all");
    }

    private static String getMatchAllForXbmc(final boolean isMatchAll) {
        return isMatchAll ? "all" : "one";
    }

    private static boolean getOrderFromXbmc(final String xbmcDirection) {
        return !xbmcDirection.equals("descending");
    }

    private static String getOrderForXbmc(final boolean isAscending) {
        return isAscending ? "ascending" : "descending";
    }

    private static TimePeriod parseXbmcTimePeriod(final String timePeriodString) throws ParseException {
        try {
            return TIME_FORMAT_MS.parse(timePeriodString);
        } catch (final ParseException pe) {
            return TIME_FORMAT_HMS.parse(timePeriodString);
        }
    }

    private static String formatXbmcTimePeriod(final TimePeriod timePeriod) {
        if (timePeriod.getHours() > 0) {
            return TIME_FORMAT_HMS.format(timePeriod);
        } else {
            return TIME_FORMAT_MS.format(timePeriod);
        }
    }

    private static Rule convertRule(final XbmcSmartPlaylist.Rule xbmcRule) throws ParseException {
        final Rule smartRule = new Rule();
        final MetadataField smartField;
        final Operator smartOperator;
        if (STRING_FIELD_KEYS.contains(xbmcRule.getField())) {
            smartField = STRING_FIELD_MAP.get(xbmcRule.getField());
            smartOperator = STRING_OPERATOR_MAP.get(xbmcRule.getOperator());
            smartRule.setOperand(xbmcRule.getOperand());
        } else if (NUMBER_FIELD_KEYS.contains(xbmcRule.getField())) {
            smartField = NUMBER_FIELD_MAP.get(xbmcRule.getField());
            smartOperator = NUMBER_OPERATOR_MAP.get(xbmcRule.getOperator());
            if (smartField == MetadataField.DURATION) {
                smartRule.setOperand(parseXbmcTimePeriod(xbmcRule.getOperand()));
            } else {
                smartRule.setOperand(xbmcRule.getOperand());
            }
        } else if (DATE_FIELD_KEYS.contains(xbmcRule.getField())) {
            smartField = DATE_FIELD_MAP.get(xbmcRule.getField());
            smartOperator = DATE_OPERATOR_MAP.get(xbmcRule.getOperator());
            if (smartOperator == Operator.IN_THE_LAST || smartOperator == Operator.NOT_IN_THE_LAST) {
                // 2 weeks, 10 days, etc
                smartRule.setOperand(TimePeriod.parseAsWords(xbmcRule.getOperand()));
            } else if (smartOperator == Operator.BEFORE || smartOperator == Operator.AFTER) {
                // parse date as yyyy-MM-dd
                smartRule.setOperand(DATE_FORMAT.parse(xbmcRule.getOperand()));
            }
        } else {
            throw new IllegalArgumentException(String.format("Invalid field \"%s\" in XBMC playlist in rule:\n%s",
                xbmcRule.getField(), xbmcRule));
        }
        if (smartOperator != null) {
            smartRule.setField(smartField);
            smartRule.setOperator(smartOperator);
        } else {
            throw new IllegalArgumentException(String.format("operator \"%s\" is not allowed on field \"%s\" in XBMC playlists or is not a valid operator, rule:\n%s",
                xbmcRule.getOperator(), smartField, xbmcRule));
        }
        return smartRule;
    }

    public static AgnosticSmartPlaylist convert(final XbmcSmartPlaylist xbmcSmartPlaylist,
            final Collection<String> errorLog) {
        final AgnosticSmartPlaylist result = new AgnosticSmartPlaylist();
        setDefaultsOn(result);
        overrideDefaults(result, xbmcSmartPlaylist, errorLog);
        if (result.getOrder().getKey() == MetadataField.PLAYLIST) {
            log(errorLog, "order by playlist is not allowed in XBMC playlists");
            result.getOrder().setKey(MetadataField.TITLE);
        }
        result.setPlaylistType(PLAYLIST_TYPE_MAP.get(xbmcSmartPlaylist.getType()));
        result.setName(xbmcSmartPlaylist.getName());

        for (final XbmcSmartPlaylist.Rule xbmcRule: xbmcSmartPlaylist.getRules()) {
            try {
                result.getRules().add(convertRule(xbmcRule));
            } catch (final ParseException pe) {
                log(errorLog, String.format("%s at index %d%s\nRule = %s", pe.getMessage(), pe.getErrorOffset(),
                    pe.getMessage().contains("date") ? "; Dates must be in the format " + DATE_FORMAT_STRING : "", xbmcRule));

            } catch (final IllegalArgumentException iae) {
                log(errorLog, iae.getMessage());
            }
        }

        return result;
    }

    private static XbmcSmartPlaylist.Rule convertRule(final Class<? extends XbmcSmartPlaylist.Rule> ruleType,
            final Rule smartRule, final Collection<String> errorLog) {
        final XbmcSmartPlaylist.Rule xbmcRule;
        try {
            xbmcRule = ruleType.newInstance();
        } catch (final InstantiationException e) {
            log(errorLog, String.format("Unable to instantiate an XBMC rule; check the constructors and implementation: %s",
                    e.getMessage()));
            return null;
        } catch (final IllegalAccessException e) {
            log(errorLog, String.format("Unable to instantiate an XBMC rule; check the constructors: %s", e.getMessage()));
            return null;
        }
        final MetadataField smartField = smartRule.getField();
        if (STRING_FIELD_VALUES.contains(smartField)) {
            xbmcRule.setField(STRING_FIELD_MAP.inverse().get(smartField));
            xbmcRule.setOperator(STRING_OPERATOR_MAP.inverse().get(smartRule.getOperator()));
        } else if (NUMBER_FIELD_VALUES.contains(smartField)) {
            xbmcRule.setField(NUMBER_FIELD_MAP.inverse().get(smartField));
        } else if (DATE_FIELD_VALUES.contains(smartField)) {
            xbmcRule.setField(DATE_FIELD_MAP.inverse().get(smartField));
        } else {
            throw new IllegalArgumentException(String.format("XBMC playlists do not support the \"%s\" field\nRule = %s",
                smartField, smartRule));
        }
        if (xbmcRule.getOperator() == null) {
            throw new IllegalArgumentException(String.format("Operator \"%s\" is not allowed on field \"%s\" in XBMC playlists",
                smartRule.getOperator(), smartField));
        }
        final Object smartOperand = smartRule.getOperand();

        if (smartOperand instanceof String) {
            xbmcRule.setOperand(smartOperand.toString());
        } else if (smartOperand instanceof TimePeriod) {
            final TimePeriod time = (TimePeriod) smartOperand;

            if (smartField == MetadataField.LAST_PLAYED) {
                final Pair<Long, TimeUnit> period = time.getLargestUnit(TimeUnit.WEEK);
                xbmcRule.setOperand(period.getKey() + " " + TIME_UNIT_MAP.get(period.getValue()));
            } else if (smartField == MetadataField.DURATION) {
                xbmcRule.setOperand(formatXbmcTimePeriod(time));
            } else {
                throw new IllegalArgumentException("Combination of field and operand types not allowed: " + smartRule);
            }
        } else if (smartOperand instanceof Date) {
            xbmcRule.setOperand(DATE_FORMAT.format((Date) smartOperand));
        } else {
            throw new IllegalArgumentException(String.format("Illegal operand type \"%s\" in smart playlist rule:\n%s",
                smartOperand.getClass().getCanonicalName(), smartRule));
        }

        return xbmcRule;
    }

    public static XbmcSmartPlaylist convert(final AgnosticSmartPlaylist agnosticSmartPlaylist,
            final Class<? extends XbmcSmartPlaylist> outputType, final Collection<String> errorLog) {
        final XbmcSmartPlaylist result;
        try {
            result = outputType.newInstance();
        } catch (final InstantiationException e) {
            log(errorLog, String.format("Unable to instantiate an XBMC playlist; check the constructors and implementation: %s",
                    e.getMessage()));
            return null;
        } catch (final IllegalAccessException e) {
            log(errorLog, String.format("Unable to instantiate an XBMC playlist; check the constructors: %s", e.getMessage()));
            return null;
        }
        setDefaultsOn(result);
        overrideDefaults(result, agnosticSmartPlaylist, errorLog);
        result.setType(PLAYLIST_TYPE_MAP.inverse().get(agnosticSmartPlaylist.getPlaylistType()));
        result.setName(agnosticSmartPlaylist.getName());

        final Class<? extends XbmcSmartPlaylist.Rule> ruleType = result.newRule().getClass();
        for (final Rule smartRule: agnosticSmartPlaylist.getRules()) {
            try {
                result.getRules().add(convertRule(ruleType, smartRule, errorLog));
            } catch (final IllegalArgumentException iae) {
                log(errorLog, iae.getMessage());
            }
        }

        return result;
    }

    /**
     * Sets the default values on the playlist. These values are what XBMC uses when they are missing from the XML.
     * @param xbmcSmartPlaylist the playlist to to set defaults on
     */
    private static void setDefaultsOn(final XbmcSmartPlaylist xbmcSmartPlaylist) {
        final XbmcSmartPlaylist.Order order = xbmcSmartPlaylist.newOrder()
            .setDirection(getOrderForXbmc(defaultOrderIsAscending))
            // default in XBMC UI is order by "none", whose sort algorithm I can't find a pattern in, so just pick a sane default
            .setSortKey(STRING_FIELD_MAP.inverse().get(defaultOrderByField));
        xbmcSmartPlaylist.setOrder(order)
            .setMatch(getMatchAllForXbmc(defaultMatchAll))
            .setLimit(defaultLimit);
    }

    /**
     * Overrides the default values in xbmcSmartPlaylist with the values present in smartPlaylist
     * @param xbmcSmartPlaylist the playlist that's being edited
     * @param smartPlaylist     the playlist whose values are being read
     * @param errorLog          log for errors encountered during operation
     */
    private static void overrideDefaults(final XbmcSmartPlaylist xbmcSmartPlaylist, final AgnosticSmartPlaylist smartPlaylist,
            final Collection<String> errorLog) {
        if (smartPlaylist.getOrder() != null) {
            if (smartPlaylist.getOrder().getKey() != null) {
                boolean set = false;
                for (final BiMap<String, MetadataField> fieldMap: FIELD_MAPS) {
                    if (fieldMap.inverse().containsKey(smartPlaylist.getOrder().getKey())) {
                        xbmcSmartPlaylist.getOrder().setSortKey(fieldMap.inverse().get(smartPlaylist.getOrder().getKey()));
                        set = true;
                        break;
                    }
                }
                if (!set) {
                    log(errorLog, String.format("Order value \"%s\" is not allowed in XBMC playlists", smartPlaylist.getOrder().getKey()));
                }
            }
            xbmcSmartPlaylist.getOrder().setDirection(getOrderForXbmc(smartPlaylist.getOrder().isAscending()));
        }
        if (smartPlaylist.isMatchAll() != null) {
            xbmcSmartPlaylist.setMatch(getMatchAllForXbmc(smartPlaylist.isMatchAll()));
        }
        if (smartPlaylist.getLimit() != null) {
            xbmcSmartPlaylist.setLimit(smartPlaylist.getLimit());
        }
    }

    /**
     * Sets the default values on the playlist. These values are what XBMC uses when they are missing from the XML.
     * @param smartPlaylist the playlist to to set defaults on
     */
    private static void setDefaultsOn(final AgnosticSmartPlaylist smartPlaylist) {
        final Order order = new Order()
            .setAscending(defaultOrderIsAscending)
            .setKey(defaultOrderByField);
        smartPlaylist.setOrder(order)
            .setMatchAll(defaultMatchAll)
            .setLimit(defaultLimit);
    }

    /**
     * Overrides the default values in smartPlaylist with the values present in xbmcSmartPlaylist
     * @param smartPlaylist     the playlist that's being edited
     * @param xbmcSmartPlaylist the playlist whose values are being read
     * @param errorLog          log for errors encountered during operation
     */
    private static void overrideDefaults(final AgnosticSmartPlaylist smartPlaylist, final XbmcSmartPlaylist xbmcSmartPlaylist,
            final Collection<String> errorLog) {
        if (xbmcSmartPlaylist.getOrder() != null) {
            if (xbmcSmartPlaylist.getOrder().getSortKey() != null) {
                boolean set = false;
                for (final BiMap<String, MetadataField> fieldMap: FIELD_MAPS) {
                    if (fieldMap.containsKey(xbmcSmartPlaylist.getOrder().getSortKey())) {
                        smartPlaylist.getOrder().setKey(fieldMap.get(xbmcSmartPlaylist.getOrder().getSortKey()));
                        set = true;
                        break;
                    }
                }
                if (!set) {
                    log(errorLog, String.format("Invalid order value \"%s\" in XBMC playlist", xbmcSmartPlaylist.getOrder().getSortKey()));
                }
            }
            if (xbmcSmartPlaylist.getOrder().getDirection() != null) {
                smartPlaylist.getOrder().setAscending(getOrderFromXbmc(xbmcSmartPlaylist.getOrder().getDirection()));
            }
        }
        if (xbmcSmartPlaylist.getMatch() != null) {
            smartPlaylist.setMatchAll(getMatchAllFromXbmc(xbmcSmartPlaylist.getMatch()));
        }
        if (xbmcSmartPlaylist.getLimit() != null) {
            smartPlaylist.setLimit(xbmcSmartPlaylist.getLimit());
        }
    }

    private static void log(final Collection<String> errorLog, final String message) {
        logger.error(message);
        errorLog.add(message);
    }
}
