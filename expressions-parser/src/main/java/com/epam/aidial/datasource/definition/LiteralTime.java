package com.epam.aidial.datasource.definition;

public class LiteralTime extends Literal {
    private String value;

    public LiteralTime(String value) {
        super(LiteralKind.TIME);
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
