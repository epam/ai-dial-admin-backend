package com.epam.aidial.datasource.definition;

public class TypeInt8 extends Type {
    private TypeInt8() {
        super(TypeKind.INT8);
    }

    public static final TypeInt8 INSTANCE = new TypeInt8();

    @Override
    public String toString() {
        return "Int8";
    }
}
