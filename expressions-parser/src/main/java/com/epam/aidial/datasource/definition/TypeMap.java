package com.epam.aidial.datasource.definition;

public class TypeMap extends Type {
    private final Type keyType;
    private final Type valueType;
    private final String string;

    public TypeMap(Type keyType, Type valueType) {
        super(TypeKind.MAP);
        this.keyType = keyType;
        this.valueType = valueType;
        this.string = "Map<" + keyType + ", " + valueType + ">";
    }

    public Type getKeyType() {
        return keyType;
    }

    public Type getValueType() {
        return valueType;
    }

    @Override
    public boolean equals(Type other) {
        return other instanceof TypeMap && equals((TypeMap) other);
    }

    public boolean equals(TypeMap other) {
        return other != null && other.keyType.equals(keyType) && other.valueType.equals(valueType);
    }

    @Override
    public String toString() {
        return string;
    }
}
