package com.epam.aidial.expressions.impl;

import com.epam.aidial.expressions.Expression;
import com.epam.aidial.expressions.FunctionCall;
import com.epam.aidial.expressions.FunctionInfo;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FunctionCallImpl implements FunctionCall {
    private final FunctionInfo function;
    private final List<Expression> args;

    public FunctionCallImpl(FunctionInfo function, List<Expression> args) {
        this.function = function;
        this.args = args;
    }

    public FunctionCallImpl(FunctionInfo function, Expression... args) {
        this.function = function;
        this.args = Arrays.asList(args);
    }

    @Override
    public FunctionInfo getFunction() {
        return function;
    }

    @Override
    public List<Expression> getArgs() {
        return args;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FunctionCallImpl)) return false;

        FunctionCallImpl that = (FunctionCallImpl) o;

        if (!getFunction().equals(that.getFunction())) return false;
        return getArgs().equals(that.getArgs());
    }

    @Override
    public int hashCode() {
        int result = getFunction().hashCode();
        result = 31 * result + getArgs().hashCode();
        return result;
    }

    @Override
    public String toString() {
        return function.toString() + "(" + args.stream().map(Object::toString).collect(Collectors.joining(",")) + ")";
    }
}
