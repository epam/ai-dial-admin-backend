package com.epam.aidial.metric.service.influx;


import com.epam.aidial.expressions.AggregationFunctionCall;
import com.epam.aidial.expressions.Alias;
import com.epam.aidial.expressions.CaseWhenExpression;
import com.epam.aidial.expressions.Column;
import com.epam.aidial.expressions.Constant;
import com.epam.aidial.expressions.Expression;
import com.epam.aidial.expressions.FunctionCall;
import com.epam.aidial.expressions.GroupFunctionCall;
import com.epam.aidial.expressions.enums.Type;
import com.epam.aidial.expressions.impl.ColumnImpl;
import com.epam.aidial.expressions.impl.FunctionImpl;
import com.epam.aidial.metric.component.TemporalNameGenerator;
import com.epam.aidial.metric.model.configuration.influx.InfluxColumnDeclaration;
import com.epam.aidial.metric.model.configuration.influx.InfluxColumnSource;
import com.epam.aidial.metric.model.configuration.influx.InfluxColumnSourceType;
import com.epam.aidial.metric.model.configuration.influx.InfluxDatasetDeclaration;
import com.epam.aidial.metric.model.configuration.influx.InfluxTableDeclaration;
import com.epam.aidial.metric.model.influx.FluxQueryContext;
import com.epam.aidial.metric.model.influx.FluxQueryPart;
import com.epam.aidial.metric.model.influx.FluxStandardImports;
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
import com.epam.aidial.ql.model.filters.impl.AndImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.NotImplementedException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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
                            TemporalNameGenerator temporalNameGenerator) {
        super(datasetDeclaration, temporalNameGenerator);
    }

    // ---- Query builders (top-level dispatch targets) ----

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
        var limitPart = SimpleFluxBuilder.createLimitPart(query);

        var split = splitFilterForPivot(tableDeclaration, query.getWhere());
        var preFilterPart = FluxConditionPartBuilder.createFilterPart(split.prePivot());
        var postFilterPart = FluxConditionPartBuilder.createFilterPart(split.postPivot());

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

    @Override
    protected FluxQueryContext buildAggregationQuery(Query query) {
        var groupByColumnNames = getGroupByColumns(query.getGroupBy()).stream()
                .map(Column::getName)
                .map(this::resolveGroupBySourceName)
                .toList();
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

            var fillNullJoinKeys = aggregationFunctionCalls.size() > 1 && !groupByColumnNames.isEmpty();
            aggregations = aggregationFunctionCalls.stream()
                    .map(f -> buildAggregationForTempTable(
                            tempTableName, columnName, expressionToOuterColumnNames.get(f),
                            query.getWhere(), groupByColumnNames, f, regexCounter, fillNullJoinKeys))
                    .toList();
        } else {
            var table = getTable(query);
            var fillNullJoinKeys = aggregationFunctionCalls.size() > 1 && !groupByColumnNames.isEmpty();
            aggregations = aggregationFunctionCalls.stream()
                    .map(f -> buildAggregationForTable(
                            table.getName(), expressionToOuterColumnNames.get(f),
                            query.getWhere(), groupByColumnNames, f, regexCounter, fillNullJoinKeys))
                    .toList();
        }

        combineAggregations(combiner, aggregations, aggregationFunctionCalls, groupByColumnNames);

        combiner.add(SimpleFluxBuilder.createUngroupPart());
        combiner.add(createRenamePart(query.getFrom(), query.getExpressions()));
        combiner.add(buildSortPart(query.getOrderBy()));
        combiner.add(SimpleFluxBuilder.createLimitPart(query));
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
        var aggregationFilterPart = createFieldFilterPart(tableDeclaration, function, args);
        var ungroupPart = SimpleFluxBuilder.createUngroupPart();
        var windowPart = SimpleFluxBuilder.createWindowFunctionPart(groupFunctionCall, aggregationFunctionCall);
        var renamePart = SimpleFluxBuilder.createRenamePart(Map.of(
                TIME_COLUMN, expressionToOuterColumnNames.get(groupFunctionCall),
                VALUE_COLUMN, expressionToOuterColumnNames.get(aggregationFunctionCall)
        ));
        var sortPart = buildSortPart(query.getOrderBy());
        var limitPart = SimpleFluxBuilder.createLimitPart(query);

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
        var groupByColumnNames = extractGroupByColumnNames(query.getGroupBy()).stream()
                .map(this::resolveGroupBySourceName)
                .toList();
        var aggregationFunctionCalls = resolveAggregationFunctionCalls(query.getExpressions());

        var table = getTable(query);
        var windowOuterName = expressionToOuterColumnNames.get(groupFunctionCall);

        var combiner = new InfluxQueryPartCombiner();
        var regexCounter = new AtomicInteger();

        var fillNullJoinKeys = aggregationFunctionCalls.size() > 1 && !groupByColumnNames.isEmpty();
        List<FluxQueryPart> aggregations = aggregationFunctionCalls.stream()
                .map(aggCall -> buildWindowColumnAggregationPart(
                        table.getName(), windowOuterName, expressionToOuterColumnNames.get(aggCall),
                        query.getWhere(), groupByColumnNames, groupFunctionCall, aggCall,
                        regexCounter, fillNullJoinKeys))
                .toList();

        var joinColumns = new ArrayList<String>();
        joinColumns.add(windowOuterName);
        joinColumns.addAll(groupByColumnNames);
        combineAggregations(combiner, aggregations, aggregationFunctionCalls, joinColumns);

        combiner.add(SimpleFluxBuilder.createUngroupPart());
        combiner.add(buildSortPart(query.getOrderBy()));
        combiner.add(SimpleFluxBuilder.createLimitPart(query));
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

    // ---- Sub-pipeline builders ----

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
        var limitPart = SimpleFluxBuilder.createLimitPart(query);

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
        var limitPart = SimpleFluxBuilder.createLimitPart(query);

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

    /**
     * Builds one aggregation sub-pipeline for a direct table scan. Chooses between the
     * field-filter fast path (skips {@code schema.fieldsAsCols()}) and the legacy pivot
     * fallback depending on whether the query references any field columns.
     */
    private FluxQueryPart buildAggregationForTable(
            String tableName,
            String outerColumnName,
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
            return buildFieldFilterAggregation(
                    tableDeclaration, outerColumnName, filter, groupByColumnNames,
                    function, args, regexCounter, fillNullJoinKeys);
        }

        return buildPivotAggregation(
                tableDeclaration, outerColumnName, filter, groupByColumnNames,
                function, args, regexCounter, fillNullJoinKeys);
    }

    /**
     * Aggregation sub-pipeline that skips {@code schema.fieldsAsCols()} entirely.
     * Pushes {@code filter(_field == "...")} to the storage engine and aggregates on
     * {@code _value}. Used when grouping and filtering reference only tags.
     */
    private FluxQueryPart buildFieldFilterAggregation(
            InfluxTableDeclaration tableDeclaration,
            String outerColumnName,
            Filter filter,
            List<String> groupByColumnNames,
            FunctionImpl function,
            List<Expression> args,
            AtomicInteger regexCounter,
            boolean fillNullJoinKeys
    ) {
        var fieldForAgg = resolveFieldForAggregation(tableDeclaration, function, args);
        var effectiveFunction = resolveEffectiveFunction(function, args);

        var keepColumns = new ArrayList<>(groupByColumnNames);
        keepColumns.add(VALUE_COLUMN);

        var combiner = new InfluxQueryPartCombiner()
                .add(SimpleFluxBuilder.createFromPart(tableDeclaration.getSource().getBucket()))
                .add(FluxConditionPartBuilder.createRangePart(filter, true))
                .add(FluxConditionPartBuilder.createFilterPart(MEASUREMENT_COLUMN, tableDeclaration.getSource().getMeasurement()))
                .add(FluxConditionPartBuilder.createFilterPart(FIELD_COLUMN, fieldForAgg))
                .add(FluxConditionPartBuilder.createFilterPart(filter, regexCounter));
        addCaseWhenFilter(combiner, function, args, VALUE_COLUMN);
        return combiner
                .add(createNullFillPart(groupByColumnNames, fillNullJoinKeys))
                .add(SimpleFluxBuilder.createGroupPart(groupByColumnNames))
                .add(createAggregationPart(effectiveFunction, VALUE_COLUMN))
                .add(SimpleFluxBuilder.createKeepPart(keepColumns))
                .add(SimpleFluxBuilder.createRenamePart(Map.of(VALUE_COLUMN, outerColumnName)))
                .build();
    }

    /**
     * Aggregation sub-pipeline using {@code schema.fieldsAsCols()} pivot. Fallback for
     * queries that group or filter on field columns.
     */
    private FluxQueryPart buildPivotAggregation(
            InfluxTableDeclaration tableDeclaration,
            String outerColumnName,
            Filter filter,
            List<String> groupByColumnNames,
            FunctionImpl function,
            List<Expression> args,
            AtomicInteger regexCounter,
            boolean fillNullJoinKeys
    ) {
        var aggregationColumnName = resolveAggregationColumnName(function, args);
        var effectiveFunction = resolveEffectiveFunction(function, args);

        var keepColumns = new ArrayList<>(groupByColumnNames);
        keepColumns.add(aggregationColumnName);

        var split = splitFilterForPivot(tableDeclaration, filter);

        var combiner = new InfluxQueryPartCombiner()
                .add(SimpleFluxBuilder.createFromPart(tableDeclaration.getSource().getBucket()))
                .add(FluxConditionPartBuilder.createRangePart(filter, true))
                .add(FluxConditionPartBuilder.createFilterPart(MEASUREMENT_COLUMN, tableDeclaration.getSource().getMeasurement()))
                .add(FluxConditionPartBuilder.createFilterPart(split.prePivot(), regexCounter))
                .add(SimpleFluxBuilder.createFieldsAsColsPart())
                .add(FluxConditionPartBuilder.createFilterPart(split.postPivot(), regexCounter));
        addCaseWhenFilter(combiner, function, args, null);
        return combiner
                .add(createNullFillPart(groupByColumnNames, fillNullJoinKeys))
                .add(SimpleFluxBuilder.createGroupPart(groupByColumnNames))
                .add(createAggregationPart(effectiveFunction, aggregationColumnName))
                .add(SimpleFluxBuilder.createKeepPart(keepColumns))
                .add(SimpleFluxBuilder.createRenamePart(Map.of(aggregationColumnName, outerColumnName)))
                .build();
    }

    /**
     * Aggregation sub-pipeline over a pre-computed temp table (e.g. from a DISTINCT subquery).
     */
    private FluxQueryPart buildAggregationForTempTable(
            String tempTableName,
            String columnName,
            String outerColumnName,
            Filter filter,
            List<String> groupByColumnNames,
            AggregationFunctionCall aggregationFunctionCall,
            AtomicInteger regexCounter,
            boolean fillNullJoinKeys
    ) {
        var function = (FunctionImpl) aggregationFunctionCall.getFunction();
        var keepColumns = new ArrayList<>(groupByColumnNames);
        keepColumns.add(columnName);

        return new InfluxQueryPartCombiner()
                .add(tempTableName)
                .add(FluxConditionPartBuilder.createRangePart(filter, false))
                .add(FluxConditionPartBuilder.createFilterPart(filter, regexCounter))
                .add(createNullFillPart(groupByColumnNames, fillNullJoinKeys))
                .add(SimpleFluxBuilder.createGroupPart(groupByColumnNames))
                .add(createAggregationPart(function, columnName))
                .add(SimpleFluxBuilder.createKeepPart(keepColumns))
                .add(SimpleFluxBuilder.createRenamePart(Map.of(columnName, outerColumnName)))
                .build();
    }

    private FluxQueryPart buildWindowColumnAggregationPart(
            String tableName,
            String windowOuterName,
            String aggOuterName,
            Filter filter,
            List<String> groupByColumnNames,
            GroupFunctionCall groupFunctionCall,
            AggregationFunctionCall aggregationFunctionCall,
            AtomicInteger regexCounter,
            boolean fillNullJoinKeys
    ) {
        var function = (FunctionImpl) aggregationFunctionCall.getFunction();
        var args = aggregationFunctionCall.getArgs();

        var tableDeclaration = getTableDeclaration(tableName);

        var windowGroupColumns = new ArrayList<String>();
        windowGroupColumns.add("_start");
        windowGroupColumns.add("_stop");
        windowGroupColumns.addAll(groupByColumnNames);

        var keepColumns = new ArrayList<String>();
        keepColumns.add("_start");
        keepColumns.addAll(groupByColumnNames);
        keepColumns.add(VALUE_COLUMN);

        return new InfluxQueryPartCombiner()
                .add(SimpleFluxBuilder.createFromPart(tableDeclaration.getSource().getBucket()))
                .add(FluxConditionPartBuilder.createRangePart(filter, true))
                .add(FluxConditionPartBuilder.createFilterPart(MEASUREMENT_COLUMN, tableDeclaration.getSource().getMeasurement()))
                .add(FluxConditionPartBuilder.createFilterPart(filter, regexCounter))
                .add(createFieldFilterPart(tableDeclaration, function, args))
                .add(createNullFillPart(groupByColumnNames, fillNullJoinKeys))
                .add(SimpleFluxBuilder.createUngroupPart())
                .add(SimpleFluxBuilder.createWindowPart(groupFunctionCall))
                .add(SimpleFluxBuilder.createGroupPart(windowGroupColumns))
                .add(createAggregationPart(function, VALUE_COLUMN))
                .add(SimpleFluxBuilder.createKeepPart(keepColumns))
                .add(SimpleFluxBuilder.createRenamePart(Map.of(
                        "_start", windowOuterName,
                        VALUE_COLUMN, aggOuterName)))
                .build();
    }

    // ---- Multi-aggregation join logic ----

    /**
     * Wraps a single aggregation branch so it always emits exactly one row, then tags it
     * with the synthetic join key. Unions the original pipeline with a one-row zero
     * sentinel, re-groups, and re-sums over the output column. Both supported
     * aggregations (count, sum) are additive, so adding a zero row is an identity
     * operation when the branch has real data and yields 0 when it has none.
     */
    private FluxQueryPart wrapWithZeroFallback(FluxQueryPart agg, String outerColumn, Type type, String joinKey) {
        var zero = zeroLiteralForType(type);
        var quotedOuter = SimpleFluxBuilder.quote(outerColumn);
        var wrappedQuery = "union(tables: [\n" + agg.getQuery()
                + ",\narray.from(rows: [{" + outerColumn + ": " + zero + "}])\n])"
                + "\n|> group()"
                + "\n|> sum(column: " + quotedOuter + ")"
                + "\n" + SimpleFluxBuilder.createSetPart(joinKey, "any");
        var imports = new HashSet<>(agg.getImports());
        imports.add(FluxStandardImports.ARRAY);
        return FluxQueryPart.of(Set.copyOf(imports), agg.getPreamble(), wrappedQuery);
    }

    private static String zeroLiteralForType(Type type) {
        return switch (type) {
            case FLOAT, DOUBLE -> "0.0";
            default -> "0";
        };
    }

    /**
     * Combines multiple aggregation sub-pipelines into the combiner using temp tables
     * and sequential joins. For a single aggregation, no join is needed.
     */
    private void combineAggregations(
            InfluxQueryPartCombiner combiner,
            List<FluxQueryPart> aggregations,
            List<AggregationFunctionCall> aggregationCalls,
            List<String> joinColumnNames
    ) {
        if (aggregations.isEmpty()) {
            throw new IllegalArgumentException("At least one aggregation expression is required");
        }
        if (aggregations.size() == 1) {
            combiner.add(aggregations.get(0));
            return;
        }

        // When there are no group-by columns, add a synthetic join key. Each branch may
        // filter down to zero rows, in which case count()/sum() emit no row at all;
        // the subsequent inner-join would then collapse the entire result and wipe out
        // real values from non-empty branches. Wrap each branch in a zero-row union so
        // every branch emits exactly one row (actual value or 0), letting the join
        // preserve non-empty branches.
        if (joinColumnNames.isEmpty()) {
            var syntheticColumn = getNewTemporaryName(TEMPORAL_COLUMN_NAME);
            joinColumnNames = List.of(syntheticColumn);
            var originalAggregations = aggregations;
            aggregations = IntStream.range(0, originalAggregations.size())
                    .mapToObj(i -> wrapWithZeroFallback(
                            originalAggregations.get(i),
                            expressionToOuterColumnNames.get(aggregationCalls.get(i)),
                            aggregationCalls.get(i).getType(),
                            syntheticColumn))
                    .toList();
        }

        var joinKeysCombined = SimpleFluxBuilder.compactWithQuoting(joinColumnNames);
        var tempTables = IntStream.range(0, aggregations.size())
                .mapToObj(i -> getNewTemporaryName(TEMPORAL_TABLE_NAME)).toList();
        var tempJoinTables = IntStream.range(0, aggregations.size() - 1)
                .mapToObj(i -> getNewTemporaryName(TEMPORAL_TABLE_NAME)).toList();

        for (int i = 0; i < aggregations.size(); i++) {
            var agg = aggregations.get(i);
            combiner.add(tempTables.get(i) + " = " + agg.getQuery(), agg.getImports(), agg.getPreamble());
        }

        combiner.add("%s = join(tables: {t1: %s, t2: %s}, on: %s)".formatted(
                tempJoinTables.get(0), tempTables.get(0), tempTables.get(1), joinKeysCombined));

        for (int i = 2; i < tempTables.size(); i++) {
            combiner.add("%s = join(tables: {t1: %s, t2: %s}, on: %s)".formatted(
                    tempJoinTables.get(i - 1), tempJoinTables.get(i - 2), tempTables.get(i), joinKeysCombined));
        }

        combiner.add(tempJoinTables.get(tempJoinTables.size() - 1));
    }

    // ---- Filter analysis (pivot-safety classification) ----

    /**
     * Result of splitting a WHERE filter around the {@code schema.fieldsAsCols()} pivot.
     * Tag-only predicates go before the pivot for storage-engine pushdown; field predicates
     * stay after the pivot where the pivoted column values are available.
     */
    private record SplitFilter(Filter prePivot, Filter postPivot) {
    }

    private SplitFilter splitFilterForPivot(InfluxTableDeclaration tableDeclaration, Filter filter) {
        if (filter == null) {
            return new SplitFilter(null, null);
        }

        if (filter instanceof And and) {
            var tagFilters = new ArrayList<Filter>();
            var fieldFilters = new ArrayList<Filter>();
            for (var child : and.getFilters()) {
                if (filterReferencesOnlyTags(tableDeclaration, child)) {
                    tagFilters.add(child);
                } else {
                    fieldFilters.add(child);
                }
            }
            return new SplitFilter(
                    toFilter(tagFilters),
                    toFilter(fieldFilters));
        }

        if (filterReferencesOnlyTags(tableDeclaration, filter)) {
            return new SplitFilter(filter, null);
        }
        return new SplitFilter(null, filter);
    }

    private static Filter toFilter(List<Filter> filters) {
        if (filters.isEmpty()) {
            return null;
        }
        return filters.size() == 1 ? filters.get(0) : AndImpl.of(filters);
    }

    /**
     * Returns true when the field-filter fast path can serve this aggregation — i.e. all
     * group-by columns are tags, WHERE references only tags, and any case-when condition
     * can be rewritten against {@code _value}.
     */
    private boolean canUseFieldFilterFastPath(
            InfluxTableDeclaration tableDeclaration,
            Filter filter,
            List<String> groupByColumnNames,
            FunctionImpl function,
            List<Expression> args
    ) {
        if (groupByColumnNames.stream().anyMatch(col -> !isTagColumn(tableDeclaration, col))) {
            return false;
        }
        if (!filterReferencesOnlyTags(tableDeclaration, filter)) {
            return false;
        }
        if (isCaseWhenSum(function, args)) {
            return isCaseWhenAgainstFirstField(tableDeclaration, args);
        }
        return true;
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
            return false;
        }
        if (filter instanceof BinaryComparisonFilter bcf) {
            return isTagOrConstantOperand(tableDeclaration, bcf.getLeftExpression())
                    && isTagOrConstantOperand(tableDeclaration, bcf.getRightExpression());
        }
        if (filter instanceof UnaryComparisonFilter ucf) {
            if (ucf.getExpression() instanceof Column col) {
                return isTimeOrTag(tableDeclaration, col.getName());
            }
            return false;
        }
        return false;
    }

    private boolean isTagOrConstantOperand(InfluxTableDeclaration tableDeclaration, Expression expression) {
        if (expression instanceof Column col) {
            return isTimeOrTag(tableDeclaration, col.getName());
        }
        return expression instanceof Constant;
    }

    private boolean isTimeOrTag(InfluxTableDeclaration tableDeclaration, String columnName) {
        return RangeFilterUtils.TIME_COLUMN.equals(columnName) || isTagColumn(tableDeclaration, columnName);
    }

    // ---- Schema helpers (table introspection) ----

    private boolean isTagColumn(InfluxTableDeclaration tableDeclaration, String columnName) {
        return getColumnSourceType(tableDeclaration, columnName) == InfluxColumnSourceType.TAG;
    }

    private InfluxColumnSourceType getColumnSourceType(InfluxTableDeclaration tableDeclaration, String columnName) {
        return tableDeclaration.getSchema().getColumns().stream()
                .filter(c -> c.getName().equals(columnName))
                .map(InfluxColumnDeclaration::getSource)
                .filter(Objects::nonNull)
                .map(InfluxColumnSource::getType)
                .findFirst()
                .orElse(null);
    }

    private String firstFieldName(InfluxTableDeclaration tableDeclaration) {
        return tableDeclaration.getSchema().getColumns().stream()
                .map(InfluxColumnDeclaration::getSource)
                .filter(columnSource -> columnSource.getType() == InfluxColumnSourceType.FIELD)
                .map(InfluxColumnSource::getColumn)
                .findFirst()
                .orElseThrow();
    }

    /**
     * Resolves the InfluxDB field name to filter on for the given aggregation function.
     * For {@code count()} and {@code sum(caseWhen)}, uses the first declared field.
     * For {@code sum(column)}, uses the column itself.
     */
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
     * Resolves the column name to aggregate on after {@code schema.fieldsAsCols()}.
     * {@code count()} uses {@code _measurement} (always present, never null, string type).
     */
    private String resolveAggregationColumnName(FunctionImpl function, List<Expression> args) {
        if ("count".equals(function.getName()) && args.isEmpty()) {
            return MEASUREMENT_COLUMN;
        }
        if ("sum".equals(function.getName()) && args.size() == 1) {
            if (args.get(0) instanceof CaseWhenExpression) {
                return MEASUREMENT_COLUMN;
            }
            return ((ColumnImpl) args.get(0)).getName();
        }
        throw new NotImplementedException("Unsupported aggregation function");
    }

    // ---- Flux part helpers (query fragment generation) ----

    /**
     * Creates a {@code filter(_field == "...")} part for the given aggregation function.
     * Used by window-aggregation paths that don't go through fieldsAsCols.
     */
    private FluxQueryPart createFieldFilterPart(
            InfluxTableDeclaration tableDeclaration,
            FunctionImpl function,
            List<Expression> args
    ) {
        var fieldName = resolveFieldForAggregation(tableDeclaration, function, args);
        return FluxConditionPartBuilder.createFilterPart(FIELD_COLUMN, fieldName);
    }

    private static boolean isCaseWhenSum(FunctionImpl function, List<Expression> args) {
        return "sum".equals(function.getName())
                && args.size() == 1 && args.get(0) instanceof CaseWhenExpression;
    }

    /** Precondition: caller must have validated {@link #isCaseWhenSum}, which guarantees {@code args.size() == 1}. */
    private boolean isCaseWhenAgainstFirstField(InfluxTableDeclaration tableDeclaration, List<Expression> args) {
        var caseWhen = (CaseWhenExpression) args.get(0);
        var condition = caseWhen.getCondition();
        if (!(condition instanceof FunctionCall fc) || fc.getArgs().size() != 2) {
            return false;
        }
        if (!(fc.getArgs().get(0) instanceof Column col) || !(fc.getArgs().get(1) instanceof Constant)) {
            return false;
        }
        var first = firstFieldName(tableDeclaration);
        return first.equals(col.getName()) && getColumnSourceType(tableDeclaration, col.getName()) == InfluxColumnSourceType.FIELD;
    }

    /**
     * Resolves the effective aggregation function. {@code sum(caseWhen)} is converted to
     * {@code count()} because the case-when condition becomes a pre-aggregation filter.
     */
    private static FunctionImpl resolveEffectiveFunction(FunctionImpl function, List<Expression> args) {
        if (isCaseWhenSum(function, args)) {
            return new FunctionImpl("count", function.getType(), function.isDeterministic());
        }
        return function;
    }

    /**
     * Adds a case-when filter to the combiner if the aggregation is {@code sum(caseWhen)}.
     *
     * @param targetColumn the column to compare against, or null to use the original column name
     */
    private void addCaseWhenFilter(InfluxQueryPartCombiner combiner, FunctionImpl function,
                                   List<Expression> args, String targetColumn) {
        if (!isCaseWhenSum(function, args)) {
            return;
        }
        var caseWhen = (CaseWhenExpression) args.get(0);
        combiner.add(createCaseWhenFilterPart(caseWhen, targetColumn));
    }

    private String createCaseWhenFilterPart(CaseWhenExpression caseWhen, String targetColumnOverride) {
        var condition = caseWhen.getCondition();
        if (condition instanceof FunctionCall fc && fc.getArgs().size() == 2
                && fc.getArgs().get(0) instanceof Column col
                && fc.getArgs().get(1) instanceof Constant val) {
            var columnName = targetColumnOverride != null ? targetColumnOverride : col.getName();
            var operator = switch (fc.getFunction().getName()) {
                case "equals" -> "==";
                case "notEquals" -> "!=";
                case "less" -> "<";
                case "greater" -> ">";
                case "lessOrEquals" -> "<=";
                case "greaterOrEquals" -> ">=";
                default -> throw new NotImplementedException(
                        "Unsupported CASE WHEN operator in Flux: " + fc.getFunction().getName());
            };
            return "|> filter(fn: (r) => r[%s] %s %s)".formatted(
                    SimpleFluxBuilder.quote(columnName),
                    operator,
                    SimpleFluxBuilder.quote(String.valueOf(val.getValue())));
        }
        throw new NotImplementedException("Unsupported CASE WHEN condition for Flux: " + condition);
    }

    private static String createNullFillPart(List<String> groupByColumnNames, boolean fillNullJoinKeys) {
        return fillNullJoinKeys ? SimpleFluxBuilder.createNullFillPart(groupByColumnNames) : "";
    }

    private String createAggregationPart(FunctionImpl function, String columnName) {
        return switch (function.getName()) {
            case "count" -> "|> count(column: %s)".formatted(SimpleFluxBuilder.quote(columnName));
            case "sum" -> "|> sum(column: %s)".formatted(SimpleFluxBuilder.quote(columnName));
            default -> throw new NotImplementedException("Unsupported aggregation function");
        };
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

    // ---- Expression / column resolution ----

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
