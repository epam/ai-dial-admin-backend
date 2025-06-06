package com.epam.aidial.ql.model.filters.impl;

import com.epam.aidial.expressions.Expression;
import com.epam.aidial.ql.common.model.enums.UnaryComparisonOperator;
import com.epam.aidial.ql.model.filters.UnaryComparisonFilter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor(staticName = "of")
public class UnaryComparisonFilterImpl implements UnaryComparisonFilter {
    private Expression expression;
    private UnaryComparisonOperator operator;
}
