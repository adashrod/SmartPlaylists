package com.adashrod.smartplaylists.playlists;

/**
 * An order defines how a playlist is sorted.
 */
public class Order {
    private boolean ascending;
    private MetadataField key;

    public boolean isAscending() {
        return ascending;
    }

    public Order setAscending(final boolean ascending) {
        this.ascending = ascending;
        return this;
    }

    public MetadataField getKey() {
        return key;
    }

    public Order setKey(final MetadataField key) {
        this.key = key;
        return this;
    }
}
