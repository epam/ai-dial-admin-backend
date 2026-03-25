package com.epam.aidial.metric.service.influx;


import com.epam.aidial.expressions.AggregationFunctionCall;
import com.epam.aidial.expressions.Constant;
import com.epam.aidial.expressions.Expression;
import com.epam.aidial.expressions.GroupFunctionCall;
import com.epam.aidial.expressions.NumberConstant;
import com.epam.aidial.metric.model.influx.FluxQueryPart;
import com.epam.aidial.metric.model.influx.FluxStandardImports;
import com.epam.aidial.metric.util.CollectorsUtils;
import com.epam.aidial.ql.common.model.enums.SortDirection;
import com.epam.aidial.ql.model.Query;
import com.epam.aidial.ql.model.Sort;
import lombok.experimental.UtilityClass;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.NotImplementedException;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@UtilityClass
public class SimpleFluxBuilder {

    public static String createFromPart(String bucketName) {
        return "from(bucket: %s)".formatted(quote(bucketName));
    }

    public static String createKeepPart(List<String> columnNames) {
        if (columnNames.isEmpty()) {
            return "";
        }
        var combinedColumnNames = compactWithQuoting(columnNames);
        return "|> keep(columns: %s)".formatted(combinedColumnNames);
    }

    public static String createUngroupPart() {
        return "|> group()";
    }

    public static String createGroupPart(List<String> columnNames) {
        if (CollectionUtils.isEmpty(columnNames)) {
            return "|> group(columns: [\"\"])"; //todo: temporary for aggregation without grouping
        }
        return "|> group(columns: %s)".formatted(compactWithQuoting(columnNames));
    }

    public static String createSortPart(List<Sort> orderBy, Map<Expression, String> expression2ColumnNames) {
        if (CollectionUtils.isEmpty(orderBy)) {
            return "";
        }

        var direction = orderBy.stream().map(Sort::getDirection).distinct()
                .collect(CollectorsUtils.toSingleton(() -> new IllegalArgumentException("Only one sort direction is allowed")))
                .orElseThrow();

        var columnNames = orderBy.stream().map(Sort::getExpression)
                .map(expression2ColumnNames::get)
                .toList();

        var noMapping = columnNames.stream().anyMatch(Objects::isNull);
        if (noMapping) {
            throw new NotImplementedException("Only sort for specified columns is supported");
        }

        var desc = direction == SortDirection.DESC;
        return SimpleFluxBuilder.createSortPart(columnNames, desc);
    }

    private static String createSortPart(List<String> columnNames, boolean desc) {
        if (CollectionUtils.isEmpty(columnNames)) {
            return "";
        }
        var combinedColumnNames = compactWithQuoting(columnNames);
        return "|> sort(columns: %s, desc: %s)".formatted(combinedColumnNames, desc);
    }

    public static String createLimitPart(Query query, long defaultLimit) {
        var limit = query.getLimit();
        var offset = query.getOffset();

        if (limit == null && offset == null) {
            return "";
        }
        var size = limit == null ? defaultLimit : limit;
        if (offset == null || offset <= 0) {
            return "|> limit(n: %d)".formatted(size);
        } else {
            return "|> limit(n: %d, offset: %d)".formatted(size, offset);
        }
    }

    public static String createDistinctPart(String columnName) {
        return "|> distinct(column: %s)".formatted(quote(columnName));
    }

    public static String createRenamePart(Map<String, String> mapping) {
        if (MapUtils.isEmpty(mapping)) {
            return "";
        }

        var actualMapping = mapping.entrySet().stream()
                .filter(entry -> !Objects.equals(entry.getKey(), entry.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        if (actualMapping.isEmpty()) {
            return "";
        }

        var columns = actualMapping.entrySet().stream()
                .map(entry -> "%s: %s".formatted(entry.getKey(), quote(entry.getValue())))
                .sorted()
                .collect(Collectors.joining(", ", "{", "}"));
        return "|> rename(columns: %s)".formatted(columns);
    }

    public static String createWindowFunctionPart(GroupFunctionCall groupFunctionCall,
                                                  AggregationFunctionCall aggregationFunctionCall) {
        var value = ((NumberConstant) groupFunctionCall.getArgs().get(1)).getNumberValue();
        var unit = (String) ((Constant) groupFunctionCall.getArgs().get(2)).getValue();
        var duration = createDuration(value, unit);

        var functionName = aggregationFunctionCall.getFunction().getName();

        return "|> aggregateWindow(every: %s, fn: %s, createEmpty: false)".formatted(duration, functionName);
    }

    public static String createWindowPart(GroupFunctionCall groupFunctionCall) {
        var value = ((NumberConstant) groupFunctionCall.getArgs().get(1)).getNumberValue();
        var unit = (String) ((Constant) groupFunctionCall.getArgs().get(2)).getValue();
        var duration = createDuration(value, unit);
        return "|> window(every: %s)".formatted(duration);
    }

    public static String createSetPart(String columnName, String value) {
        var quotedColumnName = quote(columnName);
        var quotedValue = quote(value);
        return "|> set(key: %s, value: %s)".formatted(quotedColumnName, quotedValue);
    }

    public static FluxQueryPart createFieldsAsColsPart() {
        return FluxQueryPart.of(
                Set.of(FluxStandardImports.SCHEMA),
                "|> schema.fieldsAsCols()"
        );
    }

    private static String createDuration(Number value, String unit) {
        var lowerCaseUnit = unit.toLowerCase();
        var allowedUnits = List.of(
                "ns", "us", "ms", "s", "m", "h", "d", "w", "mo", "y"
        );
        if (!allowedUnits.contains(lowerCaseUnit)) {
            throw new IllegalArgumentException("Only aggregate window functions are supported");
        }
        return value + unit;
    }

    public static String compactWithQuoting(List<String> values) {
        return values.stream()
                .collect(Collectors.joining("\", \"", "[\"", "\"]"));
    }

    public static String quote(String value) {
        return "\"" + value + "\"";
    }

}
