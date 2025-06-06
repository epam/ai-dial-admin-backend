package com.epam.aidial.ql.model.impl;

import com.epam.aidial.expressions.Column;
import com.epam.aidial.expressions.Expression;
import com.epam.aidial.ql.model.Sort;
import com.epam.aidial.ql.model.Table;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class TableImpl implements Table {
    private String name;
    @Singular
    private List<Sort> additionalSorts;
    @Singular
    private List<Expression> expressions;
    @Singular
    private Map<String, Column> columns;
}
