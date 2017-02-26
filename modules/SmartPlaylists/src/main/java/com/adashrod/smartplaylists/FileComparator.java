package com.adashrod.smartplaylists;

import java.io.File;
import java.util.Comparator;

/**
 * A comparator for {@link java.io.File}s. It supports sorting by filenames and file types. There are static functions
 * for getting a comparator, e.g. FileComparator.name(), which returns one that does name comparisons. There are
 * convenience functions for setting options on a comparator that return the comparator,
 * e.g. FileComparator.name().caseInsensitive().descending(), which creates one that does case-insensitive name comparisons
 * in descending order.
 */
public class FileComparator implements Comparator<File> {
    private Field field;
    private boolean directorySensitive = true;
    private boolean caseSensitive = true;
    private boolean ascending = true;

    /**
     * @return a Comparator that compares files by their complete file names
     */
    public static FileComparator name() {
        final FileComparator comparator = new FileComparator();
        comparator.field = Field.NAME;
        return comparator;
    }

    /**
     * @return a Comparator that compares files by their extensions
     */
    public static FileComparator type() {
        final FileComparator comparator = new FileComparator();
        comparator.field = Field.TYPE;
        return comparator;
    }

    @Override
    public int compare(final File file1, final File file2) {
        final String file1Text, file2Text;

        if (field == Field.NAME) {
            file1Text = file1 != null ? file1.getName() : "";
            file2Text = file2 != null ? file2.getName() : "";
        } else if (field == Field.TYPE) {
            if (file1 != null) {
                final String[] file1Parts = file1.getName().split("\\.");
                file1Text = file1Parts.length > 1 ? file1Parts[file1Parts.length - 1] : "";
            } else {
                file1Text = "";
            }
            if (file2 != null) {
                final String[] file2Parts = file2.getName().split("\\.");
                file2Text = file2Parts.length > 1 ? file2Parts[file2Parts.length - 1] : "";
            } else {
                file2Text = "";
            }
        } else {
            return 0;
        }

        if (directorySensitive) {
            final int dirComp = ascending ? directoryCompare(file1, file2) : directoryCompare(file2, file1);
            if (dirComp != 0) {
                return dirComp;
            }
        }
        return caseSensitive ? (ascending ? file1Text.compareTo(file2Text) : file2Text.compareTo(file2Text)) :
            (ascending ? file1Text.compareToIgnoreCase(file2Text) : file2Text.compareToIgnoreCase(file1Text));
    }

    private int directoryCompare(final File file1, final File file2) {
        final boolean file1IsDir = file1.isDirectory();
        final boolean file2IsDir = file2.isDirectory();
        if (file1IsDir == file2IsDir) {
            return 0;
        }
        if (file1IsDir) {
            return -1;
        }
        return 1;
    }

    /**
     * Set the comparator to sort directories and files separately, i.e. after sorting, all directories will be at one
     * end of the list and all files will be at the other
     * @return this
     */
    public FileComparator directorySensitive() {
        this.directorySensitive = true;
        return this;
    }

    /**
     * Set the comparator to sort directories and files together, i.e. directories and files will be intermingled after
     * sorting
     * @return this
     */
    public FileComparator directoryInsensitive() {
        this.directorySensitive = false;
        return this;
    }

    /**
     * Set the comparator to do case-sensitive comparisons
     * @return this
     */
    public FileComparator caseSensitive() {
        this.caseSensitive = true;
        return this;
    }

    /**
     * Set the comparator to ignore case when sorting
     * @return this
     */
    public FileComparator caseInsensitive() {
        this.caseSensitive = false;
        return this;
    }

    /**
     * Set the comparator to do an ascending sort (a-z)
     * @return this
     */
    public FileComparator ascending() {
        this.ascending = true;
        return this;
    }

    /**
     * Set the comparator to do a descending sort (z-a)
     * @return this
     */
    public FileComparator descending() {
        this.ascending = false;
        return this;
    }

    private static enum Field {
        NAME,
        TYPE
    }
}
