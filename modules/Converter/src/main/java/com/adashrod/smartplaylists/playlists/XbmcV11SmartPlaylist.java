package com.adashrod.smartplaylists.playlists;

import com.adashrod.smartplaylists.api.FormattedSmartPlaylist;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * JAXB-bound class for reading and writing XBMC smart playlists
 * http://wiki.xbmc.org/index.php?title=Smart_playlists
 * todo: try to reorder attributes on rules
 */
@XmlRootElement(name = "smartplaylist")
@XmlType(propOrder = {"name", "match", "rules", "order", "limit"})
public class XbmcV11SmartPlaylist implements FormattedSmartPlaylist, XbmcSmartPlaylist {
    private String type;
    private String name;
    private String match;
    private final List<XbmcSmartPlaylist.Rule> rules;
    private XbmcSmartPlaylist.Order order;
    private Integer limit;

    public XbmcV11SmartPlaylist() {
        rules = new ArrayList<>();
    }

    @Override
    public XbmcSmartPlaylist.Rule newRule() {
        return new Rule();
    }

    @Override
    public XbmcSmartPlaylist.Order newOrder() {
        return new Order();
    }

    @Override
    @XmlAttribute(name = "type")
    public String getType() {
        return type;
    }

    @Override
    public XbmcSmartPlaylist setType(final String type) {
        this.type = type;
        return this;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public XbmcSmartPlaylist setName(final String name) {
        this.name = name;
        return this;
    }

    @Override
    public String getMatch() {
        return match;
    }

    @Override
    public XbmcSmartPlaylist setMatch(final String match) {
        this.match = match;
        return this;
    }

    @Override
    @XmlElement(name = "rule", type = Rule.class)
    public List<XbmcSmartPlaylist.Rule> getRules() {
        return rules;
    }

    @Override
    @XmlElement(name = "order", type = Order.class)
    public XbmcSmartPlaylist.Order getOrder() {
        return order;
    }

    @Override
    public XbmcSmartPlaylist setOrder(final XbmcSmartPlaylist.Order order) {
        this.order = order;
        return this;
    }

    @Override
    public Integer getLimit() {
        return limit;
    }

    @Override
    public XbmcSmartPlaylist setLimit(final Integer limit) {
        this.limit = limit;
        return this;
    }

    @Override
    public String toString() {
        final JAXBContext context;
        try {
            context = JAXBContext.newInstance(XbmcV11SmartPlaylist.class);
        } catch (final JAXBException je) {
            // this shouldn't happen
            je.printStackTrace();
            return null;
        }
        try {
            final Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            final Writer writer = new StringWriter();
            marshaller.marshal(this, writer);
            return writer.toString();
        } catch (final JAXBException je) {
            je.printStackTrace();
            return null;
        }
    }

    public static class Order implements XbmcSmartPlaylist.Order {
        private String direction;
        private String sortKey;

        @Override
        @XmlAttribute(name = "direction")
        public String getDirection() {
            return direction;
        }

        @Override
        public Order setDirection(final String direction) {
            this.direction = direction;
            return this;
        }

        @Override
        @XmlValue
        public String getSortKey() {
            return sortKey;
        }

        @Override
        public Order setSortKey(final String sortKey) {
            this.sortKey = sortKey;
            return this;
        }
    }

    public static class Rule implements XbmcSmartPlaylist.Rule {
        private String field;
        private String operator;
        private String operand;

        @Override
        @XmlAttribute(name = "field")
        public String getField() {
            return field;
        }

        @Override
        public Rule setField(final String field) {
            this.field = field;
            return this;
        }

        @Override
        @XmlAttribute(name = "operator")
        public String getOperator() {
            return operator;
        }

        @Override
        public Rule setOperator(final String operator) {
            this.operator = operator;
            return this;
        }

        @Override
        @XmlValue
        public String getOperand() {
            return operand;
        }

        @Override
        public Rule setOperand(final String operand) {
            this.operand = operand;
            return this;
        }

        @Override
        public String toString() {
            return String.format("<rule field=\"%s\" operator=\"%s\">%s</rule>", field, operator, operand);
        }
    }
}
