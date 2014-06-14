package com.aaron.smartplaylists.converters;

import com.aaron.smartplaylists.api.FormattedSmartPlaylist;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Collection;

import static junit.framework.Assert.assertEquals;

/**
 * integration tests for the v12 converter
 */
public class XbmcV12Tests {
    private final String TEST_PLAYLIST_DIRECTORY = "./modules/Converter/src/test/resources/";

    private final XbmcV12PlaylistConverter xbmcV12PlaylistConverter = new XbmcV12PlaylistConverter();

    private final Collection<String> errorLog = Lists.newArrayList();

    private void testAFile(final String filename, final int expectedErrorCount) throws Exception {
        final File file = new File(TEST_PLAYLIST_DIRECTORY + filename);
        final FormattedSmartPlaylist playlist = xbmcV12PlaylistConverter.readFromFile(file);
        xbmcV12PlaylistConverter.convert(playlist, errorLog);
        assertEquals(errorLog.size(), expectedErrorCount);
    }

    @Before
    public void before() {
        errorLog.clear();
    }

    @Test
    public void oneInvalidTime() throws Exception {
        testAFile("xbmc12_1_invalid_time.xsp", 1);
    }

    @Test
    public void oneIllegalOperand() throws Exception {
        testAFile("xbmc12_1_illegal_operand.xsp", 1);
    }
}
