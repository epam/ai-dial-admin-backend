package com.epam.aidial.expressions;

import com.epam.aidial.expressions.enums.Type;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public interface FunctionCall extends Expression {
    FunctionInfo getFunction();
    List<Expression> getArgs();

    @Override
    default boolean isAggregation() {
        return getArgs().stream().anyMatch(Expression::isAggregation);
    }

    @Override
    default boolean isScale() {
        return getArgs().stream().anyMatch(Expression::isScale);
    }

    @Override
    default Set<Column> getDependentColumns() {
        return getArgs().stream().flatMap(x -> x.getDependentColumns().stream()).collect(Collectors.toSet());
    }

    @Override
    default boolean isDeterministic() {
        return getFunction().isDeterministic() && getArgs().stream().allMatch(Expression::isDeterministic);
    }

    @Override
    default Type getType() {
        return getFunction().getType(getArgs());
    }
}
