package com.aaron.smartplaylists.playlists;

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

    public void setType(String type);

    public String getName();

    public void setName(String name);

    public String getMatch();

    public void setMatch(String match);

    public List<Rule> getRules();

    public Order getOrder();

    public void setOrder(Order order);

    public Integer getLimit();

    public void setLimit(Integer limit);

    public static interface Order {
        public String getDirection();

        public void setDirection(String direction);

        public String getSortKey();

        public void setSortKey(String sortKey);
    }

    public static interface Rule {
        public String getField();

        public void setField(String field);

        public String getOperator();

        public void setOperator(String operator);

        public String getOperand();

        public void setOperand(String operand);
    }
}
