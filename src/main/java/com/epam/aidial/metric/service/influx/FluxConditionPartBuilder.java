package com.epam.aidial.metric.service.influx;

import com.epam.aidial.expressions.Column;
import com.epam.aidial.expressions.Constant;
import com.epam.aidial.expressions.Expression;
import com.epam.aidial.expressions.impl.ConstantImpl;
import com.epam.aidial.metric.model.influx.FluxQueryPart;
import com.epam.aidial.metric.model.influx.FluxStandardImports;
import com.epam.aidial.metric.util.CollectorsUtils;
import com.epam.aidial.ql.common.model.enums.BinaryComparisonOperator;
import com.epam.aidial.ql.model.Filter;
import com.epam.aidial.ql.model.filters.And;
import com.epam.aidial.ql.model.filters.BinaryComparisonFilter;
import com.epam.aidial.ql.model.filters.Not;
import com.epam.aidial.ql.model.filters.Or;
import com.epam.aidial.ql.model.filters.UnaryComparisonFilter;
import com.epam.aidial.ql.model.filters.impl.AndImpl;
import com.epam.aidial.ql.model.filters.impl.OrImpl;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.NotImplementedException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.epam.aidial.metric.model.influx.FluxStandardColumns.TIME_COLUMN;

@UtilityClass
public class FluxConditionPartBuilder {

    private static final String LIKE_NOT_IMPLEMENTED_MESSAGE
            = "LIKE functionality currently supports only: equals, starts, ends, contains. Given value: %s";

    public static FluxQueryPart createFilterPart(Filter filter) {
        var nonRangeFilters = extractNonRangeFilter(filter);
        if (nonRangeFilters.isEmpty()) {
            return FluxQueryPart.of();
        }

        var filterExpression = createFilterExpression(nonRangeFilters.get());
        var queryPart = "|> filter(fn: (r) => %s)".formatted(filterExpression.getQuery());
        return FluxQueryPart.of(filterExpression.getImports(), queryPart);
    }

    public static FluxQueryPart createFilterPart(String field, Comparable<?> value) {
        var condition = createBinaryComparisonFilter(field, value, BinaryComparisonOperator.EQUALS);
        var queryPart = "|> filter(fn: (r) => %s)".formatted(condition.getQuery());
        return FluxQueryPart.of(condition.getImports(), queryPart);
    }

    public static String createRangePart(Filter filter, boolean isRequired) {
        var rangeFilters = extractRangeFilter(filter);

        var startFilterOptional = rangeFilters.stream()
                .filter(f -> f.getOperator() == BinaryComparisonOperator.GREATER_OR_EQUALS)
                .collect(CollectorsUtils.toSingleton(() -> new IllegalArgumentException("Only one start time filter must be provided")))
                .map(f -> getInstant(f.getRightExpression()));
        if (startFilterOptional.isEmpty()) {
            if (isRequired) {
                throw new IllegalArgumentException("No start time filter provided");
            } else {
                return "";
            }
        }
        var startFilter = startFilterOptional.get();

        var endFilter = rangeFilters.stream()
                .filter(f -> f.getOperator() == BinaryComparisonOperator.LESS)
                .map(f -> getInstant(f.getRightExpression()))
                .collect(CollectorsUtils.toSingleton(() -> new IllegalArgumentException("Only one end time filter must be provided")))
                .orElse(null);

        return createRangePart(startFilter, endFilter);
    }

    private static String createRangePart(long start, Long end) {
        if (end == null) {
            return "|> range(start: %s)".formatted(convertInstantToString(start));
        } else {
            return "|> range(start: %s, stop: %s)".formatted(
                    convertInstantToString(start),
                    convertInstantToString(end)
            );
        }
    }

    private static List<BinaryComparisonFilter> extractRangeFilter(Filter filter) {
        if (filter instanceof And and) {
            return and.getFilters().stream()
                    .filter(FluxConditionPartBuilder::isRangeFilter)
                    .map(BinaryComparisonFilter.class::cast)
                    .toList();
        }

        if (isRangeFilter(filter)) {
            return List.of((BinaryComparisonFilter) filter);
        }

        return List.of();
    }

    private static Optional<Filter> extractNonRangeFilter(Filter filter) {
        if (filter == null) {
            return Optional.empty();
        }

        if (filter instanceof And and) {
            var nonRangeFilters = and.getFilters().stream().filter(f -> !isRangeFilter(f)).toList();

            if (nonRangeFilters.isEmpty()) {
                return Optional.empty();
            }
            if (nonRangeFilters.size() == 1) {
                return Optional.of(nonRangeFilters.get(0));
            }
            return Optional.of(AndImpl.of(nonRangeFilters));
        } else if (filter instanceof Or or) {
            var nonRangeFilters = or.getFilters().stream().filter(f -> !isRangeFilter(f)).toList();

            if (nonRangeFilters.isEmpty()) {
                return Optional.empty();
            }
            if (nonRangeFilters.size() == 1) {
                return Optional.of(nonRangeFilters.get(0));
            }
            return Optional.of(OrImpl.of(nonRangeFilters));
        }

        if (!isRangeFilter(filter)) {
            return Optional.of(filter);
        } else {
            return Optional.empty();
        }
    }

    private static boolean isRangeFilter(Filter filter) {
        if (filter instanceof BinaryComparisonFilter binaryComparisonFilter) {
            if (binaryComparisonFilter.getLeftExpression() instanceof Column leftColumn && leftColumn.getName().equals(TIME_COLUMN)) {
                return true;
            }
            if (binaryComparisonFilter.getRightExpression() instanceof Column rightColumn && rightColumn.getName().equals(TIME_COLUMN)) {
                return true;
            }
        }
        return false;
    }

    private static FluxQueryPart createFilterExpression(Filter filter) {
        if (filter instanceof And and) {
            var filterExpressions = and.getFilters().stream()
                    .map(FluxConditionPartBuilder::createFilterExpression)
                    .toList();
            return combineFilterExpressions(filterExpressions, "and");
        } else if (filter instanceof Or or) {
            var filterExpressions = or.getFilters().stream()
                    .map(FluxConditionPartBuilder::createFilterExpression)
                    .toList();
            return combineFilterExpressions(filterExpressions, "or");
        } else if (filter instanceof Not) {
            throw new NotImplementedException("NOT keyword is not supported yet");
        } else if (filter instanceof BinaryComparisonFilter binaryComparisonFilter) {
            return createBinaryComparisonFilter(binaryComparisonFilter);
        } else if (filter instanceof UnaryComparisonFilter unaryComparisonFilter) {
            return createUnaryComparisonFilter(unaryComparisonFilter);
        } else {
            throw new IllegalArgumentException("Unsupported filter type: %s".formatted(filter.getClass()));
        }
    }

    private static FluxQueryPart combineFilterExpressions(List<FluxQueryPart> filterExpressions, String operator) {
        var operatorWithSpaces = " " + operator + " ";
        var query = filterExpressions.stream()
                .map(FluxQueryPart::getQuery)
                .collect(Collectors.joining(operatorWithSpaces, "(", ")"));
        var imports = filterExpressions.stream()
                .map(FluxQueryPart::getImports)
                .flatMap(Set::stream)
                .collect(Collectors.toSet());
        return FluxQueryPart.of(imports, query);
    }

    private FluxQueryPart createBinaryComparisonFilter(BinaryComparisonFilter binaryComparisonFilter) {
        if (!(binaryComparisonFilter.getLeftExpression() instanceof Column column)) {
            throw new NotImplementedException("Left part of expression must be a column");
        }
        if (!(binaryComparisonFilter.getRightExpression() instanceof Constant constant)) {
            throw new NotImplementedException("Right part of expression must be a constant");
        }

        var columnName = column.getName();
        var value = constant.getValue();
        return createBinaryComparisonFilter(columnName, value, binaryComparisonFilter.getOperator());
    }

    private FluxQueryPart createBinaryComparisonFilter(String columnName, Comparable<?> value, BinaryComparisonOperator operator) {
        return switch (operator) {
            case EQUALS -> createEqualsFilter(columnName, value);
            case NOT_EQUALS -> createNotEqualsFilter(columnName, value);

            case LESS, GREATER, LESS_OR_EQUALS, GREATER_OR_EQUALS ->
                    createComparisonFilter(columnName, value, operator);

            case CONTAINS, NOT_CONTAINS, STARTS_WITH, ENDS_WITH -> createContainsFilter(columnName, value, operator);
            case LIKE -> createLikeFilter(columnName, value);
            case NOT_LIKE -> throw new NotImplementedException("NOT LIKE operator is not supported yet");

            case IN -> throw new NotImplementedException("IN operator is not supported yet");
            case NOT_IN -> throw new NotImplementedException("NOT IN operator is not supported yet");
        };
    }

    private FluxQueryPart createEqualsFilter(String columnName, Comparable<?> comparable) {
        var columnNameQuoted = SimpleFluxBuilder.quote(columnName);
        var query = "r[%s] == %s".formatted(columnNameQuoted, quoteIfNeeded(comparable));
        return FluxQueryPart.of(query);
    }

    private FluxQueryPart createNotEqualsFilter(String columnName, Comparable<?> comparable) {
        var columnNameQuoted = SimpleFluxBuilder.quote(columnName);
        var query = "r[%s] != %s".formatted(columnNameQuoted, quoteIfNeeded(comparable));
        return FluxQueryPart.of(query);
    }

    private FluxQueryPart createComparisonFilter(String columnName, Comparable<?> comparable, BinaryComparisonOperator operator) {
        var columnNameQuoted = SimpleFluxBuilder.quote(columnName);
        var value = quoteIfNeeded(comparable);
        var stringOperator = switch (operator) {
            case LESS -> "<";
            case LESS_OR_EQUALS -> "<=";
            case GREATER -> ">";
            case GREATER_OR_EQUALS -> ">=";
            default -> throw new IllegalStateException("Unexpected operator: " + operator);
        };
        var query = "r[%s] %s %s".formatted(columnNameQuoted, stringOperator, value);
        return FluxQueryPart.of(query);
    }

    private FluxQueryPart createLikeFilter(String columnName, Comparable<?> comparable) {
        var likeValue = comparable.toString();

        var firstIndex = likeValue.indexOf("%");
        var secondIndex = likeValue.indexOf("%", firstIndex + 1);

        if (firstIndex == -1) {
            return createEqualsFilter(columnName, likeValue);
        }
        if (firstIndex == 0) {
            if (secondIndex == -1) {
                var value = likeValue.substring(1);
                return createContainsFilter(columnName, value, BinaryComparisonOperator.ENDS_WITH);
            } else if (secondIndex == likeValue.length() - 1) {
                var value = likeValue.substring(1, likeValue.length() - 1);
                return createContainsFilter(columnName, value, BinaryComparisonOperator.CONTAINS);
            } else {
                throw new NotImplementedException(LIKE_NOT_IMPLEMENTED_MESSAGE.formatted(likeValue));
            }
        } else if (firstIndex == likeValue.length() - 1) {
            var value = likeValue.substring(0, likeValue.length() - 1);
            return createContainsFilter(columnName, value, BinaryComparisonOperator.STARTS_WITH);
        } else {
            throw new NotImplementedException(LIKE_NOT_IMPLEMENTED_MESSAGE.formatted(likeValue));
        }
    }

    private FluxQueryPart createContainsFilter(String columnName, Comparable<?> comparable, BinaryComparisonOperator operator) {
        var columnNameQuoted = SimpleFluxBuilder.quote(columnName);
        var value = comparable.toString();
        var valueQuoted = SimpleFluxBuilder.quote(value);
        var query = switch (operator) {
            case CONTAINS -> "strings.containsStr(v: r[%s], substr: %s)".formatted(columnNameQuoted, valueQuoted);
            case NOT_CONTAINS ->
                    "not strings.containsStr(v: r[%s], substr: %s)".formatted(columnNameQuoted, valueQuoted);
            case STARTS_WITH -> "strings.hasPrefix(v: r[%s], prefix: %s)".formatted(columnNameQuoted, valueQuoted);
            case ENDS_WITH -> "strings.hasSuffix(v: r[%s], suffix: %s)".formatted(columnNameQuoted, valueQuoted);
            default -> throw new IllegalStateException("Unexpected operator: " + operator);
        };

        query = "exists r[%s] and %s".formatted(columnNameQuoted, query);

        return FluxQueryPart.of(Set.of(FluxStandardImports.STRINGS), query);
    }

    private FluxQueryPart createUnaryComparisonFilter(UnaryComparisonFilter unaryComparisonFilter) {
        if (!(unaryComparisonFilter.getExpression() instanceof Column column)) {
            throw new NotImplementedException("Expression must be a column");
        }

        var columnNameQuoted = SimpleFluxBuilder.quote(column.getName());
        var operator = switch (unaryComparisonFilter.getOperator()) {
            case IS_NULL -> "exists";
            case IS_NOT_NULL -> "now exists";
        };

        var query = "%s r[%s]".formatted(operator, columnNameQuoted);
        return FluxQueryPart.of(query);
    }

    private String quoteIfNeeded(Comparable<?> value) {
        return value instanceof Number ? value.toString() : SimpleFluxBuilder.quote(value.toString());
    }

    private static long getInstant(Expression expression) {
        if (!(expression instanceof ConstantImpl constant)) {
            throw new IllegalArgumentException("Only constant expressions are supported");
        }
        return (long) constant.getValue();
    }

    private String convertInstantToString(long rawInstant) {
        var instant = Instant.ofEpochMilli(rawInstant);
        return instant.toString();
    }

}
