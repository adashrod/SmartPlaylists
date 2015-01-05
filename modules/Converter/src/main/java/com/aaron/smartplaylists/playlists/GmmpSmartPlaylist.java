package com.aaron.smartplaylists.playlists;

import com.aaron.smartplaylists.api.FormattedSmartPlaylist;

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

    public void setVersion(final Integer version) {
        this.version = version;
    }

    @XmlElement(name = "Name")
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @XmlElement(name = "Order")
    public Integer getOrder() {
        return order;
    }

    public void setOrder(final Integer order) {
        this.order = order;
    }

    @XmlElement(name = "Ascending")
    public Boolean isAscending() {
        return ascending;
    }

    public void setAscending(final Boolean ascending) {
        this.ascending = ascending;
    }

    @XmlElement(name = "Limit")
    public Integer getLimit() {
        return limit;
    }

    public void setLimit(final Integer limit) {
        this.limit = limit;
    }

    @XmlElement(name = "MatchAll")
    public Boolean isMatchAll() {
        return matchAll;
    }

    public void setMatchAll(final Boolean matchAll) {
        this.matchAll = matchAll;
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

        public void setVersion(final int version) {
            this.version = version;
        }

        @XmlElement(name = "Field")
        public int getField() {
            return field;
        }

        public void setField(final int field) {
            this.field = field;
        }

        @XmlElement(name = "Operator")
        public int getOperator() {
            return operator;
        }

        public void setOperator(final int operator) {
            this.operator = operator;
        }

        @XmlElement(name = "Value")
        public String getValue() {
            return value;
        }

        public void setValue(final String value) {
            this.value = value;
        }

        @XmlElement(name = "TimeUnit")
        public int getTimeUnit() {
            return timeUnit;
        }

        public void setTimeUnit(final int timeUnit) {
            this.timeUnit = timeUnit;
        }

        @Override
        public String toString() {
            return String.format("<Rule><Version>%d</Version><Field>%d</Field><Operator>%d</Operator><Value>%s</Value><TimeUnit>%d</TimeUnit></Rule>",
                version, field, operator, value, timeUnit);
        }
    }
}
