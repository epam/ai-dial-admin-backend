package com.epam.aidial.expressions.impl;

import com.epam.aidial.expressions.AggregationFunctionCall;
import com.epam.aidial.expressions.Constant;
import com.epam.aidial.expressions.Expression;
import com.epam.aidial.expressions.FunctionInfo;

import java.util.List;
import java.util.stream.Collectors;

public class AggregationFunctionCallImpl implements AggregationFunctionCall {
    private final FunctionInfo function;
    private final List<Constant> params;
    private final List<Expression> args;

    public AggregationFunctionCallImpl(FunctionInfo function, List<Constant> params, List<Expression> args) {
        this.function = function;
        this.params = params;
        this.args = args;
    }

    @Override
    public FunctionInfo getFunction() {
        return function;
    }

    @Override
    public List<Constant> getParams() {
        return params;
    }

    @Override
    public List<Expression> getArgs() {
        return args;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AggregationFunctionCallImpl)) return false;

        AggregationFunctionCallImpl that = (AggregationFunctionCallImpl) o;

        if (!getFunction().equals(that.getFunction())) return false;
        if (!getParams().equals(that.getParams())) return false;
        return getArgs().equals(that.getArgs());
    }

    @Override
    public int hashCode() {
        int result = getFunction().hashCode();
        result = 31 * result + getParams().hashCode();
        result = 31 * result + getArgs().hashCode();
        return result;
    }

    @Override
    public String toString() {
        return function.toString() +
                (params.isEmpty() ? "" : "(" + params.stream().map(Object::toString).collect(Collectors.joining(",")) + ")") +
                "(" + args.stream().map(Object::toString).collect(Collectors.joining(",")) + ")";
    }
}
