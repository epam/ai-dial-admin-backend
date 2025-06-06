package com.epam.aidial.expressions.impl;

import com.epam.aidial.expressions.Expression;
import com.epam.aidial.expressions.ValidRequestedExpression;

public class ValidRequestedExpressionImpl implements ValidRequestedExpression {
    private final String requestString;
    private final Expression expression;

    public ValidRequestedExpressionImpl(String requestString, Expression expression) {
        this.requestString = requestString;
        this.expression = expression;
    }

    @Override
    public String getRequestString() {
        return requestString;
    }

    @Override
    public Expression getExpression() {
        return expression;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ValidRequestedExpressionImpl)) return false;

        final ValidRequestedExpressionImpl that = (ValidRequestedExpressionImpl) o;

        if (!getRequestString().equals(that.getRequestString())) return false;
        return getExpression().equals(that.getExpression());
    }

    @Override
    public int hashCode() {
        int result = getRequestString().hashCode();
        result = 31 * result + getExpression().hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "ValidRequestedExpressionImpl{" +
            "requestString='" + requestString + '\'' +
            ", expression=" + expression +
            '}';
    }
}
