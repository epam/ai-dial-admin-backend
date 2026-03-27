package com.epam.aidial.metric.service;

import com.epam.aidial.expressions.Alias;
import com.epam.aidial.expressions.Column;
import com.epam.aidial.expressions.Constant;
import com.epam.aidial.expressions.Expression;
import com.epam.aidial.expressions.GroupFunctionCall;
import com.epam.aidial.expressions.NumberConstant;
import com.epam.aidial.expressions.enums.Type;
import com.epam.aidial.ql.common.model.enums.BinaryComparisonOperator;
import com.epam.aidial.ql.model.Query;
import lombok.experimental.UtilityClass;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@UtilityClass
public class WindowGapFiller {

    public static Object defaultForType(Type type) {
        return switch (type) {
            case FLOAT, DOUBLE -> 0.0;
            default -> 0L;
        };
    }

    public static boolean isWindowQuery(Query query) {
        if (query.getGroupBy() == null || query.getGroupBy().isEmpty()) {
            return false;
        }
        return extractWindowFunction(query.getGroupBy()) != null;
    }

    public static List<List<Object>> fillGaps(List<List<Object>> rows, Query query) {
        var windowFunction = extractWindowFunction(query.getGroupBy());
        if (windowFunction == null) {
            return rows;
        }

        var timeRange = extractTimeRange(query);
        if (timeRange.isEmpty()) {
            return rows;
        }

        var interval = extractInterval(windowFunction);
        var buckets = generateTimeBuckets(timeRange.get().start(), timeRange.get().end(), interval);
        if (buckets.isEmpty()) {
            return rows;
        }

        var layout = analyzeExpressions(query.getExpressions(), query.getGroupBy());
        return fillGaps(rows, buckets, query.getExpressions(), layout);
    }

    record TimeRange(Instant start, Instant end) {}

    record Interval(long value, String unit) {}

    record ColumnLayout(int timeIndex, List<Integer> groupColumnIndices, List<Integer> aggregationIndices) {}

    private static GroupFunctionCall extractWindowFunction(List<Expression> groupBy) {
        return groupBy.stream()
                .filter(GroupFunctionCall.class::isInstance)
                .map(GroupFunctionCall.class::cast)
                .findFirst()
                .orElse(null);
    }

    private static Optional<TimeRange> extractTimeRange(Query query) {
        var rangeFilters = RangeFilterUtils.extractRangeFilter(query.getWhere());
        if (rangeFilters.isEmpty()) {
            return Optional.empty();
        }

        Instant start = null;
        Instant end = null;

        for (var filter : rangeFilters) {
            if (filter.getOperator() == BinaryComparisonOperator.GREATER_OR_EQUALS) {
                start = Instant.ofEpochMilli(RangeFilterUtils.getInstant(filter.getRightExpression()));
            } else if (filter.getOperator() == BinaryComparisonOperator.LESS) {
                end = Instant.ofEpochMilli(RangeFilterUtils.getInstant(filter.getRightExpression()));
            }
        }

        if (start == null || end == null) {
            return Optional.empty();
        }

        return Optional.of(new TimeRange(start, end));
    }

    private static Interval extractInterval(GroupFunctionCall windowFunction) {
        var value = ((NumberConstant) windowFunction.getArgs().get(1)).getNumberValue().longValue();
        var unit = (String) ((Constant) windowFunction.getArgs().get(2)).getValue();
        return new Interval(value, unit);
    }

    private static List<Instant> generateTimeBuckets(Instant start, Instant end, Interval interval) {
        var firstBucket = alignToStart(start, interval);
        var step = toStepFunction(interval);

        var buckets = new ArrayList<Instant>();
        var current = firstBucket;
        while (current.toInstant().isBefore(end)) {
            buckets.add(current.toInstant());
            current = step.apply(current);
        }
        return buckets;
    }

    private static ZonedDateTime alignToStart(Instant start, Interval interval) {
        var startZoned = start.atZone(ZoneOffset.UTC);
        return switch (interval.unit().toLowerCase()) {
            case "y" -> startZoned.withMonth(1).withDayOfMonth(1).toLocalDate().atStartOfDay(ZoneOffset.UTC);
            case "mo" -> startZoned.withDayOfMonth(1).toLocalDate().atStartOfDay(ZoneOffset.UTC);
            default -> {
                // Fixed-duration units (s, m, h, d, w): align to epoch like DATE_BIN
                var epochMillis = toDuration(interval).toMillis();
                var startMillis = start.toEpochMilli();
                var aligned = startMillis - Math.floorMod(startMillis, epochMillis);
                yield Instant.ofEpochMilli(aligned).atZone(ZoneOffset.UTC);
            }
        };
    }

    private static java.util.function.UnaryOperator<ZonedDateTime> toStepFunction(Interval interval) {
        var value = interval.value();
        return switch (interval.unit().toLowerCase()) {
            case "s" -> b -> b.plusSeconds(value);
            case "m" -> b -> b.plusMinutes(value);
            case "h" -> b -> b.plusHours(value);
            case "d" -> b -> b.plusDays(value);
            case "w" -> b -> b.plusWeeks(value);
            case "mo" -> b -> b.plusMonths(value);
            case "y" -> b -> b.plusYears(value);
            default -> throw new IllegalArgumentException("Unsupported interval unit: " + interval.unit());
        };
    }

    private static Duration toDuration(Interval interval) {
        return switch (interval.unit().toLowerCase()) {
            case "s" -> Duration.ofSeconds(interval.value());
            case "m" -> Duration.ofMinutes(interval.value());
            case "h" -> Duration.ofHours(interval.value());
            case "d" -> Duration.ofDays(interval.value());
            case "w" -> Duration.ofDays(interval.value() * 7);
            default -> throw new IllegalArgumentException("Unsupported interval unit for duration: " + interval.unit());
        };
    }

    private static ColumnLayout analyzeExpressions(List<Expression> expressions, List<Expression> groupBy) {
        // Collect non-window group-by column names so we can identify them among SELECT expressions
        var groupColumnNames = new LinkedHashSet<String>();
        for (var expr : groupBy) {
            if (expr instanceof Column col && !(expr instanceof GroupFunctionCall)) {
                groupColumnNames.add(col.getName());
            }
        }

        int timeIndex = 0;
        var groupColumnIndices = new ArrayList<Integer>();
        var aggregationIndices = new ArrayList<Integer>();

        for (int i = 0; i < expressions.size(); i++) {
            var expr = expressions.get(i);

            if (expr instanceof GroupFunctionCall
                    || (expr instanceof Alias alias && alias.getExpression() instanceof GroupFunctionCall)) {
                timeIndex = i;
                continue;
            }

            if (expr.isAggregation()) {
                aggregationIndices.add(i);
                continue;
            }

            // Alias extends Column, so this covers both plain columns and aliased columns
            if (expr instanceof Column col && groupColumnNames.contains(col.getName())) {
                groupColumnIndices.add(i);
            }
        }

        return new ColumnLayout(timeIndex, groupColumnIndices, aggregationIndices);
    }

    private static List<List<Object>> fillGaps(
            List<List<Object>> rows,
            List<Instant> buckets,
            List<Expression> expressions,
            ColumnLayout layout) {

        // Window+column queries with no data: we can't fill gaps because the group key values
        // (e.g., which deployments exist) are unknown — return empty
        if (rows.isEmpty() && !layout.groupColumnIndices().isEmpty()) {
            return rows;
        }

        var groupKeys = new LinkedHashSet<List<Object>>();
        var dataMap = new LinkedHashMap<List<Object>, Map<Instant, List<Object>>>();

        for (var row : rows) {
            var groupKey = extractGroupKey(row, layout.groupColumnIndices());
            groupKeys.add(groupKey);

            var time = (Instant) row.get(layout.timeIndex());
            dataMap.computeIfAbsent(groupKey, k -> new LinkedHashMap<>()).put(time, row);
        }

        if (groupKeys.isEmpty()) {
            groupKeys.add(List.of());
        }

        var result = new ArrayList<List<Object>>();
        for (var bucket : buckets) {
            for (var groupKey : groupKeys) {
                var timeMap = dataMap.get(groupKey);
                var existing = timeMap != null ? timeMap.get(bucket) : null;
                if (existing != null) {
                    result.add(existing);
                } else {
                    result.add(buildDefaultRow(expressions, layout, bucket, groupKey));
                }
            }
        }
        return result;
    }

    private static List<Object> extractGroupKey(List<Object> row, List<Integer> groupColumnIndices) {
        var key = new ArrayList<>(groupColumnIndices.size());
        for (var idx : groupColumnIndices) {
            key.add(row.get(idx));
        }
        return key;
    }

    private static List<Object> buildDefaultRow(
            List<Expression> expressions,
            ColumnLayout layout,
            Instant bucketTime,
            List<Object> groupValues) {

        var row = new ArrayList<>(expressions.size());
        for (int i = 0; i < expressions.size(); i++) {
            row.add(null);
        }

        row.set(layout.timeIndex(), bucketTime);

        for (int i = 0; i < layout.groupColumnIndices().size(); i++) {
            row.set(layout.groupColumnIndices().get(i), groupValues.get(i));
        }

        for (var idx : layout.aggregationIndices()) {
            row.set(idx, defaultForType(expressions.get(idx).getType()));
        }

        return row;
    }
}
