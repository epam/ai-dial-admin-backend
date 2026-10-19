package com.epam.aidial.ql.common.deserializer;

import com.epam.aidial.ql.common.model.enums.BinaryComparisonOperator;
import com.epam.aidial.ql.common.model.enums.UnaryComparisonOperator;
import com.epam.aidial.ql.common.model.filters.Filter;
import com.fasterxml.jackson.annotation.JsonProperty;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class CommonFilterDeserializer<T, F extends Filter<T>> extends ValueDeserializer<F> {
    private final Map<String, Deserializer<F>> operators;
    private final Class<? extends F> andType;
    private final Class<? extends F> orType;
    private final Class<? extends F> notType;
    private final Class<? extends T> tType;

    public CommonFilterDeserializer(final BiFunction<T, UnaryComparisonOperator, F> unaryFilter,
                                    final TriFunction<T, BinaryComparisonOperator, T, F> binaryFilter,
                                    final Class<? extends F> andType,
                                    final Class<? extends F> orType,
                                    final Class<? extends F> notType,
                                    final Class<? extends T> tType) {
        operators = initOperators(unaryFilter, binaryFilter);
        this.andType = andType;
        this.orType = orType;
        this.notType = notType;
        this.tType = tType;
    }

    private Map<String, Deserializer<F>> initOperators(final BiFunction<T, UnaryComparisonOperator, F> unaryFilter,
                                                       final TriFunction<T, BinaryComparisonOperator, T, F> binaryFilter) {
        final Map<String, Deserializer<F>> operators = new HashMap<>();
        try {
            for (final BinaryComparisonOperator operator : BinaryComparisonOperator.values()) {
                final String jsonValue = operator.getClass().getField(operator.name()).getAnnotation(JsonProperty.class).value();
                operators.put(jsonValue, (node, ctxt) -> {
                    if (node.size() != 2) {
                        return ctxt.reportInputMismatch(this, "BinaryComparisonFilter must contains only two fields");
                    }
                    final JsonNode left = node.get("left");
                    if (left == null) {
                        return ctxt.reportInputMismatch(this, "Left expression can not be null");
                    }
                    final JsonNode right = node.get("right");
                    if (right == null) {
                        return ctxt.reportInputMismatch(this, "Right expression can not be null");
                    }
                    return binaryFilter.apply(ctxt.readTreeAsValue(left, tType), operator, ctxt.readTreeAsValue(right, tType));
                });
            }
            for (final UnaryComparisonOperator operator : UnaryComparisonOperator.values()) {
                final String jsonValue = operator.getClass().getField(operator.name()).getAnnotation(JsonProperty.class).value();
                operators.put(jsonValue, (node, ctxt) -> unaryFilter.apply(ctxt.readTreeAsValue(node, tType), operator));
            }
        } catch (final NoSuchFieldException e) {
            throw new IllegalStateException(e);
        }
        return operators;
    }

    @Override
    public F deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
        final JsonNode tree = ctxt.readTree(p);
        if (tree.isArray()) {
            return ctxt.reportInputMismatch(this, "Filter cannot be array");
        }
        if (tree.size() == 1) {
            final String fieldName = tree.propertyNames().iterator().next();
            final JsonNode node = tree.get(fieldName);
            switch (fieldName) {
                case "$and": return ctxt.readTreeAsValue(node, andType);
                case "$or": return ctxt.readTreeAsValue(node, orType);
                case "$not": return ctxt.readTreeAsValue(node, notType);
            }
            final Deserializer<F> deserializer = operators.get(fieldName);
            if (deserializer != null) {
                return deserializer.deserialize(node, ctxt);
            }
        }
        return ctxt.reportInputMismatch(this, "Filter should contain one property of `$and`, `$or` or `$not`");
    }
}
