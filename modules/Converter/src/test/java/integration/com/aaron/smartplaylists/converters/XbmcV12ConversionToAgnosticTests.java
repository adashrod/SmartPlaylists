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
 * integration tests for the v12 converter (XBMC format -> Agnostic)
 */
public class XbmcV12ConversionToAgnosticTests {
    private final String TEST_PLAYLIST_DIRECTORY = "./src/test/resources/";

    private final PlaylistConverter xbmcV12PlaylistConverter = new XbmcV12PlaylistConverter();

    private final Collection<String> errorLog = new ArrayList<>();

    private void testAFile(final String filename, final int expectedErrorCount) throws Exception {
        final File file = new File(TEST_PLAYLIST_DIRECTORY + filename);
        final FormattedSmartPlaylist playlist = xbmcV12PlaylistConverter.readFromFile(file);
        xbmcV12PlaylistConverter.convert(playlist, errorLog);
        assertEquals(expectedErrorCount, errorLog.size());
    }

    @Before
    public void before() {
        errorLog.clear();
    }

    @Test
    public void validButUnusualTime() throws Exception {
        testAFile("xbmc12/valid_unusual_time.xsp", 0);
    }

    @Test
    public void oneIllegalOperand() throws Exception {
        testAFile("xbmc12/1_wrong_operator.xsp", 1);
    }

    @Test
    public void oneInvalidField() throws Exception {
        testAFile("xbmc12/1_invalid_field.xsp", 1);
    }

    @Test
    public void invalidOrderByOperand() throws Exception {
        testAFile("xbmc12/invalid_order_by_operand.xsp", 1);
    }
}
