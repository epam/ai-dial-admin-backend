package com.epam.aidial.metric.service;

import com.epam.aidial.expressions.AggregationFunctionCall;
import com.epam.aidial.expressions.Alias;
import com.epam.aidial.expressions.Column;
import com.epam.aidial.expressions.Expression;
import com.epam.aidial.expressions.GroupFunctionCall;
import com.epam.aidial.expressions.impl.ColumnImpl;
import com.epam.aidial.metric.component.TemporalNameGenerator;
import com.epam.aidial.metric.model.configuration.DatasetDeclaration;
import com.epam.aidial.metric.model.configuration.TableDeclaration;
import com.epam.aidial.metric.util.CollectorsUtils;
import com.epam.aidial.ql.model.Completable;
import com.epam.aidial.ql.model.Query;
import com.epam.aidial.ql.model.Table;
import org.apache.commons.lang3.NotImplementedException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class AbstractQueryBuilder<C, T extends TableDeclaration> {

    protected static final String TEMPORAL_COLUMN_NAME = "temp_column_";

    protected final Map<String, T> tableDeclarations;
    protected final Map<Expression, String> expressionToOuterColumnNames = new HashMap<>();
    protected final Map<String, Expression> nameToExpression = new HashMap<>();
    protected final TemporalNameGenerator temporalNameGenerator;

    @SuppressWarnings("unchecked")
    protected AbstractQueryBuilder(DatasetDeclaration datasetDeclaration,
                                   TemporalNameGenerator temporalNameGenerator) {
        this.tableDeclarations = (Map<String, T>) datasetDeclaration.getTables().stream()
                .collect(Collectors.toMap(TableDeclaration::getName, table -> table));
        this.temporalNameGenerator = temporalNameGenerator;
    }

    public C buildQueryContext(Completable completable) {
        if (completable instanceof Query query) {

            resolveExpressionsToOuterColumnNames(query.getExpressions());

            if (isWindowColumnAggregationQuery(query)) {
                return buildWindowColumnAggregationQuery(query);
            } else if (isWindowAggregationQuery(query)) {
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

    protected abstract C buildSimpleQuery(Query query);

    protected abstract C buildDistinctQuery(Query query);

    protected abstract C buildAggregationQuery(Query query);

    protected abstract C buildWindowAggregationQuery(Query query);

    protected abstract C buildWindowColumnAggregationQuery(Query query);

    protected boolean isWindowColumnAggregationQuery(Query query) {
        boolean hasWindow = query.getGroupBy().stream().anyMatch(this::isWindowFunctionCall);
        boolean hasColumn = query.getGroupBy().stream().anyMatch(e -> !isWindowFunctionCall(e));
        return hasWindow && hasColumn;
    }

    protected boolean isWindowAggregationQuery(Query query) {
        return query.getGroupBy().stream().anyMatch(this::isWindowFunctionCall);
    }

    protected boolean isAggregationQuery(Query query) {
        return query.getExpressions().stream().anyMatch(this::isAggregationFunctionCall);
    }

    protected boolean isSimpleQuery(Query query) {
        return query.getExpressions().stream().anyMatch(this::isColumn) && !query.isDistinct();
    }

    protected boolean isDistinctQuery(Query query) {
        return query.isDistinct();
    }

    protected boolean isWindowFunctionCall(Expression expression) {
        if (expression instanceof GroupFunctionCall) {
            return true;
        }
        if (expression instanceof Column column) {
            var originalExpression = nameToExpression.get(column.getName());
            return originalExpression instanceof GroupFunctionCall;
        }
        return false;
    }

    protected boolean isAggregationFunctionCall(Expression expression) {
        if (expression instanceof AggregationFunctionCall) {
            return true;
        }
        return expression instanceof Alias alias && alias.getExpression() instanceof AggregationFunctionCall;
    }

    protected boolean isColumn(Expression expression) {
        if (expression instanceof ColumnImpl) {
            return true;
        }
        return expression instanceof Alias alias && alias.getExpression() instanceof Column;
    }

    protected GroupFunctionCall resolveGroupFunctionCall(List<Expression> expressions) {
        if (expressions.size() != 1) {
            throw new IllegalArgumentException("Only window function allowed for window aggregation");
        }
        var resolvedExpression = resolveAlias(expressions.get(0));
        if (resolvedExpression instanceof GroupFunctionCall groupFunctionCall) {
            return groupFunctionCall;
        }
        throw new IllegalStateException("Window function required for window aggregation");
    }

    protected AggregationFunctionCall resolveAggregationFunctionCall(List<Expression> expressions) {
        return expressions.stream()
                .map(this::resolveAlias)
                .filter(AggregationFunctionCall.class::isInstance)
                .map(AggregationFunctionCall.class::cast)
                .collect(CollectorsUtils.toSingleton(() -> new IllegalArgumentException("Only one aggregation expression allowed")))
                .orElseThrow(() -> new IllegalArgumentException("Aggregation expression must be passed"));
    }

    protected Expression resolveAlias(Expression expression) {
        if (expression instanceof Alias alias) {
            return alias.getExpression();
        } else if (expression instanceof Column column) {
            var aliasedExpression = nameToExpression.get(column.getName());
            if (aliasedExpression instanceof GroupFunctionCall groupFunctionCall) {
                return groupFunctionCall;
            }
        }
        return expression;
    }

    protected GroupFunctionCall extractWindowFunction(List<Expression> groupBy) {
        return groupBy.stream()
                .map(this::resolveAlias)
                .filter(GroupFunctionCall.class::isInstance)
                .map(GroupFunctionCall.class::cast)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Window function required in group by"));
    }

    protected List<String> extractGroupByColumnNames(List<Expression> groupBy) {
        return groupBy.stream()
                .filter(e -> !isWindowFunctionCall(e))
                .filter(Column.class::isInstance)
                .map(Column.class::cast)
                .map(Column::getName)
                .toList();
    }

    protected List<AggregationFunctionCall> resolveAggregationFunctionCalls(List<Expression> expressions) {
        return expressions.stream()
                .map(this::resolveAlias)
                .filter(AggregationFunctionCall.class::isInstance)
                .map(AggregationFunctionCall.class::cast)
                .toList();
    }

    protected List<Column> getGroupByColumns(List<Expression> groupBy) {
        var groupColumns = groupBy.stream()
                .filter(Column.class::isInstance)
                .map(Column.class::cast)
                .toList();

        if (groupBy.size() != groupColumns.size()) {
            throw new IllegalArgumentException("Invalid group by columns");
        }
        return groupColumns;
    }

    protected Table getTable(Query query) {
        if (!(query.getFrom() instanceof Table table)) {
            throw new IllegalArgumentException("Only from table sources are supported");
        }
        return table;
    }

    protected Column getDistinctColumn(Query query) {
        if (query.getExpressions().size() != 1) {
            throw new NotImplementedException("Only one distinct expression is supported");
        }
        var expression = query.getExpressions().get(0);
        if (!(expression instanceof Column column)) {
            throw new IllegalArgumentException("Only distinct columns are supported");
        }
        return column;
    }

    protected T getTableDeclaration(String tableName) {
        var tableDeclaration = tableDeclarations.get(tableName);
        if (tableDeclaration == null) {
            throw new IllegalArgumentException("Table %s not found".formatted(tableName));
        }
        return tableDeclaration;
    }

    protected List<String> getOuterColumnNames(List<Expression> expressions) {
        return expressions.stream()
                .map(this::getOuterColumnName)
                .toList();
    }

    protected String getOuterColumnName(Expression expression) {
        return expressionToOuterColumnNames.get(expression);
    }

    protected void resolveExpressionsToOuterColumnNames(List<Expression> expressions) {
        for (Expression expression : expressions) {
            if (expression instanceof Alias alias) {
                expressionToOuterColumnNames.put(alias, alias.getName());
                expressionToOuterColumnNames.put(alias.getExpression(), alias.getName());
                nameToExpression.put(alias.getName(), alias.getExpression());
            } else if (expression instanceof Column column) {
                var aliasedExpression = nameToExpression.get(column.getName());
                if (aliasedExpression instanceof GroupFunctionCall) {
                    expressionToOuterColumnNames.put(expression, getNewTemporaryName(TEMPORAL_COLUMN_NAME));
                } else {
                    expressionToOuterColumnNames.put(expression, column.getName());
                }
                nameToExpression.put(column.getName(), expression);
            } else if (expression instanceof AggregationFunctionCall) {
                expressionToOuterColumnNames.put(expression, getNewTemporaryName(TEMPORAL_COLUMN_NAME));
            } else if (expression instanceof GroupFunctionCall) {
                expressionToOuterColumnNames.put(expression, getNewTemporaryName(TEMPORAL_COLUMN_NAME));
            } else {
                throw new NotImplementedException("Unsupported expression type");
            }
        }
    }

    /**
     * Translates a groupBy column name to the underlying source column name. When the
     * user writes `groupBy: ["proj"]` referencing an alias `"project_id as proj"` in
     * `expressions`, the parser yields a Column("proj"), but the storage column is
     * actually "project_id". `nameToExpression["proj"]` was set by
     * {@link #resolveExpressionsToOuterColumnNames} to the aliased Column, so this
     * unwinds the alias. For non-aliased names it is a no-op.
     */
    protected String resolveGroupBySourceName(String name) {
        var expression = nameToExpression.get(name);
        if (expression instanceof Column column) {
            return column.getName();
        }
        return name;
    }

    /**
     * Resolves a sort/orderBy expression to its outer column name.
     * First tries direct object lookup, then resolves by name through nameToExpression.
     */
    protected String resolveOrderByColumnName(Expression expression) {
        var columnName = expressionToOuterColumnNames.get(expression);
        if (columnName != null) {
            return columnName;
        }
        if (expression instanceof Column column) {
            var resolved = nameToExpression.get(column.getName());
            if (resolved != null) {
                return expressionToOuterColumnNames.get(resolved);
            }
        }
        return null;
    }

    protected String getNewTemporaryName(String prefix) {
        return temporalNameGenerator.generateNewName(prefix);
    }
}
