package com.aaron.smartplaylists.playlists;

import com.aaron.smartplaylists.Order;
import com.aaron.smartplaylists.Rule;
import com.aaron.smartplaylists.api.SmartPlaylist;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * An AgnosticSmartPlaylist is abstract in that it doesn't have an associated file format, but can still store data, with
 * no regard for how it will be serialized/de-serialized.
 * todo: playlist type
 */
public class AgnosticSmartPlaylist implements SmartPlaylist {
    private String name;
    private boolean matchAll;
    private Integer limit;
    private Order order;
    private final List<Rule> rules;

    public AgnosticSmartPlaylist() {
        rules = Lists.newArrayList();
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public boolean isMatchAll() {
        return matchAll;
    }

    public void setMatchAll(final boolean matchAll) {
        this.matchAll = matchAll;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(final Integer limit) {
        this.limit = limit;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(final Order order) {
        this.order = order;
    }

    public List<Rule> getRules() {
        return rules;
    }

    public enum PlaylistType {
        MUSIC,
        VIDEO,
        MIXED
    }
}
