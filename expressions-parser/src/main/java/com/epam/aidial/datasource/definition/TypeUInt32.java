package com.epam.aidial.datasource.definition;

public class TypeUInt32 extends Type {
    private TypeUInt32() {
        super(TypeKind.UINT32);
    }

    public static final TypeUInt32 INSTANCE = new TypeUInt32();

    @Override
    public String toString() {
        return "UInt32";
    }
}
