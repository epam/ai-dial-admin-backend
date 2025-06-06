package com.epam.aidial.datasource.definition;

public class TypeNullable extends Type {
    private final Type underlyingType;
    private final String string;

    public TypeNullable(Type underlyingType) {
        super(TypeKind.NULLABLE);
        this.underlyingType = underlyingType;
        this.string = underlyingType + "?";
    }

    public Type getUnderlyingType() {
        return underlyingType;
    }

    @Override
    public boolean equals(Type other) {
        return other instanceof TypeNullable && equals((TypeNullable) other);
    }

    public boolean equals(TypeNullable other) {
        return other != null && other.underlyingType.equals(underlyingType);
    }

    @Override
    public String toString() {
        return string;
    }
}
