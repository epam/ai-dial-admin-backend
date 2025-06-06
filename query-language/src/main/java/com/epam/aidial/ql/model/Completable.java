package com.epam.aidial.ql.model;

import com.epam.aidial.expressions.Column;
import com.epam.aidial.expressions.Expression;

import java.util.Collections;
import java.util.Set;

public interface Completable extends From, Expression {

    @Override
    default boolean isAggregation() {
        return false;
    }

    @Override
    default boolean isScale() {
        return false;
    }

    @Override
    default boolean isDeterministic() {
        return getExpressions().stream().allMatch(Expression::isDeterministic);
    }

    @Override
    default Set<Column> getDependentColumns() {
        return Collections.emptySet();
    }
}
