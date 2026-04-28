package com.epam.aidial.metric.service.influx3;

import com.epam.aidial.expressions.Column;
import com.epam.aidial.expressions.Constant;
import com.epam.aidial.metric.service.RangeFilterUtils;
import com.epam.aidial.metric.util.CollectorsUtils;
import com.epam.aidial.ql.common.model.enums.BinaryComparisonOperator;
import com.epam.aidial.ql.model.Filter;
import com.epam.aidial.ql.model.Tuple;
import com.epam.aidial.ql.model.filters.And;
import com.epam.aidial.ql.model.filters.BinaryComparisonFilter;
import com.epam.aidial.ql.model.filters.Not;
import com.epam.aidial.ql.model.filters.Or;
import com.epam.aidial.ql.model.filters.UnaryComparisonFilter;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.NotImplementedException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@UtilityClass
public class SqlConditionBuilder {

    public static SqlConditionResult createWherePart(Filter filter, AtomicInteger paramCounter) {
        var nonRangeFilter = RangeFilterUtils.extractNonRangeFilter(filter);
        if (nonRangeFilter.isEmpty()) {
            return SqlConditionResult.empty();
        }
        return createFilterExpression(nonRangeFilter.get(), paramCounter);
    }

    public static SqlConditionResult createRangePart(Filter filter, boolean isRequired, AtomicInteger paramCounter) {
        var rangeFilters = RangeFilterUtils.extractRangeFilter(filter);

        var startFilterOptional = rangeFilters.stream()
                .filter(f -> f.getOperator() == BinaryComparisonOperator.GREATER_OR_EQUALS)
                .collect(CollectorsUtils.toSingleton(() -> new IllegalArgumentException("Only one start time filter must be provided")))
                .map(f -> RangeFilterUtils.getInstant(f.getRightExpression()));
        if (startFilterOptional.isEmpty()) {
            if (isRequired) {
                throw new IllegalArgumentException("No start time filter provided");
            } else {
                return SqlConditionResult.empty();
            }
        }
        var start = startFilterOptional.get();

        var endOptional = rangeFilters.stream()
                .filter(f -> f.getOperator() == BinaryComparisonOperator.LESS)
                .map(f -> RangeFilterUtils.getInstant(f.getRightExpression()))
                .collect(CollectorsUtils.toSingleton(() -> new IllegalArgumentException("Only one end time filter must be provided")));

        var params = new HashMap<String, Object>();
        var startParamName = "p" + paramCounter.getAndIncrement();
        params.put(startParamName, RangeFilterUtils.convertInstantToString(start));

        if (endOptional.isPresent()) {
            var endParamName = "p" + paramCounter.getAndIncrement();
            params.put(endParamName, RangeFilterUtils.convertInstantToString(endOptional.get()));
            var query = "\"time\" >= $%s AND \"time\" < $%s".formatted(startParamName, endParamName);
            return new SqlConditionResult(query, params);
        } else {
            var query = "\"time\" >= $%s".formatted(startParamName);
            return new SqlConditionResult(query, params);
        }
    }

    private static SqlConditionResult createFilterExpression(Filter filter, AtomicInteger paramCounter) {
        if (filter instanceof And and) {
            var results = and.getFilters().stream()
                    .map(f -> createFilterExpression(f, paramCounter))
                    .toList();
            return combineResults(results, "AND");
        } else if (filter instanceof Or or) {
            var results = or.getFilters().stream()
                    .map(f -> createFilterExpression(f, paramCounter))
                    .toList();
            return combineResults(results, "OR");
        } else if (filter instanceof Not) {
            throw new NotImplementedException("NOT keyword is not supported yet");
        } else if (filter instanceof BinaryComparisonFilter binaryComparisonFilter) {
            return createBinaryComparisonFilter(binaryComparisonFilter, paramCounter);
        } else if (filter instanceof UnaryComparisonFilter unaryComparisonFilter) {
            return createUnaryComparisonFilter(unaryComparisonFilter);
        } else {
            throw new IllegalArgumentException("Unsupported filter type: %s".formatted(filter.getClass()));
        }
    }

    private static SqlConditionResult combineResults(List<SqlConditionResult> results, String operator) {
        var query = results.stream()
                .map(SqlConditionResult::query)
                .collect(Collectors.joining(" " + operator + " ", "(", ")"));
        var params = new HashMap<String, Object>();
        results.forEach(r -> params.putAll(r.parameters()));
        return new SqlConditionResult(query, params);
    }

    private static SqlConditionResult createBinaryComparisonFilter(BinaryComparisonFilter filter, AtomicInteger paramCounter) {
        if (!(filter.getLeftExpression() instanceof Column column)) {
            throw new NotImplementedException("Left part of expression must be a column");
        }

        var operator = filter.getOperator();
        var right = filter.getRightExpression();
        if (operator == BinaryComparisonOperator.IN || operator == BinaryComparisonOperator.NOT_IN) {
            if (!(right instanceof Tuple tuple)) {
                throw new NotImplementedException("Right part of IN/NOT IN expression must be a tuple");
            }
            return createInFilter(column.getName(), tuple, operator, paramCounter);
        }

        if (!(right instanceof Constant constant)) {
            throw new NotImplementedException("Right part of expression must be a constant");
        }
        return createBinaryComparisonFilter(column.getName(), constant.getValue(), operator, paramCounter);
    }

    private static SqlConditionResult createBinaryComparisonFilter(String columnName, Comparable<?> value,
                                                                   BinaryComparisonOperator operator, AtomicInteger paramCounter) {
        return switch (operator) {
            case EQUALS -> createSimpleComparisonFilter(columnName, value, "=", paramCounter);
            case NOT_EQUALS -> createSimpleComparisonFilter(columnName, value, "!=", paramCounter);
            case LESS -> createSimpleComparisonFilter(columnName, value, "<", paramCounter);
            case GREATER -> createSimpleComparisonFilter(columnName, value, ">", paramCounter);
            case LESS_OR_EQUALS -> createSimpleComparisonFilter(columnName, value, "<=", paramCounter);
            case GREATER_OR_EQUALS -> createSimpleComparisonFilter(columnName, value, ">=", paramCounter);
            case CONTAINS -> createLikePatternFilter(columnName, "%" + escapeLikeWildcards(value) + "%", paramCounter);
            case NOT_CONTAINS ->
                    createNotLikePatternFilter(columnName, "%" + escapeLikeWildcards(value) + "%", paramCounter);
            case STARTS_WITH -> createLikePatternFilter(columnName, escapeLikeWildcards(value) + "%", paramCounter);
            case ENDS_WITH -> createLikePatternFilter(columnName, "%" + escapeLikeWildcards(value), paramCounter);
            case LIKE -> createLikeFilter(columnName, value, paramCounter);
            case NOT_LIKE -> throw new NotImplementedException("NOT LIKE operator is not supported yet");
            case IN, NOT_IN -> throw new IllegalStateException("IN/NOT IN handled before this point");
        };
    }

    private static SqlConditionResult createInFilter(String columnName, Tuple tuple,
                                                     BinaryComparisonOperator operator, AtomicInteger paramCounter) {
        var params = new HashMap<String, Object>();
        var paramRefs = new ArrayList<String>();
        for (var expression : tuple.getExpressions()) {
            if (!(expression instanceof Constant constant)) {
                throw new NotImplementedException("IN values must be constants");
            }
            var paramName = "p" + paramCounter.getAndIncrement();
            params.put(paramName, constant.getValue());
            paramRefs.add("$" + paramName);
        }
        var sqlOperator = operator == BinaryComparisonOperator.NOT_IN ? "NOT IN" : "IN";
        var query = "\"%s\" %s (%s)".formatted(columnName, sqlOperator, String.join(", ", paramRefs));
        return new SqlConditionResult(query, params);
    }

    private static SqlConditionResult createSimpleComparisonFilter(String columnName, Comparable<?> value,
                                                                   String operator, AtomicInteger paramCounter) {
        var paramName = "p" + paramCounter.getAndIncrement();
        var query = "\"%s\" %s $%s".formatted(columnName, operator, paramName);
        return new SqlConditionResult(query, Map.of(paramName, value));
    }

    private static SqlConditionResult createLikePatternFilter(String columnName, String pattern, AtomicInteger paramCounter) {
        var paramName = "p" + paramCounter.getAndIncrement();
        var query = "\"%s\" ILIKE $%s ESCAPE '\\'".formatted(columnName, paramName);
        return new SqlConditionResult(query, Map.of(paramName, pattern));
    }

    private static SqlConditionResult createNotLikePatternFilter(String columnName, String pattern, AtomicInteger paramCounter) {
        var paramName = "p" + paramCounter.getAndIncrement();
        var query = "\"%s\" NOT ILIKE $%s ESCAPE '\\'".formatted(columnName, paramName);
        return new SqlConditionResult(query, Map.of(paramName, pattern));
    }

    private static SqlConditionResult createLikeFilter(String columnName, Comparable<?> value, AtomicInteger paramCounter) {
        var likeValue = value.toString();

        var firstIndex = likeValue.indexOf("%");
        if (firstIndex == -1) {
            return createSimpleComparisonFilter(columnName, likeValue, "=", paramCounter);
        }

        var paramName = "p" + paramCounter.getAndIncrement();
        var query = "\"%s\" ILIKE $%s ESCAPE '\\'".formatted(columnName, paramName);
        return new SqlConditionResult(query, Map.of(paramName, likeValue));
    }

    private static String escapeLikeWildcards(Comparable<?> value) {
        return value.toString()
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }

    private static SqlConditionResult createUnaryComparisonFilter(UnaryComparisonFilter unaryComparisonFilter) {
        if (!(unaryComparisonFilter.getExpression() instanceof Column column)) {
            throw new NotImplementedException("Expression must be a column");
        }

        var query = switch (unaryComparisonFilter.getOperator()) {
            case IS_NULL -> "\"%s\" IS NULL".formatted(column.getName());
            case IS_NOT_NULL -> "\"%s\" IS NOT NULL".formatted(column.getName());
        };
        return new SqlConditionResult(query, Map.of());
    }

    public record SqlConditionResult(String query, Map<String, Object> parameters) {
        public static SqlConditionResult empty() {
            return new SqlConditionResult("", Map.of());
        }

        public boolean isEmpty() {
            return query.isEmpty();
        }
    }
}
