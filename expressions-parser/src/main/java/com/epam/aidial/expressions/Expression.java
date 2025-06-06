package com.epam.aidial.expressions;

import com.epam.aidial.expressions.enums.Type;

import java.util.Set;

public interface Expression {
    Type getType();
    boolean isAggregation();
    boolean isScale();
    boolean isDeterministic();
    Set<Column> getDependentColumns();
}
