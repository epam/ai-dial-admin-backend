package com.epam.aidial.ql.model;

import com.epam.aidial.expressions.Expression;

import java.util.List;

public interface Data {
    List<Expression> getExpressions();

    List<List<Object>> getData();
}
