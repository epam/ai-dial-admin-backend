package com.epam.aidial.datasource.definition;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TypeAction extends Type {
    private final List<Type> argumentTypes = new ArrayList<>();

    public TypeAction(@Nullable List<Type> argumentTypes) {
        super(TypeKind.ACTION);
        if (argumentTypes != null)
            this.argumentTypes.addAll(argumentTypes);
    }

    public TypeAction() {
        this(null);
    }

    @NotNull
    public List<Type> getArgumentTypes() {
        return argumentTypes;
    }
}
