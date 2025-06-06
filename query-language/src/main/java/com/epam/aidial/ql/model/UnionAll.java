package com.epam.aidial.ql.model;


import com.epam.aidial.expressions.Column;
import com.epam.aidial.expressions.Expression;
import com.epam.aidial.expressions.enums.Type;

import java.util.List;
import java.util.Map;

public interface UnionAll extends Completable {
    List<Completable> getQueries();

    @Override
    default Type getType() {
        return getQueries().get(0).getType();
    }

    @Override
    default Map<String, Column> getColumns() {
        return getQueries().get(0).getColumns();
    }

    @Override
    default List<Expression> getExpressions() {
        return getQueries().get(0).getExpressions();
    }
}
