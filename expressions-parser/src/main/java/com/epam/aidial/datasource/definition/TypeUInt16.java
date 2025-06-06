package com.epam.aidial.datasource.definition;

public class TypeUInt16 extends Type {
    private TypeUInt16() {
        super(TypeKind.UINT16);
    }

    public static final TypeUInt16 INSTANCE = new TypeUInt16();

    @Override
    public String toString() {
        return "UInt16";
    }
}
