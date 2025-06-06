package com.epam.aidial.datasource.definition;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class InterfaceMethodDef extends ItemDef {
    private final Type returnType;
    private final List<FormalParameterDef> formalParameters;

    public InterfaceMethodDef(@NotNull String name, @Nullable Type returnType, @Nullable List<String> comments) {
        super(name, name, comments);
        this.returnType = returnType;
        this.formalParameters = new ArrayList<>();
    }

    @Nullable
    public Type getReturnType() {
        return returnType;
    }

    @NotNull
    public List<FormalParameterDef> getFormalParameters() {
        return formalParameters;
    }

}
