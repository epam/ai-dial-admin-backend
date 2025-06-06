package com.epam.aidial.datasource.definition;

public abstract class Type {
    private final TypeKind kind;

    protected Type(TypeKind kind) {
        this.kind = kind;
    }

    public TypeKind getKind() {
        return kind;
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof Type && equals((Type) object);
    }

    public boolean equals(Type other) {
        return other != null && other.kind == kind;
    }
}
