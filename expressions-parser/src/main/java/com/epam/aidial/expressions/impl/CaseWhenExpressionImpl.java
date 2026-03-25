package com.epam.aidial.expressions.impl;

import com.epam.aidial.expressions.CaseWhenExpression;
import com.epam.aidial.expressions.Column;
import com.epam.aidial.expressions.Expression;
import com.epam.aidial.expressions.enums.Type;

import java.util.HashSet;
import java.util.Set;

public class CaseWhenExpressionImpl implements CaseWhenExpression {
    private final Expression condition;
    private final Expression thenExpression;
    private final Expression elseExpression;

    public CaseWhenExpressionImpl(Expression condition, Expression thenExpression, Expression elseExpression) {
        this.condition = condition;
        this.thenExpression = thenExpression;
        this.elseExpression = elseExpression;
    }

    @Override
    public Expression getCondition() {
        return condition;
    }

    @Override
    public Expression getThenExpression() {
        return thenExpression;
    }

    @Override
    public Expression getElseExpression() {
        return elseExpression;
    }

    @Override
    public Type getType() {
        return thenExpression.getType();
    }

    @Override
    public boolean isAggregation() {
        return false;
    }

    @Override
    public boolean isScale() {
        return false;
    }

    @Override
    public boolean isDeterministic() {
        return condition.isDeterministic()
                && thenExpression.isDeterministic()
                && elseExpression.isDeterministic();
    }

    @Override
    public Set<Column> getDependentColumns() {
        var columns = new HashSet<Column>();
        columns.addAll(condition.getDependentColumns());
        columns.addAll(thenExpression.getDependentColumns());
        columns.addAll(elseExpression.getDependentColumns());
        return columns;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CaseWhenExpressionImpl that)) {
            return false;
        }
        return condition.equals(that.condition)
                && thenExpression.equals(that.thenExpression)
                && elseExpression.equals(that.elseExpression);
    }

    @Override
    public int hashCode() {
        int result = condition.hashCode();
        result = 31 * result + thenExpression.hashCode();
        result = 31 * result + elseExpression.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "CASE WHEN " + condition + " THEN " + thenExpression + " ELSE " + elseExpression + " END";
    }
}
