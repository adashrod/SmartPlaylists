package com.aaron.smartplaylists.playlists;

import com.aaron.smartplaylists.Order;
import com.aaron.smartplaylists.PlaylistType;
import com.aaron.smartplaylists.Rule;
import com.aaron.smartplaylists.api.SmartPlaylist;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * An AgnosticSmartPlaylist is abstract in that it doesn't have an associated file format, but can still store data, with
 * no regard for how it will be serialized/de-serialized.
 */
public class AgnosticSmartPlaylist implements SmartPlaylist {
    private PlaylistType playlistType;
    private String name;
    private boolean matchAll;
    private Integer limit;
    private Order order;
    private final List<Rule> rules;

    public AgnosticSmartPlaylist() {
        rules = Lists.newArrayList();
    }

    public PlaylistType getPlaylistType() {
        return playlistType;
    }

    public AgnosticSmartPlaylist setPlaylistType(final PlaylistType playlistType) {
        this.playlistType = playlistType;
        return this;
    }

    public String getName() {
        return name;
    }

    public AgnosticSmartPlaylist setName(final String name) {
        this.name = name;
        return this;
    }

    public boolean isMatchAll() {
        return matchAll;
    }

    public AgnosticSmartPlaylist setMatchAll(final boolean matchAll) {
        this.matchAll = matchAll;
        return this;
    }

    public Integer getLimit() {
        return limit;
    }

    public AgnosticSmartPlaylist setLimit(final Integer limit) {
        this.limit = limit;
        return this;
    }

    public Order getOrder() {
        return order;
    }

    public AgnosticSmartPlaylist setOrder(final Order order) {
        this.order = order;
        return this;
    }

    public List<Rule> getRules() {
        return rules;
    }
}
