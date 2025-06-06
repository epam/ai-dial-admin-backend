package com.epam.aidial.datasource.definition;

public abstract class LiteralInteger extends Literal {
    protected LiteralInteger(LiteralKind kind) {
        super(kind);
    }

    public abstract LiteralInteger castTo(IntegralType type);
}
