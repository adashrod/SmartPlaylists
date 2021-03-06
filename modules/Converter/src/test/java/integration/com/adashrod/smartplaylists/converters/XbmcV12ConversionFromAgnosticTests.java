package com.adashrod.smartplaylists.converters;

import com.adashrod.smartplaylists.api.PlaylistConverter;
import com.adashrod.smartplaylists.playlists.MetadataField;
import com.adashrod.smartplaylists.playlists.Operator;
import com.adashrod.smartplaylists.playlists.Rule;
import com.adashrod.smartplaylists.playlists.AgnosticSmartPlaylist;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;

import static junit.framework.Assert.assertEquals;

/**
 * integration tests for the v12 converter (Agnostic -> XBMC)
 */
public class XbmcV12ConversionFromAgnosticTests {
    private final PlaylistConverter xbmcV12PlaylistConverter = new XbmcV12PlaylistConverter();

    private final Collection<String> errorLog = new ArrayList<>();

    @Before
    public void before() {
        errorLog.clear();
    }

    @Test
    public void invalidOrderByOperand() throws Exception {
        final AgnosticSmartPlaylist smartPlaylist = new AgnosticSmartPlaylist();
        final Rule rule = new Rule();
        rule.setField(MetadataField.DISC_NUMBER);
        rule.setOperator(Operator.IS);
        rule.setOperand("1");
        smartPlaylist.getRules().add(rule);
        xbmcV12PlaylistConverter.convert(smartPlaylist, errorLog);
        assertEquals(1, errorLog.size());
    }
}
