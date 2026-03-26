package com.epam.aidial.metric.service.influx3;

import com.epam.aidial.expressions.Expression;
import com.epam.aidial.expressions.enums.Type;
import com.epam.aidial.metric.model.configuration.influx3.Influx3DatasetDeclaration;
import com.epam.aidial.metric.service.AbstractQueryEngine;
import com.epam.aidial.ql.model.Completable;
import com.epam.aidial.ql.model.Data;
import com.epam.aidial.ql.model.Query;
import com.epam.aidial.ql.model.impl.DataImpl;
import com.influxdb.v3.client.InfluxDBClient;
import com.influxdb.v3.client.query.QueryOptions;
import com.influxdb.v3.client.query.QueryType;
import org.apache.commons.collections4.CollectionUtils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class Influx3Engine extends AbstractQueryEngine {

    private final InfluxDBClient client;
    private final SqlQueryBuilderFactory queryBuilderFactory;
    private final Influx3DatasetDeclaration influx3Declaration;

    public Influx3Engine(Influx3DatasetDeclaration declaration, InfluxDBClient client,
                         SqlQueryBuilderFactory queryBuilderFactory) {
        super(declaration);
        this.influx3Declaration = declaration;
        this.client = client;
        this.queryBuilderFactory = queryBuilderFactory;
    }

    @Override
    public Data getData(Completable completable) {
        var queryContext = queryBuilderFactory.createQueryBuilder()
                .buildQueryContext(completable);

        var rows = new ArrayList<List<Object>>();
        var options = new QueryOptions(influx3Declaration.getSource().getDatabase(), QueryType.SQL);
        try (Stream<Object[]> stream = client.query(queryContext.getQuery(), queryContext.getParameters(), options)) {
            stream.forEach(record -> {
                var row = new ArrayList<>(queryContext.getColumnNames().size());
                for (int i = 0; i < queryContext.getColumnNames().size(); i++) {
                    row.add(i < record.length ? normalizeValue(record[i]) : null);
                }
                rows.add(row);
            });
        }

        if (rows.isEmpty() && isUngroupedAggregation(completable)) {
            rows.add(buildDefaultAggregationRow(completable.getExpressions()));
        } else if (rows.size() == 1 && isUngroupedAggregation(completable)) {
            rows.set(0, normalizeAggregationRow(rows.get(0), completable.getExpressions()));
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

    private List<Object> normalizeAggregationRow(List<Object> row, List<Expression> expressions) {
        var normalized = new ArrayList<>(row.size());
        for (int i = 0; i < expressions.size(); i++) {
            var value = i < row.size() ? row.get(i) : null;
            if (value == null && expressions.get(i).isAggregation()) {
                normalized.add(defaultForType(expressions.get(i).getType()));
            } else {
                normalized.add(value);
            }
        }
        return normalized;
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

    private Object normalizeValue(Object value) {
        if (value instanceof LocalDateTime localDateTime) {
            return localDateTime.toInstant(ZoneOffset.UTC);
        }
        return value;
    }
}
