package com.adashrod.smartplaylists.playlists;

import com.adashrod.smartplaylists.api.FormattedSmartPlaylist;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * JAXB-bound class for reading and writing GoneMAD Music Player smart playlists
 * http://gonemadmusicplayer.blogspot.com/
 */
@XmlRootElement(name = "SmartPlaylist")
@XmlType(propOrder = {"version", "name", "order", "ascending", "limit", "matchAll", "rules"})
public class GmmpSmartPlaylist implements FormattedSmartPlaylist {
    public static final String DEFAULT_FILE_EXTENSION = "spl";

    private Integer version;
    private String name;
    private Integer order;
    private Boolean ascending;
    private Integer limit;
    private Boolean matchAll;
    private final List<Rule> rules;

    public GmmpSmartPlaylist() {
        rules = new ArrayList<>();
    }

    @XmlElement(name = "Version")
    public Integer getVersion() {
        return version;
    }

    public GmmpSmartPlaylist setVersion(final Integer version) {
        this.version = version;
        return this;
    }

    @XmlElement(name = "Name")
    public String getName() {
        return name;
    }

    public GmmpSmartPlaylist setName(final String name) {
        this.name = name;
        return this;
    }

    @XmlElement(name = "Order")
    public Integer getOrder() {
        return order;
    }

    public GmmpSmartPlaylist setOrder(final Integer order) {
        this.order = order;
        return this;
    }

    @XmlElement(name = "Ascending")
    public Boolean isAscending() {
        return ascending;
    }

    public GmmpSmartPlaylist setAscending(final Boolean ascending) {
        this.ascending = ascending;
        return this;
    }

    @XmlElement(name = "Limit")
    public Integer getLimit() {
        return limit;
    }

    public GmmpSmartPlaylist setLimit(final Integer limit) {
        this.limit = limit;
        return this;
    }

    @XmlElement(name = "MatchAll")
    public Boolean isMatchAll() {
        return matchAll;
    }

    public GmmpSmartPlaylist setMatchAll(final Boolean matchAll) {
        this.matchAll = matchAll;
        return this;
    }

    @XmlElementWrapper(name = "Rules")
    @XmlElement(name = "Rule")
    public List<Rule> getRules() {
        return rules;
    }

    @Override
    public String toString() {
        final JAXBContext context;
        try {
            context = JAXBContext.newInstance(GmmpSmartPlaylist.class);
        } catch (final JAXBException je) {
            // this shouldn't happen
            je.printStackTrace();
            return null;
        }
        try {
            final Marshaller marshaller = context.createMarshaller();
            // don't format output; GMMP chokes on XML with whitespace
            final Writer writer = new StringWriter();
            marshaller.marshal(this, writer);
            return writer.toString();
        } catch (final JAXBException je) {
            je.printStackTrace();
            return null;
        }
    }

    @XmlType(propOrder = {"version", "field", "operator", "value", "timeUnit"})
    public static class Rule {
        private int version;
        private int field;
        private int operator;
        private String value;
        private int timeUnit;

        @XmlElement(name = "Version")
        public int getVersion() {
            return version;
        }

        public Rule setVersion(final int version) {
            this.version = version;
            return this;
        }

        @XmlElement(name = "Field")
        public int getField() {
            return field;
        }

        public Rule setField(final int field) {
            this.field = field;
            return this;
        }

        @XmlElement(name = "Operator")
        public int getOperator() {
            return operator;
        }

        public Rule setOperator(final int operator) {
            this.operator = operator;
            return this;
        }

        @XmlElement(name = "Value")
        public String getValue() {
            return value;
        }

        public Rule setValue(final String value) {
            this.value = value;
            return this;
        }

        @XmlElement(name = "TimeUnit")
        public int getTimeUnit() {
            return timeUnit;
        }

        public Rule setTimeUnit(final int timeUnit) {
            this.timeUnit = timeUnit;
            return this;
        }

        @Override
        public String toString() {
            return String.format("<Rule><Version>%d</Version><Field>%d</Field><Operator>%d</Operator><Value>%s</Value><TimeUnit>%d</TimeUnit></Rule>",
                version, field, operator, value, timeUnit);
        }
    }
}
