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
import com.epam.aidial.metric.service.RangeFilterUtils;
import com.epam.aidial.metric.util.CollectorsUtils;
import com.epam.aidial.ql.common.model.enums.SortDirection;
import com.epam.aidial.ql.model.Filter;
import com.epam.aidial.ql.model.From;
import com.epam.aidial.ql.model.Query;
import com.epam.aidial.ql.model.Sort;
import com.epam.aidial.ql.model.Table;
import com.epam.aidial.ql.model.filters.And;
import com.epam.aidial.ql.model.filters.BinaryComparisonFilter;
import com.epam.aidial.ql.model.filters.Not;
import com.epam.aidial.ql.model.filters.Or;
import com.epam.aidial.ql.model.filters.UnaryComparisonFilter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.NotImplementedException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
        var keepPart = createKeepPart(query.getFrom(), query.getExpressions());
        var renamePart = createRenamePart(query.getFrom(), query.getExpressions());
        var ungroupPart = SimpleFluxBuilder.createUngroupPart();
        var sortPart = buildSortPart(query.getOrderBy());
        var limitPart = SimpleFluxBuilder.createLimitPart(query, datasetConfiguration.getDefaultPageSize());

        // When the WHERE clause references only tags, apply it before fieldsAsCols() so the
        // tag predicates get pushed down to the InfluxDB storage engine (TSI index) and fewer
        // rows enter the expensive pivot. Otherwise keep the filter after the pivot so that
        // field-based comparisons see the pivoted column values.
        var tagOnlyFilter = filterReferencesOnlyTags(tableDeclaration, query.getWhere());
        var preFilterPart = tagOnlyFilter ? FluxConditionPartBuilder.createFilterPart(query.getWhere()) : FluxQueryPart.of();
        var postFilterPart = tagOnlyFilter ? FluxQueryPart.of() : FluxConditionPartBuilder.createFilterPart(query.getWhere());

        var queryPartsCombined = new InfluxQueryPartCombiner()
                .add(fromPart)
                .add(rangePart)
                .add(measurementPart)
                .add(preFilterPart)
                .add(fieldsAsColsPart)
                .add(postFilterPart)
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
        var sortPart = buildSortPart(query.getOrderBy());
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
        var sortPart = buildSortPart(query.getOrderBy());
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
            var fillNullJoinKeys = aggregationFunctionCalls.size() > 1 && !groupByColumnNames.isEmpty();
            aggregations = aggregationFunctionCalls.stream()
                    .map(f -> buildSimpleAggregationQueryForTempTable(
                            tempTableName,
                            columnName,
                            expressionToOuterColumnNames.get(f),
                            query.getWhere(),
                            finalGroupByColumnNames,
                            f,
                            regexCounter,
                            fillNullJoinKeys))
                    .toList();
        } else {
            var table = getTable(query);
            var finalGroupByColumnNames = groupByColumnNames;
            var fillNullJoinKeys = aggregationFunctionCalls.size() > 1 && !groupByColumnNames.isEmpty();
            aggregations = aggregationFunctionCalls.stream()
                    .map(f -> buildSimpleAggregationQuery(
                            table.getName(),
                            expressionToOuterColumnNames.get(f),
                            query.getWhere(),
                            finalGroupByColumnNames,
                            f,
                            regexCounter,
                            fillNullJoinKeys))
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
        combiner.add(buildSortPart(query.getOrderBy()));
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
        var sortPart = buildSortPart(query.getOrderBy());
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

        var fillNullJoinKeys = aggregationFunctionCalls.size() > 1 && !groupByColumnNames.isEmpty();
        List<FluxQueryPart> aggregations = aggregationFunctionCalls.stream()
                .map(aggCall -> buildWindowColumnAggregationPart(
                        table.getName(),
                        windowOuterName,
                        expressionToOuterColumnNames.get(aggCall),
                        query.getWhere(),
                        groupByColumnNames,
                        groupFunctionCall,
                        aggCall,
                        regexCounter,
                        fillNullJoinKeys))
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
        combiner.add(buildSortPart(query.getOrderBy()));
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

    private String buildSortPart(List<Sort> orderBy) {
        if (CollectionUtils.isEmpty(orderBy)) {
            return "";
        }

        var direction = orderBy.stream().map(Sort::getDirection).distinct()
                .collect(CollectorsUtils.toSingleton(() -> new IllegalArgumentException("Only one sort direction is allowed")))
                .orElseThrow();

        var columnNames = orderBy.stream()
                .map(sort -> resolveOrderByColumnName(sort.getExpression()))
                .toList();

        var noMapping = columnNames.stream().anyMatch(Objects::isNull);
        if (noMapping) {
            throw new NotImplementedException("Only sort for specified columns is supported");
        }

        var desc = direction == SortDirection.DESC;
        return SimpleFluxBuilder.createSortPart(columnNames, desc);
    }

    private FluxQueryPart buildWindowColumnAggregationPart(
            String tableName,
            String windowOuterName,
            String aggOuterName,
            Filter filter,
            List<String> groupByColumnNames,
            com.epam.aidial.expressions.GroupFunctionCall groupFunctionCall,
            AggregationFunctionCall aggregationFunctionCall,
            AtomicInteger regexCounter,
            boolean fillNullJoinKeys
    ) {
        var function = (FunctionImpl) aggregationFunctionCall.getFunction();
        var args = aggregationFunctionCall.getArgs();

        var tableDeclaration = getTableDeclaration(tableName);
        var fromPart = SimpleFluxBuilder.createFromPart(tableDeclaration.getSource().getBucket());
        var rangePart = FluxConditionPartBuilder.createRangePart(filter, true);
        var measurementPart = FluxConditionPartBuilder.createFilterPart(MEASUREMENT_COLUMN, tableDeclaration.getSource().getMeasurement());
        var filterPart = FluxConditionPartBuilder.createFilterPart(filter, regexCounter);
        var aggregationFilterPart = createAggregationFilterPart(tableDeclaration, function, args);
        var nullFillPart = fillNullJoinKeys
                ? SimpleFluxBuilder.createNullFillPart(groupByColumnNames)
                : "";
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
                .add(nullFillPart)
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
            AtomicInteger regexCounter,
            boolean fillNullJoinKeys
    ) {
        var function = (FunctionImpl) aggregationFunctionCall.getFunction();
        var keepColumns = new ArrayList<>(groupByColumnNames);
        keepColumns.add(columnName);

        var rangePart = FluxConditionPartBuilder.createRangePart(filter, false);
        var filterPart = FluxConditionPartBuilder.createFilterPart(filter, regexCounter);
        var nullFillPart = fillNullJoinKeys
                ? SimpleFluxBuilder.createNullFillPart(groupByColumnNames)
                : "";
        var groupPart = SimpleFluxBuilder.createGroupPart(groupByColumnNames);
        var aggregationPart = createAggregationPart(function, columnName);
        var keepPart = SimpleFluxBuilder.createKeepPart(keepColumns);
        var renamePart = SimpleFluxBuilder.createRenamePart(Map.of(columnName, fieldName));

        return new InfluxQueryPartCombiner()
                .add(tempTableName)
                .add(rangePart)
                .add(filterPart)
                .add(nullFillPart)
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
            AtomicInteger regexCounter,
            boolean fillNullJoinKeys
    ) {
        var function = (FunctionImpl) aggregationFunctionCall.getFunction();
        var args = aggregationFunctionCall.getArgs();
        var tableDeclaration = getTableDeclaration(tableName);

        if (canUseFieldFilterFastPath(tableDeclaration, filter, groupByColumnNames, function, args)) {
            return buildFieldFilterAggregationQuery(
                    tableDeclaration, columnName, filter, groupByColumnNames,
                    function, args, regexCounter, fillNullJoinKeys);
        }

        return buildFieldsAsColsAggregationQuery(
                tableDeclaration, columnName, filter, groupByColumnNames,
                function, args, regexCounter, fillNullJoinKeys);
    }

    /**
     * Lean aggregation path that skips {@code schema.fieldsAsCols()} entirely.
     *
     * <p>For {@code count()} and {@code sum(field)} where grouping and filtering only reference
     * tags, we can push {@code filter(_field == "...")} down to the storage engine and aggregate
     * directly on {@code _value}. This avoids the expensive pivot that materialises every field
     * column on every row — the dominant cost when there are many fields on the measurement or
     * many aggregations joined together.
     */
    private FluxQueryPart buildFieldFilterAggregationQuery(
            InfluxTableDeclaration tableDeclaration,
            String columnName,
            Filter filter,
            List<String> groupByColumnNames,
            FunctionImpl function,
            List<Expression> args,
            AtomicInteger regexCounter,
            boolean fillNullJoinKeys
    ) {
        var fieldForAgg = resolveFieldForAggregation(tableDeclaration, function, args);
        var effectiveAggFunction = isCaseWhenSum(function, args)
                ? new FunctionImpl("count", function.getType(), function.isDeterministic())
                : function;

        var keepColumns = new ArrayList<>(groupByColumnNames);
        keepColumns.add(VALUE_COLUMN);

        var fromPart = SimpleFluxBuilder.createFromPart(tableDeclaration.getSource().getBucket());
        var rangePart = FluxConditionPartBuilder.createRangePart(filter, true);
        var measurementPart = FluxConditionPartBuilder.createFilterPart(MEASUREMENT_COLUMN, tableDeclaration.getSource().getMeasurement());
        var fieldFilterPart = FluxConditionPartBuilder.createFilterPart(FIELD_COLUMN, fieldForAgg);
        var filterPart = FluxConditionPartBuilder.createFilterPart(filter, regexCounter);
        var caseWhenFilterPart = isCaseWhenSum(function, args)
                ? createCaseWhenFilterPartOnValue((CaseWhenExpression) args.get(0), fieldForAgg)
                : "";
        var nullFillPart = fillNullJoinKeys
                ? SimpleFluxBuilder.createNullFillPart(groupByColumnNames)
                : "";
        var groupPart = SimpleFluxBuilder.createGroupPart(groupByColumnNames);
        var aggregationPart = createAggregationPart(effectiveAggFunction, VALUE_COLUMN);
        var keepPart = SimpleFluxBuilder.createKeepPart(keepColumns);
        var renamePart = SimpleFluxBuilder.createRenamePart(Map.of(VALUE_COLUMN, columnName));

        var combiner = new InfluxQueryPartCombiner()
                .add(fromPart)
                .add(rangePart)
                .add(measurementPart)
                .add(fieldFilterPart)
                .add(filterPart);
        if (!caseWhenFilterPart.isEmpty()) {
            combiner.add(caseWhenFilterPart);
        }
        return combiner
                .add(nullFillPart)
                .add(groupPart)
                .add(aggregationPart)
                .add(keepPart)
                .add(renamePart)
                .build();
    }

    /**
     * Legacy aggregation path using {@code schema.fieldsAsCols()}. Retained as a fallback for
     * queries that filter or group on field columns (which need the pivot so the field appears
     * as a row column).
     */
    private FluxQueryPart buildFieldsAsColsAggregationQuery(
            InfluxTableDeclaration tableDeclaration,
            String columnName,
            Filter filter,
            List<String> groupByColumnNames,
            FunctionImpl function,
            List<Expression> args,
            AtomicInteger regexCounter,
            boolean fillNullJoinKeys
    ) {
        var aggregationColumnName = resolveAggregationColumnName(function, args);

        var isCaseWhenSum = isCaseWhenSum(function, args);
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
        var nullFillPart = fillNullJoinKeys
                ? SimpleFluxBuilder.createNullFillPart(groupByColumnNames)
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
                .add(nullFillPart)
                .add(groupPart)
                .add(aggregationPart)
                .add(keepPart)
                .add(renamePart)
                .build();
    }

    private static boolean isCaseWhenSum(FunctionImpl function, List<Expression> args) {
        return "sum".equals(function.getName())
                && args.size() == 1 && args.get(0) instanceof CaseWhenExpression;
    }

    /**
     * Fast-path eligibility check. The field-filter pipeline can serve a query only when
     * {@code schema.fieldsAsCols()} is not actually needed — i.e. all group-by and WHERE
     * references are tags. Case-when is allowed only when the condition references the
     * same column being summed (so the comparison can be rewritten against {@code _value}).
     */
    private boolean canUseFieldFilterFastPath(
            InfluxTableDeclaration tableDeclaration,
            Filter filter,
            List<String> groupByColumnNames,
            FunctionImpl function,
            List<Expression> args
    ) {
        for (var groupColumn : groupByColumnNames) {
            if (!isTagColumn(tableDeclaration, groupColumn)) {
                return false;
            }
        }
        if (!filterReferencesOnlyTags(tableDeclaration, filter)) {
            return false;
        }
        if (isCaseWhenSum(function, args)) {
            return isCaseWhenAgainstAggregationField(tableDeclaration, function, args);
        }
        return true;
    }

    private boolean isCaseWhenAgainstAggregationField(
            InfluxTableDeclaration tableDeclaration,
            FunctionImpl function,
            List<Expression> args
    ) {
        var caseWhen = (CaseWhenExpression) args.get(0);
        var condition = caseWhen.getCondition();
        if (!(condition instanceof FunctionCall fc) || fc.getArgs().size() != 2) {
            return false;
        }
        if (!(fc.getArgs().get(0) instanceof Column col)) {
            return false;
        }
        if (!(fc.getArgs().get(1) instanceof Constant)) {
            return false;
        }
        // For sum(case when col op const then 1 else 0), the field we filter on at the storage
        // layer is the *first* field of the table (same rule as count()), so the case-when
        // column must equal that first field; otherwise it's not in the row after _field filter.
        var firstFieldName = firstFieldName(tableDeclaration);
        return firstFieldName.equals(col.getName()) && isFieldColumn(tableDeclaration, col.getName());
    }

    private boolean filterReferencesOnlyTags(InfluxTableDeclaration tableDeclaration, Filter filter) {
        if (filter == null) {
            return true;
        }
        if (filter instanceof And and) {
            return and.getFilters().stream().allMatch(f -> filterReferencesOnlyTags(tableDeclaration, f));
        }
        if (filter instanceof Or or) {
            return or.getFilters().stream().allMatch(f -> filterReferencesOnlyTags(tableDeclaration, f));
        }
        if (filter instanceof Not) {
            // NOT is not supported today by the builder anyway; be conservative.
            return false;
        }
        if (filter instanceof BinaryComparisonFilter bcf) {
            if (bcf.getLeftExpression() instanceof Column col) {
                return isSafeFilterColumn(tableDeclaration, col.getName());
            }
            if (bcf.getRightExpression() instanceof Column col) {
                return isSafeFilterColumn(tableDeclaration, col.getName());
            }
            return false;
        }
        if (filter instanceof UnaryComparisonFilter ucf) {
            if (ucf.getExpression() instanceof Column col) {
                return isSafeFilterColumn(tableDeclaration, col.getName());
            }
            return false;
        }
        return false;
    }

    private boolean isSafeFilterColumn(InfluxTableDeclaration tableDeclaration, String columnName) {
        // The time range filter is always pushed to range() and is always safe. Tag comparisons
        // run before fieldsAsCols in Flux, so they are also safe without the pivot.
        return RangeFilterUtils.TIME_COLUMN.equals(columnName) || isTagColumn(tableDeclaration, columnName);
    }

    private boolean isTagColumn(InfluxTableDeclaration tableDeclaration, String columnName) {
        return tableDeclaration.getSchema().getColumns().stream()
                .filter(c -> c.getName().equals(columnName))
                .map(InfluxColumnDeclaration::getSource)
                .filter(Objects::nonNull)
                .anyMatch(source -> source.getType() == InfluxColumnSourceType.TAG);
    }

    private boolean isFieldColumn(InfluxTableDeclaration tableDeclaration, String columnName) {
        return tableDeclaration.getSchema().getColumns().stream()
                .filter(c -> c.getName().equals(columnName))
                .map(InfluxColumnDeclaration::getSource)
                .filter(Objects::nonNull)
                .anyMatch(source -> source.getType() == InfluxColumnSourceType.FIELD);
    }

    private String firstFieldName(InfluxTableDeclaration tableDeclaration) {
        return tableDeclaration.getSchema().getColumns().stream()
                .map(InfluxColumnDeclaration::getSource)
                .filter(columnSource -> columnSource.getType() == InfluxColumnSourceType.FIELD)
                .map(InfluxColumnSource::getColumn)
                .findFirst()
                .orElseThrow();
    }

    private String resolveFieldForAggregation(
            InfluxTableDeclaration tableDeclaration,
            FunctionImpl function,
            List<Expression> args
    ) {
        if ("count".equals(function.getName()) && args.isEmpty()) {
            return firstFieldName(tableDeclaration);
        }
        if ("sum".equals(function.getName()) && args.size() == 1) {
            if (args.get(0) instanceof CaseWhenExpression) {
                return firstFieldName(tableDeclaration);
            }
            return ((ColumnImpl) args.get(0)).getName();
        }
        throw new NotImplementedException("Unsupported aggregation function");
    }

    /**
     * Case-when filter for the field-filter fast path: after {@code filter(_field == ...)},
     * every row carries the aggregation field's value in {@code _value}, so the comparison
     * targets {@code r._value} directly instead of {@code r[col]}.
     */
    private String createCaseWhenFilterPartOnValue(CaseWhenExpression caseWhen, String fieldForAgg) {
        var condition = caseWhen.getCondition();
        if (condition instanceof FunctionCall fc && fc.getArgs().size() == 2
                && fc.getArgs().get(0) instanceof Column col
                && col.getName().equals(fieldForAgg)
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
                    SimpleFluxBuilder.quote(VALUE_COLUMN),
                    operator,
                    SimpleFluxBuilder.quote(String.valueOf(val.getValue())));
        }
        throw new NotImplementedException("Unsupported CASE WHEN condition for Flux: " + condition);
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
