package com.aaron.smartplaylists.converters;

import com.aaron.smartplaylists.playlists.PlaylistType;
import com.aaron.smartplaylists.playlists.AgnosticSmartPlaylist;
import com.aaron.smartplaylists.playlists.MetadataField;
import com.aaron.smartplaylists.playlists.Operator;
import com.aaron.smartplaylists.playlists.Order;
import com.aaron.smartplaylists.util.Pair;
import com.aaron.smartplaylists.playlists.Rule;
import com.aaron.smartplaylists.util.Time;
import com.aaron.smartplaylists.playlists.XbmcSmartPlaylist;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import org.apache.log4j.Logger;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * The meat of XBMC playlist conversion. Since v11 and v12 XBMC playlists have an identical java API, the only
 * difference needed to handle the two separately is casting them when passing to functions in this class.
 * todo: make sure that <limit>0</limit> doesn't cause problems
 */
public class XbmcPlaylistConverterTools {
    private static final Logger logger = Logger.getLogger(XbmcPlaylistConverterTools.class);

    private static final BiMap<String, PlaylistType> PLAYLIST_TYPE_MAP = HashBiMap.create();
    private static final BiMap<String, MetadataField> STRING_FIELD_MAP = HashBiMap.create();
    private static final BiMap<String, MetadataField> NUMBER_FIELD_MAP = HashBiMap.create();
    private static final BiMap<String, MetadataField> DATE_FIELD_MAP = HashBiMap.create();
    private static final BiMap<String, Operator> STRING_OPERATOR_MAP = HashBiMap.create();
    private static final BiMap<String, Operator> NUMBER_OPERATOR_MAP = HashBiMap.create();
    private static final BiMap<String, Operator> DATE_OPERATOR_MAP = HashBiMap.create();
    private static final Map<Time.TimeUnit, String> TIME_UNIT_MAP = Maps.newHashMap();
    private static final Set<String> STRING_FIELD_KEYS;
    private static final Set<String> NUMBER_FIELD_KEYS;
    private static final Set<String> DATE_FIELD_KEYS;
    private static final Set<MetadataField> STRING_FIELD_VALUES;
    private static final Set<MetadataField> NUMBER_FIELD_VALUES;
    private static final Set<MetadataField> DATE_FIELD_VALUES;
    private static final String DATE_FORMAT_STRING = "yyyy-MM-dd";
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat(DATE_FORMAT_STRING);

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

        TIME_UNIT_MAP.put(Time.TimeUnit.SECONDS, "seconds");
        TIME_UNIT_MAP.put(Time.TimeUnit.MINUTES, "minutes");
        TIME_UNIT_MAP.put(Time.TimeUnit.HOURS, "hours");
        TIME_UNIT_MAP.put(Time.TimeUnit.DAYS, "days");
        TIME_UNIT_MAP.put(Time.TimeUnit.WEEKS, "weeks");

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
                smartRule.setOperand(Time.parse(xbmcRule.getOperand()));
            } else {
                smartRule.setOperand(xbmcRule.getOperand());
            }
        } else if (DATE_FIELD_KEYS.contains(xbmcRule.getField())) {
            smartField = DATE_FIELD_MAP.get(xbmcRule.getField());
            smartOperator = DATE_OPERATOR_MAP.get(xbmcRule.getOperator());
            if (smartOperator == Operator.IN_THE_LAST || smartOperator == Operator.NOT_IN_THE_LAST) {
                // 2 weeks, 10 days, etc
                smartRule.setOperand(Time.parse(xbmcRule.getOperand()));
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
        result.setPlaylistType(PLAYLIST_TYPE_MAP.get(xbmcSmartPlaylist.getType()));
        result.setName(xbmcSmartPlaylist.getName());
        result.setMatchAll(getMatchAllFromXbmc(xbmcSmartPlaylist.getMatch()));

        for (final XbmcSmartPlaylist.Rule xbmcRule: xbmcSmartPlaylist.getRules()) {
            try {
                result.getRules().add(convertRule(xbmcRule));
            } catch (final ParseException pe) {
                log(errorLog, String.format("%s at index %d%s\nRule = %s", pe.getMessage(), pe.getErrorOffset(),
                    pe.getMessage().contains("date") ? "; Dates must be in the format " + DATE_FORMAT_STRING : ""
                    ,xbmcRule));
            } catch (final IllegalArgumentException iae) {
                log(errorLog, iae.getMessage());
            }
        }

        // todo: change this to have smartplaylist -> specific playlist conversion know what the defaults are for its format
        final String direction;
        final String sortKey;
        if (xbmcSmartPlaylist.getOrder() != null ) {
            if (xbmcSmartPlaylist.getOrder().getDirection() != null) {
                direction = xbmcSmartPlaylist.getOrder().getDirection();
            } else {
                direction = "";
            }
            if (xbmcSmartPlaylist.getOrder().getSortKey() != null) {
                sortKey = xbmcSmartPlaylist.getOrder().getSortKey();
            } else {
                sortKey = STRING_FIELD_MAP.inverse().get(MetadataField.TITLE);
            }
        } else {
            direction = "";
            sortKey = STRING_FIELD_MAP.inverse().get(MetadataField.TITLE);
        }
        final Order order = new Order();
        order.setAscending(getOrderFromXbmc(direction));
        final MetadataField orderType = STRING_FIELD_MAP.get(sortKey);
        if (orderType != MetadataField.PLAYLIST) {
            order.setKey(orderType);
        } else {
            log(errorLog, "order by playlist is not allowed in XBMC playlists");
            order.setKey(MetadataField.TITLE);
        }
        result.setOrder(order);

        result.setLimit(xbmcSmartPlaylist.getLimit());

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
            throw new IllegalArgumentException(String.format("XBMC playlists do not support the \"%s\" field", smartField));
        }
        if (xbmcRule.getOperator() == null) {
            throw new IllegalArgumentException(String.format("Operator \"%s\" is not allowed on field \"%s\" in XBMC playlists",
                smartRule.getOperator(), smartField));
        }
        final Object smartOperand = smartRule.getOperand();

        if (smartOperand instanceof String) {
            xbmcRule.setOperand(smartOperand.toString());
        } else if (smartOperand instanceof Time) {
            final Time time = (Time) smartOperand;

            if (smartField == MetadataField.LAST_PLAYED) {
                final Pair<Integer, Time.TimeUnit> period = time.getLargestUnit(Time.TimeUnit.WEEKS);
                xbmcRule.setOperand(period.getFirst() + " " + TIME_UNIT_MAP.get(period.getSecond()));
            } else if (smartField == MetadataField.DURATION) {
                xbmcRule.setOperand(time.toString());
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
        result.setType(PLAYLIST_TYPE_MAP.inverse().get(agnosticSmartPlaylist.getPlaylistType()));
        result.setName(agnosticSmartPlaylist.getName());
        result.setMatch(getMatchAllForXbmc(agnosticSmartPlaylist.isMatchAll()));

        final Class<? extends XbmcSmartPlaylist.Rule> ruleType = result.newRule().getClass();
        for (final Rule smartRule: agnosticSmartPlaylist.getRules()) {
            try {
                result.getRules().add(convertRule(ruleType, smartRule, errorLog));
            } catch (final IllegalArgumentException iae) {
                log(errorLog, iae.getMessage());
            }
        }

        final XbmcSmartPlaylist.Order xbmcOrder = result.newOrder();
        // todo: define defaults for when agnosticSmartPlaylist is missing data
        xbmcOrder.setDirection(getOrderForXbmc(agnosticSmartPlaylist.getOrder() == null || agnosticSmartPlaylist.getOrder().isAscending()));
        xbmcOrder.setSortKey(STRING_FIELD_MAP.inverse().get(agnosticSmartPlaylist.getOrder() != null ?
            agnosticSmartPlaylist.getOrder().getKey() : MetadataField.TITLE));
        result.setOrder(xbmcOrder);

        result.setLimit(agnosticSmartPlaylist.getLimit());
        return result;
    }

    private static void log(final Collection<String> errorLog, final String message) {
        logger.error(message);
        errorLog.add(message);
    }
}
