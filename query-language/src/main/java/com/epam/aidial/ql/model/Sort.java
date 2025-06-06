package com.epam.aidial.ql.model;

import com.epam.aidial.expressions.Expression;
import com.epam.aidial.ql.common.model.enums.SortDirection;

public interface Sort {
    Expression getExpression();

    SortDirection getDirection();
}
