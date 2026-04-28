package com.epam.aidial.metric.service.influx;

import com.epam.aidial.expressions.Column;
import com.epam.aidial.expressions.Constant;
import com.epam.aidial.expressions.Expression;
import com.epam.aidial.metric.model.influx.FluxQueryPart;
import com.epam.aidial.metric.model.influx.FluxStandardImports;
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

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@UtilityClass
public class FluxConditionPartBuilder {

    private static final String LIKE_NOT_IMPLEMENTED_MESSAGE
            = "LIKE functionality currently supports only: equals, starts, ends, contains. Given value: %s";

    public static FluxQueryPart createFilterPart(Filter filter) {
        return createFilterPart(filter, new AtomicInteger());
    }

    public static FluxQueryPart createFilterPart(Filter filter, AtomicInteger regexCounter) {
        var nonRangeFilters = RangeFilterUtils.extractNonRangeFilter(filter);
        if (nonRangeFilters.isEmpty()) {
            return FluxQueryPart.of();
        }

        var filterExpression = createFilterExpression(nonRangeFilters.get(), regexCounter);
        var queryPart = "|> filter(fn: (r) => %s)".formatted(filterExpression.getQuery());
        return FluxQueryPart.of(filterExpression.getImports(), filterExpression.getPreamble(), queryPart);
    }

    public static FluxQueryPart createFilterPart(String field, Comparable<?> value) {
        var condition = createBinaryComparisonFilter(field, value, BinaryComparisonOperator.EQUALS, new AtomicInteger());
        var queryPart = "|> filter(fn: (r) => %s)".formatted(condition.getQuery());
        return FluxQueryPart.of(condition.getImports(), queryPart);
    }

    public static String createRangePart(Filter filter, boolean isRequired) {
        var rangeFilters = RangeFilterUtils.extractRangeFilter(filter);

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

    private static FluxQueryPart createFilterExpression(Filter filter, AtomicInteger regexCounter) {
        if (filter instanceof And and) {
            var filterExpressions = and.getFilters().stream()
                    .map(f -> createFilterExpression(f, regexCounter))
                    .toList();
            return combineFilterExpressions(filterExpressions, "and");
        } else if (filter instanceof Or or) {
            var filterExpressions = or.getFilters().stream()
                    .map(f -> createFilterExpression(f, regexCounter))
                    .toList();
            return combineFilterExpressions(filterExpressions, "or");
        } else if (filter instanceof Not) {
            throw new NotImplementedException("NOT keyword is not supported yet");
        } else if (filter instanceof BinaryComparisonFilter binaryComparisonFilter) {
            return createBinaryComparisonFilter(binaryComparisonFilter, regexCounter);
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
        var preamble = filterExpressions.stream()
                .map(FluxQueryPart::getPreamble)
                .flatMap(List::stream)
                .toList();
        return FluxQueryPart.of(imports, preamble, query);
    }

    private FluxQueryPart createBinaryComparisonFilter(BinaryComparisonFilter binaryComparisonFilter, AtomicInteger regexCounter) {
        if (!(binaryComparisonFilter.getLeftExpression() instanceof Column column)) {
            throw new NotImplementedException("Left part of expression must be a column");
        }

        var operator = binaryComparisonFilter.getOperator();
        var right = binaryComparisonFilter.getRightExpression();
        if (operator == BinaryComparisonOperator.IN || operator == BinaryComparisonOperator.NOT_IN) {
            if (!(right instanceof Tuple tuple)) {
                throw new NotImplementedException("Right part of IN/NOT IN expression must be a tuple");
            }
            return createInFilter(column.getName(), tuple, operator);
        }

        if (!(right instanceof Constant constant)) {
            throw new NotImplementedException("Right part of expression must be a constant");
        }
        return createBinaryComparisonFilter(column.getName(), constant.getValue(), operator, regexCounter);
    }

    private FluxQueryPart createBinaryComparisonFilter(String columnName, Comparable<?> value,
                                                        BinaryComparisonOperator operator, AtomicInteger regexCounter) {
        return switch (operator) {
            case EQUALS -> createEqualsFilter(columnName, value);
            case NOT_EQUALS -> createNotEqualsFilter(columnName, value);

            case LESS, GREATER, LESS_OR_EQUALS, GREATER_OR_EQUALS ->
                    createComparisonFilter(columnName, value, operator);

            case CONTAINS, NOT_CONTAINS, STARTS_WITH, ENDS_WITH -> createContainsFilter(columnName, value, operator, regexCounter);
            case LIKE -> createLikeFilter(columnName, value, regexCounter);
            case NOT_LIKE -> throw new NotImplementedException("NOT LIKE operator is not supported yet");

            case IN, NOT_IN -> throw new IllegalStateException("IN/NOT IN handled before this point");
        };
    }

    private FluxQueryPart createInFilter(String columnName, Tuple tuple, BinaryComparisonOperator operator) {
        var values = tuple.getExpressions().stream()
                .map(e -> {
                    if (!(e instanceof Constant c)) {
                        throw new NotImplementedException("IN values must be constants");
                    }
                    return quoteIfNeeded(c.getValue());
                })
                .collect(Collectors.joining(", ", "[", "]"));
        var quotedColumn = SimpleFluxBuilder.quote(columnName);
        var clause = "contains(value: r[%s], set: %s)".formatted(quotedColumn, values);
        if (operator == BinaryComparisonOperator.NOT_IN) {
            clause = "not " + clause;
        }
        return FluxQueryPart.of(clause);
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

    private FluxQueryPart createLikeFilter(String columnName, Comparable<?> comparable, AtomicInteger regexCounter) {
        var likeValue = comparable.toString();

        var firstIndex = likeValue.indexOf("%");
        var secondIndex = likeValue.indexOf("%", firstIndex + 1);

        if (firstIndex == -1) {
            return createEqualsFilter(columnName, likeValue);
        }
        if (firstIndex == 0) {
            if (secondIndex == -1) {
                var value = likeValue.substring(1);
                return createContainsFilter(columnName, value, BinaryComparisonOperator.ENDS_WITH, regexCounter);
            } else if (secondIndex == likeValue.length() - 1) {
                var value = likeValue.substring(1, likeValue.length() - 1);
                return createContainsFilter(columnName, value, BinaryComparisonOperator.CONTAINS, regexCounter);
            } else {
                throw new NotImplementedException(LIKE_NOT_IMPLEMENTED_MESSAGE.formatted(likeValue));
            }
        } else if (firstIndex == likeValue.length() - 1) {
            var value = likeValue.substring(0, likeValue.length() - 1);
            return createContainsFilter(columnName, value, BinaryComparisonOperator.STARTS_WITH, regexCounter);
        } else {
            throw new NotImplementedException(LIKE_NOT_IMPLEMENTED_MESSAGE.formatted(likeValue));
        }
    }

    private FluxQueryPart createContainsFilter(String columnName, Comparable<?> comparable,
                                                BinaryComparisonOperator operator, AtomicInteger regexCounter) {
        var columnNameQuoted = SimpleFluxBuilder.quote(columnName);
        var value = escapeRegex(comparable.toString());

        var regexPattern = switch (operator) {
            case CONTAINS, NOT_CONTAINS -> "(?i)" + value;
            case STARTS_WITH -> "(?i)^" + value;
            case ENDS_WITH -> "(?i)" + value + "$";
            default -> throw new IllegalStateException("Unexpected operator: " + operator);
        };

        var varName = "_re" + regexCounter.getAndIncrement();
        // Double backslashes for Flux string literal (Flux uses \ as escape char in strings)
        var fluxEscapedPattern = regexPattern.replace("\\", "\\\\");
        var declaration = "%s = regexp.compile(v: %s)".formatted(varName, SimpleFluxBuilder.quote(fluxEscapedPattern));

        var regexOperator = operator == BinaryComparisonOperator.NOT_CONTAINS ? "!~" : "=~";
        var query = "r[%s] %s %s".formatted(columnNameQuoted, regexOperator, varName);

        return FluxQueryPart.of(Set.of(FluxStandardImports.REGEXP), List.of(declaration), query);
    }

    private static String escapeRegex(String value) {
        return value.replaceAll("([.+*?^${}()|\\[\\]\\\\])", "\\\\$1");
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
        return RangeFilterUtils.getInstant(expression);
    }

    private String convertInstantToString(long rawInstant) {
        return RangeFilterUtils.convertInstantToString(rawInstant);
    }

}
