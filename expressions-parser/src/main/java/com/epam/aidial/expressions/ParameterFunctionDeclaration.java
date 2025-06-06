package com.epam.aidial.expressions;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.epam.aidial.expressions.enums.Type;
import org.jetbrains.annotations.Nullable;

public class ParameterFunctionDeclaration {
    private final String name;
    private final Type type;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Nullable
    private final Object defaultValue;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Nullable
    private final Object lowerBound;
    @Nullable
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final Object upperBound;

    public ParameterFunctionDeclaration(String name,
                                        Type type,
                                        @Nullable Object defaultValue,
                                        @Nullable Object lowerBound,
                                        @Nullable Object upperBound) {
        this.name = name;
        this.type = type;
        this.defaultValue = defaultValue;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    @Nullable
    public Object getDefaultValue() {
        return defaultValue;
    }

    @Nullable
    public Object getLowerBound() {
        return lowerBound;
    }

    @Nullable
    public Object getUpperBound() {
        return upperBound;
    }
}
