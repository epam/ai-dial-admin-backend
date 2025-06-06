package com.epam.aidial.ql.common.model.filters;

import com.epam.aidial.ql.common.model.enums.BinaryComparisonOperator;


public interface BinaryComparisonFilter<T> extends Filter<T> {
    T getLeft();
    BinaryComparisonOperator getOperator();
    T getRight();
}
