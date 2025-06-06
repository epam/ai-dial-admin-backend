package com.epam.aidial.datasource.definition;

public class TypeText extends Type {
    private TypeText() {
        super(TypeKind.TEXT);
    }

    public static final TypeText INSTANCE = new TypeText();

    @Override
    public String toString() {
        return "Text";
    }
}
