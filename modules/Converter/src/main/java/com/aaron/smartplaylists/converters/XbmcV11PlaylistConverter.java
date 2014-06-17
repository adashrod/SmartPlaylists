package com.aaron.smartplaylists.converters;

import com.aaron.smartplaylists.playlists.AgnosticSmartPlaylist;
import com.aaron.smartplaylists.api.FormattedSmartPlaylist;
import com.aaron.smartplaylists.api.PlaylistConverter;
import com.aaron.smartplaylists.playlists.XbmcSmartPlaylist;
import com.aaron.smartplaylists.playlists.XbmcV11SmartPlaylist;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Collection;

/**
 * XbmcV11PlaylistConverter knows how to convert {@link AgnosticSmartPlaylist}s to {@link com.aaron.smartplaylists.playlists.XbmcV11SmartPlaylist}s and vice
 * versa. It can also be used to read an {@link com.aaron.smartplaylists.playlists.XbmcV11SmartPlaylist} in from a file.
 */
public class XbmcV11PlaylistConverter implements PlaylistConverter {
    /**
     * Given an XML file that is a XBMC-formatted smart playlist, this reads that file and returns that playlist as an
     * object.
     * @param file the file to read
     * @return a {@link com.aaron.smartplaylists.playlists.XbmcV11SmartPlaylist}
     * @throws JAXBException error parsing the XML
     * @throws FileNotFoundException file not found
     */
    @Override
    public FormattedSmartPlaylist readFromFile(final File file) throws JAXBException, FileNotFoundException {
        final JAXBContext context = JAXBContext.newInstance(XbmcV11SmartPlaylist.class);
        final Unmarshaller unmarshaller = context.createUnmarshaller();
        unmarshaller.setEventHandler(new ValidationEventHandler() {
            @Override
            public boolean handleEvent(final ValidationEvent event) {
                // in case this is validating an XBMC12 against the XBMC11 schema, an "unexpected element" warning (for a <value/> inside a <rule/> wouldn't cause a JAXBException, so enforce that restriction here
                return !event.getMessage().contains("unexpected element");
            }
        });
        return (XbmcV11SmartPlaylist) unmarshaller.unmarshal(new FileReader(file));
    }

    /**
     * This takes in a {@link com.aaron.smartplaylists.playlists.XbmcV11SmartPlaylist} and converts it to an {@link AgnosticSmartPlaylist}
     * @param formattedSmartPlaylist the XBMC playlist to convert
     * @return the converted format-agnostic playlist
     */
    @Override
    public AgnosticSmartPlaylist convert(final FormattedSmartPlaylist formattedSmartPlaylist,
            final Collection<String> errorLog) {
        return XbmcPlaylistConverterTools.convert((XbmcSmartPlaylist) formattedSmartPlaylist, errorLog);
    }

    /**
     * This takes in an {@link AgnosticSmartPlaylist} and converts it to a {@link com.aaron.smartplaylists.playlists.XbmcV11SmartPlaylist}
     * @param agnosticSmartPlaylist the format-agnostic playlist to convert
     * @return the converted XBMC-formatted playlist
     */
    @Override
    public FormattedSmartPlaylist convert(final AgnosticSmartPlaylist agnosticSmartPlaylist,
            final Collection<String> errorLog) {
        return (XbmcV11SmartPlaylist) XbmcPlaylistConverterTools.convert(agnosticSmartPlaylist, XbmcV11SmartPlaylist.class,
            errorLog);
    }
}
