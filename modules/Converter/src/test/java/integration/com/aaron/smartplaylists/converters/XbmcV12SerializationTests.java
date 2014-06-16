package com.aaron.smartplaylists.converters;

import com.aaron.smartplaylists.MetadataField;
import com.aaron.smartplaylists.Operator;
import com.aaron.smartplaylists.Rule;
import com.aaron.smartplaylists.playlists.AgnosticSmartPlaylist;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;

import static junit.framework.Assert.assertEquals;

/**
 * deserialization integration tests for the v12 converter
 */
public class XbmcV12SerializationTests {
    private final XbmcV12PlaylistConverter xbmcV12PlaylistConverter = new XbmcV12PlaylistConverter();

    private final Collection<String> errorLog = Lists.newArrayList();

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
