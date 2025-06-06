package com.epam.aidial.ql.model;


import com.epam.aidial.expressions.Column;
import com.epam.aidial.expressions.Expression;
import com.epam.aidial.expressions.enums.Type;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public interface Tuple extends Expression {
    List<Expression> getExpressions();

    @Override
    default Type getType() {
        return getExpressions().size() == 1 ? getExpressions().get(0).getType() : Type.TUPLE;
    }

    @Override
    default boolean isAggregation() {
        return getExpressions().stream().anyMatch(Expression::isAggregation);
    }

    @Override
    default boolean isScale() {
        return getExpressions().stream().anyMatch(Expression::isScale);
    }

    @Override
    default boolean isDeterministic() {
        return getExpressions().stream().allMatch(Expression::isDeterministic);
    }

    @Override
    default Set<Column> getDependentColumns() {
        return getExpressions().stream().flatMap(x -> x.getDependentColumns().stream()).collect(Collectors.toSet());
    }
}
