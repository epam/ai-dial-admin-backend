package com.epam.aidial.metric.service.influx;

import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.epam.aidial.cfg.utils.ResourceUtils;
import com.epam.aidial.expressions.enums.Type;
import com.epam.aidial.expressions.impl.AggregationFunctionCallImpl;
import com.epam.aidial.expressions.impl.AliasImpl;
import com.epam.aidial.expressions.impl.ColumnImpl;
import com.epam.aidial.expressions.impl.ConstantImpl;
import com.epam.aidial.expressions.impl.FunctionImpl;
import com.epam.aidial.expressions.impl.GroupFunctionCallImpl;
import com.epam.aidial.expressions.impl.NumberConstantImpl;
import com.epam.aidial.metric.component.TemporalNameGenerator;
import com.epam.aidial.metric.config.InfluxDatasetConfiguration;
import com.epam.aidial.metric.model.configuration.DatasetDeclaration;
import com.epam.aidial.metric.model.configuration.influx.InfluxDatasetDeclaration;
import com.epam.aidial.metric.model.influx.FluxStandardImports;
import com.epam.aidial.ql.common.model.enums.BinaryComparisonOperator;
import com.epam.aidial.ql.common.model.enums.SortDirection;
import com.epam.aidial.ql.model.filters.impl.AndImpl;
import com.epam.aidial.ql.model.filters.impl.BinaryComparisonFilterImpl;
import com.epam.aidial.ql.model.impl.QueryImpl;
import com.epam.aidial.ql.model.impl.SortImpl;
import com.epam.aidial.ql.model.impl.TableImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FluxQueryBuilderTest {

    private static final ObjectMapper OBJECT_MAPPER = JsonMapperConfiguration.createJsonMapper();

    private FluxQueryBuilder fluxQueryBuilder;

    @BeforeEach
    void setUp() throws JsonProcessingException {
        var testMetricConfig = ResourceUtils.readResource("/metrics/metric.config.influx2.json");
        var datasetDeclaration = (InfluxDatasetDeclaration) OBJECT_MAPPER.readValue(testMetricConfig, DatasetDeclaration.class);

        var datasetConfiguration = new InfluxDatasetConfiguration();
        datasetConfiguration.setDefaultPageSize(50);

        fluxQueryBuilder = new FluxQueryBuilder(datasetDeclaration, datasetConfiguration, new TemporalNameGenerator());
    }

    @Test
    void buildQuery_SimpleSelect() {
        var completable = QueryImpl.builder()
                .distinct(false)
                .expressions(List.of(
                        new ColumnImpl(Type.STRING, "deployment"),
                        new ColumnImpl(Type.DOUBLE, "price"),
                        new ColumnImpl(Type.INT_64, "prompt_tokens")
                ))
                .from(TableImpl.builder().name("analytics").build())
                .where(AndImpl.of(List.of(
                        BinaryComparisonFilterImpl.of(new ColumnImpl(Type.TIMESTAMP, "_time"),
                                BinaryComparisonOperator.GREATER_OR_EQUALS, new ConstantImpl(Type.TIMESTAMP, 1739286720000L)),
                        BinaryComparisonFilterImpl.of(new ColumnImpl(Type.TIMESTAMP, "_time"),
                                BinaryComparisonOperator.LESS, new ConstantImpl(Type.TIMESTAMP, 1739290800000L))
                )))
                .orderBy(List.of(
                        SortImpl.of(new ColumnImpl(Type.INT_64, "prompt_tokens"), SortDirection.DESC)
                ))
                .build();


        var actual = fluxQueryBuilder.buildQueryContext(completable);

        assertThat(actual.getImports()).containsExactlyInAnyOrder(FluxStandardImports.SCHEMA);
        assertThat(actual.getQuery()).isEqualTo("""
                from(bucket: "analytics-realtime")
                |> range(start: 2025-02-11T15:12:00Z, stop: 2025-02-11T16:20:00Z)
                |> filter(fn: (r) => r["_measurement"] == "analytics")
                |> schema.fieldsAsCols()
                |> keep(columns: ["deployment", "price", "prompt_tokens"])
                |> group()
                |> sort(columns: ["prompt_tokens"], desc: true)""");
        assertThat(actual.getColumnNames()).isEqualTo(List.of("deployment", "price", "prompt_tokens"));
    }

    @Test
    void buildQuery_SimpleSelect_StringComparison() {
        var completable = QueryImpl.builder()
                .distinct(false)
                .expressions(List.of(
                        new ColumnImpl(Type.STRING, "deployment"),
                        new ColumnImpl(Type.DOUBLE, "price"),
                        new ColumnImpl(Type.INT_64, "prompt_tokens")
                ))
                .from(TableImpl.builder().name("analytics").build())
                .where(AndImpl.of(List.of(
                        BinaryComparisonFilterImpl.of(new ColumnImpl(Type.TIMESTAMP, "_time"),
                                BinaryComparisonOperator.GREATER_OR_EQUALS, new ConstantImpl(Type.TIMESTAMP, 1739286720000L)),
                        BinaryComparisonFilterImpl.of(new ColumnImpl(Type.TIMESTAMP, "_time"),
                                BinaryComparisonOperator.LESS, new ConstantImpl(Type.TIMESTAMP, 1739290800000L)),
                        BinaryComparisonFilterImpl.of(new ColumnImpl(Type.STRING, "deployment"),
                                BinaryComparisonOperator.STARTS_WITH, new ConstantImpl(Type.STRING, "value"))
                )))
                .orderBy(List.of(
                        SortImpl.of(new ColumnImpl(Type.INT_64, "prompt_tokens"), SortDirection.DESC)
                ))
                .build();


        var actual = fluxQueryBuilder.buildQueryContext(completable);

        assertThat(actual.getImports()).containsExactlyInAnyOrder(
                FluxStandardImports.SCHEMA,
                FluxStandardImports.REGEXP
        );
        assertThat(actual.getPreamble()).containsExactly(
                "_re0 = regexp.compile(v: \"(?i)^value\")"
        );
        assertThat(actual.getQuery()).isEqualTo("""
                from(bucket: "analytics-realtime")
                |> range(start: 2025-02-11T15:12:00Z, stop: 2025-02-11T16:20:00Z)
                |> filter(fn: (r) => r["_measurement"] == "analytics")
                |> schema.fieldsAsCols()
                |> filter(fn: (r) => r["deployment"] =~ _re0)
                |> keep(columns: ["deployment", "price", "prompt_tokens"])
                |> group()
                |> sort(columns: ["prompt_tokens"], desc: true)""");
        assertThat(actual.getColumnNames()).isEqualTo(List.of("deployment", "price", "prompt_tokens"));
    }

    @Test
    void buildQuery_SimpleSelect_Alias() {
        var completable = QueryImpl.builder()
                .distinct(false)
                .expressions(List.of(
                        new AliasImpl("a",
                                new ColumnImpl(Type.STRING, "deployment")
                        ),
                        new AliasImpl("b",
                                new ColumnImpl(Type.DOUBLE, "price")
                        ),
                        new AliasImpl("c",
                                new ColumnImpl(Type.INT_64, "prompt_tokens")
                        )
                ))
                .from(TableImpl.builder().name("analytics").build())
                .where(AndImpl.of(List.of(
                        BinaryComparisonFilterImpl.of(new ColumnImpl(Type.TIMESTAMP, "_time"),
                                BinaryComparisonOperator.GREATER_OR_EQUALS, new ConstantImpl(Type.TIMESTAMP, 1739286720000L)),
                        BinaryComparisonFilterImpl.of(new ColumnImpl(Type.TIMESTAMP, "_time"),
                                BinaryComparisonOperator.LESS, new ConstantImpl(Type.TIMESTAMP, 1739290800000L))
                )))
                .orderBy(List.of(
                        SortImpl.of(new ColumnImpl(Type.INT_64, "prompt_tokens"), SortDirection.DESC)
                ))
                .build();


        var actual = fluxQueryBuilder.buildQueryContext(completable);

        assertThat(actual.getImports()).containsExactlyInAnyOrder(FluxStandardImports.SCHEMA);
        assertThat(actual.getQuery()).isEqualTo("""
                from(bucket: "analytics-realtime")
                |> range(start: 2025-02-11T15:12:00Z, stop: 2025-02-11T16:20:00Z)
                |> filter(fn: (r) => r["_measurement"] == "analytics")
                |> schema.fieldsAsCols()
                |> keep(columns: ["deployment", "price", "prompt_tokens"])
                |> rename(columns: {deployment: "a", price: "b", prompt_tokens: "c"})
                |> group()
                |> sort(columns: ["c"], desc: true)""");
        assertThat(actual.getColumnNames()).isEqualTo(List.of("a", "b", "c"));
    }

    @Test
    void buildQuery_SimpleDistinctValueSelect() {
        var completable = QueryImpl.builder()
                .distinct(true)
                .expressions(List.of(
                        new ColumnImpl(Type.STRING, "user_hash")
                ))
                .from(TableImpl.builder().name("analytics").build())
                .where(AndImpl.of(List.of(
                        BinaryComparisonFilterImpl.of(new ColumnImpl(Type.TIMESTAMP, "_time"),
                                BinaryComparisonOperator.GREATER_OR_EQUALS, new ConstantImpl(Type.TIMESTAMP, 1739286720000L)),
                        BinaryComparisonFilterImpl.of(new ColumnImpl(Type.TIMESTAMP, "_time"),
                                BinaryComparisonOperator.LESS, new ConstantImpl(Type.TIMESTAMP, 1739290800000L))
                )))
                .build();


        var actual = fluxQueryBuilder.buildQueryContext(completable);

        assertThat(actual.getImports()).isEmpty();
        assertThat(actual.getQuery()).isEqualTo("""
                from(bucket: "analytics-realtime")
                |> range(start: 2025-02-11T15:12:00Z, stop: 2025-02-11T16:20:00Z)
                |> filter(fn: (r) => r["_measurement"] == "analytics")
                |> filter(fn: (r) => r["_field"] == "user_hash")
                |> group()
                |> distinct(column: "_value")
                |> rename(columns: {_value: "user_hash"})
                |> keep(columns: ["user_hash"])""");
        assertThat(actual.getColumnNames()).isEqualTo(List.of("user_hash"));
    }

    @Test
    void buildQuery_SimpleDistinctValueSelect_Filtering() {
        var completable = QueryImpl.builder()
                .distinct(true)
                .expressions(List.of(
                        new ColumnImpl(Type.STRING, "user_hash")
                ))
                .from(TableImpl.builder().name("analytics").build())
                .where(AndImpl.of(List.of(
                        BinaryComparisonFilterImpl.of(new ColumnImpl(Type.TIMESTAMP, "_time"),
                                BinaryComparisonOperator.GREATER_OR_EQUALS, new ConstantImpl(Type.TIMESTAMP, 1739286720000L)),
                        BinaryComparisonFilterImpl.of(new ColumnImpl(Type.TIMESTAMP, "_time"),
                                BinaryComparisonOperator.LESS, new ConstantImpl(Type.TIMESTAMP, 1739290800000L)),
                        BinaryComparisonFilterImpl.of(new ColumnImpl(Type.STRING, "deployment"),
                                BinaryComparisonOperator.EQUALS, new ConstantImpl(Type.STRING, "dep_value"))
                )))
                .build();


        var actual = fluxQueryBuilder.buildQueryContext(completable);

        assertThat(actual.getImports()).isEmpty();
        assertThat(actual.getQuery()).isEqualTo("""
                from(bucket: "analytics-realtime")
                |> range(start: 2025-02-11T15:12:00Z, stop: 2025-02-11T16:20:00Z)
                |> filter(fn: (r) => r["_measurement"] == "analytics")
                |> filter(fn: (r) => r["_field"] == "user_hash")
                |> filter(fn: (r) => r["deployment"] == "dep_value")
                |> group()
                |> distinct(column: "_value")
                |> rename(columns: {_value: "user_hash"})
                |> keep(columns: ["user_hash"])""");
        assertThat(actual.getColumnNames()).isEqualTo(List.of("user_hash"));
    }

    @Test
    void buildQuery_SimpleDistinctValueSelect_Alias() {
        var completable = QueryImpl.builder()
                .distinct(true)
                .expressions(List.of(
                        new AliasImpl("a",
                                new ColumnImpl(Type.STRING, "user_hash")
                        )
                ))
                .from(TableImpl.builder().name("analytics").build())
                .where(AndImpl.of(List.of(
                        BinaryComparisonFilterImpl.of(new ColumnImpl(Type.TIMESTAMP, "_time"),
                                BinaryComparisonOperator.GREATER_OR_EQUALS, new ConstantImpl(Type.TIMESTAMP, 1739286720000L)),
                        BinaryComparisonFilterImpl.of(new ColumnImpl(Type.TIMESTAMP, "_time"),
                                BinaryComparisonOperator.LESS, new ConstantImpl(Type.TIMESTAMP, 1739290800000L))
                )))
                .build();


        var actual = fluxQueryBuilder.buildQueryContext(completable);

        assertThat(actual.getImports()).isEmpty();
        assertThat(actual.getQuery()).isEqualTo("""
                from(bucket: "analytics-realtime")
                |> range(start: 2025-02-11T15:12:00Z, stop: 2025-02-11T16:20:00Z)
                |> filter(fn: (r) => r["_measurement"] == "analytics")
                |> filter(fn: (r) => r["_field"] == "user_hash")
                |> group()
                |> distinct(column: "_value")
                |> rename(columns: {_value: "a"})
                |> keep(columns: ["a"])""");
        assertThat(actual.getColumnNames()).isEqualTo(List.of("a"));
    }

    @Test
    void buildQuery_SimpleDistinctTagSelect() {
        var completable = QueryImpl.builder()
                .distinct(true)
                .expressions(List.of(
                        new ColumnImpl(Type.STRING, "deployment")
                ))
                .from(TableImpl.builder().name("analytics").build())
                .where(AndImpl.of(List.of(
                        BinaryComparisonFilterImpl.of(new ColumnImpl(Type.TIMESTAMP, "_time"),
                                BinaryComparisonOperator.GREATER_OR_EQUALS, new ConstantImpl(Type.TIMESTAMP, 1739286720000L)),
                        BinaryComparisonFilterImpl.of(new ColumnImpl(Type.TIMESTAMP, "_time"),
                                BinaryComparisonOperator.LESS, new ConstantImpl(Type.TIMESTAMP, 1739290800000L))
                )))
                .build();


        var actual = fluxQueryBuilder.buildQueryContext(completable);

        assertThat(actual.getImports()).isEmpty();
        assertThat(actual.getQuery()).isEqualTo("""
                from(bucket: "analytics-realtime")
                |> range(start: 2025-02-11T15:12:00Z, stop: 2025-02-11T16:20:00Z)
                |> filter(fn: (r) => r["_measurement"] == "analytics")
                |> keep(columns: ["deployment"])
                |> group()
                |> distinct(column: "deployment")
                |> rename(columns: {_value: "deployment"})""");
        assertThat(actual.getColumnNames()).isEqualTo(List.of("deployment"));
    }

    @Test
    void buildQuery_SimpleDistinctTagSelect_Filtering() {
        var completable = QueryImpl.builder()
                .distinct(true)
                .expressions(List.of(
                        new ColumnImpl(Type.STRING, "deployment")
                ))
                .from(TableImpl.builder().name("analytics").build())
                .where(AndImpl.of(List.of(
                        BinaryComparisonFilterImpl.of(new ColumnImpl(Type.TIMESTAMP, "_time"),
                                BinaryComparisonOperator.GREATER_OR_EQUALS, new ConstantImpl(Type.TIMESTAMP, 1739286720000L)),
                        BinaryComparisonFilterImpl.of(new ColumnImpl(Type.TIMESTAMP, "_time"),
                                BinaryComparisonOperator.LESS, new ConstantImpl(Type.TIMESTAMP, 1739290800000L)),
                        BinaryComparisonFilterImpl.of(new ColumnImpl(Type.STRING, "deployment"),
                                BinaryComparisonOperator.EQUALS, new ConstantImpl(Type.STRING, "dep_value"))
                )))
                .build();


        var actual = fluxQueryBuilder.buildQueryContext(completable);

        assertThat(actual.getImports()).isEmpty();
        assertThat(actual.getQuery()).isEqualTo("""
                from(bucket: "analytics-realtime")
                |> range(start: 2025-02-11T15:12:00Z, stop: 2025-02-11T16:20:00Z)
                |> filter(fn: (r) => r["_measurement"] == "analytics")
                |> filter(fn: (r) => r["deployment"] == "dep_value")
                |> keep(columns: ["deployment"])
                |> group()
                |> distinct(column: "deployment")
                |> rename(columns: {_value: "deployment"})""");
        assertThat(actual.getColumnNames()).isEqualTo(List.of("deployment"));
    }

    @Test
    void buildQuery_SimpleDistinctTagSelect_Alias() {
        var completable = QueryImpl.builder()
                .distinct(true)
                .expressions(List.of(
                        new AliasImpl("a",
                                new ColumnImpl(Type.STRING, "deployment")
                        )
                ))
                .from(TableImpl.builder().name("analytics").build())
                .where(AndImpl.of(List.of(
                        BinaryComparisonFilterImpl.of(new ColumnImpl(Type.TIMESTAMP, "_time"),
                                BinaryComparisonOperator.GREATER_OR_EQUALS, new ConstantImpl(Type.TIMESTAMP, 1739286720000L)),
                        BinaryComparisonFilterImpl.of(new ColumnImpl(Type.TIMESTAMP, "_time"),
                                BinaryComparisonOperator.LESS, new ConstantImpl(Type.TIMESTAMP, 1739290800000L))
                )))
                .build();


        var actual = fluxQueryBuilder.buildQueryContext(completable);

        assertThat(actual.getImports()).isEmpty();
        assertThat(actual.getQuery()).isEqualTo("""
                from(bucket: "analytics-realtime")
                |> range(start: 2025-02-11T15:12:00Z, stop: 2025-02-11T16:20:00Z)
                |> filter(fn: (r) => r["_measurement"] == "analytics")
                |> keep(columns: ["deployment"])
                |> group()
                |> distinct(column: "deployment")
                |> rename(columns: {_value: "a"})""");
        assertThat(actual.getColumnNames()).isEqualTo(List.of("a"));
    }

    @Test
    void buildQuery_SimpleAggregate() {
        var completable = QueryImpl.builder()
                .distinct(false)
                .expressions(List.of(
                        new AggregationFunctionCallImpl(
                                new FunctionImpl("count", Type.INT_64, true),
                                List.of(),
                                List.of()
                        ),
                        new AggregationFunctionCallImpl(
                                new FunctionImpl("sum", Type.DOUBLE, true),
                                List.of(),
                                List.of(new ColumnImpl(Type.DOUBLE, "price"))
                        ),
                        new AggregationFunctionCallImpl(
                                new FunctionImpl("sum", Type.INT_64, true),
                                List.of(),
                                List.of(new ColumnImpl(Type.INT_64, "prompt_tokens"))
                        )
                ))
                .from(TableImpl.builder().name("analytics").build())
                .where(AndImpl.of(List.of(
                        BinaryComparisonFilterImpl.of(new ColumnImpl(Type.TIMESTAMP, "_time"),
                                BinaryComparisonOperator.GREATER_OR_EQUALS, new ConstantImpl(Type.TIMESTAMP, 1739286720000L)),
                        BinaryComparisonFilterImpl.of(new ColumnImpl(Type.TIMESTAMP, "_time"),
                                BinaryComparisonOperator.LESS, new ConstantImpl(Type.TIMESTAMP, 1739290800000L))
                )))
                .build();


        var actual = fluxQueryBuilder.buildQueryContext(completable);

        assertThat(actual.getImports()).isEmpty();
        assertThat(actual.getQuery()).isEqualTo("""
                temp_table_0 = from(bucket: "analytics-realtime")
                |> range(start: 2025-02-11T15:12:00Z, stop: 2025-02-11T16:20:00Z)
                |> filter(fn: (r) => r["_measurement"] == "analytics")
                |> filter(fn: (r) => r["_field"] == "user_hash")
                |> group(columns: [""])
                |> count()
                |> keep(columns: ["_value"])
                |> rename(columns: {_value: "temp_column_0"})
                |> set(key: "temp_column_3", value: "any")
                temp_table_1 = from(bucket: "analytics-realtime")
                |> range(start: 2025-02-11T15:12:00Z, stop: 2025-02-11T16:20:00Z)
                |> filter(fn: (r) => r["_measurement"] == "analytics")
                |> filter(fn: (r) => r["_field"] == "price")
                |> group(columns: [""])
                |> sum()
                |> keep(columns: ["_value"])
                |> rename(columns: {_value: "temp_column_1"})
                |> set(key: "temp_column_3", value: "any")
                temp_table_2 = from(bucket: "analytics-realtime")
                |> range(start: 2025-02-11T15:12:00Z, stop: 2025-02-11T16:20:00Z)
                |> filter(fn: (r) => r["_measurement"] == "analytics")
                |> filter(fn: (r) => r["_field"] == "prompt_tokens")
                |> group(columns: [""])
                |> sum()
                |> keep(columns: ["_value"])
                |> rename(columns: {_value: "temp_column_2"})
                |> set(key: "temp_column_3", value: "any")
                temp_table_3 = join(tables: {t1: temp_table_0, t2: temp_table_1}, on: ["temp_column_3"])
                temp_table_4 = join(tables: {t1: temp_table_3, t2: temp_table_2}, on: ["temp_column_3"])
                temp_table_4
                |> group()""");
        assertThat(actual.getColumnNames()).isEqualTo(List.of("temp_column_0", "temp_column_1", "temp_column_2"));
    }

    @Test
    void buildQuery_SimpleAggregate_Filtering() {
        var completable = QueryImpl.builder()
                .distinct(false)
                .expressions(List.of(
                        new AggregationFunctionCallImpl(
                                new FunctionImpl("count", Type.INT_64, true),
                                List.of(),
                                List.of()
                        ),
                        new AggregationFunctionCallImpl(
                                new FunctionImpl("sum", Type.DOUBLE, true),
                                List.of(),
                                List.of(new ColumnImpl(Type.DOUBLE, "price"))
                        ),
                        new AggregationFunctionCallImpl(
                                new FunctionImpl("sum", Type.INT_64, true),
                                List.of(),
                                List.of(new ColumnImpl(Type.INT_64, "prompt_tokens"))
                        )
                ))
                .from(TableImpl.builder().name("analytics").build())
                .where(AndImpl.of(List.of(
                        BinaryComparisonFilterImpl.of(new ColumnImpl(Type.TIMESTAMP, "_time"),
                                BinaryComparisonOperator.GREATER_OR_EQUALS, new ConstantImpl(Type.TIMESTAMP, 1739286720000L)),
                        BinaryComparisonFilterImpl.of(new ColumnImpl(Type.TIMESTAMP, "_time"),
                                BinaryComparisonOperator.LESS, new ConstantImpl(Type.TIMESTAMP, 1739290800000L)),
                        BinaryComparisonFilterImpl.of(new ColumnImpl(Type.STRING, "deployment"),
                                BinaryComparisonOperator.EQUALS, new ConstantImpl(Type.STRING, "dep_value"))
                )))
                .build();


        var actual = fluxQueryBuilder.buildQueryContext(completable);

        assertThat(actual.getImports()).isEmpty();
        assertThat(actual.getQuery()).isEqualTo("""
                temp_table_0 = from(bucket: "analytics-realtime")
                |> range(start: 2025-02-11T15:12:00Z, stop: 2025-02-11T16:20:00Z)
                |> filter(fn: (r) => r["_measurement"] == "analytics")
                |> filter(fn: (r) => r["deployment"] == "dep_value")
                |> filter(fn: (r) => r["_field"] == "user_hash")
                |> group(columns: [""])
                |> count()
                |> keep(columns: ["_value"])
                |> rename(columns: {_value: "temp_column_0"})
                |> set(key: "temp_column_3", value: "any")
                temp_table_1 = from(bucket: "analytics-realtime")
                |> range(start: 2025-02-11T15:12:00Z, stop: 2025-02-11T16:20:00Z)
                |> filter(fn: (r) => r["_measurement"] == "analytics")
                |> filter(fn: (r) => r["deployment"] == "dep_value")
                |> filter(fn: (r) => r["_field"] == "price")
                |> group(columns: [""])
                |> sum()
                |> keep(columns: ["_value"])
                |> rename(columns: {_value: "temp_column_1"})
                |> set(key: "temp_column_3", value: "any")
                temp_table_2 = from(bucket: "analytics-realtime")
                |> range(start: 2025-02-11T15:12:00Z, stop: 2025-02-11T16:20:00Z)
                |> filter(fn: (r) => r["_measurement"] == "analytics")
                |> filter(fn: (r) => r["deployment"] == "dep_value")
                |> filter(fn: (r) => r["_field"] == "prompt_tokens")
                |> group(columns: [""])
                |> sum()
                |> keep(columns: ["_value"])
                |> rename(columns: {_value: "temp_column_2"})
                |> set(key: "temp_column_3", value: "any")
                temp_table_3 = join(tables: {t1: temp_table_0, t2: temp_table_1}, on: ["temp_column_3"])
                temp_table_4 = join(tables: {t1: temp_table_3, t2: temp_table_2}, on: ["temp_column_3"])
                temp_table_4
                |> group()""");
        assertThat(actual.getColumnNames()).isEqualTo(List.of("temp_column_0", "temp_column_1", "temp_column_2"));
    }

    @Test
    void buildQuery_SimpleAggregate_Alias() {
        var completable = QueryImpl.builder()
                .distinct(false)
                .expressions(List.of(
                        new AliasImpl("a",
                                new AggregationFunctionCallImpl(
                                        new FunctionImpl("count", Type.INT_64, true),
                                        List.of(),
                                        List.of()
                                )),
                        new AliasImpl("b",
                                new AggregationFunctionCallImpl(
                                        new FunctionImpl("sum", Type.DOUBLE, true),
                                        List.of(),
                                        List.of(new ColumnImpl(Type.DOUBLE, "price"))
                                )),
                        new AliasImpl("c",
                                new AggregationFunctionCallImpl(
                                        new FunctionImpl("sum", Type.INT_64, true),
                                        List.of(),
                                        List.of(new ColumnImpl(Type.INT_64, "prompt_tokens"))
                                ))
                ))
                .from(TableImpl.builder().name("analytics").build())
                .where(AndImpl.of(List.of(
                        BinaryComparisonFilterImpl.of(new ColumnImpl(Type.TIMESTAMP, "_time"),
                                BinaryComparisonOperator.GREATER_OR_EQUALS, new ConstantImpl(Type.TIMESTAMP, 1739286720000L)),
                        BinaryComparisonFilterImpl.of(new ColumnImpl(Type.TIMESTAMP, "_time"),
                                BinaryComparisonOperator.LESS, new ConstantImpl(Type.TIMESTAMP, 1739290800000L))
                )))
                .build();


        var actual = fluxQueryBuilder.buildQueryContext(completable);

        assertThat(actual.getImports()).isEmpty();
        assertThat(actual.getQuery()).isEqualTo("""
                temp_table_0 = from(bucket: "analytics-realtime")
                |> range(start: 2025-02-11T15:12:00Z, stop: 2025-02-11T16:20:00Z)
                |> filter(fn: (r) => r["_measurement"] == "analytics")
                |> filter(fn: (r) => r["_field"] == "user_hash")
                |> group(columns: [""])
                |> count()
                |> keep(columns: ["_value"])
                |> rename(columns: {_value: "a"})
                |> set(key: "temp_column_0", value: "any")
                temp_table_1 = from(bucket: "analytics-realtime")
                |> range(start: 2025-02-11T15:12:00Z, stop: 2025-02-11T16:20:00Z)
                |> filter(fn: (r) => r["_measurement"] == "analytics")
                |> filter(fn: (r) => r["_field"] == "price")
                |> group(columns: [""])
                |> sum()
                |> keep(columns: ["_value"])
                |> rename(columns: {_value: "b"})
                |> set(key: "temp_column_0", value: "any")
                temp_table_2 = from(bucket: "analytics-realtime")
                |> range(start: 2025-02-11T15:12:00Z, stop: 2025-02-11T16:20:00Z)
                |> filter(fn: (r) => r["_measurement"] == "analytics")
                |> filter(fn: (r) => r["_field"] == "prompt_tokens")
                |> group(columns: [""])
                |> sum()
                |> keep(columns: ["_value"])
                |> rename(columns: {_value: "c"})
                |> set(key: "temp_column_0", value: "any")
                temp_table_3 = join(tables: {t1: temp_table_0, t2: temp_table_1}, on: ["temp_column_0"])
                temp_table_4 = join(tables: {t1: temp_table_3, t2: temp_table_2}, on: ["temp_column_0"])
                temp_table_4
                |> group()""");
        assertThat(actual.getColumnNames()).isEqualTo(List.of("a", "b", "c"));
    }

    @Test
    void buildQuery_GroupAggregate() {
        var completable = QueryImpl.builder()
                .distinct(false)
                .expressions(List.of(
                        new ColumnImpl(Type.TIMESTAMP, "deployment"),
                        new AggregationFunctionCallImpl(
                                new FunctionImpl("count", Type.INT_64, true),
                                List.of(),
                                List.of()
                        ),
                        new AggregationFunctionCallImpl(
                                new FunctionImpl("sum", Type.DOUBLE, true),
                                List.of(),
                                List.of(new ColumnImpl(Type.DOUBLE, "price"))
                        ),
                        new AggregationFunctionCallImpl(
                                new FunctionImpl("sum", Type.INT_64, true),
                                List.of(),
                                List.of(new ColumnImpl(Type.INT_64, "prompt_tokens"))
                        )
                ))
                .from(TableImpl.builder().name("analytics").build())
                .where(AndImpl.of(List.of(
                        BinaryComparisonFilterImpl.of(new ColumnImpl(Type.TIMESTAMP, "_time"),
                                BinaryComparisonOperator.GREATER_OR_EQUALS, new ConstantImpl(Type.TIMESTAMP, 1739286720000L)),
                        BinaryComparisonFilterImpl.of(new ColumnImpl(Type.TIMESTAMP, "_time"),
                                BinaryComparisonOperator.LESS, new ConstantImpl(Type.TIMESTAMP, 1739290800000L))
                )))
                .groupBy(List.of(
                        new ColumnImpl(Type.TIMESTAMP, "deployment")
                ))
                .orderBy(List.of(
                        SortImpl.of(
                                new AggregationFunctionCallImpl(
                                        new FunctionImpl("sum", Type.INT_64, true),
                                        List.of(),
                                        List.of(new ColumnImpl(Type.INT_64, "prompt_tokens"))
                                ),
                                SortDirection.DESC
                        )
                ))
                .build();


        var actual = fluxQueryBuilder.buildQueryContext(completable);

        assertThat(actual.getImports()).isEmpty();
        assertThat(actual.getQuery()).isEqualTo("""
                temp_table_0 = from(bucket: "analytics-realtime")
                |> range(start: 2025-02-11T15:12:00Z, stop: 2025-02-11T16:20:00Z)
                |> filter(fn: (r) => r["_measurement"] == "analytics")
                |> filter(fn: (r) => r["_field"] == "user_hash")
                |> group(columns: ["deployment"])
                |> count()
                |> keep(columns: ["deployment", "_value"])
                |> rename(columns: {_value: "temp_column_0"})
                temp_table_1 = from(bucket: "analytics-realtime")
                |> range(start: 2025-02-11T15:12:00Z, stop: 2025-02-11T16:20:00Z)
                |> filter(fn: (r) => r["_measurement"] == "analytics")
                |> filter(fn: (r) => r["_field"] == "price")
                |> group(columns: ["deployment"])
                |> sum()
                |> keep(columns: ["deployment", "_value"])
                |> rename(columns: {_value: "temp_column_1"})
                temp_table_2 = from(bucket: "analytics-realtime")
                |> range(start: 2025-02-11T15:12:00Z, stop: 2025-02-11T16:20:00Z)
                |> filter(fn: (r) => r["_measurement"] == "analytics")
                |> filter(fn: (r) => r["_field"] == "prompt_tokens")
                |> group(columns: ["deployment"])
                |> sum()
                |> keep(columns: ["deployment", "_value"])
                |> rename(columns: {_value: "temp_column_2"})
                temp_table_3 = join(tables: {t1: temp_table_0, t2: temp_table_1}, on: ["deployment"])
                temp_table_4 = join(tables: {t1: temp_table_3, t2: temp_table_2}, on: ["deployment"])
                temp_table_4
                |> group()
                |> sort(columns: ["temp_column_2"], desc: true)""");
        assertThat(actual.getColumnNames())
                .isEqualTo(List.of("deployment", "temp_column_0", "temp_column_1", "temp_column_2"));
    }

    @Test
    void buildQuery_GroupAggregate_Filtering() {
        var completable = QueryImpl.builder()
                .distinct(false)
                .expressions(List.of(
                        new ColumnImpl(Type.TIMESTAMP, "deployment"),
                        new AggregationFunctionCallImpl(
                                new FunctionImpl("count", Type.INT_64, true),
                                List.of(),
                                List.of()
                        ),
                        new AggregationFunctionCallImpl(
                                new FunctionImpl("sum", Type.DOUBLE, true),
                                List.of(),
                                List.of(new ColumnImpl(Type.DOUBLE, "price"))
                        ),
                        new AggregationFunctionCallImpl(
                                new FunctionImpl("sum", Type.INT_64, true),
                                List.of(),
                                List.of(new ColumnImpl(Type.INT_64, "prompt_tokens"))
                        )
                ))
                .from(TableImpl.builder().name("analytics").build())
                .where(AndImpl.of(List.of(
                        BinaryComparisonFilterImpl.of(new ColumnImpl(Type.TIMESTAMP, "_time"),
                                BinaryComparisonOperator.GREATER_OR_EQUALS, new ConstantImpl(Type.TIMESTAMP, 1739286720000L)),
                        BinaryComparisonFilterImpl.of(new ColumnImpl(Type.TIMESTAMP, "_time"),
                                BinaryComparisonOperator.LESS, new ConstantImpl(Type.TIMESTAMP, 1739290800000L)),
                        BinaryComparisonFilterImpl.of(new ColumnImpl(Type.STRING, "deployment"),
                                BinaryComparisonOperator.EQUALS, new ConstantImpl(Type.STRING, "dep_value"))
                )))
                .groupBy(List.of(
                        new ColumnImpl(Type.TIMESTAMP, "deployment")
                ))
                .orderBy(List.of(
                        SortImpl.of(
                                new AggregationFunctionCallImpl(
                                        new FunctionImpl("sum", Type.INT_64, true),
                                        List.of(),
                                        List.of(new ColumnImpl(Type.INT_64, "prompt_tokens"))
                                ),
                                SortDirection.DESC
                        )
                ))
                .build();


        var actual = fluxQueryBuilder.buildQueryContext(completable);

        assertThat(actual.getImports()).isEmpty();
        assertThat(actual.getQuery()).isEqualTo("""
                temp_table_0 = from(bucket: "analytics-realtime")
                |> range(start: 2025-02-11T15:12:00Z, stop: 2025-02-11T16:20:00Z)
                |> filter(fn: (r) => r["_measurement"] == "analytics")
                |> filter(fn: (r) => r["deployment"] == "dep_value")
                |> filter(fn: (r) => r["_field"] == "user_hash")
                |> group(columns: ["deployment"])
                |> count()
                |> keep(columns: ["deployment", "_value"])
                |> rename(columns: {_value: "temp_column_0"})
                temp_table_1 = from(bucket: "analytics-realtime")
                |> range(start: 2025-02-11T15:12:00Z, stop: 2025-02-11T16:20:00Z)
                |> filter(fn: (r) => r["_measurement"] == "analytics")
                |> filter(fn: (r) => r["deployment"] == "dep_value")
                |> filter(fn: (r) => r["_field"] == "price")
                |> group(columns: ["deployment"])
                |> sum()
                |> keep(columns: ["deployment", "_value"])
                |> rename(columns: {_value: "temp_column_1"})
                temp_table_2 = from(bucket: "analytics-realtime")
                |> range(start: 2025-02-11T15:12:00Z, stop: 2025-02-11T16:20:00Z)
                |> filter(fn: (r) => r["_measurement"] == "analytics")
                |> filter(fn: (r) => r["deployment"] == "dep_value")
                |> filter(fn: (r) => r["_field"] == "prompt_tokens")
                |> group(columns: ["deployment"])
                |> sum()
                |> keep(columns: ["deployment", "_value"])
                |> rename(columns: {_value: "temp_column_2"})
                temp_table_3 = join(tables: {t1: temp_table_0, t2: temp_table_1}, on: ["deployment"])
                temp_table_4 = join(tables: {t1: temp_table_3, t2: temp_table_2}, on: ["deployment"])
                temp_table_4
                |> group()
                |> sort(columns: ["temp_column_2"], desc: true)""");
        assertThat(actual.getColumnNames())
                .isEqualTo(List.of("deployment", "temp_column_0", "temp_column_1", "temp_column_2"));
    }

    @Test
    void buildQuery_AggregateOverDistinct() {
        var completable =
                QueryImpl.builder()
                        .expressions(List.of(
                                new AggregationFunctionCallImpl(
                                        new FunctionImpl("count", Type.INT_64, true),
                                        List.of(),
                                        List.of()
                                )
                        ))
                        .from(
                                QueryImpl.builder()
                                        .distinct(true)
                                        .expressions(List.of(
                                                new ColumnImpl(Type.STRING, "user_hash")
                                        ))
                                        .from(TableImpl.builder()
                                                .name("analytics")
                                                .build())
                                        .where(AndImpl.of(List.of(
                                                BinaryComparisonFilterImpl.of(new ColumnImpl(Type.TIMESTAMP, "_time"),
                                                        BinaryComparisonOperator.GREATER_OR_EQUALS, new ConstantImpl(Type.TIMESTAMP, 1739286720000L)),
                                                BinaryComparisonFilterImpl.of(new ColumnImpl(Type.TIMESTAMP, "_time"),
                                                        BinaryComparisonOperator.LESS, new ConstantImpl(Type.TIMESTAMP, 1739290800000L))
                                        )))
                                        .build()
                        )
                        .build();


        var actual = fluxQueryBuilder.buildQueryContext(completable);

        assertThat(actual.getImports()).isEmpty();
        assertThat(actual.getQuery()).isEqualTo("""
                temp_table_0 = from(bucket: "analytics-realtime")
                |> range(start: 2025-02-11T15:12:00Z, stop: 2025-02-11T16:20:00Z)
                |> filter(fn: (r) => r["_measurement"] == "analytics")
                |> filter(fn: (r) => r["_field"] == "user_hash")
                |> group()
                |> distinct(column: "_value")
                |> rename(columns: {_value: "user_hash"})
                |> keep(columns: ["user_hash"])
                temp_table_0
                |> group(columns: [""])
                |> count(column: "user_hash")
                |> keep(columns: ["user_hash"])
                |> rename(columns: {user_hash: "temp_column_0"})
                |> group()""");
        assertThat(actual.getColumnNames()).isEqualTo(List.of("temp_column_0"));
    }

    @Test
    void buildQuery_Windowing() {
        var completable =
                QueryImpl.builder()
                        .expressions(List.of(
                                new GroupFunctionCallImpl(
                                        new FunctionImpl("window", null, true),
                                        List.of(),
                                        List.of(
                                                new ColumnImpl(Type.TIMESTAMP, "_time"),
                                                NumberConstantImpl.valueOf(10L),
                                                new ConstantImpl(Type.STRING, "m")
                                        )
                                ),
                                new AggregationFunctionCallImpl(
                                        new FunctionImpl("sum", Type.DOUBLE, true),
                                        List.of(),
                                        List.of(new ColumnImpl(Type.DOUBLE, "price"))
                                )
                        ))
                        .from(TableImpl.builder().name("analytics").build())
                        .where(AndImpl.of(List.of(
                                BinaryComparisonFilterImpl.of(new ColumnImpl(Type.TIMESTAMP, "_time"),
                                        BinaryComparisonOperator.GREATER_OR_EQUALS, new ConstantImpl(Type.TIMESTAMP, 1739286720000L)),
                                BinaryComparisonFilterImpl.of(new ColumnImpl(Type.TIMESTAMP, "_time"),
                                        BinaryComparisonOperator.LESS, new ConstantImpl(Type.TIMESTAMP, 1739290800000L))
                        )))
                        .groupBy(List.of(
                                new GroupFunctionCallImpl(
                                        new FunctionImpl("window", null, true),
                                        List.of(),
                                        List.of(
                                                new ColumnImpl(Type.TIMESTAMP, "_time"),
                                                NumberConstantImpl.valueOf(10L),
                                                new ConstantImpl(Type.STRING, "m")
                                        )
                                )
                        ))
                        .build();


        var actual = fluxQueryBuilder.buildQueryContext(completable);

        assertThat(actual.getImports()).isEmpty();
        assertThat(actual.getQuery()).isEqualTo("""
                from(bucket: "analytics-realtime")
                |> range(start: 2025-02-11T15:12:00Z, stop: 2025-02-11T16:20:00Z)
                |> filter(fn: (r) => r["_measurement"] == "analytics")
                |> filter(fn: (r) => r["_field"] == "price")
                |> group()
                |> aggregateWindow(every: 10m, fn: sum, createEmpty: false)
                |> rename(columns: {_time: "temp_column_0", _value: "temp_column_1"})""");
        assertThat(actual.getColumnNames()).isEqualTo(List.of("temp_column_0", "temp_column_1"));
    }

    @Test
    void buildQuery_Windowing_Filtering() {
        var completable =
                QueryImpl.builder()
                        .expressions(List.of(
                                new GroupFunctionCallImpl(
                                        new FunctionImpl("window", null, true),
                                        List.of(),
                                        List.of(
                                                new ColumnImpl(Type.TIMESTAMP, "_time"),
                                                NumberConstantImpl.valueOf(10L),
                                                new ConstantImpl(Type.STRING, "m")
                                        )
                                ),
                                new AggregationFunctionCallImpl(
                                        new FunctionImpl("sum", Type.DOUBLE, true),
                                        List.of(),
                                        List.of(new ColumnImpl(Type.DOUBLE, "price"))
                                )
                        ))
                        .from(TableImpl.builder().name("analytics").build())
                        .where(AndImpl.of(List.of(
                                BinaryComparisonFilterImpl.of(new ColumnImpl(Type.TIMESTAMP, "_time"),
                                        BinaryComparisonOperator.GREATER_OR_EQUALS, new ConstantImpl(Type.TIMESTAMP, 1739286720000L)),
                                BinaryComparisonFilterImpl.of(new ColumnImpl(Type.TIMESTAMP, "_time"),
                                        BinaryComparisonOperator.LESS, new ConstantImpl(Type.TIMESTAMP, 1739290800000L)),
                                BinaryComparisonFilterImpl.of(new ColumnImpl(Type.STRING, "deployment"),
                                        BinaryComparisonOperator.EQUALS, new ConstantImpl(Type.STRING, "dep_value"))
                        )))
                        .groupBy(List.of(
                                new GroupFunctionCallImpl(
                                        new FunctionImpl("window", null, true),
                                        List.of(),
                                        List.of(
                                                new ColumnImpl(Type.TIMESTAMP, "_time"),
                                                NumberConstantImpl.valueOf(10L),
                                                new ConstantImpl(Type.STRING, "m")
                                        )
                                )
                        ))
                        .build();


        var actual = fluxQueryBuilder.buildQueryContext(completable);

        assertThat(actual.getImports()).isEmpty();
        assertThat(actual.getQuery()).isEqualTo("""
                from(bucket: "analytics-realtime")
                |> range(start: 2025-02-11T15:12:00Z, stop: 2025-02-11T16:20:00Z)
                |> filter(fn: (r) => r["_measurement"] == "analytics")
                |> filter(fn: (r) => r["deployment"] == "dep_value")
                |> filter(fn: (r) => r["_field"] == "price")
                |> group()
                |> aggregateWindow(every: 10m, fn: sum, createEmpty: false)
                |> rename(columns: {_time: "temp_column_0", _value: "temp_column_1"})""");
        assertThat(actual.getColumnNames()).isEqualTo(List.of("temp_column_0", "temp_column_1"));
    }

    @Test
    void buildQuery_Windowing_Alias() {
        var completable =
                QueryImpl.builder()
                        .expressions(List.of(
                                new AliasImpl("a",
                                        new GroupFunctionCallImpl(
                                                new FunctionImpl("window", null, true),
                                                List.of(),
                                                List.of(
                                                        new ColumnImpl(Type.TIMESTAMP, "_time"),
                                                        NumberConstantImpl.valueOf(10L),
                                                        new ConstantImpl(Type.STRING, "m")
                                                )
                                        )
                                ),
                                new AliasImpl("b",
                                        new AggregationFunctionCallImpl(
                                                new FunctionImpl("sum", Type.DOUBLE, true),
                                                List.of(),
                                                List.of(new ColumnImpl(Type.DOUBLE, "price"))
                                        )
                                )
                        ))
                        .from(TableImpl.builder().name("analytics").build())
                        .where(AndImpl.of(List.of(
                                BinaryComparisonFilterImpl.of(new ColumnImpl(Type.TIMESTAMP, "_time"),
                                        BinaryComparisonOperator.GREATER_OR_EQUALS, new ConstantImpl(Type.TIMESTAMP, 1739286720000L)),
                                BinaryComparisonFilterImpl.of(new ColumnImpl(Type.TIMESTAMP, "_time"),
                                        BinaryComparisonOperator.LESS, new ConstantImpl(Type.TIMESTAMP, 1739290800000L))
                        )))
                        .groupBy(List.of(
                                new GroupFunctionCallImpl(
                                        new FunctionImpl("window", null, true),
                                        List.of(),
                                        List.of(
                                                new ColumnImpl(Type.TIMESTAMP, "_time"),
                                                NumberConstantImpl.valueOf(10L),
                                                new ConstantImpl(Type.STRING, "m")
                                        )
                                )
                        ))
                        .build();


        var actual = fluxQueryBuilder.buildQueryContext(completable);

        assertThat(actual.getImports()).isEmpty();
        assertThat(actual.getQuery()).isEqualTo("""
                from(bucket: "analytics-realtime")
                |> range(start: 2025-02-11T15:12:00Z, stop: 2025-02-11T16:20:00Z)
                |> filter(fn: (r) => r["_measurement"] == "analytics")
                |> filter(fn: (r) => r["_field"] == "price")
                |> group()
                |> aggregateWindow(every: 10m, fn: sum, createEmpty: false)
                |> rename(columns: {_time: "a", _value: "b"})""");
        assertThat(actual.getColumnNames()).isEqualTo(List.of("a", "b"));
    }

}