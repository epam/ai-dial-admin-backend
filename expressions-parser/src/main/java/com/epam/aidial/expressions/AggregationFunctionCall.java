package com.epam.aidial.expressions;

import java.util.List;

public interface AggregationFunctionCall extends FunctionCall {
    @Override
    FunctionInfo getFunction();
    List<Constant> getParams();

    @Override
    default boolean isAggregation() {
        return true;
    }
}
