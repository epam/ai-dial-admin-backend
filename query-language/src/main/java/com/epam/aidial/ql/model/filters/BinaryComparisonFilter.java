package com.epam.aidial.ql.model.filters;

import com.epam.aidial.expressions.Expression;
import com.epam.aidial.ql.common.model.enums.BinaryComparisonOperator;
import com.epam.aidial.ql.model.Filter;

public interface BinaryComparisonFilter extends Filter {
    Expression getLeftExpression();

    BinaryComparisonOperator getOperator();

    Expression getRightExpression();
}
