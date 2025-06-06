package com.epam.aidial.expressions.impl;

import com.epam.aidial.expressions.Constant;
import com.epam.aidial.expressions.Expression;
import com.epam.aidial.expressions.FunctionInfo;
import com.epam.aidial.expressions.GroupFunctionCall;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@EqualsAndHashCode
@AllArgsConstructor
public class GroupFunctionCallImpl implements GroupFunctionCall {
    private final FunctionInfo function;
    private final List<Constant> params;
    private final List<Expression> args;

    @Override
    public String toString() {
        return function.toString() +
               (params.isEmpty() ? "" : "(" + params.stream().map(Object::toString).collect(Collectors.joining(",")) + ")") +
               "(" + args.stream().map(Object::toString).collect(Collectors.joining(",")) + ")";
    }
}
