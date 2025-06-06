package com.epam.aidial.datasource.definition;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class ItemDef {
    private final String name;
    private final String fullName;
    private final List<String> comments = new ArrayList<>();
    private final List<Decorator> decorators = new ArrayList<>();

    protected ItemDef(@NotNull String name, @NotNull String fullName, @Nullable List<String> comments) {
        this.name = name;
        this.fullName = fullName;
        if (comments != null)
            this.comments.addAll(comments);
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public String getFullName() {
        return fullName;
    }

    @NotNull
    public List<Decorator> getDecorators() {
        return decorators;
    }

    @NotNull
    public List<String> getComments() {
        return comments;
    }
}
