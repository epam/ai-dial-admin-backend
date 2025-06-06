package com.epam.aidial.expressions.impl;

import com.epam.aidial.expressions.Expression;
import com.epam.aidial.expressions.FunctionInfo;
import com.epam.aidial.expressions.enums.Type;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;

@Getter
@EqualsAndHashCode
@AllArgsConstructor
public class FunctionImpl implements FunctionInfo {
    private final String name;
    private final Type type;
    private final boolean deterministic;

    public FunctionImpl(String name, Type type) {
        this(name, type, true);
    }

    @Override
    public Type getType(List<Expression> fields) {
        return type;
    }

    @Override
    public String toString() {
        return "[" + type + "]" + name;
    }
}
