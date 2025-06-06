package com.epam.aidial.datasource.definition;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class PropertyDef<T extends ItemDef> extends ItemDef {
    private final Type type;
    private final T owner;

    public PropertyDef(@NotNull T owner, @NotNull String name, @NotNull Type type, @Nullable List<String> comments) {
        super(name, owner.getFullName() + "." + name, comments);
        this.type = type;
        this.owner = owner;
    }

    @NotNull
    public Type getType() {
        return type;
    }

    @NotNull
    public T getOwner() {
        return owner;
    }
}
