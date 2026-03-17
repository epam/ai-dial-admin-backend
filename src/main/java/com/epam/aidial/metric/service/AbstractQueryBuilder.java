package com.epam.aidial.metric.service;

import com.epam.aidial.expressions.AggregationFunctionCall;
import com.epam.aidial.expressions.Alias;
import com.epam.aidial.expressions.Column;
import com.epam.aidial.expressions.Expression;
import com.epam.aidial.expressions.GroupFunctionCall;
import com.epam.aidial.expressions.impl.ColumnImpl;
import com.epam.aidial.metric.component.TemporalNameGenerator;
import com.epam.aidial.metric.config.AbstractDatasetConfiguration;
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

public abstract class AbstractQueryBuilder<C> {

    protected static final String TEMPORAL_COLUMN_NAME = "temp_column_";

    protected final Map<String, ? extends TableDeclaration> tableDeclarations;
    protected final AbstractDatasetConfiguration datasetConfiguration;
    protected final Map<Expression, String> expressionToOuterColumnNames = new HashMap<>();
    protected final Map<String, Expression> aliasToExpression = new HashMap<>();
    protected final TemporalNameGenerator temporalNameGenerator;

    protected AbstractQueryBuilder(DatasetDeclaration datasetDeclaration,
                                   AbstractDatasetConfiguration datasetConfiguration,
                                   TemporalNameGenerator temporalNameGenerator) {
        this.tableDeclarations = datasetDeclaration.getTables().stream()
                .collect(Collectors.toMap(TableDeclaration::getName, table -> table));
        this.datasetConfiguration = datasetConfiguration;
        this.temporalNameGenerator = temporalNameGenerator;
    }

    public C buildQueryContext(Completable completable) {
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

    protected abstract C buildSimpleQuery(Query query);

    protected abstract C buildDistinctQuery(Query query);

    protected abstract C buildAggregationQuery(Query query);

    protected abstract C buildWindowAggregationQuery(Query query);

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
            var originalExpression = aliasToExpression.get(column.getName());
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
            var aliasedExpression = aliasToExpression.get(column.getName());
            if (aliasedExpression instanceof GroupFunctionCall groupFunctionCall) {
                return groupFunctionCall;
            }
        }
        return expression;
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

    @SuppressWarnings("unchecked")
    protected <T extends TableDeclaration> T getTableDeclaration(String tableName) {
        var tableDeclaration = tableDeclarations.get(tableName);
        if (tableDeclaration == null) {
            throw new IllegalArgumentException("Table %s not found".formatted(tableName));
        }
        return (T) tableDeclaration;
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

    protected void resolveAliases(List<Expression> expressions) {
        for (Expression expression : expressions) {
            if (expression instanceof Alias alias) {
                aliasToExpression.put(alias.getName(), alias.getExpression());
            }
        }
    }

    protected String getNewTemporaryName(String prefix) {
        return temporalNameGenerator.generateNewName(prefix);
    }
}
