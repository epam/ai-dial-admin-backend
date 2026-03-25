package com.epam.aidial.metric.service.influx;


import com.epam.aidial.expressions.AggregationFunctionCall;
import com.epam.aidial.expressions.Alias;
import com.epam.aidial.expressions.CaseWhenExpression;
import com.epam.aidial.expressions.Column;
import com.epam.aidial.expressions.Constant;
import com.epam.aidial.expressions.Expression;
import com.epam.aidial.expressions.FunctionCall;
import com.epam.aidial.expressions.impl.ColumnImpl;
import com.epam.aidial.expressions.impl.FunctionImpl;
import com.epam.aidial.metric.component.TemporalNameGenerator;
import com.epam.aidial.metric.config.InfluxDatasetConfiguration;
import com.epam.aidial.metric.model.configuration.influx.InfluxColumnDeclaration;
import com.epam.aidial.metric.model.configuration.influx.InfluxColumnSource;
import com.epam.aidial.metric.model.configuration.influx.InfluxColumnSourceType;
import com.epam.aidial.metric.model.configuration.influx.InfluxDatasetDeclaration;
import com.epam.aidial.metric.model.configuration.influx.InfluxTableDeclaration;
import com.epam.aidial.metric.model.influx.FluxQueryContext;
import com.epam.aidial.metric.model.influx.FluxQueryPart;
import com.epam.aidial.metric.service.AbstractQueryBuilder;
import com.epam.aidial.metric.util.CollectorsUtils;
import com.epam.aidial.ql.model.Filter;
import com.epam.aidial.ql.model.From;
import com.epam.aidial.ql.model.Query;
import com.epam.aidial.ql.model.Table;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static com.epam.aidial.metric.model.influx.FluxStandardColumns.FIELD_COLUMN;
import static com.epam.aidial.metric.model.influx.FluxStandardColumns.MEASUREMENT_COLUMN;
import static com.epam.aidial.metric.model.influx.FluxStandardColumns.TIME_COLUMN;
import static com.epam.aidial.metric.model.influx.FluxStandardColumns.VALUE_COLUMN;

@Slf4j
public class FluxQueryBuilder extends AbstractQueryBuilder<FluxQueryContext, InfluxTableDeclaration> {

    private static final String TEMPORAL_TABLE_NAME = "temp_table_";

    public FluxQueryBuilder(InfluxDatasetDeclaration datasetDeclaration,
                            InfluxDatasetConfiguration datasourceConfiguration,
                            TemporalNameGenerator temporalNameGenerator) {
        super(datasetDeclaration, datasourceConfiguration, temporalNameGenerator);
    }

    @Override
    protected FluxQueryContext buildSimpleQuery(Query query) {
        var table = getTable(query);
        var tableDeclaration = getTableDeclaration(table.getName());
        var fromPart = SimpleFluxBuilder.createFromPart(tableDeclaration.getSource().getBucket());
        var rangePart = FluxConditionPartBuilder.createRangePart(query.getWhere(), true);
        var measurementPart = FluxConditionPartBuilder.createFilterPart(MEASUREMENT_COLUMN, tableDeclaration.getSource().getMeasurement());
        var fieldsAsColsPart = SimpleFluxBuilder.createFieldsAsColsPart();
        var filterPart = FluxConditionPartBuilder.createFilterPart(query.getWhere());
        var keepPart = createKeepPart(query.getFrom(), query.getExpressions());
        var renamePart = createRenamePart(query.getFrom(), query.getExpressions());
        var ungroupPart = SimpleFluxBuilder.createUngroupPart();
        var sortPart = SimpleFluxBuilder.createSortPart(query.getOrderBy(), expressionToOuterColumnNames);
        var limitPart = SimpleFluxBuilder.createLimitPart(query, datasetConfiguration.getDefaultPageSize());

        var queryPartsCombined = new InfluxQueryPartCombiner()
                .add(fromPart)
                .add(rangePart)
                .add(measurementPart)
                .add(fieldsAsColsPart)
                .add(filterPart)
                .add(keepPart)
                .add(renamePart)
                .add(ungroupPart)
                .add(sortPart)
                .add(limitPart)
                .build();

        var columnNames = query.getExpressions().stream()
                .filter(Column.class::isInstance)
                .map(Column.class::cast)
                .map(Column::getName)
                .toList();

        return FluxQueryContext.builder()
                .imports(queryPartsCombined.getImports())
                .preamble(queryPartsCombined.getPreamble())
                .query(queryPartsCombined.getQuery())
                .columnNames(columnNames)
                .build();
    }

    @Override
    protected FluxQueryContext buildDistinctQuery(Query query) {
        var table = getTable(query);
        var column = getDistinctColumn(query);
        var columnSource = getSourceColumn(table, column);
        var outerColumnName = getOuterColumnName(column);

        return switch (columnSource.getType()) {
            case TAG -> buildDistinctQueryForTag(query, table, columnSource.getColumn(), outerColumnName);
            case FIELD -> buildDistinctQueryForField(query, table, columnSource.getColumn(), outerColumnName);
        };
    }

    private FluxQueryContext buildDistinctQueryForTag(Query query, Table table, String tagName, String outerColumnName) {
        var tableDeclaration = getTableDeclaration(table.getName());
        var fromPart = SimpleFluxBuilder.createFromPart(tableDeclaration.getSource().getBucket());
        var rangePart = FluxConditionPartBuilder.createRangePart(query.getWhere(), true);
        var measurementPart = FluxConditionPartBuilder.createFilterPart(MEASUREMENT_COLUMN, tableDeclaration.getSource().getMeasurement());
        var filterPart = FluxConditionPartBuilder.createFilterPart(query.getWhere());
        var keepPart = SimpleFluxBuilder.createKeepPart(List.of(tagName));
        var ungroupPart = SimpleFluxBuilder.createUngroupPart();
        var distinctPart = SimpleFluxBuilder.createDistinctPart(tagName);
        var renamePart = SimpleFluxBuilder.createRenamePart(Map.of(VALUE_COLUMN, outerColumnName));
        var sortPart = SimpleFluxBuilder.createSortPart(query.getOrderBy(), expressionToOuterColumnNames);
        var limitPart = SimpleFluxBuilder.createLimitPart(query, datasetConfiguration.getDefaultPageSize());

        var queryPartsCombined = new InfluxQueryPartCombiner()
                .add(fromPart)
                .add(rangePart)
                .add(measurementPart)
                .add(filterPart)
                .add(keepPart)
                .add(ungroupPart)
                .add(distinctPart)
                .add(sortPart)
                .add(renamePart)
                .add(limitPart)
                .build();

        return FluxQueryContext.builder()
                .imports(queryPartsCombined.getImports())
                .preamble(queryPartsCombined.getPreamble())
                .query(queryPartsCombined.getQuery())
                .columnNames(List.of(outerColumnName))
                .build();
    }

    private FluxQueryContext buildDistinctQueryForField(Query query, Table table, String fieldName, String outerColumnName) {
        var tableDeclaration = getTableDeclaration(table.getName());
        var fromPart = SimpleFluxBuilder.createFromPart(tableDeclaration.getSource().getBucket());
        var rangePart = FluxConditionPartBuilder.createRangePart(query.getWhere(), true);
        var measurementPart = FluxConditionPartBuilder.createFilterPart(MEASUREMENT_COLUMN, tableDeclaration.getSource().getMeasurement());
        var specificFieldPart = FluxConditionPartBuilder.createFilterPart(FIELD_COLUMN, fieldName);
        var filterPart = FluxConditionPartBuilder.createFilterPart(query.getWhere());
        var ungroupPart = SimpleFluxBuilder.createUngroupPart();
        var distinctPart = SimpleFluxBuilder.createDistinctPart(VALUE_COLUMN);
        var keepPart = SimpleFluxBuilder.createKeepPart(List.of(outerColumnName));
        var renamePart = SimpleFluxBuilder.createRenamePart(Map.of(VALUE_COLUMN, outerColumnName));
        var sortPart = SimpleFluxBuilder.createSortPart(query.getOrderBy(), expressionToOuterColumnNames);
        var limitPart = SimpleFluxBuilder.createLimitPart(query, datasetConfiguration.getDefaultPageSize());

        var queryPartsCombined = new InfluxQueryPartCombiner()
                .add(fromPart)
                .add(rangePart)
                .add(measurementPart)
                .add(specificFieldPart)
                .add(filterPart)
                .add(ungroupPart)
                .add(distinctPart)
                .add(sortPart)
                .add(limitPart)
                .add(renamePart)
                .add(keepPart)
                .build();

        return FluxQueryContext.builder()
                .imports(queryPartsCombined.getImports())
                .preamble(queryPartsCombined.getPreamble())
                .query(queryPartsCombined.getQuery())
                .columnNames(List.of(outerColumnName))
                .build();
    }

    @Override
    protected FluxQueryContext buildAggregationQuery(Query query) {
        var groupByColumnNames = getGroupByColumns(query.getGroupBy()).stream().map(Column::getName).toList();
        var aggregationFunctionCalls = query.getExpressions().stream()
                .map(this::resolveAlias)
                .filter(e -> e instanceof AggregationFunctionCall)
                .map(e -> (AggregationFunctionCall) e)
                .toList();

        var combiner = new InfluxQueryPartCombiner();
        var regexCounter = new AtomicInteger();
        List<FluxQueryPart> aggregations;
        if (query.getFrom() instanceof Query innerQuery) {
            var innerQueryContext = buildQueryContext(innerQuery);
            String tempTableName = getNewTemporaryName(TEMPORAL_TABLE_NAME);
            String columnName = innerQueryContext.getColumnNames().get(0);
            combiner.add(tempTableName + " = " + innerQueryContext.getQuery(), innerQueryContext.getImports(), innerQueryContext.getPreamble());

            var finalGroupByColumnNames = groupByColumnNames;
            aggregations = aggregationFunctionCalls.stream()
                    .map(f -> buildSimpleAggregationQueryForTempTable(
                            tempTableName,
                            columnName,
                            expressionToOuterColumnNames.get(f),
                            query.getWhere(),
                            finalGroupByColumnNames,
                            f,
                            regexCounter))
                    .toList();
        } else {
            var table = getTable(query);
            var finalGroupByColumnNames1 = groupByColumnNames;
            aggregations = aggregationFunctionCalls.stream()
                    .map(f -> buildSimpleAggregationQuery(
                            table.getName(),
                            expressionToOuterColumnNames.get(f),
                            query.getWhere(),
                            finalGroupByColumnNames1,
                            f,
                            regexCounter))
                    .toList();
        }

        if (aggregations.size() == 1) {
            combiner.add(aggregations.get(0));
        } else {

            if (groupByColumnNames.isEmpty()) {
                var groupByColumnName = getNewTemporaryName(TEMPORAL_COLUMN_NAME);
                groupByColumnNames = List.of(groupByColumnName);
                aggregations = aggregations.stream()
                        .map(aggregation -> new InfluxQueryPartCombiner()
                                .add(aggregation)
                                .add(SimpleFluxBuilder.createSetPart(groupByColumnName, "any"))
                                .build())
                        .toList();
            }

            var aggregationColumnsCombined = SimpleFluxBuilder.compactWithQuoting(groupByColumnNames);
            var tempTables = IntStream.range(0, aggregations.size())
                    .mapToObj(i -> getNewTemporaryName(TEMPORAL_TABLE_NAME)).toList();
            var tempJoinTables = IntStream.range(0, aggregations.size() - 1)
                    .mapToObj(i -> getNewTemporaryName(TEMPORAL_TABLE_NAME)).toList();

            for (int i = 0; i < aggregations.size(); i++) {
                var aggregation = aggregations.get(i);
                combiner.add(tempTables.get(i) + " = " + aggregation.getQuery(), aggregation.getImports(), aggregation.getPreamble());
            }

            combiner.add("%s = join(tables: {t1: %s, t2: %s}, on: %s)".formatted(
                    tempJoinTables.get(0), tempTables.get(0), tempTables.get(1), aggregationColumnsCombined
            ));

            for (int i = 2; i < tempTables.size(); i++) {
                combiner.add("%s = join(tables: {t1: %s, t2: %s}, on: %s)".formatted(
                        tempJoinTables.get(i - 1), tempJoinTables.get(i - 2), tempTables.get(i), aggregationColumnsCombined
                ));
            }

            combiner.add(tempJoinTables.get(tempJoinTables.size() - 1));
        }

        combiner.add(SimpleFluxBuilder.createUngroupPart());
        combiner.add(createRenamePart(query.getFrom(), query.getExpressions()));
        combiner.add(SimpleFluxBuilder.createSortPart(query.getOrderBy(), expressionToOuterColumnNames));
        combiner.add(SimpleFluxBuilder.createLimitPart(query, datasetConfiguration.getDefaultPageSize()));
        var combined = combiner.build();

        var columnNames = query.getExpressions().stream()
                .map(expressionToOuterColumnNames::get)
                .toList();
        return FluxQueryContext.builder()
                .imports(combined.getImports())
                .preamble(combined.getPreamble())
                .query(combined.getQuery())
                .columnNames(columnNames)
                .build();
    }

    @Override
    protected FluxQueryContext buildWindowAggregationQuery(Query query) {
        var groupFunctionCall = resolveGroupFunctionCall(query.getGroupBy());
        var aggregationFunctionCall = resolveAggregationFunctionCall(query.getExpressions());

        var table = getTable(query);

        var function = (FunctionImpl) aggregationFunctionCall.getFunction();
        var args = aggregationFunctionCall.getArgs();

        var tableDeclaration = getTableDeclaration(table.getName());
        var fromPart = SimpleFluxBuilder.createFromPart(tableDeclaration.getSource().getBucket());
        var rangePart = FluxConditionPartBuilder.createRangePart(query.getWhere(), true);
        var measurementPart = FluxConditionPartBuilder.createFilterPart(MEASUREMENT_COLUMN, tableDeclaration.getSource().getMeasurement());
        var filterPart = FluxConditionPartBuilder.createFilterPart(query.getWhere());
        var aggregationFilterPart = createAggregationFilterPart(tableDeclaration, function, args);
        var ungroupPart = SimpleFluxBuilder.createUngroupPart();
        var windowPart = SimpleFluxBuilder.createWindowFunctionPart(groupFunctionCall, aggregationFunctionCall);
        var renamePart = SimpleFluxBuilder.createRenamePart(Map.of(
                TIME_COLUMN, expressionToOuterColumnNames.get(groupFunctionCall),
                VALUE_COLUMN, expressionToOuterColumnNames.get(aggregationFunctionCall)
        ));
        var sortPart = SimpleFluxBuilder.createSortPart(query.getOrderBy(), expressionToOuterColumnNames);
        var limitPart = SimpleFluxBuilder.createLimitPart(query, datasetConfiguration.getDefaultPageSize());

        var queryPartsCombined = new InfluxQueryPartCombiner()
                .add(fromPart)
                .add(rangePart)
                .add(measurementPart)
                .add(filterPart)
                .add(aggregationFilterPart)
                .add(ungroupPart)
                .add(windowPart)
                .add(renamePart)
                .add(sortPart)
                .add(limitPart)
                .build();

        var columnNames = query.getExpressions().stream()
                .map(expressionToOuterColumnNames::get)
                .toList();
        return FluxQueryContext.builder()
                .imports(queryPartsCombined.getImports())
                .preamble(queryPartsCombined.getPreamble())
                .query(queryPartsCombined.getQuery())
                .columnNames(columnNames)
                .build();
    }

    @Override
    protected FluxQueryContext buildWindowColumnAggregationQuery(Query query) {
        var groupFunctionCall = extractWindowFunction(query.getGroupBy());
        var groupByColumnNames = extractGroupByColumnNames(query.getGroupBy());
        var aggregationFunctionCalls = resolveAggregationFunctionCalls(query.getExpressions());

        var table = getTable(query);
        var windowOuterName = expressionToOuterColumnNames.get(groupFunctionCall);

        var combiner = new InfluxQueryPartCombiner();
        var regexCounter = new AtomicInteger();

        List<FluxQueryPart> aggregations = aggregationFunctionCalls.stream()
                .map(aggCall -> buildWindowColumnAggregationPart(
                        table.getName(),
                        windowOuterName,
                        expressionToOuterColumnNames.get(aggCall),
                        query.getWhere(),
                        groupByColumnNames,
                        groupFunctionCall,
                        aggCall,
                        regexCounter))
                .toList();

        if (aggregations.isEmpty()) {
            throw new IllegalArgumentException("At least one aggregation expression is required");
        } else if (aggregations.size() == 1) {
            combiner.add(aggregations.get(0));
        } else {
            var joinColumns = new ArrayList<String>();
            joinColumns.add(windowOuterName);
            joinColumns.addAll(groupByColumnNames);
            var joinColumnsCombined = SimpleFluxBuilder.compactWithQuoting(joinColumns);

            var tempTables = IntStream.range(0, aggregations.size())
                    .mapToObj(i -> getNewTemporaryName(TEMPORAL_TABLE_NAME)).toList();
            var tempJoinTables = IntStream.range(0, aggregations.size() - 1)
                    .mapToObj(i -> getNewTemporaryName(TEMPORAL_TABLE_NAME)).toList();

            for (int i = 0; i < aggregations.size(); i++) {
                var aggregation = aggregations.get(i);
                combiner.add(tempTables.get(i) + " = " + aggregation.getQuery(), aggregation.getImports(), aggregation.getPreamble());
            }

            combiner.add("%s = join(tables: {t1: %s, t2: %s}, on: %s)".formatted(
                    tempJoinTables.get(0), tempTables.get(0), tempTables.get(1), joinColumnsCombined));

            for (int i = 2; i < tempTables.size(); i++) {
                combiner.add("%s = join(tables: {t1: %s, t2: %s}, on: %s)".formatted(
                        tempJoinTables.get(i - 1), tempJoinTables.get(i - 2), tempTables.get(i), joinColumnsCombined));
            }

            combiner.add(tempJoinTables.get(tempJoinTables.size() - 1));
        }

        combiner.add(SimpleFluxBuilder.createUngroupPart());
        combiner.add(SimpleFluxBuilder.createSortPart(query.getOrderBy(), expressionToOuterColumnNames));
        combiner.add(SimpleFluxBuilder.createLimitPart(query, datasetConfiguration.getDefaultPageSize()));
        var combined = combiner.build();

        var columnNames = query.getExpressions().stream()
                .map(expressionToOuterColumnNames::get)
                .toList();
        return FluxQueryContext.builder()
                .imports(combined.getImports())
                .preamble(combined.getPreamble())
                .query(combined.getQuery())
                .columnNames(columnNames)
                .build();
    }

    private FluxQueryPart buildWindowColumnAggregationPart(
            String tableName,
            String windowOuterName,
            String aggOuterName,
            Filter filter,
            List<String> groupByColumnNames,
            com.epam.aidial.expressions.GroupFunctionCall groupFunctionCall,
            AggregationFunctionCall aggregationFunctionCall,
            AtomicInteger regexCounter
    ) {
        var function = (FunctionImpl) aggregationFunctionCall.getFunction();
        var args = aggregationFunctionCall.getArgs();

        var tableDeclaration = getTableDeclaration(tableName);
        var fromPart = SimpleFluxBuilder.createFromPart(tableDeclaration.getSource().getBucket());
        var rangePart = FluxConditionPartBuilder.createRangePart(filter, true);
        var measurementPart = FluxConditionPartBuilder.createFilterPart(MEASUREMENT_COLUMN, tableDeclaration.getSource().getMeasurement());
        var filterPart = FluxConditionPartBuilder.createFilterPart(filter, regexCounter);
        var aggregationFilterPart = createAggregationFilterPart(tableDeclaration, function, args);
        var ungroupPart = SimpleFluxBuilder.createUngroupPart();
        var windowPart = SimpleFluxBuilder.createWindowPart(groupFunctionCall);

        // Group by window boundaries + column names
        var windowGroupColumns = new ArrayList<String>();
        windowGroupColumns.add("_start");
        windowGroupColumns.add("_stop");
        windowGroupColumns.addAll(groupByColumnNames);
        var windowGroupPart = SimpleFluxBuilder.createGroupPart(windowGroupColumns);

        // Aggregate on _value (the field value after field filter)
        var aggColumnName = VALUE_COLUMN;
        var aggregationPart = createAggregationPart(function, aggColumnName);

        // Keep window start + groupBy columns + aggregated value
        var keepColumns = new ArrayList<String>();
        keepColumns.add("_start");
        keepColumns.addAll(groupByColumnNames);
        keepColumns.add(aggColumnName);
        var keepPart = SimpleFluxBuilder.createKeepPart(keepColumns);

        // Rename _start → windowOuterName, _value → aggOuterName
        var renamePart = SimpleFluxBuilder.createRenamePart(Map.of(
                "_start", windowOuterName,
                aggColumnName, aggOuterName
        ));

        return new InfluxQueryPartCombiner()
                .add(fromPart)
                .add(rangePart)
                .add(measurementPart)
                .add(filterPart)
                .add(aggregationFilterPart)
                .add(ungroupPart)
                .add(windowPart)
                .add(windowGroupPart)
                .add(aggregationPart)
                .add(keepPart)
                .add(renamePart)
                .build();
    }

    private FluxQueryPart buildSimpleAggregationQueryForTempTable(
            String tempTableName,
            String columnName,
            String fieldName,
            Filter filter,
            List<String> groupByColumnNames,
            AggregationFunctionCall aggregationFunctionCall,
            AtomicInteger regexCounter
    ) {
        var function = (FunctionImpl) aggregationFunctionCall.getFunction();
        var keepColumns = new ArrayList<>(groupByColumnNames);
        keepColumns.add(columnName);

        var rangePart = FluxConditionPartBuilder.createRangePart(filter, false);
        var filterPart = FluxConditionPartBuilder.createFilterPart(filter, regexCounter);
        var groupPart = SimpleFluxBuilder.createGroupPart(groupByColumnNames);
        var aggregationPart = createAggregationPart(function, columnName);
        var keepPart = SimpleFluxBuilder.createKeepPart(keepColumns);
        var renamePart = SimpleFluxBuilder.createRenamePart(Map.of(columnName, fieldName));

        return new InfluxQueryPartCombiner()
                .add(tempTableName)
                .add(rangePart)
                .add(filterPart)
                .add(groupPart)
                .add(aggregationPart)
                .add(keepPart)
                .add(renamePart)
                .build();
    }

    private FluxQueryPart buildSimpleAggregationQuery(
            String tableName,
            String columnName,
            Filter filter,
            List<String> groupByColumnNames,
            AggregationFunctionCall aggregationFunctionCall,
            AtomicInteger regexCounter
    ) {
        var function = (FunctionImpl) aggregationFunctionCall.getFunction();
        var args = aggregationFunctionCall.getArgs();

        var tableDeclaration = getTableDeclaration(tableName);
        var aggregationColumnName = resolveAggregationColumnName(function, args);

        // For sum(case when cond then 1 else 0 end), use count after filtering by condition
        var isCaseWhenSum = "sum".equals(function.getName())
                && args.size() == 1 && args.get(0) instanceof CaseWhenExpression;
        var effectiveAggFunction = isCaseWhenSum
                ? new FunctionImpl("count", function.getType(), function.isDeterministic())
                : function;

        var keepColumns = new ArrayList<>(groupByColumnNames);
        keepColumns.add(aggregationColumnName);

        var fromPart = SimpleFluxBuilder.createFromPart(tableDeclaration.getSource().getBucket());
        var rangePart = FluxConditionPartBuilder.createRangePart(filter, true);
        var filterPart = FluxConditionPartBuilder.createFilterPart(filter, regexCounter);
        var measurementPart = FluxConditionPartBuilder.createFilterPart(MEASUREMENT_COLUMN, tableDeclaration.getSource().getMeasurement());
        var fieldsAsColsPart = SimpleFluxBuilder.createFieldsAsColsPart();
        var caseWhenFilterPart = isCaseWhenSum
                ? createCaseWhenFilterPart((CaseWhenExpression) args.get(0))
                : "";
        var groupPart = SimpleFluxBuilder.createGroupPart(groupByColumnNames);
        var aggregationPart = createAggregationPart(effectiveAggFunction, aggregationColumnName);
        var keepPart = SimpleFluxBuilder.createKeepPart(keepColumns);
        var renamePart = SimpleFluxBuilder.createRenamePart(Map.of(aggregationColumnName, columnName));

        var combiner = new InfluxQueryPartCombiner()
                .add(fromPart)
                .add(rangePart)
                .add(measurementPart)
                .add(fieldsAsColsPart)
                .add(filterPart);
        if (!caseWhenFilterPart.isEmpty()) {
            combiner.add(caseWhenFilterPart);
        }
        return combiner
                .add(groupPart)
                .add(aggregationPart)
                .add(keepPart)
                .add(renamePart)
                .build();
    }

    private String createCaseWhenFilterPart(CaseWhenExpression caseWhen) {
        var condition = caseWhen.getCondition();
        if (condition instanceof FunctionCall fc && fc.getArgs().size() == 2
                && fc.getArgs().get(0) instanceof Column col
                && fc.getArgs().get(1) instanceof Constant val) {
            var funcName = fc.getFunction().getName();
            var operator = switch (funcName) {
                case "equals" -> "==";
                case "notEquals" -> "!=";
                case "less" -> "<";
                case "greater" -> ">";
                case "lessOrEquals" -> "<=";
                case "greaterOrEquals" -> ">=";
                default -> throw new NotImplementedException("Unsupported CASE WHEN operator in Flux: " + funcName);
            };
            return "|> filter(fn: (r) => r[%s] %s %s)".formatted(
                    SimpleFluxBuilder.quote(col.getName()),
                    operator,
                    SimpleFluxBuilder.quote(String.valueOf(val.getValue())));
        }
        throw new NotImplementedException("Unsupported CASE WHEN condition for Flux: " + condition);
    }

    private FluxQueryPart createAggregationFilterPart(InfluxTableDeclaration tableDeclaration,
                                                      FunctionImpl function,
                                                      List<Expression> args) {
        if ("count".equals(function.getName()) && args.isEmpty()) {
            var tableFieldName = tableDeclaration.getSchema().getColumns().stream()
                    .map(InfluxColumnDeclaration::getSource)
                    .filter(columnSource -> columnSource.getType() == InfluxColumnSourceType.FIELD)
                    .map(InfluxColumnSource::getColumn)
                    .findFirst()
                    .orElseThrow();
            return FluxConditionPartBuilder.createFilterPart(FIELD_COLUMN, tableFieldName);
        }

        if ("sum".equals(function.getName()) && args.size() == 1) {
            if (args.get(0) instanceof CaseWhenExpression) {
                // sum(case when ... then 1 else 0 end) → use same field filter as count()
                var tableFieldName = tableDeclaration.getSchema().getColumns().stream()
                        .map(InfluxColumnDeclaration::getSource)
                        .filter(columnSource -> columnSource.getType() == InfluxColumnSourceType.FIELD)
                        .map(InfluxColumnSource::getColumn)
                        .findFirst()
                        .orElseThrow();
                return FluxConditionPartBuilder.createFilterPart(FIELD_COLUMN, tableFieldName);
            }
            var tableFieldName = ((ColumnImpl) args.get(0)).getName();
            return FluxConditionPartBuilder.createFilterPart(FIELD_COLUMN, tableFieldName);
        }

        throw new NotImplementedException("Unsupported aggregation function");
    }

    private String resolveAggregationColumnName(FunctionImpl function,
                                                List<Expression> args) {
        if ("count".equals(function.getName()) && args.isEmpty()) {
            // Use _measurement for count() because it is guaranteed to exist on every record
            // after fieldsAsCols(), is a string type (Flux cannot aggregate time-type columns),
            // and is never null. Other columns (tags or fields) may be absent
            // if no data in the queried time range contains them.
            return MEASUREMENT_COLUMN;
        }

        if ("sum".equals(function.getName()) && args.size() == 1) {
            if (args.get(0) instanceof CaseWhenExpression) {
                // sum(case when ... then 1 else 0 end) is conditional counting → use _measurement
                return MEASUREMENT_COLUMN;
            }
            return ((ColumnImpl) args.get(0)).getName();
        }

        throw new NotImplementedException("Unsupported aggregation function");
    }

    private String createAggregationPart(FunctionImpl function, String columnName) {
        return switch (function.getName()) {
            case "count" -> "|> count(column: %s)".formatted(SimpleFluxBuilder.quote(columnName));
            case "sum" -> "|> sum(column: %s)".formatted(SimpleFluxBuilder.quote(columnName));
            default -> throw new NotImplementedException("Unsupported aggregation function");
        };
    }

    private String createKeepPart(From from, List<Expression> expressions) {
        var sourceColumnNames = getSourceColumnNames(from, expressions);
        return SimpleFluxBuilder.createKeepPart(sourceColumnNames);
    }

    private String createRenamePart(From from, List<Expression> expressions) {
        var sourceColumnNames = getSourceColumnNames(from, expressions);
        var outerColumnNames = getOuterColumnNames(expressions);

        var mapping = new HashMap<String, String>();
        for (int i = 0; i < sourceColumnNames.size(); i++) {
            var sourceColumnName = sourceColumnNames.get(i);
            var outerColumnName = outerColumnNames.get(i);
            if (!sourceColumnName.equals(outerColumnName)) {
                mapping.put(sourceColumnNames.get(i), outerColumnNames.get(i));
            }
        }

        return SimpleFluxBuilder.createRenamePart(mapping);
    }

    private InfluxColumnDeclaration getColumnDeclaration(String tableName, String columnName) {
        var tableDeclaration = getTableDeclaration(tableName);

        return tableDeclaration.getSchema().getColumns().stream()
                .filter(columnDeclaration -> columnDeclaration.getName().equals(columnName))
                .collect(CollectorsUtils.toSingleton(() -> new IllegalArgumentException("Multiple columns found for name: %s".formatted(columnName))))
                .orElseThrow(() -> new IllegalArgumentException("No column is find for name: %s".formatted(columnName)));
    }

    private List<String> getSourceColumnNames(From from, List<Expression> expressions) {
        return getSourceColumns(from, expressions).stream()
                .map(InfluxColumnSource::getColumn)
                .toList();
    }

    private List<InfluxColumnSource> getSourceColumns(From from, List<Expression> expressions) {
        return expressions.stream()
                .map(expression -> getSourceColumn(from, expression))
                .toList();
    }

    private InfluxColumnSource getSourceColumn(From from, Expression expression) {
        if (from instanceof Table table) {
            if (expression instanceof Alias alias && alias.getExpression() instanceof Column column) {
                var columnDeclaration = getColumnDeclaration(table.getName(), column.getName());
                return columnDeclaration.getSource();
            } else if (expression instanceof ColumnImpl column) {
                var columnDeclaration = getColumnDeclaration(table.getName(), column.getName());
                return columnDeclaration.getSource();
            } else {
                var columnName = expressionToOuterColumnNames.get(expression);
                if (columnName == null) {
                    throw new IllegalArgumentException("Cannot extract column name from expression: %s".formatted(expression));
                }
                return new InfluxColumnSource(columnName, InfluxColumnSourceType.TAG);
            }
        } else if (from instanceof Query) {
            var columnName = expressionToOuterColumnNames.get(expression);
            if (columnName == null) {
                throw new IllegalArgumentException("Cannot extract column name from expression: %s".formatted(expression));
            }
            return new InfluxColumnSource(columnName, InfluxColumnSourceType.TAG);
        }

        throw new NotImplementedException("Cannot extract column name from expression: %s".formatted(expression));
    }

}
