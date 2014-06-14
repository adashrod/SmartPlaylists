package com.aaron.smartplaylists.converters;

import com.aaron.smartplaylists.playlists.AgnosticSmartPlaylist;
import com.aaron.smartplaylists.MetadataField;
import com.aaron.smartplaylists.Operator;
import com.aaron.smartplaylists.Order;
import com.aaron.smartplaylists.Pair;
import com.aaron.smartplaylists.Rule;
import com.aaron.smartplaylists.Time;
import com.aaron.smartplaylists.api.XbmcSmartPlaylist;
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

/**
 * The meat of XBMC playlist conversion. Since v11 and v12 XBMC playlists have an identical java API, the only
 * difference needed to handle the two separately is casting them when passing to functions in this class.
 * todo: make sure that <limit>0</limit> doesn't cause problems
 * todo: serialize playlist type
 */
public class XbmcPlaylistConverterTools {
    private static final Logger logger = Logger.getLogger(XbmcPlaylistConverterTools.class);

    private static final BiMap<String, MetadataField> FIELD_MAP = HashBiMap.create();
    private static final BiMap<String, Operator> OPERATOR_MAP = HashBiMap.create();
    private static final Map<Time.TimeUnit, String> TIME_UNIT_MAP = Maps.newHashMap();
    private static final String DATE_FORMAT_STRING = "yyyy-MM-dd";
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat(DATE_FORMAT_STRING);

    static {
        FIELD_MAP.put("genre", MetadataField.GENRE);
        FIELD_MAP.put("album", MetadataField.ALBUM);
        FIELD_MAP.put("artist", MetadataField.ARTIST);
        FIELD_MAP.put("albumartist", MetadataField.ALBUM_ARTIST);
        FIELD_MAP.put("title", MetadataField.TITLE);
        FIELD_MAP.put("year", MetadataField.YEAR);
        FIELD_MAP.put("time", MetadataField.TIME);
        FIELD_MAP.put("tracknumber", MetadataField.TRACK_NUMBER);
        FIELD_MAP.put("filename", MetadataField.FILE_NAME);
        FIELD_MAP.put("path", MetadataField.PATH);
        FIELD_MAP.put("playcount", MetadataField.PLAY_COUNT);
        FIELD_MAP.put("lastplayed", MetadataField.LAST_PLAYED);
        FIELD_MAP.put("rating", MetadataField.RATING);
        FIELD_MAP.put("comment", MetadataField.COMMENT);
        FIELD_MAP.put("playlist", MetadataField.PLAYLIST);
        OPERATOR_MAP.put("contains", Operator.CONTAINS);
        OPERATOR_MAP.put("doesnotcontain", Operator.DOES_NOT_CONTAIN);
        OPERATOR_MAP.put("is", Operator.IS);
        OPERATOR_MAP.put("isnot", Operator.IS_NOT);
        OPERATOR_MAP.put("startswith", Operator.STARTS_WITH);
        OPERATOR_MAP.put("endswith", Operator.ENDS_WITH);
        OPERATOR_MAP.put("lessthan", Operator.LESS_THAN);
        OPERATOR_MAP.put("greaterthan", Operator.GREATER_THAN);
        OPERATOR_MAP.put("after", Operator.AFTER);
        OPERATOR_MAP.put("before", Operator.BEFORE);
        OPERATOR_MAP.put("inthelast", Operator.IN_THE_LAST);
        OPERATOR_MAP.put("notinthelast", Operator.NOT_IN_THE_LAST);
        TIME_UNIT_MAP.put(Time.TimeUnit.SECONDS, "seconds");
        TIME_UNIT_MAP.put(Time.TimeUnit.MINUTES, "minutes");
        TIME_UNIT_MAP.put(Time.TimeUnit.HOURS, "hours");
        TIME_UNIT_MAP.put(Time.TimeUnit.DAYS, "days");
        TIME_UNIT_MAP.put(Time.TimeUnit.WEEKS, "weeks");
        // todo: order by doesn't support playlist
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
        final MetadataField smartField = FIELD_MAP.get(xbmcRule.getField());
        if (smartField != null) {
            smartRule.setField(smartField);
        } else {
            throw new IllegalArgumentException(String.format("\"%s\" is not a valid field in XBMC playlists", xbmcRule.getField()));
        }
        final Operator smartOperator = OPERATOR_MAP.get(xbmcRule.getOperator());
        if (smartOperator != null) {
            smartRule.setOperator(smartOperator);
        } else {
            throw new IllegalArgumentException(String.format("\"%s\" is not a valid operator in XBMC playlists", xbmcRule.getOperator()));
        }
        // todo: add more checks that field/operator combos are valid
        if (smartRule.getField() == MetadataField.LAST_PLAYED) {
            if (smartRule.getOperator() == Operator.IN_THE_LAST || smartRule.getOperator() == Operator.NOT_IN_THE_LAST) {
                // 2 weeks, 10 days, etc
                smartRule.setOperand(Time.parse(xbmcRule.getOperand()));
            } else if (smartRule.getOperator() == Operator.BEFORE || smartRule.getOperator() == Operator.AFTER) {
                // parse date as yyyy-MM-dd
                smartRule.setOperand(DATE_FORMAT.parse(xbmcRule.getOperand()));
            } else {
                throw new IllegalArgumentException("Combination of field and operand types not allowed: " + xbmcRule);
            }
        } else if (smartRule.getField() == MetadataField.TIME) {
            smartRule.setOperand(Time.parse(xbmcRule.getOperand()));
        } else {
            smartRule.setOperand(xbmcRule.getOperand());
        }
        return smartRule;
    }

    public static AgnosticSmartPlaylist convert(final XbmcSmartPlaylist xbmcSmartPlaylist,
            final Collection<String> errorLog) {
        final AgnosticSmartPlaylist result = new AgnosticSmartPlaylist();
        result.setName(xbmcSmartPlaylist.getName());
        result.setMatchAll(getMatchAllFromXbmc(xbmcSmartPlaylist.getMatch()));

        for (final XbmcSmartPlaylist.Rule xbmcRule: xbmcSmartPlaylist.getRules()) {
            try {
                result.getRules().add(convertRule(xbmcRule));
            } catch (final ParseException pe) {
                log(errorLog, String.format("%s at index %d%s\nRule = %s", pe.getMessage(), pe.getErrorOffset(),
                    pe.getMessage().contains("date") ? "; Dates must be in the format " + DATE_FORMAT_STRING : ""
                    ,xbmcRule));
                //continue;
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
                sortKey = FIELD_MAP.inverse().get(MetadataField.TITLE);
            }
        } else {
            direction = "";
            sortKey = FIELD_MAP.inverse().get(MetadataField.TITLE);
        }
        final Order order = new Order();
        order.setAscending(getOrderFromXbmc(direction));
        order.setKey(FIELD_MAP.get(sortKey));
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
            log(errorLog,
                String.format("Unable to instantiate an XBMC rule; check the constructors and implementation: %s",
                    e.getMessage()));
            return null;
        } catch (final IllegalAccessException e) {
            log(errorLog,
                String.format("Unable to instantiate an XBMC rule; check the constructors: %s", e.getMessage()));
            return null;
        }
        xbmcRule.setField(FIELD_MAP.inverse().get(smartRule.getField()));
        xbmcRule.setOperator(OPERATOR_MAP.inverse().get(smartRule.getOperator()));
        final Object operand = smartRule.getOperand();

        if (operand instanceof String) {
            xbmcRule.setOperand(operand.toString());
        } else if (operand instanceof Time) {
            final Time time = (Time)operand;

            if (smartRule.getField() == MetadataField.LAST_PLAYED) {
                final Pair<Integer, Time.TimeUnit> period = time.getLargestUnit(Time.TimeUnit.WEEKS);
                xbmcRule.setOperand(period.getFirst() + " " + TIME_UNIT_MAP.get(period.getSecond()));
            } else if (smartRule.getField() == MetadataField.TIME) {
                xbmcRule.setOperand(time.toString());
            } else {
                throw new IllegalArgumentException("Combination of field and operand types not allowed: " + smartRule);
            }
        } else if (operand instanceof Date) {
            xbmcRule.setOperand(DATE_FORMAT.format((Date) operand));
        } else {
            throw new IllegalArgumentException("Illegal operand type in smart playlist: " + operand.getClass().getCanonicalName());
        }

        return xbmcRule;
    }

    public static XbmcSmartPlaylist convert(final AgnosticSmartPlaylist agnosticSmartPlaylist,
            final Class<? extends XbmcSmartPlaylist> outputType, final Collection<String> errorLog) {
        final XbmcSmartPlaylist result;
        try {
            result = outputType.newInstance();
        } catch (final InstantiationException e) {
            log(errorLog,
                String.format("Unable to instantiate an XBMC playlist; check the constructors and implementation: %s",
                    e.getMessage()));
            return null;
        } catch (final IllegalAccessException e) {
            log(errorLog,
                String.format("Unable to instantiate an XBMC playlist; check the constructors: %s", e.getMessage()));
            return null;
        }
        result.setName(agnosticSmartPlaylist.getName());
        result.setMatch(getMatchAllForXbmc(agnosticSmartPlaylist.isMatchAll()));

        final Class<? extends XbmcSmartPlaylist.Rule> ruleType = result.newRule().getClass();
        for (final Rule smartRule: agnosticSmartPlaylist.getRules()) {
            try {
                result.getRules().add(convertRule(ruleType, smartRule, errorLog));
            } catch (final IllegalArgumentException iae) {
                log(errorLog, iae.getMessage());
                //continue;
            }
        }

        final XbmcSmartPlaylist.Order order = result.newOrder();
        order.setDirection(getOrderForXbmc(agnosticSmartPlaylist.getOrder().isAscending()));
        order.setSortKey(FIELD_MAP.inverse().get(agnosticSmartPlaylist.getOrder().getKey()));
        result.setOrder(order);

        result.setLimit(agnosticSmartPlaylist.getLimit());
        return result;
    }

    private static void log(final Collection<String> errorLog, final String message) {
        logger.error(message);
        errorLog.add(message);
    }
}
