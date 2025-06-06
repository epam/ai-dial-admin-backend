package com.epam.aidial.expressions.impl;

import com.epam.aidial.expressions.Alias;
import com.epam.aidial.expressions.Expression;

public class AliasImpl implements Alias {
    private final String name;
    private final Expression expression;

    public AliasImpl(String name, Expression expression) {
        this.name = name;
        this.expression = expression;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Expression getExpression() {
        return expression;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Alias)) return expression.equals(o);

        Alias alias = (Alias) o;

        if (!getName().equals(alias.getName())) return false;
        return getExpression().equals(alias.getExpression());
    }

    @Override
    public int hashCode() {
        return expression.hashCode();
    }

    @Override
    public String toString() {
        return "AliasImpl{" +
                "name='" + name + '\'' +
                ", expression=" + expression +
                '}';
    }
}
