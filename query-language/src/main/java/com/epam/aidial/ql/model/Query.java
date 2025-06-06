package com.epam.aidial.ql.model;


import com.epam.aidial.expressions.Column;
import com.epam.aidial.expressions.Expression;
import com.epam.aidial.expressions.enums.Type;
import com.epam.aidial.ql.helpers.ExpressionUtils;

import java.util.List;
import java.util.Map;

public interface Query extends Completable {
    boolean isDistinct();

    List<Expression> getExpressions();

    From getFrom();

    Join getJoin();

    Filter getPreScale();

    Filter getWhere();

    List<Expression> getGroupBy();

    boolean withTotals();

    Filter getHaving();

    List<Sort> getOrderBy();

    LimitBy getLimitBy();

    Long getOffset();

    Long getLimit();

    @Override
    default Type getType() {
        return getExpressions().size() == 1 ? getExpressions().get(0).getType() : Type.TUPLE;
    }

    @Override
    default Map<String, Column> getColumns() {
        return ExpressionUtils.getColumns(getExpressions());
    }
}
