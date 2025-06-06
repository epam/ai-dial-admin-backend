package com.epam.aidial.datasource.definition;

public class LiteralNull extends Literal {
    private LiteralNull() {
        super(LiteralKind.NULL);
    }

    public Object getValue() {
        return null;
    }

    @Override
    public String toString() {
        return "null";
    }

    public final static LiteralNull INSTANCE = new LiteralNull();
}
