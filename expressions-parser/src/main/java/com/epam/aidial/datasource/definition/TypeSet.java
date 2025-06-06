package com.epam.aidial.datasource.definition;

public class TypeSet extends Type {
    private final Type underlyingType;
    private final String string;

    public TypeSet(Type underlyingType) {
        super(TypeKind.SET);
        this.underlyingType = underlyingType;
        this.string = "Set<" + underlyingType + ">";
    }

    public Type getUnderlyingType() {
        return underlyingType;
    }

    @Override
    public boolean equals(Type other) {
        return other instanceof TypeSet && equals((TypeSet) other);
    }

    public boolean equals(TypeSet other) {
        return other != null && other.underlyingType.equals(underlyingType);
    }

    @Override
    public String toString() {
        return string;
    }
}
