package com.epam.aidial.datasource.definition;

public class LiteralTimestamp extends Literal {
    private String value;

    public LiteralTimestamp(String value) {
        super(LiteralKind.TIMESTAMP);
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "\"" + value + "\"";
    }

    public static final String MIN_VALUE_AS_STRING = "#MIN";
    public static final String MAX_VALUE_AS_STRING = "#MAX";
}
