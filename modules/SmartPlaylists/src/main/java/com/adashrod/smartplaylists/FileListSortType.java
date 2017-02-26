package com.adashrod.smartplaylists;

import java.io.File;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * An enum that specifies how a list of files is currently sorted.
 */
public enum FileListSortType {
    ASCENDING_NAME,  // alphabetically by complete file name (a-z)
    DESCENDING_NAME, // reverse-alphabetically by complete file name (z-a)
    ASCENDING_TYPE,  // alphabetically by file extension (a-z)
    DESCENDING_TYPE; // reverse-alphabetically by file extension (a-z)

    private static final Map<FileListSortType, FileListSortType> complements = new HashMap<>();
    private static final Map<FileListSortType, Comparator<File>> complementComparators = new HashMap<>();

    static {
        complements.put(ASCENDING_NAME, DESCENDING_NAME);
        complements.put(DESCENDING_NAME, ASCENDING_NAME);
        complements.put(ASCENDING_TYPE, DESCENDING_TYPE);
        complements.put(DESCENDING_TYPE, ASCENDING_TYPE);
        complementComparators.put(ASCENDING_NAME, FileComparator.name().descending().caseInsensitive());
        complementComparators.put(DESCENDING_NAME, FileComparator.name().ascending().caseInsensitive());
        complementComparators.put(ASCENDING_TYPE, FileComparator.type().descending().caseInsensitive());
        complementComparators.put(DESCENDING_TYPE, FileComparator.type().ascending().caseInsensitive());
    }

    /**
     * @return true if the sort is a "sort-by-name" type of sort
     */
    public boolean isNameType() {
        return this == ASCENDING_NAME || this == DESCENDING_NAME;
    }

    /**
     * @return true if the sort is a "sort-by-type" type of sort
     */
    public boolean isTypeType() {
        return this == ASCENDING_TYPE || this == DESCENDING_TYPE;
    }

    /**
     * @return the ascending sort type for descending and vice-versa, based on the same sort field (name, type, etc)
     */
    public FileListSortType getComplement() {
        return complements.get(this);
    }

    /**
     * @return the file comparator that corresponds to the complement sort type
     */
    public Comparator<File> getComplementComparator() {
        return complementComparators.get(this);
    }
}
