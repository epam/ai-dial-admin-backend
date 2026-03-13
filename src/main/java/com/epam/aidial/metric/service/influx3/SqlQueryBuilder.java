package com.epam.aidial.metric.service.influx3;

import com.epam.aidial.expressions.AggregationFunctionCall;
import com.epam.aidial.expressions.Alias;
import com.epam.aidial.expressions.Column;
import com.epam.aidial.expressions.Constant;
import com.epam.aidial.expressions.Expression;
import com.epam.aidial.expressions.GroupFunctionCall;
import com.epam.aidial.expressions.NumberConstant;
import com.epam.aidial.expressions.impl.ColumnImpl;
import com.epam.aidial.expressions.impl.FunctionImpl;
import com.epam.aidial.metric.component.TemporalNameGenerator;
import com.epam.aidial.metric.config.Influx3DatasetConfiguration;
import com.epam.aidial.metric.model.configuration.influx3.Influx3ColumnSource;
import com.epam.aidial.metric.model.configuration.influx3.Influx3DatasetDeclaration;
import com.epam.aidial.metric.model.configuration.influx3.Influx3TableDeclaration;
import com.epam.aidial.metric.model.influx3.SqlQueryContext;
import com.epam.aidial.metric.service.influx3.SqlConditionBuilder.SqlConditionResult;
import com.epam.aidial.metric.util.CollectorsUtils;
import com.epam.aidial.ql.common.model.enums.SortDirection;
import com.epam.aidial.ql.model.Completable;
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
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
public class SqlQueryBuilder {

    private static final String TEMPORAL_COLUMN_NAME = "temp_column_";

    private final Map<String, Influx3TableDeclaration> tableDeclarations;
    private final Influx3DatasetConfiguration datasetConfiguration;

    private final Map<Expression, String> expressionToOuterColumnNames = new HashMap<>();
    private final Map<String, Expression> aliasToExpression = new HashMap<>();
    private final TemporalNameGenerator temporalNameGenerator;

    public SqlQueryBuilder(Influx3DatasetDeclaration datasetDeclaration,
                           Influx3DatasetConfiguration datasourceConfiguration,
                           TemporalNameGenerator temporalNameGenerator) {
        this.tableDeclarations = datasetDeclaration.getTables().stream()
                .collect(Collectors.toMap(Influx3TableDeclaration::getName, table -> table));
        this.datasetConfiguration = datasourceConfiguration;
        this.temporalNameGenerator = temporalNameGenerator;
    }

    public SqlQueryContext buildQueryContext(Completable completable) {
        if (completable instanceof Query query) {

            resolveExpressionsToOuterColumnNames(query.getExpressions());
            resolveAliases(query.getExpressions());

            if (isWindowAggregationQuery(query)) {
                return buildWindowAggregationQuery(query);
            } else if (isAggregationQuery(query)) {
                return buildAggregationQuery(query);
            } else if (isSimpleQuery(query)) {
                return buildSimpleQuery(query);
            } else if (isDistinctQuery(query)) {
                return buildDistinctQuery(query);
            }
        }
        throw new NotImplementedException("Unsupported query type");
    }

    private boolean isWindowAggregationQuery(Query query) {
        return query.getGroupBy().stream().anyMatch(this::isWindowFunctionCall);
    }

    private boolean isAggregationQuery(Query query) {
        return query.getExpressions().stream().anyMatch(this::isAggregationFunctionCall);
    }

    private boolean isSimpleQuery(Query query) {
        return query.getExpressions().stream().anyMatch(this::isColumn) && !query.isDistinct();
    }

    private boolean isDistinctQuery(Query query) {
        return query.isDistinct();
    }

    private boolean isWindowFunctionCall(Expression expression) {
        if (expression instanceof GroupFunctionCall) {
            return true;
        }
        if (expression instanceof Column column) {
            var originalExpression = aliasToExpression.get(column.getName());
            return originalExpression instanceof GroupFunctionCall;
        }
        return false;
    }

    private boolean isAggregationFunctionCall(Expression expression) {
        if (expression instanceof AggregationFunctionCall) {
            return true;
        }
        return expression instanceof Alias alias && alias.getExpression() instanceof AggregationFunctionCall;
    }

    private boolean isColumn(Expression expression) {
        if (expression instanceof ColumnImpl) {
            return true;
        }
        return expression instanceof Alias alias && alias.getExpression() instanceof Column;
    }

    private SqlQueryContext buildSimpleQuery(Query query) {
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

    private SqlQueryContext buildDistinctQuery(Query query) {
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

    private SqlQueryContext buildAggregationQuery(Query query) {
        var paramCounter = new AtomicInteger(0);
        var allParams = new HashMap<String, Object>();

        var groupByColumns = getGroupByColumns(query.getGroupBy()).stream().map(Column::getName).toList();
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

        // Build SELECT with aggregations
        var selectParts = new ArrayList<String>();

        // Add group by columns to select
        for (var groupByCol : groupByColumns) {
            selectParts.add("\"" + groupByCol + "\"");
        }

        // Add aggregation expressions
        for (var aggCall : aggregationFunctionCalls) {
            var function = (FunctionImpl) aggCall.getFunction();
            var outerName = expressionToOuterColumnNames.get(aggCall);
            var aggSql = buildAggregationExpression(function, aggCall.getArgs());
            selectParts.add(aggSql + " AS \"" + outerName + "\"");
        }

        var sql = new StringBuilder();
        sql.append("SELECT ").append(String.join(", ", selectParts));
        sql.append(" FROM ").append(tableName);
        if (!innerWhereClause.isEmpty()) {
            sql.append(" WHERE ").append(innerWhereClause);
        }
        if (!groupByColumns.isEmpty()) {
            var groupByClause = groupByColumns.stream()
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

    private SqlQueryContext buildWindowAggregationQuery(Query query) {
        var groupFunctionCall = resolveGroupFunctionCall(query.getGroupBy());
        var aggregationFunctionCall = resolveAggregationFunctionCall(query.getExpressions());

        var table = getTable(query);
        var tableDeclaration = getTableDeclaration(table.getName());
        var tableName = tableDeclaration.getSource().getTable();

        var function = (FunctionImpl) aggregationFunctionCall.getFunction();
        var windowOuterName = expressionToOuterColumnNames.get(groupFunctionCall);
        var aggOuterName = expressionToOuterColumnNames.get(aggregationFunctionCall);

        var paramCounter = new AtomicInteger(0);
        var allParams = new HashMap<String, Object>();

        // Build WHERE clause
        var whereClause = buildWhereClause(query.getWhere(), true, paramCounter, allParams);

        // Build window function: DATE_BIN(INTERVAL '...', time, TIMESTAMP '1970-01-01T00:00:00Z')
        var windowInterval = buildWindowInterval(groupFunctionCall);
        var windowExpr = "DATE_BIN(%s, \"time\", TIMESTAMP '1970-01-01T00:00:00Z')".formatted(windowInterval);

        // Build aggregation expression
        var aggSql = buildAggregationExpression(function, aggregationFunctionCall.getArgs());

        var sql = new StringBuilder();
        sql.append("SELECT ");
        sql.append(windowExpr).append(" AS \"").append(windowOuterName).append("\"");
        sql.append(", ").append(aggSql).append(" AS \"").append(aggOuterName).append("\"");
        sql.append(" FROM \"").append(tableName).append("\"");
        if (!whereClause.isEmpty()) {
            sql.append(" WHERE ").append(whereClause);
        }
        sql.append(" GROUP BY \"").append(windowOuterName).append("\"");

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

    private String buildAggregationExpression(FunctionImpl function, List<Expression> args) {
        return switch (function.getName()) {
            case "count" -> "COUNT(*)";
            case "sum" -> {
                if (args.isEmpty()) {
                    throw new IllegalArgumentException("sum requires an argument");
                }
                var column = ((ColumnImpl) args.get(0)).getName();
                yield "SUM(\"%s\")".formatted(column);
            }
            default -> throw new NotImplementedException("Unsupported aggregation function: " + function.getName());
        };
    }

    private String buildWindowInterval(GroupFunctionCall groupFunctionCall) {
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
        if (CollectionUtils.isEmpty(orderBy)) {
            return "";
        }

        return orderBy.stream()
                .map(sort -> {
                    var columnName = resolveOrderByColumnName(sort.getExpression());
                    if (columnName == null) {
                        throw new NotImplementedException("Only sort for specified columns is supported");
                    }
                    var direction = sort.getDirection() == SortDirection.DESC ? " DESC" : " ASC";
                    return "\"" + columnName + "\"" + direction;
                })
                .collect(Collectors.joining(", "));
    }

    private String resolveOrderByColumnName(Expression expression) {
        // Direct lookup by object identity
        var columnName = expressionToOuterColumnNames.get(expression);
        if (columnName != null) {
            return columnName;
        }

        // Fallback: lookup by column name (for sort expressions referencing aliases/columns)
        if (expression instanceof Column column) {
            // Check if it matches an outer column name directly
            if (expressionToOuterColumnNames.containsValue(column.getName())) {
                return column.getName();
            }
        }

        return null;
    }

    private String buildLimitClause(Query query) {
        var limit = query.getLimit();
        var offset = query.getOffset();

        if (limit == null && offset == null) {
            return "";
        }

        var size = limit == null ? datasetConfiguration.getDefaultPageSize() : limit;
        var sb = new StringBuilder("LIMIT ").append(size);
        if (offset != null && offset > 0) {
            sb.append(" OFFSET ").append(offset);
        }
        return sb.toString();
    }

    private Column getDistinctColumn(Query query) {
        if (query.getExpressions().size() != 1) {
            throw new NotImplementedException("Only one distinct expression is supported");
        }
        var expression = query.getExpressions().get(0);
        if (!(expression instanceof Column column)) {
            throw new IllegalArgumentException("Only distinct columns are supported");
        }
        return column;
    }

    private GroupFunctionCall resolveGroupFunctionCall(List<Expression> expressions) {
        if (expressions.size() != 1) {
            throw new IllegalArgumentException("Only window function allowed for window aggregation");
        }
        var resolvedExpression = resolveAlias(expressions.get(0));
        if (resolvedExpression instanceof GroupFunctionCall groupFunctionCall) {
            return groupFunctionCall;
        }
        throw new IllegalStateException("Window function required for window aggregation");
    }

    private AggregationFunctionCall resolveAggregationFunctionCall(List<Expression> expressions) {
        return expressions.stream()
                .map(this::resolveAlias)
                .filter(AggregationFunctionCall.class::isInstance)
                .map(AggregationFunctionCall.class::cast)
                .collect(CollectorsUtils.toSingleton(() -> new IllegalArgumentException("Only one aggregation expression allowed")))
                .orElseThrow(() -> new IllegalArgumentException("Aggregation expression must be passed"));
    }

    private Expression resolveAlias(Expression expression) {
        if (expression instanceof Alias alias) {
            return alias.getExpression();
        } else if (expression instanceof Column column) {
            var aliasedExpression = aliasToExpression.get(column.getName());
            if (aliasedExpression instanceof GroupFunctionCall groupFunctionCall) {
                return groupFunctionCall;
            }
        }
        return expression;
    }

    private List<Column> getGroupByColumns(List<Expression> groupBy) {
        var groupColumns = groupBy.stream()
                .filter(Column.class::isInstance)
                .map(Column.class::cast)
                .toList();

        if (groupBy.size() != groupColumns.size()) {
            throw new IllegalArgumentException("Invalid group by columns");
        }
        return groupColumns;
    }

    private String getSourceColumnName(From from, Expression expression) {
        if (from instanceof Table table) {
            if (expression instanceof Alias alias && alias.getExpression() instanceof Column column) {
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
        var tableSchema = (com.epam.aidial.metric.model.configuration.StaticTableSchema) tableDeclaration.getSchema();
        return tableSchema.getColumns().stream()
                .filter(col -> col.getName().equals(columnName))
                .map(col -> ((Influx3ColumnSource) col.getSource()).getColumn())
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Column %s not found in table %s".formatted(columnName, tableName)));
    }

    private Table getTable(Query query) {
        if (!(query.getFrom() instanceof Table table)) {
            throw new IllegalArgumentException("Only from table sources are supported");
        }
        return table;
    }

    private Influx3TableDeclaration getTableDeclaration(String tableName) {
        var tableDeclaration = tableDeclarations.get(tableName);
        if (tableDeclaration == null) {
            throw new IllegalArgumentException("Table %s not found".formatted(tableName));
        }
        return tableDeclaration;
    }

    private List<String> getOuterColumnNames(List<Expression> expressions) {
        return expressions.stream()
                .map(this::getOuterColumnName)
                .toList();
    }

    private String getOuterColumnName(Expression expression) {
        return expressionToOuterColumnNames.get(expression);
    }

    private void resolveExpressionsToOuterColumnNames(List<Expression> expressions) {
        for (Expression expression : expressions) {
            if (expression instanceof Alias alias) {
                expressionToOuterColumnNames.put(alias, alias.getName());
                expressionToOuterColumnNames.put(alias.getExpression(), alias.getName());
            } else if (expression instanceof Column column) {
                var aliasedExpression = aliasToExpression.get(column.getName());
                if (aliasedExpression instanceof GroupFunctionCall) {
                    expressionToOuterColumnNames.put(expression, getNewTemporaryName(TEMPORAL_COLUMN_NAME));
                } else {
                    expressionToOuterColumnNames.put(expression, column.getName());
                }
            } else if (expression instanceof AggregationFunctionCall) {
                expressionToOuterColumnNames.put(expression, getNewTemporaryName(TEMPORAL_COLUMN_NAME));
            } else if (expression instanceof GroupFunctionCall) {
                expressionToOuterColumnNames.put(expression, getNewTemporaryName(TEMPORAL_COLUMN_NAME));
            } else {
                throw new NotImplementedException("Unsupported expression type");
            }
        }
    }

    private void resolveAliases(List<Expression> expressions) {
        for (Expression expression : expressions) {
            if (expression instanceof Alias alias) {
                aliasToExpression.put(alias.getName(), alias.getExpression());
            }
        }
    }

    private String getNewTemporaryName(String prefix) {
        return temporalNameGenerator.generateNewName(prefix);
    }
}
