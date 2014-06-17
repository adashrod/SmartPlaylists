package com.aaron.smartplaylists.playlists;

/**
 * These are all of the different types of metadata fields that can be used for rules and ordering. Note: RANDOM should
 * only be used in the context of ordering, not rules.
 */
public enum MetadataField {
    // strings
    ARTIST,
    ALBUM_ARTIST,
    TITLE,
    ALBUM,
    GENRE,
    PATH,
    FILE_NAME,
    COMMENT,
    PLAYLIST,

    // numbers/times
    YEAR,
    DISC_NUMBER,
    TRACK_NUMBER,
    DURATION, // time
    PLAY_COUNT,
    RATING,

    // dates/numbers
    LAST_PLAYED,
    DATE_ADDED,

    // used only for ordering
    RANDOM
}
