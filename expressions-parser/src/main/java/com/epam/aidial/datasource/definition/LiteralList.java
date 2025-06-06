package com.epam.aidial.datasource.definition;

import java.util.ArrayList;
import java.util.List;

public final class LiteralList extends Literal {
    private final List<Literal> value;

    public LiteralList() {
        super(LiteralKind.LIST);
        value = new ArrayList<>();
    }

    public List<Literal> getValue() {
        return value;
    }

    @Override
    public String toString() {
        if (getValue().size() == 0)
            return "{}";

        final StringBuilder builder = new StringBuilder("{ ").append(value.get(0));
        for (int i = 1; i < value.size(); i += 1)
            builder.append(", ").append(value.get(i));
        return builder.append(" }").toString();
    }
}
