package com.epam.aidial.datasource.definition;

public class LiteralDecimal extends Literal {
    private final long value;

    public LiteralDecimal(long value) {
        super(LiteralKind.DECIMAL);
        this.value = value;
    }

    public long getValue() {
        return value;
    }

    @Override
    public String toString() {
        return Long.toString(value);
    }
}
