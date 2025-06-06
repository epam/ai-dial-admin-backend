package com.epam.aidial.expressions;

import com.epam.aidial.expressions.enums.Type;

import java.util.List;

public interface FunctionInfo {
    String getName();
    Type getType(List<Expression> fields);
    boolean isDeterministic();
}
