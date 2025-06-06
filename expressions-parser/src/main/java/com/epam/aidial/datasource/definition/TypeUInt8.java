package com.epam.aidial.datasource.definition;

public class TypeUInt8 extends Type {
    private TypeUInt8() {
        super(TypeKind.UINT8);
    }

    public static final TypeUInt8 INSTANCE = new TypeUInt8();

    @Override
    public String toString() {
        return "UInt8";
    }
}
