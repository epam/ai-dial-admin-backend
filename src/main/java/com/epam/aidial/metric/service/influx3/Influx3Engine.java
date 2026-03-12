package com.epam.aidial.metric.service.influx3;

import com.epam.aidial.datasource.definition.FormalParameterDef;
import com.epam.aidial.datasource.definition.InterfaceMethodDef;
import com.epam.aidial.datasource.definition.TypeFloat64;
import com.epam.aidial.datasource.definition.TypeInt64;
import com.epam.aidial.datasource.definition.TypeText;
import com.epam.aidial.datasource.definition.TypeTimestamp;
import com.epam.aidial.datasource.definition.TypeUInt64;
import com.epam.aidial.expressions.Column;
import com.epam.aidial.expressions.FunctionsDatasource;
import com.epam.aidial.expressions.enums.Type;
import com.epam.aidial.expressions.impl.ColumnImpl;
import com.epam.aidial.metric.model.configuration.ColumnType;
import com.epam.aidial.metric.model.configuration.StaticTableSchema;
import com.epam.aidial.metric.model.configuration.TableSchema;
import com.epam.aidial.metric.model.configuration.influx3.Influx3DatasetDeclaration;
import com.epam.aidial.metric.model.configuration.influx3.Influx3TableDeclaration;
import com.epam.aidial.ql.Engine;
import com.epam.aidial.ql.model.Completable;
import com.epam.aidial.ql.model.Data;
import com.epam.aidial.ql.model.Table;
import com.epam.aidial.ql.model.impl.DataImpl;
import com.epam.aidial.ql.model.impl.TableImpl;
import com.influxdb.v3.client.InfluxDBClient;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class Influx3Engine implements Engine {

    private final Influx3DatasetDeclaration declaration;
    private final InfluxDBClient client;
    private final SqlQueryBuilderFactory queryBuilderFactory;

    @Override
    public String getName() {
        return declaration.getName();
    }

    @Override
    public Map<String, Table> getTables() {
        return declaration.getTables().stream()
                .map(this::getTable)
                .collect(Collectors.toMap(Table::getName, table -> table));
    }

    private Table getTable(Influx3TableDeclaration tableDeclaration) {
        return TableImpl.builder()
                .name(tableDeclaration.getName())
                .columns(getColumns(tableDeclaration.getSchema()))
                .build();
    }

    private Map<String, Column> getColumns(TableSchema tableSchema) {
        if (!(tableSchema instanceof StaticTableSchema staticTableSchema)) {
            throw new IllegalArgumentException("Table schema must be static");
        }

        return staticTableSchema.getColumns().stream()
                .map(column -> new ColumnImpl(getType(column.getType()), column.getName()))
                .collect(Collectors.toMap(Column::getName, column -> column));
    }

    private Type getType(ColumnType columnType) {
        return switch (columnType) {
            case STRING -> Type.STRING;
            case TIMESTAMP -> Type.TIMESTAMP;
            case DOUBLE -> Type.DOUBLE;
            case INT64 -> Type.INT_64;
            case UINT64 -> Type.UINT_64;
        };
    }

    @Override
    public FunctionsDatasource getFunctions() {
        return new FunctionsDatasource() {
            @Override
            public Map<String, List<InterfaceMethodDef>> getMethodsByName() {
                return Map.of();
            }

            @Override
            public Map<String, List<InterfaceMethodDef>> getGroupMethodsByName() {
                var window = new InterfaceMethodDef("window", null, null);
                window.getFormalParameters().addAll(List.of(
                        new FormalParameterDef(window, "column", TypeTimestamp.INSTANCE, false, null),
                        new FormalParameterDef(window, "value", TypeInt64.INSTANCE, false, null),
                        new FormalParameterDef(window, "unit", TypeText.INSTANCE, false, null)
                ));
                return Map.of(
                        "window", List.of(window)
                );
            }

            @Override
            public Map<String, List<InterfaceMethodDef>> getAggregationMethodsByName() {
                var count = new InterfaceMethodDef("count", TypeInt64.INSTANCE, null);

                var sumInt64 = new InterfaceMethodDef("sum", TypeInt64.INSTANCE, null);
                sumInt64.getFormalParameters().add(new FormalParameterDef(sumInt64, "x", TypeInt64.INSTANCE, false, null));
                var sumUint64 = new InterfaceMethodDef("sum", TypeUInt64.INSTANCE, null);
                sumUint64.getFormalParameters().add(new FormalParameterDef(sumUint64, "x", TypeUInt64.INSTANCE, false, null));
                var sumFloat64 = new InterfaceMethodDef("sum", TypeFloat64.INSTANCE, null);
                sumFloat64.getFormalParameters().add(new FormalParameterDef(sumFloat64, "x", TypeFloat64.INSTANCE, false, null));

                return Map.of(
                        "count", List.of(count),
                        "sum", List.of(sumInt64, sumUint64, sumFloat64)
                );
            }

            @Override
            public List<InterfaceMethodDef> getNegateMethods() {
                return List.of();
            }

            @Override
            public List<InterfaceMethodDef> getAndMethods() {
                return List.of();
            }

            @Override
            public List<InterfaceMethodDef> getOrMethods() {
                return List.of();
            }

            @Override
            public List<InterfaceMethodDef> getNotMethods() {
                return List.of();
            }

            @Override
            public List<InterfaceMethodDef> getIfMethods() {
                return List.of();
            }

            @Override
            public List<InterfaceMethodDef> getPlusMethods() {
                return List.of();
            }

            @Override
            public List<InterfaceMethodDef> getMinusMethods() {
                return List.of();
            }

            @Override
            public List<InterfaceMethodDef> getMultiplyMethods() {
                return List.of();
            }

            @Override
            public List<InterfaceMethodDef> getDivideMethods() {
                return List.of();
            }

            @Override
            public List<InterfaceMethodDef> getModuloMethods() {
                return List.of();
            }

            @Override
            public List<InterfaceMethodDef> getEqualsMethods() {
                return List.of();
            }

            @Override
            public List<InterfaceMethodDef> getNotEqualsMethods() {
                return List.of();
            }

            @Override
            public List<InterfaceMethodDef> getLessMethods() {
                return List.of();
            }

            @Override
            public List<InterfaceMethodDef> getGreaterMethods() {
                return List.of();
            }

            @Override
            public List<InterfaceMethodDef> getLessOrEqualsMethods() {
                return List.of();
            }

            @Override
            public List<InterfaceMethodDef> getGreaterOrEqualsMethods() {
                return List.of();
            }
        };
    }

    @Override
    public Data getData(Completable completable) {
        var queryContext = queryBuilderFactory.createQueryBuilder()
                .buildQueryContext(completable);

        var rows = new ArrayList<List<Object>>();
        try (Stream<Object[]> stream = client.query(queryContext.getQuery(), queryContext.getParameters(), null)) {
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
