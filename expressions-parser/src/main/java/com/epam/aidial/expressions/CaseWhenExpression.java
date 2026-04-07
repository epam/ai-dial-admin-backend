package com.epam.aidial.expressions;

public interface CaseWhenExpression extends Expression {
    Expression getCondition();
    Expression getThenExpression();
    Expression getElseExpression();
}
