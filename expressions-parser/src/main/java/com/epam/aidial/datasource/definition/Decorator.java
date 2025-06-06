package com.epam.aidial.datasource.definition;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class Decorator {
    private final List<DecoratorPropertyValue> arguments;
    private final String name;

    public Decorator(String name, List<DecoratorPropertyValue> arguments) {
        this.name = name;
        this.arguments = arguments != null ? arguments : new ArrayList<>();
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public List<DecoratorPropertyValue> getArguments() {
        return arguments;
    }
}
