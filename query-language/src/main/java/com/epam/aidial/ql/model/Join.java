package com.epam.aidial.ql.model;

import com.epam.aidial.expressions.Expression;
import com.epam.aidial.ql.common.model.enums.JoinStrictness;
import com.epam.aidial.ql.common.model.enums.JoinType;

import java.util.List;

public interface Join {
    JoinStrictness getStrictness();

    JoinType getType();

    From getFrom();

    List<Expression> getLeftExpressions();

    List<Expression> getRightExpressions();
}
