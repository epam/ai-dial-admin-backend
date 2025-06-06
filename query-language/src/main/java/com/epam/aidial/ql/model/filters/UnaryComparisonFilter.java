package com.epam.aidial.ql.model.filters;

import com.epam.aidial.expressions.Expression;
import com.epam.aidial.ql.common.model.enums.UnaryComparisonOperator;
import com.epam.aidial.ql.model.Filter;

public interface UnaryComparisonFilter extends Filter {
    Expression getExpression();

    UnaryComparisonOperator getOperator();
}
