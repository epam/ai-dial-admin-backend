package com.epam.aidial.expressions;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Set;

public interface Constant extends Expression, Comparable<Constant> {
    Comparable getValue();

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
        return Collections.emptySet();
    }

    default int compareTo(@NotNull Constant o) {
        final int x = TypeHelper.compare(getType(), o.getType());
        if (x != 0) {
            return x;
        }
        if (TypeHelper.isNumber(getType())) {
            if (TypeHelper.isReal(getType()) || TypeHelper.isReal(o.getType())) {
                return Double.compare(((Number) getValue()).doubleValue(), ((Number) o.getValue()).doubleValue());
            } else {
                return Double.compare(((Number) getValue()).longValue(), ((Number) o.getValue()).longValue());
            }
        }
        return getValue().compareTo(o.getValue());
    }
}
