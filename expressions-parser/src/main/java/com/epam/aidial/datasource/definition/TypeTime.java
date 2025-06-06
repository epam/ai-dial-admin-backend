package com.epam.aidial.datasource.definition;

public class TypeTime extends Type {
    private TypeTime() {
        super(TypeKind.TIME);
    }

    public static final TypeTime INSTANCE = new TypeTime();

    @Override
    public String toString() {
        return "Time";
    }
}
