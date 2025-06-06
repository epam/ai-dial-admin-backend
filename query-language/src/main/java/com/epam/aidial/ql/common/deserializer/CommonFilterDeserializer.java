package com.epam.aidial.ql.common.deserializer;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.exc.PropertyBindingException;
import com.epam.aidial.ql.common.model.enums.BinaryComparisonOperator;
import com.epam.aidial.ql.common.model.enums.UnaryComparisonOperator;
import com.epam.aidial.ql.common.model.filters.Filter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class CommonFilterDeserializer<T, F extends Filter<T>> extends JsonDeserializer<F> {
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
                operators.put(jsonValue, (node, codec) -> {
                    if (node.size() != 2) {
                        throw PropertyBindingException.from(node.traverse(codec), "BinaryComparisonFilter must contains only two fields");
                    }
                    final TreeNode left = node.get("left");
                    if (left == null) {
                        throw PropertyBindingException.from(node.traverse(codec), "Left expression can not be null");
                    }
                    final TreeNode right = node.get("right");
                    if (right == null) {
                        throw PropertyBindingException.from(node.traverse(codec), "Right expression can not be null");
                    }
                    return binaryFilter.apply(left.traverse(codec).readValueAs(tType), operator, right.traverse(codec).readValueAs(tType));
                });
            }
            for (final UnaryComparisonOperator operator : UnaryComparisonOperator.values()) {
                final String jsonValue = operator.getClass().getField(operator.name()).getAnnotation(JsonProperty.class).value();
                operators.put(jsonValue, (node, codec) -> unaryFilter.apply(node.traverse(codec).readValueAs(tType), operator));
            }
        } catch (final NoSuchFieldException e) {
            throw new IllegalStateException(e);
        }
        return operators;
    }

    @Override
    public F deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        final TreeNode tree = p.getCodec().readTree(p);
        if (tree.isArray()) {
            throw PropertyBindingException.from(p, "Filter cannot be array");
        }
        if (tree.size() == 1) {
            final String fieldName = tree.fieldNames().next();
            final TreeNode node = tree.get(fieldName);
            switch (fieldName) {
                case "$and": return node.traverse(p.getCodec()).readValueAs(andType);
                case "$or": return node.traverse(p.getCodec()).readValueAs(orType);
                case "$not": return node.traverse(p.getCodec()).readValueAs(notType);
            }
            final Deserializer<F> deserializer = operators.get(fieldName);
            if (deserializer != null) {
                return deserializer.deserialize(node, p.getCodec());
            }
        }
        throw PropertyBindingException.from(p, "Filter should contain one property of `$and`, `$or` or `$not`");
    }
}
