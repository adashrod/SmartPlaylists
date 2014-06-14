package com.aaron.smartplaylists.api;

import com.aaron.smartplaylists.playlists.AgnosticSmartPlaylist;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collection;

/**
 * A PlaylistConverter is a utility for converting between {@link AgnosticSmartPlaylist}s and subclasses of
 * {@link FormattedSmartPlaylist}
 */
public interface PlaylistConverter {
    /**
     * Uses a de-serializer, such as JAXB, to read in the file and returns the bound java object.
     * @param file the file to read
     * @return A specific kind of formatted smart playlist
     * @throws JAXBException error de-serializing XML
     * @throws FileNotFoundException file not found
     */
    FormattedSmartPlaylist readFromFile(File file) throws JAXBException, FileNotFoundException;

    /**
     * Converts an {@link AgnosticSmartPlaylist} into an object that has a specific, associated file format
     * @param agnosticSmartPlaylist the format-agnostic playlist to convert
     * @param errorLog any errors encountered during the conversion will be appended to this
     * @return the same playlist in a specific format
     */
    FormattedSmartPlaylist convert(AgnosticSmartPlaylist agnosticSmartPlaylist, Collection<String> errorLog);

    /**
     * Converts a {@link FormattedSmartPlaylist} that has an associated file format and converts it into a general,
     * format-agnostic smart playlist object.
     * @param specificSmartPlaylist the formatted playlist to convert
     * @param errorLog any errors encountered during the conversion will be appended to this
     * @return the same playlist without format
     */
    AgnosticSmartPlaylist convert(FormattedSmartPlaylist specificSmartPlaylist, Collection<String> errorLog);
}
