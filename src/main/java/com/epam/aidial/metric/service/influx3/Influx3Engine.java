package com.epam.aidial.metric.service.influx3;

import com.epam.aidial.metric.model.configuration.influx3.Influx3DatasetDeclaration;
import com.epam.aidial.metric.service.AbstractQueryEngine;
import com.epam.aidial.ql.model.Completable;
import com.epam.aidial.ql.model.Data;
import com.epam.aidial.ql.model.impl.DataImpl;
import com.influxdb.v3.client.InfluxDBClient;
import com.influxdb.v3.client.query.QueryOptions;
import com.influxdb.v3.client.query.QueryType;

import java.util.ArrayList;
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

        var rows = new ArrayList<java.util.List<Object>>();
        var options = new QueryOptions(influx3Declaration.getSource().getDatabase(), QueryType.SQL);
        try (Stream<Object[]> stream = client.query(queryContext.getQuery(), queryContext.getParameters(), options)) {
            stream.forEach(record -> {
                var row = new ArrayList<>(queryContext.getColumnNames().size());
                for (int i = 0; i < queryContext.getColumnNames().size(); i++) {
                    row.add(i < record.length ? record[i] : null);
                }
                rows.add(row);
            });
        }

        return DataImpl.builder()
                .expressions(completable.getExpressions())
                .data(rows)
                .build();
    }
}
