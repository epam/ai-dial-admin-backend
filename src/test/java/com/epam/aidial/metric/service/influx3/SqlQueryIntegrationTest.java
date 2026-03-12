package com.epam.aidial.metric.service.influx3;

import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.epam.aidial.cfg.utils.ResourceUtils;
import com.epam.aidial.metric.config.Influx3DatasetConfiguration;
import com.epam.aidial.metric.model.configuration.DatasetDeclaration;
import com.epam.aidial.metric.model.configuration.influx3.Influx3DatasetDeclaration;
import com.epam.aidial.metric.model.influx3.SqlQueryContext;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SqlQueryIntegrationTest {

    private static final ObjectMapper OBJECT_MAPPER = JsonMapperConfiguration.createJsonMapper();
    private static final String FROM_TS = "'2025-02-11T15:12:00Z'";
    private static final String TO_TS = "'2025-02-11T16:20:00Z'";

    private Influx3Engine engine;
    private LanguageConverter languageConverter;
    private SqlQueryBuilderFactory sqlQueryBuilderFactory;

    @BeforeEach
    void setUp() throws JsonProcessingException {
        var testMetricConfig = ResourceUtils.readResource("/metrics/metric.config.influx3.json");
        var datasetDeclaration = (Influx3DatasetDeclaration) OBJECT_MAPPER.readValue(testMetricConfig, DatasetDeclaration.class);

        var datasetConfiguration = new Influx3DatasetConfiguration();
        datasetConfiguration.setDefaultPageSize(50);

        sqlQueryBuilderFactory = new SqlQueryBuilderFactory(datasetDeclaration, datasetConfiguration);
        engine = new Influx3Engine(datasetDeclaration, null, sqlQueryBuilderFactory);
        languageConverter = new LanguageConverter(engine);
    }

    // -- Test cases --

    @Test
    void simpleSelect_fromSql() {
        var actual = buildFromSql(
                "SELECT deployment, price, prompt_tokens FROM analytics WHERE _time >= '2025-02-11T15:12:00Z' AND _time < '2025-02-11T16:20:00Z' ORDER BY prompt_tokens DESC");

        assertThat(actual.getQuery()).isEqualTo("""
                SELECT "deployment", "price", "prompt_tokens" FROM "analytics" \
                WHERE "time" >= $p0 AND "time" < $p1 \
                ORDER BY "prompt_tokens" DESC""");
        assertThat(actual.getColumnNames()).isEqualTo(List.of("deployment", "price", "prompt_tokens"));
        assertThat(actual.getParameters()).containsEntry("p0", "2025-02-11T15:12:00Z");
        assertThat(actual.getParameters()).containsEntry("p1", "2025-02-11T16:20:00Z");
    }

    @Test
    void simpleSelect_fromJson() {
        var queryDto = jsonQuery(false,
                List.of(expr("deployment"), expr("price"), expr("prompt_tokens")),
                List.of(),
                List.of(new SortDto(expr("prompt_tokens"), SortDirection.DESC)));

        var actual = buildFromJson(queryDto);

        assertThat(actual.getQuery()).isEqualTo("""
                SELECT "deployment", "price", "prompt_tokens" FROM "analytics" \
                WHERE "time" >= $p0 AND "time" < $p1 \
                ORDER BY "prompt_tokens" DESC""");
        assertThat(actual.getColumnNames()).isEqualTo(List.of("deployment", "price", "prompt_tokens"));
        assertThat(actual.getParameters()).containsEntry("p0", "2025-02-11T15:12:00Z");
        assertThat(actual.getParameters()).containsEntry("p1", "2025-02-11T16:20:00Z");
    }

    @Test
    void stringComparison_fromSql() {
        var actual = buildFromSql(
                "SELECT deployment, price, prompt_tokens FROM analytics WHERE _time >= '2025-02-11T15:12:00Z' AND _time < '2025-02-11T16:20:00Z' AND deployment LIKE 'value%' ORDER BY prompt_tokens DESC");

        assertThat(actual.getQuery()).isEqualTo("""
                SELECT "deployment", "price", "prompt_tokens" FROM "analytics" \
                WHERE "time" >= $p0 AND "time" < $p1 AND "deployment" LIKE $p2 ESCAPE '\\' \
                ORDER BY "prompt_tokens" DESC""");
        assertThat(actual.getParameters()).containsEntry("p2", "value%");
    }

    @Test
    void stringComparison_fromJson() {
        var queryDto = jsonQuery(false,
                List.of(expr("deployment"), expr("price"), expr("prompt_tokens")),
                List.of(new BinaryComparisonFilterDto(expr("deployment"), BinaryComparisonOperator.LIKE, expr("'value%'"))),
                List.of(new SortDto(expr("prompt_tokens"), SortDirection.DESC)));

        var actual = buildFromJson(queryDto);

        assertThat(actual.getQuery()).isEqualTo("""
                SELECT "deployment", "price", "prompt_tokens" FROM "analytics" \
                WHERE "time" >= $p0 AND "time" < $p1 AND "deployment" LIKE $p2 ESCAPE '\\' \
                ORDER BY "prompt_tokens" DESC""");
        assertThat(actual.getParameters()).containsEntry("p2", "value%");
    }

    @Test
    void selectWithAliases_fromSql() {
        var actual = buildFromSql(
                "SELECT deployment AS a, price AS b, prompt_tokens AS c FROM analytics WHERE _time >= '2025-02-11T15:12:00Z' AND _time < '2025-02-11T16:20:00Z' ORDER BY prompt_tokens DESC");

        assertThat(actual.getQuery()).isEqualTo("""
                SELECT "deployment" AS "a", "price" AS "b", "prompt_tokens" AS "c" FROM "analytics" \
                WHERE "time" >= $p0 AND "time" < $p1 \
                ORDER BY "c" DESC""");
        assertThat(actual.getColumnNames()).isEqualTo(List.of("a", "b", "c"));
    }

    @Test
    void selectWithAliases_fromJson() {
        var queryDto = jsonQuery(false,
                List.of(expr("deployment AS a"), expr("price AS b"), expr("prompt_tokens AS c")),
                List.of(),
                List.of(new SortDto(expr("prompt_tokens"), SortDirection.DESC)));

        var actual = buildFromJson(queryDto);

        assertThat(actual.getQuery()).isEqualTo("""
                SELECT "deployment" AS "a", "price" AS "b", "prompt_tokens" AS "c" FROM "analytics" \
                WHERE "time" >= $p0 AND "time" < $p1 \
                ORDER BY "c" DESC""");
        assertThat(actual.getColumnNames()).isEqualTo(List.of("a", "b", "c"));
    }

    @Test
    void distinctValueSelect_fromSql() {
        var actual = buildFromSql(
                "SELECT DISTINCT user_hash FROM analytics WHERE _time >= '2025-02-11T15:12:00Z' AND _time < '2025-02-11T16:20:00Z'");

        assertThat(actual.getQuery()).isEqualTo("""
                SELECT DISTINCT "user_hash" FROM "analytics" \
                WHERE "time" >= $p0 AND "time" < $p1""");
        assertThat(actual.getColumnNames()).isEqualTo(List.of("user_hash"));
    }

    @Test
    void distinctValueSelect_fromJson() {
        var queryDto = jsonQuery(true,
                List.of(expr("user_hash")),
                List.of(),
                List.of());

        var actual = buildFromJson(queryDto);

        assertThat(actual.getQuery()).isEqualTo("""
                SELECT DISTINCT "user_hash" FROM "analytics" \
                WHERE "time" >= $p0 AND "time" < $p1""");
        assertThat(actual.getColumnNames()).isEqualTo(List.of("user_hash"));
    }

    @Test
    void distinctWithAlias_fromSql() {
        var actual = buildFromSql(
                "SELECT DISTINCT user_hash AS a FROM analytics WHERE _time >= '2025-02-11T15:12:00Z' AND _time < '2025-02-11T16:20:00Z'");

        assertThat(actual.getQuery()).isEqualTo("""
                SELECT DISTINCT "user_hash" AS "a" FROM "analytics" \
                WHERE "time" >= $p0 AND "time" < $p1""");
        assertThat(actual.getColumnNames()).isEqualTo(List.of("a"));
    }

    @Test
    void distinctWithAlias_fromJson() {
        var queryDto = jsonQuery(true,
                List.of(expr("user_hash AS a")),
                List.of(),
                List.of());

        var actual = buildFromJson(queryDto);

        assertThat(actual.getQuery()).isEqualTo("""
                SELECT DISTINCT "user_hash" AS "a" FROM "analytics" \
                WHERE "time" >= $p0 AND "time" < $p1""");
        assertThat(actual.getColumnNames()).isEqualTo(List.of("a"));
    }

    @Test
    void distinctWithFiltering_fromSql() {
        var actual = buildFromSql(
                "SELECT DISTINCT user_hash FROM analytics WHERE _time >= '2025-02-11T15:12:00Z' AND _time < '2025-02-11T16:20:00Z' AND deployment = 'dep_value'");

        assertThat(actual.getQuery()).isEqualTo("""
                SELECT DISTINCT "user_hash" FROM "analytics" \
                WHERE "time" >= $p0 AND "time" < $p1 AND "deployment" = $p2""");
        assertThat(actual.getParameters()).containsEntry("p2", "dep_value");
    }

    @Test
    void distinctWithFiltering_fromJson() {
        var queryDto = jsonQuery(true,
                List.of(expr("user_hash")),
                List.of(new BinaryComparisonFilterDto(expr("deployment"), BinaryComparisonOperator.EQUALS, expr("'dep_value'"))),
                List.of());

        var actual = buildFromJson(queryDto);

        assertThat(actual.getQuery()).isEqualTo("""
                SELECT DISTINCT "user_hash" FROM "analytics" \
                WHERE "time" >= $p0 AND "time" < $p1 AND "deployment" = $p2""");
        assertThat(actual.getParameters()).containsEntry("p2", "dep_value");
    }

    @Test
    void simpleAggregate_fromSql() {
        var actual = buildFromSql(
                "SELECT count(), sum(price), sum(prompt_tokens) FROM analytics WHERE _time >= '2025-02-11T15:12:00Z' AND _time < '2025-02-11T16:20:00Z'");

        assertThat(actual.getQuery()).isEqualTo("""
                SELECT COUNT(*) AS "temp_column_0", SUM("price") AS "temp_column_1", SUM("prompt_tokens") AS "temp_column_2" \
                FROM "analytics" \
                WHERE "time" >= $p0 AND "time" < $p1""");
        assertThat(actual.getColumnNames()).isEqualTo(List.of("temp_column_0", "temp_column_1", "temp_column_2"));
    }

    @Test
    void simpleAggregate_fromJson() {
        var queryDto = jsonQuery(false,
                List.of(expr("count()"), expr("sum(price)"), expr("sum(prompt_tokens)")),
                List.of(),
                List.of());

        var actual = buildFromJson(queryDto);

        assertThat(actual.getQuery()).isEqualTo("""
                SELECT COUNT(*) AS "temp_column_0", SUM("price") AS "temp_column_1", SUM("prompt_tokens") AS "temp_column_2" \
                FROM "analytics" \
                WHERE "time" >= $p0 AND "time" < $p1""");
        assertThat(actual.getColumnNames()).isEqualTo(List.of("temp_column_0", "temp_column_1", "temp_column_2"));
    }

    @Test
    void aggregateWithAliases_fromSql() {
        var actual = buildFromSql(
                "SELECT count() AS a, sum(price) AS b, sum(prompt_tokens) AS c FROM analytics WHERE _time >= '2025-02-11T15:12:00Z' AND _time < '2025-02-11T16:20:00Z'");

        assertThat(actual.getQuery()).isEqualTo("""
                SELECT COUNT(*) AS "a", SUM("price") AS "b", SUM("prompt_tokens") AS "c" \
                FROM "analytics" \
                WHERE "time" >= $p0 AND "time" < $p1""");
        assertThat(actual.getColumnNames()).isEqualTo(List.of("a", "b", "c"));
    }

    @Test
    void aggregateWithAliases_fromJson() {
        var queryDto = jsonQuery(false,
                List.of(expr("count() AS a"), expr("sum(price) AS b"), expr("sum(prompt_tokens) AS c")),
                List.of(),
                List.of());

        var actual = buildFromJson(queryDto);

        assertThat(actual.getQuery()).isEqualTo("""
                SELECT COUNT(*) AS "a", SUM("price") AS "b", SUM("prompt_tokens") AS "c" \
                FROM "analytics" \
                WHERE "time" >= $p0 AND "time" < $p1""");
        assertThat(actual.getColumnNames()).isEqualTo(List.of("a", "b", "c"));
    }

    @Test
    void groupAggregate_fromSql() {
        var actual = buildFromSql(
                "SELECT deployment, count(), sum(price) FROM analytics WHERE _time >= '2025-02-11T15:12:00Z' AND _time < '2025-02-11T16:20:00Z' GROUP BY deployment ORDER BY deployment ASC");

        assertThat(actual.getQuery()).isEqualTo("""
                SELECT "deployment", COUNT(*) AS "temp_column_0", SUM("price") AS "temp_column_1" \
                FROM "analytics" \
                WHERE "time" >= $p0 AND "time" < $p1 \
                GROUP BY "deployment" \
                ORDER BY "deployment" ASC""");
        assertThat(actual.getColumnNames()).isEqualTo(List.of("deployment", "temp_column_0", "temp_column_1"));
    }

    @Test
    void groupAggregate_fromJson() {
        var queryDto = jsonQuery(false,
                List.of(expr("deployment"), expr("count()"), expr("sum(price)")),
                List.of(),
                List.of(new SortDto(expr("deployment"), SortDirection.ASC)));
        queryDto.setGroupBy(List.of(expr("deployment")));

        var actual = buildFromJson(queryDto);

        assertThat(actual.getQuery()).isEqualTo("""
                SELECT "deployment", COUNT(*) AS "temp_column_0", SUM("price") AS "temp_column_1" \
                FROM "analytics" \
                WHERE "time" >= $p0 AND "time" < $p1 \
                GROUP BY "deployment" \
                ORDER BY "deployment" ASC""");
        assertThat(actual.getColumnNames()).isEqualTo(List.of("deployment", "temp_column_0", "temp_column_1"));
    }

    @Test
    void windowAggregation_fromSql() {
        var actual = buildFromSql(
                "SELECT window(_time, 1, 'h') AS time_window, count() AS total FROM analytics WHERE _time >= '2025-02-11T15:12:00Z' AND _time < '2025-02-11T16:20:00Z' GROUP BY time_window ORDER BY time_window ASC");

        assertThat(actual.getQuery()).isEqualTo("""
                SELECT DATE_BIN(INTERVAL '1 hour', "time", TIMESTAMP '1970-01-01T00:00:00Z') AS "time_window"\
                , COUNT(*) AS "total" \
                FROM "analytics" \
                WHERE "time" >= $p0 AND "time" < $p1 \
                GROUP BY "time_window" \
                ORDER BY "time_window" ASC""");
        assertThat(actual.getColumnNames()).isEqualTo(List.of("time_window", "total"));
    }

    @Test
    void windowAggregation_fromJson() {
        var queryDto = jsonQuery(false,
                List.of(expr("window(_time, 1, 'h') AS time_window"), expr("count() AS total")),
                List.of(),
                List.of(new SortDto(expr("time_window"), SortDirection.ASC)));
        queryDto.setGroupBy(List.of(expr("time_window")));

        var actual = buildFromJson(queryDto);

        assertThat(actual.getQuery()).isEqualTo("""
                SELECT DATE_BIN(INTERVAL '1 hour', "time", TIMESTAMP '1970-01-01T00:00:00Z') AS "time_window"\
                , COUNT(*) AS "total" \
                FROM "analytics" \
                WHERE "time" >= $p0 AND "time" < $p1 \
                GROUP BY "time_window" \
                ORDER BY "time_window" ASC""");
        assertThat(actual.getColumnNames()).isEqualTo(List.of("time_window", "total"));
    }

    @Test
    void selectWithLimit_fromSql() {
        var actual = buildFromSql(
                "SELECT deployment FROM analytics WHERE _time >= '2025-02-11T15:12:00Z' AND _time < '2025-02-11T16:20:00Z' LIMIT 5, 10");

        assertThat(actual.getQuery()).isEqualTo("""
                SELECT "deployment" FROM "analytics" \
                WHERE "time" >= $p0 AND "time" < $p1 \
                LIMIT 10 OFFSET 5""");
    }

    @Test
    void selectWithLimit_fromJson() {
        var queryDto = jsonQuery(false,
                List.of(expr("deployment")),
                List.of(),
                List.of());
        queryDto.setLimit(10L);
        queryDto.setOffset(5L);

        var actual = buildFromJson(queryDto);

        assertThat(actual.getQuery()).isEqualTo("""
                SELECT "deployment" FROM "analytics" \
                WHERE "time" >= $p0 AND "time" < $p1 \
                LIMIT 10 OFFSET 5""");
    }

    // -- Helper methods --

    private SqlQueryContext buildFromSql(String sql) {
        var dto = QueryParserUtil.parse(sql);
        return buildFromDto(dto);
    }

    private SqlQueryContext buildFromJson(CompletableDto dto) {
        return buildFromDto(dto);
    }

    private SqlQueryContext buildFromDto(CompletableDto dto) {
        var completable = languageConverter.convert(dto, engine.getTables());
        return sqlQueryBuilderFactory.createQueryBuilder().buildQueryContext(completable);
    }

    private static QueryDto jsonQuery(boolean distinct, List<StringExpressionDto> expressions,
                                      List<FilterDto> extraFilters, List<SortDto> orderBy) {
        var queryDto = new QueryDto();
        queryDto.setDistinct(distinct);
        queryDto.setExpressions(new ArrayList<>(expressions));
        queryDto.setFrom(new TableDto("analytics"));
        queryDto.setWhere(timeRangeFilter(extraFilters.toArray(new FilterDto[0])));
        queryDto.setOrderBy(orderBy);
        return queryDto;
    }

    private static AndDto timeRangeFilter(FilterDto... extra) {
        var filters = new ArrayList<FilterDto>();
        filters.add(new BinaryComparisonFilterDto(expr("_time"), BinaryComparisonOperator.GREATER_OR_EQUALS, expr(FROM_TS)));
        filters.add(new BinaryComparisonFilterDto(expr("_time"), BinaryComparisonOperator.LESS, expr(TO_TS)));
        filters.addAll(List.of(extra));
        return new AndDto(filters);
    }

    private static StringExpressionDto expr(String expression) {
        return new StringExpressionDto(expression);
    }
}
