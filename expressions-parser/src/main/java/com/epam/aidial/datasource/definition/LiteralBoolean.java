package com.epam.aidial.datasource.definition;

public class LiteralBoolean extends Literal {
    private boolean value;

    private LiteralBoolean(boolean value) {
        super(LiteralKind.BOOLEAN);
        this.value = value;
    }

    public boolean getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value ? "true" : "false";
    }

    public static final LiteralBoolean TRUE = new LiteralBoolean(true);
    public static final LiteralBoolean FALSE = new LiteralBoolean(false);
}
