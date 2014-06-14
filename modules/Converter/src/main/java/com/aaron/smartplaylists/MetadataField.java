package com.aaron.smartplaylists;

/**
 * These are all of the different types of metadata fields that can be used for rules and ordering. Note: RANDOM should
 * only be used in the context of ordering, not rules.
 */
public enum MetadataField {
    GENRE,
    ALBUM,
    ARTIST,
    ALBUM_ARTIST,
    TITLE,
    YEAR,
    TIME,
    TRACK_NUMBER,
    FILE_NAME,
    PATH,
    PLAY_COUNT,
    LAST_PLAYED,
    RATING,
    COMMENT,
    PLAYLIST,

    DATE_ADDED,
    DISC_NUMBER,

    RANDOM
}
