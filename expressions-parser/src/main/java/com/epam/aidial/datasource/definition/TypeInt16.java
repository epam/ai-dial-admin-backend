package com.epam.aidial.datasource.definition;

public final class TypeInt16 extends Type {
    private TypeInt16() {
        super(TypeKind.INT16);
    }

    public static final TypeInt16 INSTANCE = new TypeInt16();

    @Override
    public String toString() {
        return "Int16";
    }
}
