package com.aaron.smartplaylists.api;

import com.aaron.smartplaylists.converters.GmmpPlaylistConverter;
import com.aaron.smartplaylists.converters.XbmcV12PlaylistConverter;
import com.aaron.smartplaylists.converters.XbmcV11PlaylistConverter;
import com.aaron.smartplaylists.playlists.AgnosticSmartPlaylist;
import com.aaron.smartplaylists.playlists.GmmpSmartPlaylist;
import com.aaron.smartplaylists.playlists.XbmcV12SmartPlaylist;
import com.aaron.smartplaylists.playlists.XbmcV11SmartPlaylist;
import org.apache.log4j.Logger;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This class provides a simple API for loading playlists from files and converting between two playlist types
 */
public class ConverterApi {
    private static final Logger logger = Logger.getLogger(ConverterApi.class);

    private final XbmcV11PlaylistConverter xbmcV11PlaylistConverter = new XbmcV11PlaylistConverter();
    private final XbmcV12PlaylistConverter xbmcV12PlaylistConverter = new XbmcV12PlaylistConverter();
    private final GmmpPlaylistConverter gmmpPlaylistConverter = new GmmpPlaylistConverter();

    private final Map<Class<? extends FormattedSmartPlaylist>, PlaylistConverter> classConverterMap = new HashMap<>();

    private final Deque<PlaylistConverter> converters = new LinkedList<>();

    private final List<String> errorLog = new ArrayList<>();

    public ConverterApi() {
        converters.add(xbmcV12PlaylistConverter);
        converters.add(xbmcV11PlaylistConverter);
        converters.add(gmmpPlaylistConverter);

        classConverterMap.put(XbmcV11SmartPlaylist.class, xbmcV11PlaylistConverter);
        classConverterMap.put(XbmcV12SmartPlaylist.class, xbmcV12PlaylistConverter);
        classConverterMap.put(GmmpSmartPlaylist.class, gmmpPlaylistConverter);
    }

    /**
     * This changes the order of the converters according to the file extension of the playlist. Converters that match
     * the file extension are put at the front to minimize the number of times that a playlist is attempted to be
     * de-serialized with the wrong converter.
     * @param filenameExtension the file extension of the file being read in
     */
    private void reorderConverterList(final String filenameExtension) {
        if (XbmcV11SmartPlaylist.DEFAULT_FILE_EXTENSION.equals(filenameExtension)) {
            converters.remove(xbmcV11PlaylistConverter);
            converters.addFirst(xbmcV11PlaylistConverter);
            converters.remove(xbmcV12PlaylistConverter);
            converters.addFirst(xbmcV12PlaylistConverter);
        } else if (GmmpSmartPlaylist.DEFAULT_FILE_EXTENSION.equals(filenameExtension)) {
            converters.remove(gmmpPlaylistConverter);
            converters.addFirst(gmmpPlaylistConverter);
        }
    }

    public FormattedSmartPlaylist loadFromFile(final File file) throws FileNotFoundException {
        final String[] filenameParts = file.getName().split("\\.");
        reorderConverterList(filenameParts.length > 1 ? filenameParts[filenameParts.length - 1].toLowerCase() : null);

        for (final PlaylistConverter converter: converters) {
            try {
                return converter.readFromFile(file);
            } catch (final JAXBException ignored) {}
        }
        logger.error("couldn't find a way to de-serialize the playlist file");
        throw new IllegalArgumentException("couldn't find a way to de-serialize the playlist file");
    }

    private PlaylistConverter findConverter(final Class<? extends FormattedSmartPlaylist> type) {
        final PlaylistConverter converter = classConverterMap.get(type);
        if (converter == null) {
            throw new IllegalArgumentException("couldn't find a converter for " + type.toString());
        }
        return converter;
    }

    /**
     * Converts a FormattedSmartPlaylist into an AgnosticSmartPlaylist. This conversion step is a necessary, intermediary
     * step in converting one FormattedSmartPlaylist into another type of FormattedSmartPlaylist
     * @param formattedSmartPlaylist a playlist with a format
     * @return a playlist with no format
     */
    public AgnosticSmartPlaylist convert(final FormattedSmartPlaylist formattedSmartPlaylist) {
        final PlaylistConverter converter = findConverter(formattedSmartPlaylist.getClass());
        return converter.convert(formattedSmartPlaylist, errorLog);
    }

    /**
     * Converts an AgnosticSmartPlaylist into a specific type of FormattedSmartPlaylist
     * @param agnosticSmartPlaylist the playlist to convert
     * @param outputType the desired FormattedSmartPlaylist type
     * @return an object of outputType's type
     */
    public FormattedSmartPlaylist convert(final AgnosticSmartPlaylist agnosticSmartPlaylist, final Class<? extends FormattedSmartPlaylist> outputType) {
        final PlaylistConverter converter = findConverter(outputType);
        return converter.convert(agnosticSmartPlaylist, errorLog);
    }

    /**
     * Converts a FormattedSmartPlaylist into a different type of FormattedSmartPlaylist, e.g. XBMC -> GMMP
     * @param formattedSmartPlaylist the playlist to convert
     * @param outputType the desired FormattedSmartPlaylist type
     * @return an object of outputType's type
     */
    public FormattedSmartPlaylist convert(final FormattedSmartPlaylist formattedSmartPlaylist, final Class<? extends FormattedSmartPlaylist> outputType) {
        final AgnosticSmartPlaylist intermediary = convert(formattedSmartPlaylist);
        return convert(intermediary, outputType);
    }

    public List<String> getErrorLog() {
        return errorLog;
    }

    public void clearLog() {
        errorLog.clear();
    }
}
