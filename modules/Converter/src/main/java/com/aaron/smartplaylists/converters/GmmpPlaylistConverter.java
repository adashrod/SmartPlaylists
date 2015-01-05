package com.aaron.smartplaylists.converters;

import com.aaron.smartplaylists.playlists.MetadataField;
import com.aaron.smartplaylists.playlists.Operator;
import com.aaron.smartplaylists.playlists.Order;
import com.aaron.smartplaylists.playlists.PlaylistType;
import com.aaron.smartplaylists.playlists.Rule;
import com.aaron.smartplaylists.playlists.AgnosticSmartPlaylist;
import com.aaron.smartplaylists.api.FormattedSmartPlaylist;
import com.aaron.smartplaylists.api.PlaylistConverter;
import com.aaron.smartplaylists.playlists.GmmpSmartPlaylist;
import com.aaron.timeperiod.Pair;
import com.aaron.timeperiod.TimePeriod;
import com.aaron.timeperiod.TimeUnit;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.apache.log4j.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Set;

/**
 * GmmpPlaylistConverter knows how to convert {@link AgnosticSmartPlaylist}s to {@link GmmpSmartPlaylist}s and vice
 * versa. It can also be used to read a {@link GmmpSmartPlaylist} in from a file.
 */
public class GmmpPlaylistConverter implements PlaylistConverter {
    private static final Logger logger = Logger.getLogger(GmmpPlaylistConverter.class);

    private static final BiMap<Integer, MetadataField> STRING_FIELD_MAP = HashBiMap.create();
    private static final BiMap<Integer, MetadataField> NUMBER_FIELD_MAP = HashBiMap.create();
    private static final BiMap<Integer, MetadataField> DATE_FIELD_MAP = HashBiMap.create();
    private static final BiMap<Integer, Operator> STRING_OPERATOR_MAP = HashBiMap.create();
    private static final BiMap<Integer, Operator> NUMBER_OPERATOR_MAP = HashBiMap.create();
    private static final BiMap<Integer, Operator> DATE_OPERATOR_MAP = HashBiMap.create();
    private static final BiMap<Integer, MetadataField> ORDER_BY_MAP = HashBiMap.create();
    private static final BiMap<Integer, TimeUnit> TIME_UNIT_MAP = HashBiMap.create();
    private static final Set<Integer> STRING_FIELD_KEYS;
    private static final Set<Integer> NUMBER_FIELD_KEYS;
    private static final Set<Integer> DATE_FIELD_KEYS;
    private static final Set<MetadataField> STRING_FIELD_VALUES;
    private static final Set<MetadataField> NUMBER_FIELD_VALUES;
    private static final Set<MetadataField> DATE_FIELD_VALUES;
    private static final String DATE_FORMAT_STRING = "yyyy/MM/dd";
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat(DATE_FORMAT_STRING);

    private static final boolean defaultMatchAll = false;
    private static final MetadataField defaultOrderByField = MetadataField.RANDOM;
    private static final boolean defaultOrderIsAscending = false;
    private static final int defaultLimit = 0; // no limit

    static {
        STRING_FIELD_MAP.put(0, MetadataField.ARTIST);
        STRING_FIELD_MAP.put(1, MetadataField.ALBUM_ARTIST);
        STRING_FIELD_MAP.put(2, MetadataField.TITLE);
        STRING_FIELD_MAP.put(3, MetadataField.ALBUM);
        STRING_FIELD_MAP.put(4, MetadataField.GENRE);
        STRING_FIELD_MAP.put(5, MetadataField.FILE_NAME);
        NUMBER_FIELD_MAP.put(6, MetadataField.YEAR);
        NUMBER_FIELD_MAP.put(7, MetadataField.DURATION); // time units
        NUMBER_FIELD_MAP.put(8, MetadataField.RATING);
        NUMBER_FIELD_MAP.put(9, MetadataField.PLAY_COUNT);
        NUMBER_FIELD_MAP.put(12, MetadataField.TRACK_NUMBER);
        NUMBER_FIELD_MAP.put(13, MetadataField.DISC_NUMBER);
        DATE_FIELD_MAP.put(10, MetadataField.DATE_ADDED); // time units when operator is 2 or 3
        DATE_FIELD_MAP.put(11, MetadataField.LAST_PLAYED); // time units when operator is 2 or 3
        STRING_OPERATOR_MAP.put(0, Operator.IS);
        STRING_OPERATOR_MAP.put(1, Operator.IS_NOT);
        STRING_OPERATOR_MAP.put(2, Operator.CONTAINS);
        STRING_OPERATOR_MAP.put(3, Operator.DOES_NOT_CONTAIN);
        STRING_OPERATOR_MAP.put(4, Operator.STARTS_WITH);
        STRING_OPERATOR_MAP.put(5, Operator.ENDS_WITH);
        NUMBER_OPERATOR_MAP.put(0, Operator.IS);
        NUMBER_OPERATOR_MAP.put(1, Operator.IS_NOT);
        NUMBER_OPERATOR_MAP.put(2, Operator.GREATER_THAN);
        NUMBER_OPERATOR_MAP.put(3, Operator.GREATER_THAN_OR_EQUAL);
        NUMBER_OPERATOR_MAP.put(4, Operator.LESS_THAN);
        NUMBER_OPERATOR_MAP.put(5, Operator.LESS_THAN_OR_EQUAL);
        DATE_OPERATOR_MAP.put(0, Operator.AFTER);
        DATE_OPERATOR_MAP.put(1, Operator.BEFORE);
        DATE_OPERATOR_MAP.put(2, Operator.IN_THE_LAST);
        DATE_OPERATOR_MAP.put(3, Operator.NOT_IN_THE_LAST);

        ORDER_BY_MAP.put(0, MetadataField.RANDOM);
        ORDER_BY_MAP.put(1, MetadataField.ARTIST);
        ORDER_BY_MAP.put(2, MetadataField.ALBUM_ARTIST);
        ORDER_BY_MAP.put(3, MetadataField.TITLE);
        ORDER_BY_MAP.put(4, MetadataField.ALBUM);
        ORDER_BY_MAP.put(5, MetadataField.GENRE);
        ORDER_BY_MAP.put(6, MetadataField.FILE_NAME);
        ORDER_BY_MAP.put(7, MetadataField.YEAR);
        ORDER_BY_MAP.put(8, MetadataField.DURATION);
        ORDER_BY_MAP.put(9, MetadataField.RATING);
        ORDER_BY_MAP.put(10, MetadataField.PLAY_COUNT);
        ORDER_BY_MAP.put(11, MetadataField.DATE_ADDED);
        ORDER_BY_MAP.put(12, MetadataField.LAST_PLAYED);
        ORDER_BY_MAP.put(13, MetadataField.TRACK_NUMBER);
        ORDER_BY_MAP.put(14, MetadataField.DISC_NUMBER);

        TIME_UNIT_MAP.put(-1, null);
        TIME_UNIT_MAP.put(0, TimeUnit.SECOND);
        TIME_UNIT_MAP.put(1, TimeUnit.MINUTE);
        TIME_UNIT_MAP.put(2, TimeUnit.HOUR);
        TIME_UNIT_MAP.put(3, TimeUnit.DAY);

        STRING_FIELD_KEYS = STRING_FIELD_MAP.keySet();
        NUMBER_FIELD_KEYS = NUMBER_FIELD_MAP.keySet();
        DATE_FIELD_KEYS = DATE_FIELD_MAP.keySet();
        STRING_FIELD_VALUES = STRING_FIELD_MAP.values();
        NUMBER_FIELD_VALUES = NUMBER_FIELD_MAP.values();
        DATE_FIELD_VALUES = DATE_FIELD_MAP.values();
    }

    /**
     * Given an XML file that is a GMMP-formatted smart playlist, this reads that file and returns that playlist as an
     * object.
     * @param file the file to read
     * @return a {@link GmmpSmartPlaylist}
     * @throws JAXBException error parsing the XML
     * @throws FileNotFoundException file not found
     */
    public FormattedSmartPlaylist readFromFile(final File file) throws JAXBException, FileNotFoundException {
        final JAXBContext context = JAXBContext.newInstance(GmmpSmartPlaylist.class);
        final Unmarshaller unmarshaller = context.createUnmarshaller();
        return (GmmpSmartPlaylist) unmarshaller.unmarshal(new FileReader(file));
    }

    /**
     * This takes in a {@link GmmpSmartPlaylist} and converts it to an {@link AgnosticSmartPlaylist}
     * @param formattedSmartPlaylist the GMMP playlist to convert
     * @return the converted format-agnostic playlist
     */
    public AgnosticSmartPlaylist convert(final FormattedSmartPlaylist formattedSmartPlaylist,
            final Collection<String> errorLog) {
        final AgnosticSmartPlaylist result = new AgnosticSmartPlaylist();
        final GmmpSmartPlaylist gmmpSmartPlaylist = (GmmpSmartPlaylist) formattedSmartPlaylist;
        setDefaultsOn(result);
        overrideDefaults(result, gmmpSmartPlaylist, errorLog);
        result.setPlaylistType(PlaylistType.MUSIC);
        result.setName(gmmpSmartPlaylist.getName());

        for (final GmmpSmartPlaylist.Rule gmmpRule: gmmpSmartPlaylist.getRules()) {
            final Rule smartRule = new Rule();
            final int field = gmmpRule.getField();
            if (STRING_FIELD_KEYS.contains(field)) {
                smartRule.setField(STRING_FIELD_MAP.get(field));
                smartRule.setOperator(STRING_OPERATOR_MAP.get(gmmpRule.getOperator()));
                smartRule.setOperand(gmmpRule.getValue());
            } else if (NUMBER_FIELD_KEYS.contains(field)) {
                smartRule.setField(NUMBER_FIELD_MAP.get(field));
                smartRule.setOperator(NUMBER_OPERATOR_MAP.get(gmmpRule.getOperator()));
                if (smartRule.getField() == MetadataField.DURATION) {
                    final int amountOfTime = Integer.parseInt(gmmpRule.getValue());
                    final TimeUnit timeUnit = TIME_UNIT_MAP.get(gmmpRule.getTimeUnit());
                    if (amountOfTime < 0) {
                        log(errorLog, String.format("Negative times are not allowed %d", amountOfTime));
                    } else if (timeUnit == null) {
                        log(errorLog, String.format("Invalid time unit value %d", gmmpRule.getTimeUnit()));
                    } else {
                        smartRule.setOperand(new TimePeriod(amountOfTime, timeUnit));
                    }
                } else {
                    smartRule.setOperand(gmmpRule.getValue());
                }
            } else if (DATE_FIELD_KEYS.contains(field)) {
                smartRule.setField(DATE_FIELD_MAP.get(field));
                smartRule.setOperator(DATE_OPERATOR_MAP.get(gmmpRule.getOperator()));
                if (smartRule.getOperator() == Operator.BEFORE || smartRule.getOperator() == Operator.AFTER) {
                    try {
                        smartRule.setOperand(DATE_FORMAT.parse(gmmpRule.getValue()));
                    } catch (final ParseException pe) {
                        log(errorLog, String.format("%s at index %d. Dates must be in the format: %s ",
                            pe.getMessage(), pe.getErrorOffset(), DATE_FORMAT_STRING));
                        continue;
                    }
                } else if (smartRule.getOperator() == Operator.IN_THE_LAST || smartRule.getOperator() == Operator.NOT_IN_THE_LAST) {
                    // time period
                    final int amountOfTime = Integer.parseInt(gmmpRule.getValue());
                    final TimeUnit timeUnit = TIME_UNIT_MAP.get(gmmpRule.getTimeUnit());
                    if (amountOfTime < 0) {
                        log(errorLog, String.format("Negative times are not allowed %d", amountOfTime));
                    } else if (timeUnit == null) {
                        log(errorLog, String.format("Invalid time unit value %d", gmmpRule.getTimeUnit()));
                    } else {
                        smartRule.setOperand(new TimePeriod(amountOfTime, timeUnit));
                    }
                }
            } else {
                log(errorLog, String.format("Invalid field \"%d\" in GMMP rule:\n%s", field, gmmpRule));
            }
            if (smartRule.getOperator() == null) {
                log(errorLog, String.format("Invalid combination of field \"%d\" (%s) and operator \"%d\" in rule:\n%s", field,
                    smartRule.getField(), gmmpRule.getOperator(), gmmpRule));
                continue;
            }
            result.getRules().add(smartRule);
        }

        return result;
    }

    /**
     * This takes in an {@link AgnosticSmartPlaylist} and converts it to a {@link GmmpSmartPlaylist}
     * @param agnosticSmartPlaylist the format-agnostic playlist to convert
     * @return the converted GMMP-formatted playlist
     */
    public FormattedSmartPlaylist convert(final AgnosticSmartPlaylist agnosticSmartPlaylist,
            final Collection<String> errorLog) {
        final GmmpSmartPlaylist result = new GmmpSmartPlaylist();
        setDefaultsOn(result);
        overrideDefaults(result, agnosticSmartPlaylist, errorLog);
        result.setName(agnosticSmartPlaylist.getName());
        for (final Rule smartRule: agnosticSmartPlaylist.getRules()) {
            final GmmpSmartPlaylist.Rule gmmpRule = new GmmpSmartPlaylist.Rule();
            gmmpRule.setVersion(1);
            final MetadataField field = smartRule.getField();
            if (STRING_FIELD_VALUES.contains(field)) {
                gmmpRule.setField(STRING_FIELD_MAP.inverse().get(field));
                gmmpRule.setOperator(STRING_OPERATOR_MAP.inverse().get(smartRule.getOperator()));
            } else if (NUMBER_FIELD_VALUES.contains(field)) {
                gmmpRule.setField(NUMBER_FIELD_MAP.inverse().get(field));
                gmmpRule.setOperator(NUMBER_OPERATOR_MAP.inverse().get(smartRule.getOperator()));
            } else if (DATE_FIELD_VALUES.contains(field)) {
                gmmpRule.setField(DATE_FIELD_MAP.inverse().get(field));
                gmmpRule.setOperator(DATE_OPERATOR_MAP.inverse().get(smartRule.getOperator()));
            } else {
                log(errorLog, String.format("%s field is not supported in GMMP playlists\nRule = %s", field, smartRule));
                continue;
            }
            final Object operand = smartRule.getOperand();
            if (operand instanceof String) {
                gmmpRule.setValue((String) operand);
                gmmpRule.setTimeUnit(TIME_UNIT_MAP.inverse().get(null));
            } else if (operand instanceof TimePeriod) {
                final TimePeriod time = (TimePeriod) operand;
                // GMMP only supports time units up to days (does not support weeks)
                final Pair<Long, TimeUnit> timePair = time.getLargestUnit(TimeUnit.DAY);
                gmmpRule.setValue(timePair.getFirst().toString());
                gmmpRule.setTimeUnit(TIME_UNIT_MAP.inverse().get(timePair.getSecond()));
            } else if (operand instanceof Date) {
                gmmpRule.setValue(DATE_FORMAT.format(operand));
                gmmpRule.setTimeUnit(TIME_UNIT_MAP.inverse().get(null));
            } else {
                log(errorLog, String.format("Something went horribly wrong. the operand of a %s should be a %s, %s, or %s, but was a %s. Rule = %s",
                    Rule.class.getCanonicalName(), String.class.getName(), TimePeriod.class.getCanonicalName(),
                    Date.class.getName(), operand.getClass().getCanonicalName(), smartRule));
            }

            result.getRules().add(gmmpRule);
        }
        return result;
    }

    /**
     * Sets the default values on the playlist. These values are what GMMP uses when they are missing from the XML.
     * @param gmmpSmartPlaylist the playlist to to set defaults on
     */
    private void setDefaultsOn(final GmmpSmartPlaylist gmmpSmartPlaylist) {
        gmmpSmartPlaylist.setVersion(1)
            .setOrder(ORDER_BY_MAP.inverse().get(defaultOrderByField))
            .setAscending(defaultOrderIsAscending)
            .setMatchAll(defaultMatchAll)
            .setLimit(defaultLimit);
    }

    /**
     * Overrides the default values in gmmpSmartPlaylist with the values present in smartPlaylist
     * @param gmmpSmartPlaylist the playlist that's being edited
     * @param smartPlaylist     the playlist whose values are being read
     * @param errorLog          log for errors encountered during operation
     */
    private void overrideDefaults(final GmmpSmartPlaylist gmmpSmartPlaylist, final AgnosticSmartPlaylist smartPlaylist,
            final Collection<String> errorLog) {
        if (smartPlaylist.isMatchAll() != null) {
            gmmpSmartPlaylist.setMatchAll(smartPlaylist.isMatchAll());
        }
        if (smartPlaylist.getOrder() != null) {
            if (smartPlaylist.getOrder().getKey() != null) {
                if (ORDER_BY_MAP.inverse().get(smartPlaylist.getOrder().getKey()) != null) {
                    gmmpSmartPlaylist.setOrder(ORDER_BY_MAP.inverse().get(smartPlaylist.getOrder().getKey()));
                } else {
                    log(errorLog, String.format("Order value %s is not allowed in GMMP playlists", smartPlaylist.getOrder().getKey()));
                }
            }
            gmmpSmartPlaylist.setAscending(smartPlaylist.getOrder().isAscending());
        }
        if (smartPlaylist.getLimit() != null) {
            gmmpSmartPlaylist.setLimit(smartPlaylist.getLimit());
        }
    }

    /**
     * Sets the default values on the playlist. These values are what GMMP uses when they are missing from the XML.
     * @param smartPlaylist the playlist to to set defaults on
     */
    private void setDefaultsOn(final AgnosticSmartPlaylist smartPlaylist) {
        final Order order = new Order()
            .setKey(defaultOrderByField)
            .setAscending(defaultOrderIsAscending);
        smartPlaylist.setOrder(order)
            .setMatchAll(defaultMatchAll)
            .setLimit(defaultLimit);
    }

    /**
     * Overrides the default values in smartPlaylist with the values present in gmmpSmartPlaylist
     * @param smartPlaylist     the playlist that's being edited
     * @param gmmpSmartPlaylist the playlist whose values are being read
     * @param errorLog          log for errors encountered during operation
     */
    private void overrideDefaults(final AgnosticSmartPlaylist smartPlaylist, final GmmpSmartPlaylist gmmpSmartPlaylist,
            final Collection<String> errorLog) {
        if (gmmpSmartPlaylist.getOrder() != null) {
            if (ORDER_BY_MAP.get(gmmpSmartPlaylist.getOrder()) != null) {
                smartPlaylist.getOrder().setKey(ORDER_BY_MAP.get(gmmpSmartPlaylist.getOrder()));
            } else {
                log(errorLog, String.format("Invalid order value \"%d\" in GMMP playlist", gmmpSmartPlaylist.getOrder()));
            }
        }
        if (gmmpSmartPlaylist.isAscending()) {
            smartPlaylist.getOrder().setAscending(gmmpSmartPlaylist.isAscending());
        }
        if (gmmpSmartPlaylist.isMatchAll() != null) {
            smartPlaylist.setMatchAll(gmmpSmartPlaylist.isMatchAll());
        }
        if (gmmpSmartPlaylist.getLimit() != null) {
            smartPlaylist.setLimit(gmmpSmartPlaylist.getLimit());
        }
    }

    private void log(final Collection<String> errorLog, final String message) {
        logger.error(message);
        errorLog.add(message);
    }
}
