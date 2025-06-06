package com.epam.aidial.datasource.definition;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FormalParameterDef extends PropertyDef<InterfaceMethodDef> {
    private final boolean isParameterArray;
    private final String fullName;

    public FormalParameterDef(@NotNull InterfaceMethodDef owner, @NotNull String name, @NotNull Type type, boolean isParameterArray, @Nullable List<String> comments) {
        super(owner, name, type, comments);
        this.fullName = owner.getFullName() + ":" + name;
        this.isParameterArray = isParameterArray;
    }

    @NotNull
    @Override
    public String getFullName() {
        return fullName;
    }

    public boolean isParameterArray() {
        return isParameterArray;
    }
}
