package com.epam.aidial.datasource.definition;

public class TypeDate extends Type {
    private TypeDate() {
        super(TypeKind.DATE);
    }

    public static final TypeDate INSTANCE = new TypeDate();

    @Override
    public String toString() {
        return "Date";
    }
}
