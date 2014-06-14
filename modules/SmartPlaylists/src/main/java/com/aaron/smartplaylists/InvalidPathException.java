package com.aaron.smartplaylists;

/**
 * Thrown to indicate that a path that could not be found on the file system was requested.
 */
public class InvalidPathException extends Exception {
    public InvalidPathException(final String message) {
        super(message);
    }
}
