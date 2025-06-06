package com.epam.aidial.ql.model.impl;


import com.epam.aidial.expressions.Column;
import com.epam.aidial.expressions.Expression;
import com.epam.aidial.expressions.enums.Type;
import com.epam.aidial.ql.helpers.ExpressionUtils;
import com.epam.aidial.ql.model.Filter;
import com.epam.aidial.ql.model.From;
import com.epam.aidial.ql.model.Join;
import com.epam.aidial.ql.model.LimitBy;
import com.epam.aidial.ql.model.Query;
import com.epam.aidial.ql.model.Sort;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class QueryImpl implements Query {
    private boolean distinct;
    @Singular
    private List<Expression> expressions;
    private From from;
    @Nullable
    private Join join;
    @Nullable
    private Filter preScale;
    @Nullable
    private Filter where;
    @Builder.Default
    private List<Expression> groupBy = Collections.emptyList();
    boolean withTotals;
    @Nullable
    private Filter having;
    @Builder.Default
    private List<Sort> orderBy = Collections.emptyList();
    @Nullable
    private LimitBy limitBy;
    @Nullable
    private Long offset;
    @Nullable
    private Long limit;

    @Override
    public boolean withTotals() {
        return withTotals;
    }

    @Override
    public Type getType() {
        return getExpressions().size() == 1 ? getExpressions().get(0).getType() : Type.TUPLE;
    }

    @Override
    public Map<String, Column> getColumns() {
        return ExpressionUtils.getColumns(getExpressions());
    }

}
