package com.epam.aidial.datasource.definition;

public class TypeUInt64 extends Type {
    private TypeUInt64() {
        super(TypeKind.UINT64);
    }

    public static final TypeUInt64 INSTANCE = new TypeUInt64();

    @Override
    public String toString() {
        return "UInt64";
    }
}
