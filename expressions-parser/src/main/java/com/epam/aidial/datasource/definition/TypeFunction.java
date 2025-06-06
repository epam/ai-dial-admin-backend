package com.epam.aidial.datasource.definition;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class TypeFunction extends Type {
    private final List<Type> argumentTypes = new ArrayList<>();
    private final Type returnType;

    protected TypeFunction(@NotNull List<Type> argumentTypes, @NotNull Type returnType) {
        super(TypeKind.FUNCTION);
        this.returnType = returnType;
        this.argumentTypes.addAll(argumentTypes);
    }

    @NotNull
    public List<Type> getArgumentTypes() {
        return argumentTypes;
    }

    @NotNull
    public Type getReturnType() {
        return returnType;
    }
}
