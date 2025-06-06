package com.epam.aidial.datasource.definition;

public class TypeList extends Type {
    private final Type underlyingType;
    private final String string;

    public TypeList(Type underlyingType) {
        super(TypeKind.LIST);
        this.underlyingType = underlyingType;
        this.string = "List<" + underlyingType + ">";
    }

    public Type getUnderlyingType() {
        return underlyingType;
    }

    @Override
    public boolean equals(Type other) {
        return other instanceof TypeList && equals((TypeList) other);
    }

    public boolean equals(TypeList other) {
        return other != null && other.underlyingType.equals(underlyingType);
    }

    @Override
    public String toString() {
        return string;
    }
}
