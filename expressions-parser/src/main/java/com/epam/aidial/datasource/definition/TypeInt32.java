package com.epam.aidial.datasource.definition;

public class TypeInt32 extends Type {
    private TypeInt32() {
        super(TypeKind.INT32);
    }

    public static final TypeInt32 INSTANCE = new TypeInt32();

    @Override
    public String toString() {
        return "Int32";
    }
}
