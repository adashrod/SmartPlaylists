package com.adashrod.smartplaylists.playlists;

/**
 * A Rule determines whether or not media should be included in a playlist. It specifies a metadata field, an operator,
 * and an operand. E.g. "genre" "is not" "country".
 */
public class Rule {
    private MetadataField field;
    private Operator operator;
    /**
     * can be a String, Date, or Time
     */
    private Object operand;

    public MetadataField getField() {
        return field;
    }

    public Rule setField(final MetadataField field) {
        this.field = field;
        return this;
    }

    public Operator getOperator() {
        return operator;
    }

    public Rule setOperator(final Operator operator) {
        this.operator = operator;
        return this;
    }

    public Object getOperand() {
        return operand;
    }

    public Rule setOperand(final Object operand) {
        this.operand = operand;
        return this;
    }

    @Override
    public String toString() {
        return String.format("field=%s, operator=%s, operand=%s", field, operator, operand);
    }
}
