package com.epam.aidial.metric.service.influx;

import com.epam.aidial.expressions.Expression;
import com.epam.aidial.expressions.enums.Type;
import com.epam.aidial.metric.model.configuration.influx.InfluxDatasetDeclaration;
import com.epam.aidial.metric.service.AbstractQueryEngine;
import com.epam.aidial.metric.service.WindowGapFiller;
import com.epam.aidial.ql.model.Completable;
import com.epam.aidial.ql.model.Data;
import com.epam.aidial.ql.model.Query;
import com.epam.aidial.ql.model.impl.DataImpl;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.query.FluxTable;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

public class InfluxEngine extends AbstractQueryEngine {

    private final InfluxDBClient client;
    private final FluxQueryBuilderFactory queryBuilderFactory;
    private final WindowGapFiller windowGapFiller;

    public InfluxEngine(InfluxDatasetDeclaration declaration, InfluxDBClient client,
                        FluxQueryBuilderFactory queryBuilderFactory, WindowGapFiller windowGapFiller) {
        super(declaration);
        this.client = client;
        this.queryBuilderFactory = queryBuilderFactory;
        this.windowGapFiller = windowGapFiller;
    }

    @Override
    public Data getData(Completable completable, boolean fillGaps) {
        var queryContext = queryBuilderFactory.createQueryBuilder()
                .buildQueryContext(completable);
        var tables = client.getQueryApi().query(queryContext.buildFullQuery());
        var rows = toRows(tables, queryContext.getColumnNames());

        if (rows.isEmpty() && isUngroupedAggregation(completable)) {
            rows.add(buildDefaultAggregationRow(completable.getExpressions()));
        }

        if (fillGaps && completable instanceof Query query && WindowGapFiller.isWindowQuery(query)) {
            rows = new ArrayList<>(windowGapFiller.fillGaps(rows, query));
        }

        return DataImpl.builder()
                .expressions(completable.getExpressions())
                .data(rows)
                .build();
    }

    private boolean isUngroupedAggregation(Completable completable) {
        if (!(completable instanceof Query query)) {
            return false;
        }
        if (!CollectionUtils.isEmpty(query.getGroupBy())) {
            return false;
        }
        return query.getExpressions().stream().anyMatch(Expression::isAggregation);
    }

    private List<Object> buildDefaultAggregationRow(List<Expression> expressions) {
        var row = new ArrayList<>();
        for (var expression : expressions) {
            row.add(expression.isAggregation() ? defaultForType(expression.getType()) : null);
        }
        return row;
    }

    private Object defaultForType(Type type) {
        return switch (type) {
            case FLOAT, DOUBLE -> 0.0;
            default -> 0L;
        };
    }

    private ArrayList<List<Object>> toRows(List<FluxTable> tables, List<String> columnNames) {
        var rows = new ArrayList<List<Object>>();
        for (var table : tables) {
            for (var record : table.getRecords()) {
                var row = new ArrayList<>();
                for (var columnName : columnNames) {
                    var value = record.getValueByKey(columnName);
                    if (SimpleFluxBuilder.NULL_SENTINEL.equals(value)) {
                        value = null;
                    }
                    row.add(value);
                }
                rows.add(row);
            }
        }

        return rows;
    }

}
