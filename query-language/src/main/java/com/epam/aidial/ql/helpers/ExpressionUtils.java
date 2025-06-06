package com.epam.aidial.ql.helpers;

import com.epam.aidial.expressions.Column;
import com.epam.aidial.expressions.Expression;
import com.epam.aidial.expressions.Alias;
import com.epam.aidial.expressions.impl.ColumnImpl;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ExpressionUtils {
    public static Map<String, Column> getColumns(final List<? extends Expression> expressions) {
        return expressions.stream()
            .filter(x -> x instanceof Column)
            .map(x -> x instanceof Alias ? new ColumnImpl(x.getType(), ((Alias) x).getName()) : (Column) x)
            .collect(Collectors.toMap(Column::getName, Function.identity(), (o, o2) -> o));
    }
}
