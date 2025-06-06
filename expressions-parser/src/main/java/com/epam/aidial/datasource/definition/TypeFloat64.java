package com.epam.aidial.datasource.definition;

public class TypeFloat64 extends Type {
    private TypeFloat64() {
        super(TypeKind.FLOAT64);
    }

    public static final TypeFloat64 INSTANCE = new TypeFloat64();

    @Override
    public String toString() {
        return "Float64";
    }
}
