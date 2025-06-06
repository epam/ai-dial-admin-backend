package com.epam.aidial.datasource.definition;

import org.jetbrains.annotations.NotNull;

public class DecoratorPropertyValue {
    private final String name;
    private final boolean isDefault;
    private Literal value;

    public DecoratorPropertyValue(@NotNull String name, @NotNull Literal value, boolean isDefault) {
        this.name = name;
        this.value = value;
        this.isDefault = isDefault;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public Literal getValue() {
        return value;
    }

    public boolean isDefault() {
        return isDefault;
    }

    void setValue(Literal value) {
        this.value = value;
    }
}
