package com.epam.aidial.datasource.definition;

public class LiteralType extends Literal {
    private final Type value;

    public LiteralType(Type value) {
        super(LiteralKind.TYPE);
        this.value = value;
    }

    public Type getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "typeof(" + value + ")";
    }
}
