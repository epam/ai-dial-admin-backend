package com.epam.aidial.ql.common.model.filters;

import com.epam.aidial.ql.common.model.enums.UnaryComparisonOperator;

public interface UnaryComparisonFilter<T> extends Filter<T> {
    T getExpression();
    UnaryComparisonOperator getOperator();
}
