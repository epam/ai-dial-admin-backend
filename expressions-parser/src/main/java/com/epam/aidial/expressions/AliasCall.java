package com.epam.aidial.expressions;

import com.epam.aidial.expressions.enums.Type;

import java.util.Collections;
import java.util.Set;

public interface AliasCall extends Column {
    Alias getAlias();

    @Override
    default boolean isAggregation() {
        return getAlias().isAggregation();
    }

    @Override
    default Set<Column> getDependentColumns() {
        return Collections.singleton(getAlias());
    }

    @Override
    default String getName() {
        return getAlias().getName();
    }

    @Override
    default Type getType() {
        return getAlias().getType();
    }
}
