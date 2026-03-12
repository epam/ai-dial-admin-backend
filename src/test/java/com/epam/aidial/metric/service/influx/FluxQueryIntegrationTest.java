package com.epam.aidial.metric.service.influx;

import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.epam.aidial.cfg.utils.ResourceUtils;
import com.epam.aidial.metric.config.InfluxDatasetConfiguration;
import com.epam.aidial.metric.model.configuration.DatasetDeclaration;
import com.epam.aidial.metric.model.configuration.influx.InfluxDatasetDeclaration;
import com.epam.aidial.metric.model.influx.FluxQueryContext;
import com.epam.aidial.metric.model.influx.FluxStandardImports;
import com.epam.aidial.ql.LanguageConverter;
import com.epam.aidial.ql.common.model.enums.BinaryComparisonOperator;
import com.epam.aidial.ql.common.model.enums.SortDirection;
import com.epam.aidial.ql.deserializers.sql.QueryParserUtil;
import com.epam.aidial.ql.dto.CompletableDto;
import com.epam.aidial.ql.dto.FilterDto;
import com.epam.aidial.ql.dto.QueryDto;
import com.epam.aidial.ql.dto.SortDto;
import com.epam.aidial.ql.dto.StringExpressionDto;
import com.epam.aidial.ql.dto.TableDto;
import com.epam.aidial.ql.dto.filters.AndDto;
import com.epam.aidial.ql.dto.filters.BinaryComparisonFilterDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class FluxQueryIntegrationTest {

    private static final ObjectMapper OBJECT_MAPPER = JsonMapperConfiguration.createJsonMapper();

    private InfluxEngine engine;
    private LanguageConverter languageConverter;
    private FluxQueryBuilderFactory fluxQueryBuilderFactory;

    @BeforeEach
    void setUp() throws JsonProcessingException {
        var testMetricConfig = ResourceUtils.readResource("/metrics/metric.config.influx2.json");
        var datasetDeclaration = (InfluxDatasetDeclaration) OBJECT_MAPPER.readValue(testMetricConfig, DatasetDeclaration.class);

        var datasetConfiguration = new InfluxDatasetConfiguration();
        datasetConfiguration.setDefaultPageSize(50);

        fluxQueryBuilderFactory = new FluxQueryBuilderFactory(datasetDeclaration, datasetConfiguration);
        engine = new InfluxEngine(datasetDeclaration, null, fluxQueryBuilderFactory);
        languageConverter = new LanguageConverter(engine);
    }

    // -- integration helpers --

    private FluxQueryContext buildFromSql(String sql) {
        var dto = QueryParserUtil.parse(sql);
        return buildFromDto(dto);
    }

    private FluxQueryContext buildFromJson(CompletableDto dto) {
        return buildFromDto(dto);
    }

    private FluxQueryContext buildFromDto(CompletableDto dto) {
        var completable = languageConverter.convert(dto, engine.getTables());
        return fluxQueryBuilderFactory.createQueryBuilder().buildQueryContext(completable);
    }

    // -- common JSON helpers --

    private static StringExpressionDto expr(String expression) {
        return new StringExpressionDto(expression);
    }

    private static AndDto timeRangeFilter(FilterDto... extra) {
        var filters = new ArrayList<FilterDto>();
        filters.add(new BinaryComparisonFilterDto(expr("_time"), BinaryComparisonOperator.GREATER_OR_EQUALS, expr("'2025-02-11T15:12:00Z'")));
        filters.add(new BinaryComparisonFilterDto(expr("_time"), BinaryComparisonOperator.LESS, expr("'2025-02-11T16:20:00Z'")));
        filters.addAll(List.of(extra));
        return new AndDto(filters);
    }

    // ===== 1. simpleSelect =====

    @Test
    void simpleSelect_fromSql() {
        var result = buildFromSql("SELECT deployment, price, prompt_tokens FROM analytics WHERE _time >= '2025-02-11T15:12:00Z' AND _time < '2025-02-11T16:20:00Z' ORDER BY prompt_tokens DESC");

        assertThat(result.getImports()).isEqualTo(Set.of(FluxStandardImports.SCHEMA));
        assertThat(result.getQuery()).isEqualTo("""
                from(bucket: "analytics-realtime")
                |> range(start: 2025-02-11T15:12:00Z, stop: 2025-02-11T16:20:00Z)
                |> filter(fn: (r) => r["_measurement"] == "analytics")
                |> schema.fieldsAsCols()
                |> keep(columns: ["deployment", "price", "prompt_tokens"])
                |> group()
                |> sort(columns: ["prompt_tokens"], desc: true)""");
        assertThat(result.getColumnNames()).isEqualTo(List.of("deployment", "price", "prompt_tokens"));
    }

    @Test
    void simpleSelect_fromJson() {
        var queryDto = new QueryDto();
        queryDto.setDistinct(false);
        queryDto.setExpressions(List.of(expr("deployment"), expr("price"), expr("prompt_tokens")));
        queryDto.setFrom(new TableDto("analytics"));
        queryDto.setWhere(timeRangeFilter());
        queryDto.setOrderBy(List.of(new SortDto(expr("prompt_tokens"), SortDirection.DESC)));

        var result = buildFromJson(queryDto);

        assertThat(result.getImports()).isEqualTo(Set.of(FluxStandardImports.SCHEMA));
        assertThat(result.getQuery()).isEqualTo("""
                from(bucket: "analytics-realtime")
                |> range(start: 2025-02-11T15:12:00Z, stop: 2025-02-11T16:20:00Z)
                |> filter(fn: (r) => r["_measurement"] == "analytics")
                |> schema.fieldsAsCols()
                |> keep(columns: ["deployment", "price", "prompt_tokens"])
                |> group()
                |> sort(columns: ["prompt_tokens"], desc: true)""");
        assertThat(result.getColumnNames()).isEqualTo(List.of("deployment", "price", "prompt_tokens"));
    }

    // ===== 2. selectWithAliases =====

    @Test
    void selectWithAliases_fromSql() {
        var result = buildFromSql("SELECT deployment AS a, price AS b, prompt_tokens AS c FROM analytics WHERE _time >= '2025-02-11T15:12:00Z' AND _time < '2025-02-11T16:20:00Z' ORDER BY prompt_tokens DESC");

        assertThat(result.getImports()).isEqualTo(Set.of(FluxStandardImports.SCHEMA));
        assertThat(result.getQuery()).isEqualTo("""
                from(bucket: "analytics-realtime")
                |> range(start: 2025-02-11T15:12:00Z, stop: 2025-02-11T16:20:00Z)
                |> filter(fn: (r) => r["_measurement"] == "analytics")
                |> schema.fieldsAsCols()
                |> keep(columns: ["deployment", "price", "prompt_tokens"])
                |> rename(columns: {deployment: "a", price: "b", prompt_tokens: "c"})
                |> group()
                |> sort(columns: ["c"], desc: true)""");
        assertThat(result.getColumnNames()).isEqualTo(List.of("a", "b", "c"));
    }

    @Test
    void selectWithAliases_fromJson() {
        var queryDto = new QueryDto();
        queryDto.setDistinct(false);
        queryDto.setExpressions(List.of(expr("deployment AS a"), expr("price AS b"), expr("prompt_tokens AS c")));
        queryDto.setFrom(new TableDto("analytics"));
        queryDto.setWhere(timeRangeFilter());
        queryDto.setOrderBy(List.of(new SortDto(expr("prompt_tokens"), SortDirection.DESC)));

        var result = buildFromJson(queryDto);

        assertThat(result.getImports()).isEqualTo(Set.of(FluxStandardImports.SCHEMA));
        assertThat(result.getQuery()).isEqualTo("""
                from(bucket: "analytics-realtime")
                |> range(start: 2025-02-11T15:12:00Z, stop: 2025-02-11T16:20:00Z)
                |> filter(fn: (r) => r["_measurement"] == "analytics")
                |> schema.fieldsAsCols()
                |> keep(columns: ["deployment", "price", "prompt_tokens"])
                |> rename(columns: {deployment: "a", price: "b", prompt_tokens: "c"})
                |> group()
                |> sort(columns: ["c"], desc: true)""");
        assertThat(result.getColumnNames()).isEqualTo(List.of("a", "b", "c"));
    }

    // ===== 3. distinctValueSelect =====

    @Test
    void distinctValueSelect_fromSql() {
        var result = buildFromSql("SELECT DISTINCT user_hash FROM analytics WHERE _time >= '2025-02-11T15:12:00Z' AND _time < '2025-02-11T16:20:00Z'");

        assertThat(result.getImports()).isEqualTo(Collections.emptySet());
        assertThat(result.getQuery()).isEqualTo("""
                from(bucket: "analytics-realtime")
                |> range(start: 2025-02-11T15:12:00Z, stop: 2025-02-11T16:20:00Z)
                |> filter(fn: (r) => r["_measurement"] == "analytics")
                |> filter(fn: (r) => r["_field"] == "user_hash")
                |> group()
                |> distinct(column: "_value")
                |> rename(columns: {_value: "user_hash"})
                |> keep(columns: ["user_hash"])""");
        assertThat(result.getColumnNames()).isEqualTo(List.of("user_hash"));
    }

    @Test
    void distinctValueSelect_fromJson() {
        var queryDto = new QueryDto();
        queryDto.setDistinct(true);
        queryDto.setExpressions(List.of(expr("user_hash")));
        queryDto.setFrom(new TableDto("analytics"));
        queryDto.setWhere(timeRangeFilter());

        var result = buildFromJson(queryDto);

        assertThat(result.getImports()).isEqualTo(Collections.emptySet());
        assertThat(result.getQuery()).isEqualTo("""
                from(bucket: "analytics-realtime")
                |> range(start: 2025-02-11T15:12:00Z, stop: 2025-02-11T16:20:00Z)
                |> filter(fn: (r) => r["_measurement"] == "analytics")
                |> filter(fn: (r) => r["_field"] == "user_hash")
                |> group()
                |> distinct(column: "_value")
                |> rename(columns: {_value: "user_hash"})
                |> keep(columns: ["user_hash"])""");
        assertThat(result.getColumnNames()).isEqualTo(List.of("user_hash"));
    }

    // ===== 4. distinctValueSelectWithAlias =====

    @Test
    void distinctValueSelectWithAlias_fromSql() {
        var result = buildFromSql("SELECT DISTINCT user_hash AS a FROM analytics WHERE _time >= '2025-02-11T15:12:00Z' AND _time < '2025-02-11T16:20:00Z'");

        assertThat(result.getImports()).isEqualTo(Collections.emptySet());
        assertThat(result.getQuery()).isEqualTo("""
                from(bucket: "analytics-realtime")
                |> range(start: 2025-02-11T15:12:00Z, stop: 2025-02-11T16:20:00Z)
                |> filter(fn: (r) => r["_measurement"] == "analytics")
                |> filter(fn: (r) => r["_field"] == "user_hash")
                |> group()
                |> distinct(column: "_value")
                |> rename(columns: {_value: "a"})
                |> keep(columns: ["a"])""");
        assertThat(result.getColumnNames()).isEqualTo(List.of("a"));
    }

    @Test
    void distinctValueSelectWithAlias_fromJson() {
        var queryDto = new QueryDto();
        queryDto.setDistinct(true);
        queryDto.setExpressions(List.of(expr("user_hash AS a")));
        queryDto.setFrom(new TableDto("analytics"));
        queryDto.setWhere(timeRangeFilter());

        var result = buildFromJson(queryDto);

        assertThat(result.getImports()).isEqualTo(Collections.emptySet());
        assertThat(result.getQuery()).isEqualTo("""
                from(bucket: "analytics-realtime")
                |> range(start: 2025-02-11T15:12:00Z, stop: 2025-02-11T16:20:00Z)
                |> filter(fn: (r) => r["_measurement"] == "analytics")
                |> filter(fn: (r) => r["_field"] == "user_hash")
                |> group()
                |> distinct(column: "_value")
                |> rename(columns: {_value: "a"})
                |> keep(columns: ["a"])""");
        assertThat(result.getColumnNames()).isEqualTo(List.of("a"));
    }

    // ===== 5. distinctValueSelectWithFiltering =====

    @Test
    void distinctValueSelectWithFiltering_fromSql() {
        var result = buildFromSql("SELECT DISTINCT user_hash FROM analytics WHERE _time >= '2025-02-11T15:12:00Z' AND _time < '2025-02-11T16:20:00Z' AND deployment = 'dep_value'");

        assertThat(result.getImports()).isEqualTo(Collections.emptySet());
        assertThat(result.getQuery()).isEqualTo("""
                from(bucket: "analytics-realtime")
                |> range(start: 2025-02-11T15:12:00Z, stop: 2025-02-11T16:20:00Z)
                |> filter(fn: (r) => r["_measurement"] == "analytics")
                |> filter(fn: (r) => r["_field"] == "user_hash")
                |> filter(fn: (r) => r["deployment"] == "dep_value")
                |> group()
                |> distinct(column: "_value")
                |> rename(columns: {_value: "user_hash"})
                |> keep(columns: ["user_hash"])""");
        assertThat(result.getColumnNames()).isEqualTo(List.of("user_hash"));
    }

    @Test
    void distinctValueSelectWithFiltering_fromJson() {
        var queryDto = new QueryDto();
        queryDto.setDistinct(true);
        queryDto.setExpressions(List.of(expr("user_hash")));
        queryDto.setFrom(new TableDto("analytics"));
        queryDto.setWhere(timeRangeFilter(
                new BinaryComparisonFilterDto(expr("deployment"), BinaryComparisonOperator.EQUALS, expr("'dep_value'"))
        ));

        var result = buildFromJson(queryDto);

        assertThat(result.getImports()).isEqualTo(Collections.emptySet());
        assertThat(result.getQuery()).isEqualTo("""
                from(bucket: "analytics-realtime")
                |> range(start: 2025-02-11T15:12:00Z, stop: 2025-02-11T16:20:00Z)
                |> filter(fn: (r) => r["_measurement"] == "analytics")
                |> filter(fn: (r) => r["_field"] == "user_hash")
                |> filter(fn: (r) => r["deployment"] == "dep_value")
                |> group()
                |> distinct(column: "_value")
                |> rename(columns: {_value: "user_hash"})
                |> keep(columns: ["user_hash"])""");
        assertThat(result.getColumnNames()).isEqualTo(List.of("user_hash"));
    }

    // ===== 6. distinctTagSelect =====

    @Test
    void distinctTagSelect_fromSql() {
        var result = buildFromSql("SELECT DISTINCT deployment FROM analytics WHERE _time >= '2025-02-11T15:12:00Z' AND _time < '2025-02-11T16:20:00Z'");

        assertThat(result.getImports()).isEqualTo(Collections.emptySet());
        assertThat(result.getQuery()).isEqualTo("""
                from(bucket: "analytics-realtime")
                |> range(start: 2025-02-11T15:12:00Z, stop: 2025-02-11T16:20:00Z)
                |> filter(fn: (r) => r["_measurement"] == "analytics")
                |> keep(columns: ["deployment"])
                |> group()
                |> distinct(column: "deployment")
                |> rename(columns: {_value: "deployment"})""");
        assertThat(result.getColumnNames()).isEqualTo(List.of("deployment"));
    }

    @Test
    void distinctTagSelect_fromJson() {
        var queryDto = new QueryDto();
        queryDto.setDistinct(true);
        queryDto.setExpressions(List.of(expr("deployment")));
        queryDto.setFrom(new TableDto("analytics"));
        queryDto.setWhere(timeRangeFilter());

        var result = buildFromJson(queryDto);

        assertThat(result.getImports()).isEqualTo(Collections.emptySet());
        assertThat(result.getQuery()).isEqualTo("""
                from(bucket: "analytics-realtime")
                |> range(start: 2025-02-11T15:12:00Z, stop: 2025-02-11T16:20:00Z)
                |> filter(fn: (r) => r["_measurement"] == "analytics")
                |> keep(columns: ["deployment"])
                |> group()
                |> distinct(column: "deployment")
                |> rename(columns: {_value: "deployment"})""");
        assertThat(result.getColumnNames()).isEqualTo(List.of("deployment"));
    }

    // ===== 7. distinctTagSelectWithFiltering =====

    @Test
    void distinctTagSelectWithFiltering_fromSql() {
        var result = buildFromSql("SELECT DISTINCT deployment FROM analytics WHERE _time >= '2025-02-11T15:12:00Z' AND _time < '2025-02-11T16:20:00Z' AND deployment = 'dep_value'");

        assertThat(result.getImports()).isEqualTo(Collections.emptySet());
        assertThat(result.getQuery()).isEqualTo("""
                from(bucket: "analytics-realtime")
                |> range(start: 2025-02-11T15:12:00Z, stop: 2025-02-11T16:20:00Z)
                |> filter(fn: (r) => r["_measurement"] == "analytics")
                |> filter(fn: (r) => r["deployment"] == "dep_value")
                |> keep(columns: ["deployment"])
                |> group()
                |> distinct(column: "deployment")
                |> rename(columns: {_value: "deployment"})""");
        assertThat(result.getColumnNames()).isEqualTo(List.of("deployment"));
    }

    @Test
    void distinctTagSelectWithFiltering_fromJson() {
        var queryDto = new QueryDto();
        queryDto.setDistinct(true);
        queryDto.setExpressions(List.of(expr("deployment")));
        queryDto.setFrom(new TableDto("analytics"));
        queryDto.setWhere(timeRangeFilter(
                new BinaryComparisonFilterDto(expr("deployment"), BinaryComparisonOperator.EQUALS, expr("'dep_value'"))
        ));

        var result = buildFromJson(queryDto);

        assertThat(result.getImports()).isEqualTo(Collections.emptySet());
        assertThat(result.getQuery()).isEqualTo("""
                from(bucket: "analytics-realtime")
                |> range(start: 2025-02-11T15:12:00Z, stop: 2025-02-11T16:20:00Z)
                |> filter(fn: (r) => r["_measurement"] == "analytics")
                |> filter(fn: (r) => r["deployment"] == "dep_value")
                |> keep(columns: ["deployment"])
                |> group()
                |> distinct(column: "deployment")
                |> rename(columns: {_value: "deployment"})""");
        assertThat(result.getColumnNames()).isEqualTo(List.of("deployment"));
    }

    // ===== 8. simpleAggregate =====

    @Test
    void simpleAggregate_fromSql() {
        var result = buildFromSql("SELECT count(), sum(price), sum(prompt_tokens) FROM analytics WHERE _time >= '2025-02-11T15:12:00Z' AND _time < '2025-02-11T16:20:00Z'");

        assertThat(result.getImports()).isEqualTo(Collections.emptySet());
        assertThat(result.getQuery()).isEqualTo("""
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
        assertThat(result.getColumnNames()).isEqualTo(List.of("temp_column_0", "temp_column_1", "temp_column_2"));
    }

    @Test
    void simpleAggregate_fromJson() {
        var queryDto = new QueryDto();
        queryDto.setDistinct(false);
        queryDto.setExpressions(List.of(expr("count()"), expr("sum(price)"), expr("sum(prompt_tokens)")));
        queryDto.setFrom(new TableDto("analytics"));
        queryDto.setWhere(timeRangeFilter());

        var result = buildFromJson(queryDto);

        assertThat(result.getImports()).isEqualTo(Collections.emptySet());
        assertThat(result.getQuery()).isEqualTo("""
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
        assertThat(result.getColumnNames()).isEqualTo(List.of("temp_column_0", "temp_column_1", "temp_column_2"));
    }

    // ===== 9. aggregateWithAliases =====

    @Test
    void aggregateWithAliases_fromSql() {
        var result = buildFromSql("SELECT count() AS a, sum(price) AS b, sum(prompt_tokens) AS c FROM analytics WHERE _time >= '2025-02-11T15:12:00Z' AND _time < '2025-02-11T16:20:00Z'");

        assertThat(result.getImports()).isEqualTo(Collections.emptySet());
        assertThat(result.getQuery()).isEqualTo("""
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
        assertThat(result.getColumnNames()).isEqualTo(List.of("a", "b", "c"));
    }

    @Test
    void aggregateWithAliases_fromJson() {
        var queryDto = new QueryDto();
        queryDto.setDistinct(false);
        queryDto.setExpressions(List.of(expr("count() AS a"), expr("sum(price) AS b"), expr("sum(prompt_tokens) AS c")));
        queryDto.setFrom(new TableDto("analytics"));
        queryDto.setWhere(timeRangeFilter());

        var result = buildFromJson(queryDto);

        assertThat(result.getImports()).isEqualTo(Collections.emptySet());
        assertThat(result.getQuery()).isEqualTo("""
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
        assertThat(result.getColumnNames()).isEqualTo(List.of("a", "b", "c"));
    }

    // ===== 10. groupAggregate =====

    @Test
    void groupAggregate_fromSql() {
        var result = buildFromSql("SELECT deployment, count(), sum(price) FROM analytics WHERE _time >= '2025-02-11T15:12:00Z' AND _time < '2025-02-11T16:20:00Z' GROUP BY deployment ORDER BY deployment ASC");

        assertThat(result.getImports()).isEqualTo(Collections.emptySet());
        assertThat(result.getQuery()).isEqualTo("""
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
                temp_table_2 = join(tables: {t1: temp_table_0, t2: temp_table_1}, on: ["deployment"])
                temp_table_2
                |> group()
                |> sort(columns: ["deployment"], desc: false)""");
        assertThat(result.getColumnNames()).isEqualTo(List.of("deployment", "temp_column_0", "temp_column_1"));
    }

    @Test
    void groupAggregate_fromJson() {
        var queryDto = new QueryDto();
        queryDto.setDistinct(false);
        queryDto.setExpressions(List.of(expr("deployment"), expr("count()"), expr("sum(price)")));
        queryDto.setFrom(new TableDto("analytics"));
        queryDto.setWhere(timeRangeFilter());
        queryDto.setGroupBy(List.of(expr("deployment")));
        queryDto.setOrderBy(List.of(new SortDto(expr("deployment"), SortDirection.ASC)));

        var result = buildFromJson(queryDto);

        assertThat(result.getImports()).isEqualTo(Collections.emptySet());
        assertThat(result.getQuery()).isEqualTo("""
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
                temp_table_2 = join(tables: {t1: temp_table_0, t2: temp_table_1}, on: ["deployment"])
                temp_table_2
                |> group()
                |> sort(columns: ["deployment"], desc: false)""");
        assertThat(result.getColumnNames()).isEqualTo(List.of("deployment", "temp_column_0", "temp_column_1"));
    }

    // ===== 11. windowAggregation =====

    @Test
    void windowAggregation_fromSql() {
        var result = buildFromSql("SELECT window(_time, 1, 'h') AS time_window, count() AS total FROM analytics WHERE _time >= '2025-02-11T15:12:00Z' AND _time < '2025-02-11T16:20:00Z' GROUP BY time_window");

        assertThat(result.getImports()).isEqualTo(Collections.emptySet());
        assertThat(result.getQuery()).isEqualTo("""
                from(bucket: "analytics-realtime")
                |> range(start: 2025-02-11T15:12:00Z, stop: 2025-02-11T16:20:00Z)
                |> filter(fn: (r) => r["_measurement"] == "analytics")
                |> filter(fn: (r) => r["_field"] == "user_hash")
                |> group()
                |> aggregateWindow(every: 1h, fn: count, createEmpty: false)
                |> rename(columns: {_time: "time_window", _value: "total"})""");
        assertThat(result.getColumnNames()).isEqualTo(List.of("time_window", "total"));
    }

    @Test
    void windowAggregation_fromJson() {
        var queryDto = new QueryDto();
        queryDto.setDistinct(false);
        queryDto.setExpressions(List.of(expr("window(_time, 1, 'h') AS time_window"), expr("count() AS total")));
        queryDto.setFrom(new TableDto("analytics"));
        queryDto.setWhere(timeRangeFilter());
        queryDto.setGroupBy(List.of(expr("time_window")));

        var result = buildFromJson(queryDto);

        assertThat(result.getImports()).isEqualTo(Collections.emptySet());
        assertThat(result.getQuery()).isEqualTo("""
                from(bucket: "analytics-realtime")
                |> range(start: 2025-02-11T15:12:00Z, stop: 2025-02-11T16:20:00Z)
                |> filter(fn: (r) => r["_measurement"] == "analytics")
                |> filter(fn: (r) => r["_field"] == "user_hash")
                |> group()
                |> aggregateWindow(every: 1h, fn: count, createEmpty: false)
                |> rename(columns: {_time: "time_window", _value: "total"})""");
        assertThat(result.getColumnNames()).isEqualTo(List.of("time_window", "total"));
    }
}
