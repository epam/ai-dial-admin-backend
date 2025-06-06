package com.epam.aidial.expressions;

import java.util.Collections;
import java.util.Set;

public interface Column extends Expression {
    String getName();

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
        return true;
    }

    @Override
    default Set<Column> getDependentColumns() {
        return Collections.singleton(this);
    }
}
