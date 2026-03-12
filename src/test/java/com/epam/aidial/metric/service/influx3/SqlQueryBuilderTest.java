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
import com.epam.aidial.metric.config.Influx3DatasetConfiguration;
import com.epam.aidial.metric.model.configuration.DatasetDeclaration;
import com.epam.aidial.metric.model.configuration.influx3.Influx3DatasetDeclaration;
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

class SqlQueryBuilderTest {

    private static final ObjectMapper OBJECT_MAPPER = JsonMapperConfiguration.createJsonMapper();

    private SqlQueryBuilder sqlQueryBuilder;

    @BeforeEach
    void setUp() throws JsonProcessingException {
        var testMetricConfig = ResourceUtils.readResource("/metrics/metric.config.influx3.json");
        var datasetDeclaration = (Influx3DatasetDeclaration) OBJECT_MAPPER.readValue(testMetricConfig, DatasetDeclaration.class);

        var datasetConfiguration = new Influx3DatasetConfiguration();
        datasetConfiguration.setDefaultPageSize(50);

        sqlQueryBuilder = new SqlQueryBuilder(datasetDeclaration, datasetConfiguration, new TemporalNameGenerator());
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

        var actual = sqlQueryBuilder.buildQueryContext(completable);

        assertThat(actual.getQuery()).isEqualTo(
                "SELECT \"deployment\", \"price\", \"prompt_tokens\" FROM \"analytics\""
                        + " WHERE \"time\" >= $p0 AND \"time\" < $p1"
                        + " ORDER BY \"prompt_tokens\" DESC");
        assertThat(actual.getColumnNames()).isEqualTo(List.of("deployment", "price", "prompt_tokens"));
        assertThat(actual.getParameters()).containsEntry("p0", "2025-02-11T15:12:00Z");
        assertThat(actual.getParameters()).containsEntry("p1", "2025-02-11T16:20:00Z");
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

        var actual = sqlQueryBuilder.buildQueryContext(completable);

        assertThat(actual.getQuery()).isEqualTo(
                "SELECT \"deployment\", \"price\", \"prompt_tokens\" FROM \"analytics\""
                        + " WHERE \"time\" >= $p0 AND \"time\" < $p1 AND \"deployment\" LIKE $p2 ESCAPE '\\'"
                        + " ORDER BY \"prompt_tokens\" DESC");
        assertThat(actual.getParameters()).containsEntry("p2", "value%");
        assertThat(actual.getColumnNames()).isEqualTo(List.of("deployment", "price", "prompt_tokens"));
    }

    @Test
    void buildQuery_SimpleSelect_Alias() {
        var completable = QueryImpl.builder()
                .distinct(false)
                .expressions(List.of(
                        new AliasImpl("a", new ColumnImpl(Type.STRING, "deployment")),
                        new AliasImpl("b", new ColumnImpl(Type.DOUBLE, "price")),
                        new AliasImpl("c", new ColumnImpl(Type.INT_64, "prompt_tokens"))
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

        var actual = sqlQueryBuilder.buildQueryContext(completable);

        assertThat(actual.getQuery()).isEqualTo(
                "SELECT \"deployment\" AS \"a\", \"price\" AS \"b\", \"prompt_tokens\" AS \"c\" FROM \"analytics\""
                        + " WHERE \"time\" >= $p0 AND \"time\" < $p1"
                        + " ORDER BY \"c\" DESC");
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

        var actual = sqlQueryBuilder.buildQueryContext(completable);

        assertThat(actual.getQuery()).isEqualTo(
                "SELECT DISTINCT \"user_hash\" FROM \"analytics\""
                        + " WHERE \"time\" >= $p0 AND \"time\" < $p1");
        assertThat(actual.getColumnNames()).isEqualTo(List.of("user_hash"));
    }

    @Test
    void buildQuery_SimpleDistinctValueSelect_Alias() {
        var completable = QueryImpl.builder()
                .distinct(true)
                .expressions(List.of(
                        new AliasImpl("a", new ColumnImpl(Type.STRING, "user_hash"))
                ))
                .from(TableImpl.builder().name("analytics").build())
                .where(AndImpl.of(List.of(
                        BinaryComparisonFilterImpl.of(new ColumnImpl(Type.TIMESTAMP, "_time"),
                                BinaryComparisonOperator.GREATER_OR_EQUALS, new ConstantImpl(Type.TIMESTAMP, 1739286720000L)),
                        BinaryComparisonFilterImpl.of(new ColumnImpl(Type.TIMESTAMP, "_time"),
                                BinaryComparisonOperator.LESS, new ConstantImpl(Type.TIMESTAMP, 1739290800000L))
                )))
                .build();

        var actual = sqlQueryBuilder.buildQueryContext(completable);

        assertThat(actual.getQuery()).isEqualTo(
                "SELECT DISTINCT \"user_hash\" AS \"a\" FROM \"analytics\""
                        + " WHERE \"time\" >= $p0 AND \"time\" < $p1");
        assertThat(actual.getColumnNames()).isEqualTo(List.of("a"));
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

        var actual = sqlQueryBuilder.buildQueryContext(completable);

        assertThat(actual.getQuery()).isEqualTo(
                "SELECT DISTINCT \"user_hash\" FROM \"analytics\""
                        + " WHERE \"time\" >= $p0 AND \"time\" < $p1 AND \"deployment\" = $p2");
        assertThat(actual.getParameters()).containsEntry("p2", "dep_value");
        assertThat(actual.getColumnNames()).isEqualTo(List.of("user_hash"));
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

        var actual = sqlQueryBuilder.buildQueryContext(completable);

        assertThat(actual.getQuery()).isEqualTo(
                "SELECT COUNT(*) AS \"temp_column_0\", SUM(\"price\") AS \"temp_column_1\", SUM(\"prompt_tokens\") AS \"temp_column_2\""
                        + " FROM \"analytics\""
                        + " WHERE \"time\" >= $p0 AND \"time\" < $p1");
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

        var actual = sqlQueryBuilder.buildQueryContext(completable);

        assertThat(actual.getQuery()).isEqualTo(
                "SELECT COUNT(*) AS \"a\", SUM(\"price\") AS \"b\", SUM(\"prompt_tokens\") AS \"c\""
                        + " FROM \"analytics\""
                        + " WHERE \"time\" >= $p0 AND \"time\" < $p1");
        assertThat(actual.getColumnNames()).isEqualTo(List.of("a", "b", "c"));
    }

    @Test
    void buildQuery_GroupAggregate() {
        var completable = QueryImpl.builder()
                .distinct(false)
                .expressions(List.of(
                        new ColumnImpl(Type.STRING, "deployment"),
                        new AggregationFunctionCallImpl(
                                new FunctionImpl("count", Type.INT_64, true),
                                List.of(),
                                List.of()
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
                        new ColumnImpl(Type.STRING, "deployment")
                ))
                .orderBy(List.of(
                        SortImpl.of(new ColumnImpl(Type.STRING, "deployment"), SortDirection.ASC)
                ))
                .build();

        var actual = sqlQueryBuilder.buildQueryContext(completable);

        assertThat(actual.getQuery()).isEqualTo(
                "SELECT \"deployment\", COUNT(*) AS \"temp_column_0\", SUM(\"price\") AS \"temp_column_1\""
                        + " FROM \"analytics\""
                        + " WHERE \"time\" >= $p0 AND \"time\" < $p1"
                        + " GROUP BY \"deployment\""
                        + " ORDER BY \"deployment\" ASC");
        assertThat(actual.getColumnNames()).isEqualTo(List.of("deployment", "temp_column_0", "temp_column_1"));
    }

    @Test
    void buildQuery_WindowAggregation() {
        var completable = QueryImpl.builder()
                .distinct(false)
                .expressions(List.of(
                        new AliasImpl("window",
                                new GroupFunctionCallImpl(
                                        new FunctionImpl("window", Type.TIMESTAMP, true),
                                        List.of(),
                                        List.of(
                                                new ColumnImpl(Type.TIMESTAMP, "_time"),
                                                NumberConstantImpl.valueOf(1L),
                                                new ConstantImpl(Type.STRING, "h")
                                        )
                                )
                        ),
                        new AliasImpl("total",
                                new AggregationFunctionCallImpl(
                                        new FunctionImpl("count", Type.INT_64, true),
                                        List.of(),
                                        List.of()
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
                        new ColumnImpl(Type.TIMESTAMP, "window")
                ))
                .orderBy(List.of(
                        SortImpl.of(new ColumnImpl(Type.TIMESTAMP, "window"), SortDirection.ASC)
                ))
                .build();

        var actual = sqlQueryBuilder.buildQueryContext(completable);

        assertThat(actual.getQuery()).isEqualTo(
                "SELECT DATE_BIN(INTERVAL '1 hour', \"time\", TIMESTAMP '1970-01-01T00:00:00Z') AS \"window\""
                        + ", COUNT(*) AS \"total\""
                        + " FROM \"analytics\""
                        + " WHERE \"time\" >= $p0 AND \"time\" < $p1"
                        + " GROUP BY \"window\""
                        + " ORDER BY \"window\" ASC");
        assertThat(actual.getColumnNames()).isEqualTo(List.of("window", "total"));
    }

    @Test
    void buildQuery_SimpleSelect_WithLimit() {
        var completable = QueryImpl.builder()
                .distinct(false)
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
                .limit(10L)
                .offset(5L)
                .build();

        var actual = sqlQueryBuilder.buildQueryContext(completable);

        assertThat(actual.getQuery()).isEqualTo(
                "SELECT \"deployment\" FROM \"analytics\""
                        + " WHERE \"time\" >= $p0 AND \"time\" < $p1"
                        + " LIMIT 10 OFFSET 5");
        assertThat(actual.getColumnNames()).isEqualTo(List.of("deployment"));
    }
}
