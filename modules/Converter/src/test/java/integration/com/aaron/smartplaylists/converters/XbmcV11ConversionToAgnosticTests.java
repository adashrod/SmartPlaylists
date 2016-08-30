package com.aaron.smartplaylists.converters;

import com.aaron.smartplaylists.api.FormattedSmartPlaylist;
import com.aaron.smartplaylists.api.PlaylistConverter;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import static junit.framework.Assert.assertEquals;

/**
 * integration tests for the v11 converter (XBMC format -> Agnostic)
 */
public class XbmcV11ConversionToAgnosticTests {
    private final String TEST_PLAYLIST_DIRECTORY = "./src/test/resources/";

    private final PlaylistConverter xbmcV11PlaylistConverter = new XbmcV11PlaylistConverter();

    private final Collection<String> errorLog = new ArrayList<>();

    private void testAFile(final String filename, final int expectedErrorCount) throws Exception {
        final File file = new File(TEST_PLAYLIST_DIRECTORY + filename);
        final FormattedSmartPlaylist playlist = xbmcV11PlaylistConverter.readFromFile(file);
        xbmcV11PlaylistConverter.convert(playlist, errorLog);
        assertEquals(expectedErrorCount, errorLog.size());
    }

    @Before
    public void before() {
        errorLog.clear();
    }

    @Test
    public void oneInvalidDate() throws Exception {
        testAFile("xbmc11/1_invalid_date.xsp", 1);
    }

    @Test
    public void twoInvalidTimes() throws Exception {
        testAFile("xbmc11/2_invalid_times.xsp", 2);
    }

    @Test
    public void oneInvalidField() throws Exception {
        testAFile("xbmc11/1_invalid_field.xsp", 1);
    }

    @Test
    public void oneInvalidOperator() throws Exception {
        testAFile("xbmc11/1_invalid_operator.xsp", 1);
    }

    @Test
    public void validPlaylist1() throws Exception {
        testAFile("xbmc11/valid_playlist1.xsp", 0);
    }

    @Test
    public void validPlaylist2() throws Exception {
        testAFile("xbmc11/valid_playlist2.xsp", 0);
    }
}
