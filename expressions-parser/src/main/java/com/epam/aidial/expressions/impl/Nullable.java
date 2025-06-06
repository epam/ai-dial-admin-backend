package com.epam.aidial.expressions.impl;

import com.epam.aidial.expressions.enums.Type;
import com.epam.aidial.expressions.Column;
import com.epam.aidial.expressions.Expression;
import com.epam.aidial.expressions.stubs.EnumItemDeclaration;

import java.util.List;
import java.util.Set;

public class Nullable implements Expression {
    private final Expression expression;
    private final List<EnumItemDeclaration> enumValues;

    public Nullable(Expression expression) {
        this(expression, null);
    }

    public Nullable(Expression expression, @org.jetbrains.annotations.Nullable List<EnumItemDeclaration> enumValues) {
        this.expression = expression;
        this.enumValues = enumValues;
    }

    @Override
    public Type getType() {
        return expression.getType();
    }

    public Expression getExpression() {
        return expression;
    }

    @Override
    public boolean isAggregation() {
        return getExpression().isAggregation();
    }

    @Override
    public boolean isScale() {
        return getExpression().isScale();
    }

    @Override
    public boolean isDeterministic() {
        return getExpression().isDeterministic();
    }

    @Override
    public Set<Column> getDependentColumns() {
        return getExpression().getDependentColumns();
    }

    @org.jetbrains.annotations.Nullable
    public List<EnumItemDeclaration> getEnumValues() {
        return enumValues;
    }

    @Override
    public int hashCode() {
        return expression.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Nullable)) return expression.equals(o);

        Nullable nullable = (Nullable) o;

        return expression.equals(nullable.expression);
    }

    @Override
    public String toString() {
        return "Nullable(" + expression + ')';
    }
}
