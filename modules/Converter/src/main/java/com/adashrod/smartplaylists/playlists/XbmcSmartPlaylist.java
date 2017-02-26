package com.adashrod.smartplaylists.playlists;

import java.util.List;

/**
 * An interface for the different types of XBMC smart playlist. Currently there are two versions. They have the exact
 * same java implementation, but slightly different XML annotations.
 */
public interface XbmcSmartPlaylist {
    public static final String DEFAULT_FILE_EXTENSION = "xsp";

    public Rule newRule();

    public Order newOrder();

    public String getType();

    public XbmcSmartPlaylist setType(String type);

    public String getName();

    public XbmcSmartPlaylist setName(String name);

    public String getMatch();

    public XbmcSmartPlaylist setMatch(String match);

    public List<Rule> getRules();

    public Order getOrder();

    public XbmcSmartPlaylist setOrder(Order order);

    public Integer getLimit();

    public XbmcSmartPlaylist setLimit(Integer limit);

    public static interface Order {
        public String getDirection();

        public Order setDirection(String direction);

        public String getSortKey();

        public Order setSortKey(String sortKey);
    }

    public static interface Rule {
        public String getField();

        public Rule setField(String field);

        public String getOperator();

        public Rule setOperator(String operator);

        public String getOperand();

        public Rule setOperand(String operand);
    }
}
