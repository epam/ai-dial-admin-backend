package com.epam.aidial.expressions.impl;

import com.epam.aidial.expressions.Alias;
import com.epam.aidial.expressions.AliasCall;

public class AliasCallImpl implements AliasCall {
    private final Alias alias;

    public AliasCallImpl(Alias alias) {
        this.alias = alias;
    }

    @Override
    public Alias getAlias() {
        return alias;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AliasCallImpl)) return false;

        AliasCallImpl aliasCall = (AliasCallImpl) o;

        return getAlias().equals(aliasCall.getAlias());
    }

    @Override
    public int hashCode() {
        return getAlias().hashCode();
    }
}
