package com.epam.aidial.expressions;

import com.epam.aidial.expressions.enums.Type;

import java.util.Set;

public interface Alias extends Column {
    Expression getExpression();

    @Override
    default boolean isAggregation() {
        return getExpression().isAggregation();
    }

    @Override
    default Set<Column> getDependentColumns() {
        return getExpression().getDependentColumns();
    }

    @Override
    default boolean isScale() {
        return getExpression().isScale();
    }

    @Override
    default boolean isDeterministic() {
        return getExpression().isDeterministic();
    }

    @Override
    default Type getType() {
        return getExpression().getType();
    }
}
