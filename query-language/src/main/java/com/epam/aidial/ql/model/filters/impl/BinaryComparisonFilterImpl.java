package com.epam.aidial.ql.model.filters.impl;

import com.epam.aidial.expressions.Expression;
import com.epam.aidial.ql.common.model.enums.BinaryComparisonOperator;
import com.epam.aidial.ql.model.filters.BinaryComparisonFilter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor(staticName = "of")
public class BinaryComparisonFilterImpl implements BinaryComparisonFilter {
    private Expression leftExpression;
    private BinaryComparisonOperator operator;
    private Expression rightExpression;
}
