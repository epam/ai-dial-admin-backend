package com.epam.aidial.ql.model;

import com.epam.aidial.expressions.Expression;

import java.util.List;

public interface LimitBy {
    List<Expression> getExpressions();

    Long getCount();
}
