package com.aaron.smartplaylists.converters;

import com.aaron.smartplaylists.playlists.AgnosticSmartPlaylist;
import com.aaron.smartplaylists.api.FormattedSmartPlaylist;
import com.aaron.smartplaylists.api.PlaylistConverter;
import com.aaron.smartplaylists.playlists.XbmcSmartPlaylist;
import com.aaron.smartplaylists.playlists.XbmcV12SmartPlaylist;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Collection;

/**
 * XbmcV12PlaylistConverter knows how to convert {@link AgnosticSmartPlaylist}s to {@link com.aaron.smartplaylists.playlists.XbmcV12SmartPlaylist}s and vice
 * versa. It can also be used to read an {@link com.aaron.smartplaylists.playlists.XbmcV12SmartPlaylist} in from a file.
 */
public class XbmcV12PlaylistConverter implements PlaylistConverter {
    /**
     * Given an XML file that is a XBMC-formatted smart playlist, this reads that file and returns that playlist as an
     * object.
     * @param file the file to read
     * @return a {@link com.aaron.smartplaylists.playlists.XbmcV12SmartPlaylist}
     * @throws JAXBException error parsing the XML
     * @throws FileNotFoundException file not found
     */
    @Override
    public FormattedSmartPlaylist readFromFile(final File file) throws JAXBException, FileNotFoundException {
        final JAXBContext context = JAXBContext.newInstance(XbmcV12SmartPlaylist.class);
        final Unmarshaller unmarshaller = context.createUnmarshaller();
        final XbmcV12SmartPlaylist result = (XbmcV12SmartPlaylist) unmarshaller.unmarshal(new FileReader(file));
        for (final XbmcSmartPlaylist.Rule rule: result.getRules()) {
            if (rule.getOperand() == null) {
                // since the required = true annotation doesn't enforce that the element is actually there, enforce it manually here in the absence of schema validation
                throw new JAXBException("Required element <value> was not found");
            }
        }
        return result;
    }

    /**
     * This takes in a {@link com.aaron.smartplaylists.playlists.XbmcV12SmartPlaylist} and converts it to an {@link AgnosticSmartPlaylist}
     * @param formattedSmartPlaylist the XBMC playlist to convert
     * @return the converted format-agnostic playlist
     */
    @Override
    public AgnosticSmartPlaylist convert(final FormattedSmartPlaylist formattedSmartPlaylist,
            final Collection<String> errorLog) {
        return XbmcPlaylistConverterTools.convert((XbmcSmartPlaylist) formattedSmartPlaylist, errorLog);
    }

    /**
     * This takes in an {@link AgnosticSmartPlaylist} and converts it to a {@link com.aaron.smartplaylists.playlists.XbmcV12SmartPlaylist}
     * @param agnosticSmartPlaylist the format-agnostic playlist to convert
     * @return the converted XBMC-formatted playlist
     */
    @Override
    public FormattedSmartPlaylist convert(final AgnosticSmartPlaylist agnosticSmartPlaylist,
            final Collection<String> errorLog) {
        return (XbmcV12SmartPlaylist) XbmcPlaylistConverterTools.convert(agnosticSmartPlaylist, XbmcV12SmartPlaylist.class,
            errorLog);
    }
}
