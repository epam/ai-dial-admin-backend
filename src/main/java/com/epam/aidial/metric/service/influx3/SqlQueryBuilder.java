package com.epam.aidial.metric.service.influx3;

import com.epam.aidial.expressions.AggregationFunctionCall;
import com.epam.aidial.expressions.CaseWhenExpression;
import com.epam.aidial.expressions.Column;
import com.epam.aidial.expressions.Constant;
import com.epam.aidial.expressions.Expression;
import com.epam.aidial.expressions.FunctionCall;
import com.epam.aidial.expressions.NumberConstant;
import com.epam.aidial.expressions.impl.ColumnImpl;
import com.epam.aidial.expressions.impl.FunctionImpl;
import com.epam.aidial.metric.component.TemporalNameGenerator;
import com.epam.aidial.metric.model.configuration.influx3.Influx3DatasetDeclaration;
import com.epam.aidial.metric.model.configuration.influx3.Influx3TableDeclaration;
import com.epam.aidial.metric.model.influx3.SqlQueryContext;
import com.epam.aidial.metric.service.AbstractQueryBuilder;
import com.epam.aidial.ql.common.model.enums.SortDirection;
import com.epam.aidial.ql.model.Filter;
import com.epam.aidial.ql.model.From;
import com.epam.aidial.ql.model.Query;
import com.epam.aidial.ql.model.Sort;
import com.epam.aidial.ql.model.Table;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.NotImplementedException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
public class SqlQueryBuilder extends AbstractQueryBuilder<SqlQueryContext, Influx3TableDeclaration> {

    public SqlQueryBuilder(Influx3DatasetDeclaration datasetDeclaration,
                           TemporalNameGenerator temporalNameGenerator) {
        super(datasetDeclaration, temporalNameGenerator);
    }

    @Override
    protected SqlQueryContext buildSimpleQuery(Query query) {
        var table = getTable(query);
        var tableDeclaration = getTableDeclaration(table.getName());
        var tableName = tableDeclaration.getSource().getTable();

        var paramCounter = new AtomicInteger(0);
        var allParams = new HashMap<String, Object>();

        // Build SELECT columns
        var selectColumns = buildSelectColumns(query.getFrom(), query.getExpressions());
        var outerColumnNames = getOuterColumnNames(query.getExpressions());

        // Build WHERE clause
        var whereClause = buildWhereClause(query.getWhere(), true, paramCounter, allParams);

        // Build ORDER BY
        var orderByClause = buildOrderByClause(query.getOrderBy());

        // Build LIMIT/OFFSET
        var limitClause = buildLimitClause(query);

        var sql = new StringBuilder();
        sql.append("SELECT ").append(selectColumns);
        sql.append(" FROM \"").append(tableName).append("\"");
        if (!whereClause.isEmpty()) {
            sql.append(" WHERE ").append(whereClause);
        }
        if (!orderByClause.isEmpty()) {
            sql.append(" ORDER BY ").append(orderByClause);
        }
        if (!limitClause.isEmpty()) {
            sql.append(" ").append(limitClause);
        }

        return SqlQueryContext.builder()
                .query(sql.toString())
                .columnNames(outerColumnNames)
                .parameters(allParams)
                .build();
    }

    @Override
    protected SqlQueryContext buildDistinctQuery(Query query) {
        var table = getTable(query);
        var column = getDistinctColumn(query);
        var tableDeclaration = getTableDeclaration(table.getName());
        var tableName = tableDeclaration.getSource().getTable();
        var sourceColumnName = getSourceColumnName(table, column);
        var outerColumnName = getOuterColumnName(column);

        var paramCounter = new AtomicInteger(0);
        var allParams = new HashMap<String, Object>();

        // Build WHERE clause
        var whereClause = buildWhereClause(query.getWhere(), true, paramCounter, allParams);

        // Build ORDER BY
        var orderByClause = buildOrderByClause(query.getOrderBy());

        // Build LIMIT/OFFSET
        var limitClause = buildLimitClause(query);

        var sql = new StringBuilder();
        sql.append("SELECT DISTINCT \"").append(sourceColumnName).append("\"");
        if (!sourceColumnName.equals(outerColumnName)) {
            sql.append(" AS \"").append(outerColumnName).append("\"");
        }
        sql.append(" FROM \"").append(tableName).append("\"");
        if (!whereClause.isEmpty()) {
            sql.append(" WHERE ").append(whereClause);
        }
        if (!orderByClause.isEmpty()) {
            sql.append(" ORDER BY ").append(orderByClause);
        }
        if (!limitClause.isEmpty()) {
            sql.append(" ").append(limitClause);
        }

        return SqlQueryContext.builder()
                .query(sql.toString())
                .columnNames(List.of(outerColumnName))
                .parameters(allParams)
                .build();
    }

    @Override
    protected SqlQueryContext buildAggregationQuery(Query query) {
        var paramCounter = new AtomicInteger(0);
        var allParams = new HashMap<String, Object>();

        // Outer names are what the user wrote in groupBy (which may be aliases from
        // expressions). Source names are what we emit in the SQL — alias references
        // must be unwound to the underlying storage column.
        var groupByOuterNames = getGroupByColumns(query.getGroupBy()).stream().map(Column::getName).toList();
        var groupBySourceNames = groupByOuterNames.stream()
                .map(this::resolveGroupBySourceName)
                .toList();
        var aggregationFunctionCalls = query.getExpressions().stream()
                .map(this::resolveAlias)
                .filter(AggregationFunctionCall.class::isInstance)
                .map(AggregationFunctionCall.class::cast)
                .toList();

        String tableName;
        String innerWhereClause;

        if (query.getFrom() instanceof Query innerQuery) {
            // Subquery — build inner query context and use as subquery
            var innerContext = buildQueryContext(innerQuery);
            allParams.putAll(innerContext.getParameters());
            paramCounter.set(innerContext.getParameters().size());
            tableName = "(" + innerContext.getQuery() + ")";
            innerWhereClause = buildWhereClause(query.getWhere(), false, paramCounter, allParams);
        } else {
            var table = getTable(query);
            var tableDeclaration = getTableDeclaration(table.getName());
            tableName = "\"" + tableDeclaration.getSource().getTable() + "\"";
            innerWhereClause = buildWhereClause(query.getWhere(), true, paramCounter, allParams);
        }

        // Build SELECT with aggregations — preserve expression order
        var expressionColumnNames = query.getExpressions().stream()
                .map(expressionToOuterColumnNames::get)
                .collect(Collectors.toSet());
        var selectParts = new ArrayList<String>();

        // Add group by columns to select only if they appear in expressions; render
        // `"source" AS "outer"` when the user defined an alias.
        for (int i = 0; i < groupByOuterNames.size(); i++) {
            var outer = groupByOuterNames.get(i);
            if (!expressionColumnNames.contains(outer)) {
                continue;
            }
            var source = groupBySourceNames.get(i);
            if (source.equals(outer)) {
                selectParts.add("\"" + outer + "\"");
            } else {
                selectParts.add("\"" + source + "\" AS \"" + outer + "\"");
            }
        }

        // Add aggregation expressions
        for (var aggCall : aggregationFunctionCalls) {
            var function = (FunctionImpl) aggCall.getFunction();
            var outerName = expressionToOuterColumnNames.get(aggCall);
            var aggSql = buildAggregationExpression(function, aggCall.getArgs(), paramCounter, allParams);
            selectParts.add(aggSql + " AS \"" + outerName + "\"");
        }

        var sql = new StringBuilder();
        sql.append("SELECT ").append(String.join(", ", selectParts));
        sql.append(" FROM ").append(tableName);
        if (!innerWhereClause.isEmpty()) {
            sql.append(" WHERE ").append(innerWhereClause);
        }
        if (!groupBySourceNames.isEmpty()) {
            var groupByClause = groupBySourceNames.stream()
                    .map(col -> "\"" + col + "\"")
                    .collect(Collectors.joining(", "));
            sql.append(" GROUP BY ").append(groupByClause);
        }

        // Build ORDER BY
        var orderByClause = buildOrderByClause(query.getOrderBy());
        if (!orderByClause.isEmpty()) {
            sql.append(" ORDER BY ").append(orderByClause);
        }

        // Build LIMIT/OFFSET
        var limitClause = buildLimitClause(query);
        if (!limitClause.isEmpty()) {
            sql.append(" ").append(limitClause);
        }

        var columnNames = query.getExpressions().stream()
                .map(expressionToOuterColumnNames::get)
                .toList();

        return SqlQueryContext.builder()
                .query(sql.toString())
                .columnNames(columnNames)
                .parameters(allParams)
                .build();
    }

    @Override
    protected SqlQueryContext buildWindowAggregationQuery(Query query) {
        var groupFunctionCall = resolveGroupFunctionCall(query.getGroupBy());
        var aggregationFunctionCall = resolveAggregationFunctionCall(query.getExpressions());

        var table = getTable(query);
        var tableDeclaration = getTableDeclaration(table.getName());
        var tableName = tableDeclaration.getSource().getTable();

        var function = (FunctionImpl) aggregationFunctionCall.getFunction();
        var windowOuterName = expressionToOuterColumnNames.get(groupFunctionCall);
        var aggOuterName = expressionToOuterColumnNames.get(aggregationFunctionCall);

        // Use a temp name in SQL to avoid colliding with InfluxDB's reserved "time" column
        var windowSqlAlias = getNewTemporaryName(TEMPORAL_COLUMN_NAME);

        var paramCounter = new AtomicInteger(0);
        var allParams = new HashMap<String, Object>();

        // Build WHERE clause
        var whereClause = buildWhereClause(query.getWhere(), true, paramCounter, allParams);

        // Build window function: DATE_BIN(INTERVAL '...', time, TIMESTAMP '1970-01-01T00:00:00Z')
        var windowInterval = buildWindowInterval(groupFunctionCall);
        var windowExpr = "DATE_BIN(%s, \"time\", TIMESTAMP '1970-01-01T00:00:00Z')".formatted(windowInterval);

        // Build aggregation expression
        var aggSql = buildAggregationExpression(function, aggregationFunctionCall.getArgs(), paramCounter, allParams);

        var sql = new StringBuilder();
        sql.append("SELECT ");
        sql.append(windowExpr).append(" AS \"").append(windowSqlAlias).append("\"");
        sql.append(", ").append(aggSql).append(" AS \"").append(aggOuterName).append("\"");
        sql.append(" FROM \"").append(tableName).append("\"");
        if (!whereClause.isEmpty()) {
            sql.append(" WHERE ").append(whereClause);
        }
        sql.append(" GROUP BY \"").append(windowSqlAlias).append("\"");

        // Build ORDER BY — map user alias to SQL alias for the window column
        var orderByClause = buildOrderByClause(query.getOrderBy(), Map.of(windowOuterName, windowSqlAlias));
        if (!orderByClause.isEmpty()) {
            sql.append(" ORDER BY ").append(orderByClause);
        }

        // Build LIMIT/OFFSET
        var limitClause = buildLimitClause(query);
        if (!limitClause.isEmpty()) {
            sql.append(" ").append(limitClause);
        }

        var columnNames = query.getExpressions().stream()
                .map(expressionToOuterColumnNames::get)
                .toList();

        return SqlQueryContext.builder()
                .query(sql.toString())
                .columnNames(columnNames)
                .parameters(allParams)
                .build();
    }

    @Override
    protected SqlQueryContext buildWindowColumnAggregationQuery(Query query) {
        var groupFunctionCall = extractWindowFunction(query.getGroupBy());
        var groupByColumnNames = extractGroupByColumnNames(query.getGroupBy());

        var table = getTable(query);
        var tableDeclaration = getTableDeclaration(table.getName());
        var tableName = tableDeclaration.getSource().getTable();

        var paramCounter = new AtomicInteger(0);
        var allParams = new HashMap<String, Object>();

        // Build WHERE clause
        var whereClause = buildWhereClause(query.getWhere(), true, paramCounter, allParams);

        // Build window expression
        var windowInterval = buildWindowInterval(groupFunctionCall);
        var windowExpr = "DATE_BIN(%s, \"time\", TIMESTAMP '1970-01-01T00:00:00Z')".formatted(windowInterval);

        // Use a temp name in SQL to avoid colliding with InfluxDB's reserved "time" column
        var windowOuterName = expressionToOuterColumnNames.get(groupFunctionCall);
        var windowSqlAlias = getNewTemporaryName(TEMPORAL_COLUMN_NAME);

        // Build SELECT — preserve expression order
        var selectParts = new ArrayList<String>();
        for (var expression : query.getExpressions()) {
            var resolved = resolveAlias(expression);
            var outerName = expressionToOuterColumnNames.get(expression);
            if (resolved instanceof com.epam.aidial.expressions.GroupFunctionCall) {
                selectParts.add(windowExpr + " AS \"" + windowSqlAlias + "\"");
            } else if (resolved instanceof AggregationFunctionCall aggCall) {
                var function = (FunctionImpl) aggCall.getFunction();
                var aggSql = buildAggregationExpression(function, aggCall.getArgs(), paramCounter, allParams);
                selectParts.add(aggSql + " AS \"" + outerName + "\"");
            } else if (resolved instanceof Column) {
                selectParts.add("\"" + outerName + "\"");
            }
        }

        // Build GROUP BY: window alias + column names
        var groupByParts = new ArrayList<String>();
        groupByParts.add("\"" + windowSqlAlias + "\"");
        for (var colName : groupByColumnNames) {
            groupByParts.add("\"" + colName + "\"");
        }

        var sql = new StringBuilder();
        sql.append("SELECT ").append(String.join(", ", selectParts));
        sql.append(" FROM \"").append(tableName).append("\"");
        if (!whereClause.isEmpty()) {
            sql.append(" WHERE ").append(whereClause);
        }
        sql.append(" GROUP BY ").append(String.join(", ", groupByParts));

        // Build ORDER BY — map user alias to SQL alias for the window column
        var orderByClause = buildOrderByClause(query.getOrderBy(), Map.of(windowOuterName, windowSqlAlias));
        if (!orderByClause.isEmpty()) {
            sql.append(" ORDER BY ").append(orderByClause);
        }

        // Build LIMIT/OFFSET
        var limitClause = buildLimitClause(query);
        if (!limitClause.isEmpty()) {
            sql.append(" ").append(limitClause);
        }

        var columnNames = query.getExpressions().stream()
                .map(expressionToOuterColumnNames::get)
                .toList();

        return SqlQueryContext.builder()
                .query(sql.toString())
                .columnNames(columnNames)
                .parameters(allParams)
                .build();
    }

    private String buildAggregationExpression(FunctionImpl function, List<Expression> args,
                                               AtomicInteger paramCounter, Map<String, Object> allParams) {
        return switch (function.getName()) {
            case "count" -> "COUNT(*)";
            case "sum" -> {
                if (args.isEmpty()) {
                    throw new IllegalArgumentException("sum requires an argument");
                }
                if (args.get(0) instanceof CaseWhenExpression caseWhen) {
                    yield buildSumCaseWhen(caseWhen, paramCounter, allParams);
                }
                var column = ((ColumnImpl) args.get(0)).getName();
                yield "SUM(\"%s\")".formatted(column);
            }
            default -> throw new NotImplementedException("Unsupported aggregation function: " + function.getName());
        };
    }

    private String buildSumCaseWhen(CaseWhenExpression caseWhen, AtomicInteger paramCounter, Map<String, Object> allParams) {
        var conditionSql = buildConditionSql(caseWhen.getCondition(), paramCounter, allParams);
        var thenValue = renderConstantValue(caseWhen.getThenExpression());
        var elseValue = renderConstantValue(caseWhen.getElseExpression());
        return "SUM(CASE WHEN %s THEN %s ELSE %s END)".formatted(conditionSql, thenValue, elseValue);
    }

    private String buildConditionSql(Expression condition, AtomicInteger paramCounter, Map<String, Object> allParams) {
        if (condition instanceof FunctionCall fc) {
            var funcName = fc.getFunction().getName();
            if (fc.getArgs().size() == 2
                    && fc.getArgs().get(0) instanceof Column col
                    && fc.getArgs().get(1) instanceof Constant val) {
                var operator = switch (funcName) {
                    case "equals" -> "=";
                    case "notEquals" -> "!=";
                    case "less" -> "<";
                    case "greater" -> ">";
                    case "lessOrEquals" -> "<=";
                    case "greaterOrEquals" -> ">=";
                    default -> throw new NotImplementedException("Unsupported CASE WHEN operator: " + funcName);
                };
                var paramName = "p" + paramCounter.getAndIncrement();
                allParams.put(paramName, val.getValue());
                return "\"%s\" %s $%s".formatted(col.getName(), operator, paramName);
            }
        }
        throw new NotImplementedException("Unsupported CASE WHEN condition: " + condition);
    }

    private String renderConstantValue(Expression expression) {
        if (expression instanceof NumberConstant nc) {
            return String.valueOf(nc.getNumberValue());
        }
        if (expression instanceof Constant c) {
            return "'" + c.getValue() + "'";
        }
        throw new NotImplementedException("Only constant values are supported in CASE WHEN THEN/ELSE");
    }

    private String buildWindowInterval(com.epam.aidial.expressions.GroupFunctionCall groupFunctionCall) {
        var value = ((NumberConstant) groupFunctionCall.getArgs().get(1)).getNumberValue();
        var unit = (String) ((Constant) groupFunctionCall.getArgs().get(2)).getValue();
        return "INTERVAL '%s %s'".formatted(value, mapIntervalUnit(unit));
    }

    private String mapIntervalUnit(String unit) {
        return switch (unit.toLowerCase()) {
            case "s" -> "second";
            case "m" -> "minute";
            case "h" -> "hour";
            case "d" -> "day";
            case "w" -> "week";
            case "mo" -> "month";
            case "y" -> "year";
            default -> throw new IllegalArgumentException("Unsupported interval unit: " + unit);
        };
    }

    private String buildSelectColumns(From from, List<Expression> expressions) {
        var parts = new ArrayList<String>();
        for (var expression : expressions) {
            var sourceColumnName = getSourceColumnName(from, expression);
            var outerColumnName = expressionToOuterColumnNames.get(expression);
            if (sourceColumnName.equals(outerColumnName)) {
                parts.add("\"" + sourceColumnName + "\"");
            } else {
                parts.add("\"" + sourceColumnName + "\" AS \"" + outerColumnName + "\"");
            }
        }
        return String.join(", ", parts);
    }

    private String buildWhereClause(Filter filter, boolean isRangeRequired, AtomicInteger paramCounter, Map<String, Object> allParams) {
        var rangePart = SqlConditionBuilder.createRangePart(filter, isRangeRequired, paramCounter);
        var filterPart = SqlConditionBuilder.createWherePart(filter, paramCounter);

        allParams.putAll(rangePart.parameters());
        allParams.putAll(filterPart.parameters());

        if (rangePart.isEmpty() && filterPart.isEmpty()) {
            return "";
        }
        if (rangePart.isEmpty()) {
            return filterPart.query();
        }
        if (filterPart.isEmpty()) {
            return rangePart.query();
        }
        return rangePart.query() + " AND " + filterPart.query();
    }

    private String buildOrderByClause(List<Sort> orderBy) {
        return buildOrderByClause(orderBy, Map.of());
    }

    private String buildOrderByClause(List<Sort> orderBy, Map<String, String> sqlAliasOverrides) {
        if (CollectionUtils.isEmpty(orderBy)) {
            return "";
        }

        return orderBy.stream()
                .map(sort -> {
                    var columnName = resolveOrderByColumnName(sort.getExpression());
                    if (columnName == null) {
                        throw new NotImplementedException("Only sort for specified columns is supported");
                    }
                    columnName = sqlAliasOverrides.getOrDefault(columnName, columnName);
                    var direction = sort.getDirection() == SortDirection.DESC ? " DESC" : " ASC";
                    return "\"" + columnName + "\"" + direction;
                })
                .collect(Collectors.joining(", "));
    }

    private String buildLimitClause(Query query) {
        var limit = query.getLimit();
        if (limit == null) {
            return "";
        }
        var sb = new StringBuilder("LIMIT ").append(limit);
        var offset = query.getOffset();
        if (offset != null && offset > 0) {
            sb.append(" OFFSET ").append(offset);
        }
        return sb.toString();
    }

    private String getSourceColumnName(From from, Expression expression) {
        if (from instanceof Table table) {
            if (expression instanceof com.epam.aidial.expressions.Alias alias && alias.getExpression() instanceof Column column) {
                return getSourceColumnNameFromDeclaration(table.getName(), column.getName());
            } else if (expression instanceof ColumnImpl column) {
                return getSourceColumnNameFromDeclaration(table.getName(), column.getName());
            } else {
                var columnName = expressionToOuterColumnNames.get(expression);
                if (columnName == null) {
                    throw new IllegalArgumentException("Cannot extract column name from expression: %s".formatted(expression));
                }
                return columnName;
            }
        } else if (from instanceof Query) {
            var columnName = expressionToOuterColumnNames.get(expression);
            if (columnName == null) {
                throw new IllegalArgumentException("Cannot extract column name from expression: %s".formatted(expression));
            }
            return columnName;
        }

        throw new NotImplementedException("Cannot extract column name from expression: %s".formatted(expression));
    }

    private String getSourceColumnNameFromDeclaration(String tableName, String columnName) {
        var tableDeclaration = getTableDeclaration(tableName);
        return tableDeclaration.getSchema().getColumns().stream()
                .filter(col -> col.getName().equals(columnName))
                .map(col -> col.getSource().getColumn())
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Column %s not found in table %s".formatted(columnName, tableName)));
    }
}
