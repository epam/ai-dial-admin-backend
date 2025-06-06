package com.epam.aidial.expressions;

import java.util.List;

public interface GroupFunctionCall extends FunctionCall {
    @Override
    FunctionInfo getFunction();

    List<Constant> getParams();

    @Override
    default boolean isAggregation() {
        return false;
    }
}
