package com.aaron.smartplaylists;

/**
 * An order defines how a playlist is sorted.
 */
public class Order {
    private boolean ascending;
    private MetadataField key;

    public boolean isAscending() {
        return ascending;
    }

    public void setAscending(final boolean ascending) {
        this.ascending = ascending;
    }

    public MetadataField getKey() {
        return key;
    }

    public void setKey(final MetadataField key) {
        this.key = key;
    }
}
