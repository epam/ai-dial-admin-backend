package com.epam.aidial.metric.service.influx3;

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
import com.epam.aidial.metric.model.configuration.DatasetDeclaration;
import com.epam.aidial.metric.model.configuration.influx3.Influx3DatasetDeclaration;
import com.epam.aidial.ql.common.model.enums.BinaryComparisonOperator;
import com.epam.aidial.ql.common.model.enums.SortDirection;
import com.epam.aidial.ql.model.Filter;
import com.epam.aidial.ql.model.filters.impl.AndImpl;
import com.epam.aidial.ql.model.filters.impl.BinaryComparisonFilterImpl;
import com.epam.aidial.ql.model.impl.QueryImpl;
import com.epam.aidial.ql.model.impl.SortImpl;
import com.epam.aidial.ql.model.impl.TableImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SqlQueryBuilderTest {

    private static final ObjectMapper OBJECT_MAPPER = JsonMapperConfiguration.createJsonMapper();
    private static final long FROM_TS = 1739286720000L;
    private static final long TO_TS = 1739290800000L;
    private static final TableImpl ANALYTICS = TableImpl.builder().name("analytics").build();

    private SqlQueryBuilder sqlQueryBuilder;

    @BeforeEach
    void setUp() throws JsonProcessingException {
        var testMetricConfig = ResourceUtils.readResource("/metrics/metric.config.influx3.json");
        var datasetDeclaration = (Influx3DatasetDeclaration) OBJECT_MAPPER.readValue(testMetricConfig, DatasetDeclaration.class);

        sqlQueryBuilder = new SqlQueryBuilder(datasetDeclaration, new TemporalNameGenerator());
    }

    @Test
    void buildQuery_SimpleSelect() {
        var query = baseQuery()
                .expressions(List.of(col(Type.STRING, "deployment"), col(Type.DOUBLE, "price"), col(Type.INT_64, "prompt_tokens")))
                .where(timeRange())
                .orderBy(List.of(SortImpl.of(col(Type.INT_64, "prompt_tokens"), SortDirection.DESC)))
                .build();

        var actual = sqlQueryBuilder.buildQueryContext(query);

        assertThat(actual.getQuery()).isEqualTo("""
                SELECT "deployment", "price", "prompt_tokens" FROM "analytics" \
                WHERE "time" >= $p0 AND "time" < $p1 \
                ORDER BY "prompt_tokens" DESC""");
        assertThat(actual.getColumnNames()).isEqualTo(List.of("deployment", "price", "prompt_tokens"));
        assertThat(actual.getParameters()).containsEntry("p0", "2025-02-11T15:12:00Z");
        assertThat(actual.getParameters()).containsEntry("p1", "2025-02-11T16:20:00Z");
    }

    @Test
    void buildQuery_SimpleSelect_StringComparison() {
        var query = baseQuery()
                .expressions(List.of(col(Type.STRING, "deployment"), col(Type.DOUBLE, "price"), col(Type.INT_64, "prompt_tokens")))
                .where(timeRangeWith(
                        filter(col(Type.STRING, "deployment"), BinaryComparisonOperator.STARTS_WITH, val(Type.STRING, "value"))
                ))
                .orderBy(List.of(SortImpl.of(col(Type.INT_64, "prompt_tokens"), SortDirection.DESC)))
                .build();

        var actual = sqlQueryBuilder.buildQueryContext(query);

        assertThat(actual.getQuery()).isEqualTo("""
                SELECT "deployment", "price", "prompt_tokens" FROM "analytics" \
                WHERE "time" >= $p0 AND "time" < $p1 AND "deployment" ILIKE $p2 ESCAPE '\\' \
                ORDER BY "prompt_tokens" DESC""");
        assertThat(actual.getParameters()).containsEntry("p2", "value%");
        assertThat(actual.getColumnNames()).isEqualTo(List.of("deployment", "price", "prompt_tokens"));
    }

    @Test
    void buildQuery_SimpleSelect_Alias() {
        var query = baseQuery()
                .expressions(List.of(
                        new AliasImpl("a", col(Type.STRING, "deployment")),
                        new AliasImpl("b", col(Type.DOUBLE, "price")),
                        new AliasImpl("c", col(Type.INT_64, "prompt_tokens"))
                ))
                .where(timeRange())
                .orderBy(List.of(SortImpl.of(col(Type.INT_64, "prompt_tokens"), SortDirection.DESC)))
                .build();

        var actual = sqlQueryBuilder.buildQueryContext(query);

        assertThat(actual.getQuery()).isEqualTo("""
                SELECT "deployment" AS "a", "price" AS "b", "prompt_tokens" AS "c" FROM "analytics" \
                WHERE "time" >= $p0 AND "time" < $p1 \
                ORDER BY "c" DESC""");
        assertThat(actual.getColumnNames()).isEqualTo(List.of("a", "b", "c"));
    }

    @Test
    void buildQuery_SimpleDistinctValueSelect() {
        var query = baseQuery()
                .distinct(true)
                .expressions(List.of(col(Type.STRING, "user_hash")))
                .where(timeRange())
                .build();

        var actual = sqlQueryBuilder.buildQueryContext(query);

        assertThat(actual.getQuery()).isEqualTo("""
                SELECT DISTINCT "user_hash" FROM "analytics" \
                WHERE "time" >= $p0 AND "time" < $p1""");
        assertThat(actual.getColumnNames()).isEqualTo(List.of("user_hash"));
    }

    @Test
    void buildQuery_SimpleDistinctValueSelect_Alias() {
        var query = baseQuery()
                .distinct(true)
                .expressions(List.of(new AliasImpl("a", col(Type.STRING, "user_hash"))))
                .where(timeRange())
                .build();

        var actual = sqlQueryBuilder.buildQueryContext(query);

        assertThat(actual.getQuery()).isEqualTo("""
                SELECT DISTINCT "user_hash" AS "a" FROM "analytics" \
                WHERE "time" >= $p0 AND "time" < $p1""");
        assertThat(actual.getColumnNames()).isEqualTo(List.of("a"));
    }

    @Test
    void buildQuery_SimpleDistinctValueSelect_Filtering() {
        var query = baseQuery()
                .distinct(true)
                .expressions(List.of(col(Type.STRING, "user_hash")))
                .where(timeRangeWith(
                        filter(col(Type.STRING, "deployment"), BinaryComparisonOperator.EQUALS, val(Type.STRING, "dep_value"))
                ))
                .build();

        var actual = sqlQueryBuilder.buildQueryContext(query);

        assertThat(actual.getQuery()).isEqualTo("""
                SELECT DISTINCT "user_hash" FROM "analytics" \
                WHERE "time" >= $p0 AND "time" < $p1 AND "deployment" = $p2""");
        assertThat(actual.getParameters()).containsEntry("p2", "dep_value");
        assertThat(actual.getColumnNames()).isEqualTo(List.of("user_hash"));
    }

    @Test
    void buildQuery_SimpleAggregate() {
        var query = baseQuery()
                .expressions(List.of(count(), sum(Type.DOUBLE, "price"), sum(Type.INT_64, "prompt_tokens")))
                .where(timeRange())
                .build();

        var actual = sqlQueryBuilder.buildQueryContext(query);

        assertThat(actual.getQuery()).isEqualTo("""
                SELECT COUNT(*) AS "temp_column_0", SUM("price") AS "temp_column_1", SUM("prompt_tokens") AS "temp_column_2" \
                FROM "analytics" \
                WHERE "time" >= $p0 AND "time" < $p1""");
        assertThat(actual.getColumnNames()).isEqualTo(List.of("temp_column_0", "temp_column_1", "temp_column_2"));
    }

    @Test
    void buildQuery_SimpleAggregate_Alias() {
        var query = baseQuery()
                .expressions(List.of(
                        new AliasImpl("a", count()),
                        new AliasImpl("b", sum(Type.DOUBLE, "price")),
                        new AliasImpl("c", sum(Type.INT_64, "prompt_tokens"))
                ))
                .where(timeRange())
                .build();

        var actual = sqlQueryBuilder.buildQueryContext(query);

        assertThat(actual.getQuery()).isEqualTo("""
                SELECT COUNT(*) AS "a", SUM("price") AS "b", SUM("prompt_tokens") AS "c" \
                FROM "analytics" \
                WHERE "time" >= $p0 AND "time" < $p1""");
        assertThat(actual.getColumnNames()).isEqualTo(List.of("a", "b", "c"));
    }

    @Test
    void buildQuery_GroupAggregate() {
        var query = baseQuery()
                .expressions(List.of(col(Type.STRING, "deployment"), count(), sum(Type.DOUBLE, "price")))
                .where(timeRange())
                .groupBy(List.of(col(Type.STRING, "deployment")))
                .orderBy(List.of(SortImpl.of(col(Type.STRING, "deployment"), SortDirection.ASC)))
                .build();

        var actual = sqlQueryBuilder.buildQueryContext(query);

        assertThat(actual.getQuery()).isEqualTo("""
                SELECT "deployment", COUNT(*) AS "temp_column_0", SUM("price") AS "temp_column_1" \
                FROM "analytics" \
                WHERE "time" >= $p0 AND "time" < $p1 \
                GROUP BY "deployment" \
                ORDER BY "deployment" ASC""");
        assertThat(actual.getColumnNames()).isEqualTo(List.of("deployment", "temp_column_0", "temp_column_1"));
    }

    @Test
    void buildQuery_WindowAggregation() {
        var query = baseQuery()
                .expressions(List.of(
                        new AliasImpl("window",
                                new GroupFunctionCallImpl(
                                        new FunctionImpl("window", Type.TIMESTAMP, true),
                                        List.of(),
                                        List.of(col(Type.TIMESTAMP, "_time"), NumberConstantImpl.valueOf(1L), val(Type.STRING, "h"))
                                )),
                        new AliasImpl("total", count())
                ))
                .where(timeRange())
                .groupBy(List.of(col(Type.TIMESTAMP, "window")))
                .orderBy(List.of(SortImpl.of(col(Type.TIMESTAMP, "window"), SortDirection.ASC)))
                .build();

        var actual = sqlQueryBuilder.buildQueryContext(query);

        assertThat(actual.getQuery()).isEqualTo("""
                SELECT DATE_BIN(INTERVAL '1 hour', "time", TIMESTAMP '1970-01-01T00:00:00Z') AS "temp_column_0"\
                , COUNT(*) AS "total" \
                FROM "analytics" \
                WHERE "time" >= $p0 AND "time" < $p1 \
                GROUP BY "temp_column_0" \
                ORDER BY "temp_column_0" ASC""");
        assertThat(actual.getColumnNames()).isEqualTo(List.of("window", "total"));
    }

    @Test
    void buildQuery_AggregateFromSubquery_ParameterNamesDoNotCollide() {
        var innerQuery = baseQuery()
                .expressions(List.of(col(Type.STRING, "deployment"), col(Type.DOUBLE, "price")))
                .where(timeRange())
                .build();

        var outerQuery = QueryImpl.builder()
                .distinct(false)
                .from(innerQuery)
                .expressions(List.of(col(Type.STRING, "deployment"), count()))
                .where(AndImpl.of(List.of(
                        filter(col(Type.STRING, "deployment"), BinaryComparisonOperator.EQUALS, val(Type.STRING, "dep_value"))
                )))
                .groupBy(List.of(col(Type.STRING, "deployment")))
                .build();

        var actual = sqlQueryBuilder.buildQueryContext(outerQuery);

        // Inner query uses p0, p1 for time range; outer WHERE must use p2 (not p0)
        assertThat(actual.getQuery()).isEqualTo("""
                SELECT "deployment", COUNT(*) AS "temp_column_0" \
                FROM (SELECT "deployment", "price" FROM "analytics" \
                WHERE "time" >= $p0 AND "time" < $p1) \
                WHERE "deployment" = $p2 \
                GROUP BY "deployment\"""");
        assertThat(actual.getParameters()).containsEntry("p0", "2025-02-11T15:12:00Z");
        assertThat(actual.getParameters()).containsEntry("p1", "2025-02-11T16:20:00Z");
        assertThat(actual.getParameters()).containsEntry("p2", "dep_value");
        assertThat(actual.getParameters()).hasSize(3);
    }

    @Test
    void buildQuery_SimpleSelect_WithLimit() {
        var query = baseQuery()
                .expressions(List.of(col(Type.STRING, "deployment")))
                .where(timeRange())
                .limit(10L)
                .offset(5L)
                .build();

        var actual = sqlQueryBuilder.buildQueryContext(query);

        assertThat(actual.getQuery()).isEqualTo("""
                SELECT "deployment" FROM "analytics" \
                WHERE "time" >= $p0 AND "time" < $p1 \
                LIMIT 10 OFFSET 5""");
        assertThat(actual.getColumnNames()).isEqualTo(List.of("deployment"));
    }

    @Test
    void buildQuery_WindowColumnAggregation() {
        var query = baseQuery()
                .expressions(List.of(
                        new AliasImpl("window",
                                new GroupFunctionCallImpl(
                                        new FunctionImpl("window", Type.TIMESTAMP, true),
                                        List.of(),
                                        List.of(col(Type.TIMESTAMP, "_time"), NumberConstantImpl.valueOf(1L), val(Type.STRING, "h"))
                                )),
                        col(Type.STRING, "deployment"),
                        new AliasImpl("total", count())
                ))
                .where(timeRange())
                .groupBy(List.of(col(Type.TIMESTAMP, "window"), col(Type.STRING, "deployment")))
                .orderBy(List.of(SortImpl.of(col(Type.TIMESTAMP, "window"), SortDirection.ASC)))
                .build();

        var actual = sqlQueryBuilder.buildQueryContext(query);

        assertThat(actual.getQuery()).isEqualTo("""
                SELECT DATE_BIN(INTERVAL '1 hour', "time", TIMESTAMP '1970-01-01T00:00:00Z') AS "temp_column_0"\
                , "deployment"\
                , COUNT(*) AS "total" \
                FROM "analytics" \
                WHERE "time" >= $p0 AND "time" < $p1 \
                GROUP BY "temp_column_0", "deployment" \
                ORDER BY "temp_column_0" ASC""");
        assertThat(actual.getColumnNames()).isEqualTo(List.of("window", "deployment", "total"));
    }

    @Test
    void buildQuery_WindowColumnAggregation_MultipleColumns() {
        var query = baseQuery()
                .expressions(List.of(
                        new AliasImpl("window",
                                new GroupFunctionCallImpl(
                                        new FunctionImpl("window", Type.TIMESTAMP, true),
                                        List.of(),
                                        List.of(col(Type.TIMESTAMP, "_time"), NumberConstantImpl.valueOf(1L), val(Type.STRING, "d"))
                                )),
                        col(Type.STRING, "deployment"),
                        col(Type.STRING, "project_id"),
                        new AliasImpl("total", count())
                ))
                .where(timeRange())
                .groupBy(List.of(col(Type.TIMESTAMP, "window"), col(Type.STRING, "deployment"), col(Type.STRING, "project_id")))
                .build();

        var actual = sqlQueryBuilder.buildQueryContext(query);

        assertThat(actual.getQuery()).isEqualTo("""
                SELECT DATE_BIN(INTERVAL '1 day', "time", TIMESTAMP '1970-01-01T00:00:00Z') AS "temp_column_0"\
                , "deployment"\
                , "project_id"\
                , COUNT(*) AS "total" \
                FROM "analytics" \
                WHERE "time" >= $p0 AND "time" < $p1 \
                GROUP BY "temp_column_0", "deployment", "project_id\"""");
        assertThat(actual.getColumnNames()).isEqualTo(List.of("window", "deployment", "project_id", "total"));
    }

    // -- helpers --

    private static QueryImpl.QueryImplBuilder baseQuery() {
        return QueryImpl.builder()
                .distinct(false)
                .from(ANALYTICS);
    }

    private static ColumnImpl col(Type type, String name) {
        return new ColumnImpl(type, name);
    }

    private static ConstantImpl val(Type type, Comparable<?> value) {
        return new ConstantImpl(type, value);
    }

    private static BinaryComparisonFilterImpl filter(ColumnImpl column, BinaryComparisonOperator op, ConstantImpl value) {
        return BinaryComparisonFilterImpl.of(column, op, value);
    }

    private static AndImpl timeRange() {
        return timeRangeWith();
    }

    private static AndImpl timeRangeWith(BinaryComparisonFilterImpl... extraFilters) {
        var filters = new ArrayList<Filter>();
        filters.add(filter(col(Type.TIMESTAMP, "_time"), BinaryComparisonOperator.GREATER_OR_EQUALS, val(Type.TIMESTAMP, FROM_TS)));
        filters.add(filter(col(Type.TIMESTAMP, "_time"), BinaryComparisonOperator.LESS, val(Type.TIMESTAMP, TO_TS)));
        filters.addAll(List.of(extraFilters));
        return AndImpl.of(filters);
    }

    private static AggregationFunctionCallImpl count() {
        return new AggregationFunctionCallImpl(new FunctionImpl("count", Type.INT_64, true), List.of(), List.of());
    }

    private static AggregationFunctionCallImpl sum(Type type, String column) {
        return new AggregationFunctionCallImpl(new FunctionImpl("sum", type, true), List.of(), List.of(col(type, column)));
    }
}
