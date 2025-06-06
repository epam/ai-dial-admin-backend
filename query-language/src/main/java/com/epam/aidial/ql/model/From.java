package com.epam.aidial.ql.model;

import com.epam.aidial.expressions.Column;
import com.epam.aidial.expressions.Expression;

import java.util.List;
import java.util.Map;

public interface From {
    List<Expression> getExpressions();
    Map<String, Column> getColumns();
}
