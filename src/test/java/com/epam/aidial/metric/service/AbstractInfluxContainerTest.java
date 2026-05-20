package com.epam.aidial.metric.service;

import com.epam.aidial.ql.Engine;
import com.epam.aidial.ql.LanguageConverter;
import com.epam.aidial.ql.deserializers.json.QueryLanguageModule;
import com.epam.aidial.ql.dto.CompletableDto;
import com.epam.aidial.ql.model.Data;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Shared test suite for InfluxDB 2 and InfluxDB 3 metrics extraction.
 * Both engines must produce identical results from the same line-protocol data
 * when queried with the same JSON statements.
 */
public abstract class AbstractInfluxContainerTest {

    private static final ObjectMapper QUERY_MAPPER = new ObjectMapper();
    private static final Comparator<Double> DOUBLE_COMPARATOR =
            (a, b) -> Math.abs(a - b) < 0.001 ? 0 : Double.compare(a, b);

    static {
        QUERY_MAPPER.registerModule(new QueryLanguageModule());
    }

    // mcp_analytics records WITHOUT project_id tag — reproduces the scenario
    // where a schema-defined column is absent from actual data.
    // 3 records inside the time range.
    private static final List<String> MCP_RECORDS_NO_PROJECT = List.of(
            // INSIDE #1: 2026-03-11T14:00:00Z
            "mcp_analytics,deployment=gpt-4,mcp_method=tools/call "
            + "execution_path=\"path1\",chat_id=\"chat1\",user_hash=\"user1\" "
            + "1773237600000000000",
            // INSIDE #2: 2026-03-12T10:00:00Z
            "mcp_analytics,deployment=gpt-4,mcp_method=tools/list "
            + "execution_path=\"path2\",chat_id=\"chat2\",user_hash=\"user2\" "
            + "1773309600000000000",
            // INSIDE #3: 2026-03-12T18:00:00Z
            "mcp_analytics,deployment=gpt-3.5,mcp_method=tools/call "
            + "execution_path=\"path3\",chat_id=\"chat3\",user_hash=\"user1\" "
            + "1773338400000000000"
    );

    // mcp_analytics records WITH project_id tag — enables per-project aggregation.
    // 4 records inside the time range.
    private static final List<String> MCP_RECORDS_WITH_PROJECT = List.of(
            // INSIDE #4: 2026-03-11T15:00:00Z
            "mcp_analytics,deployment=gpt-4,mcp_method=tools/call,project_id=proj1 "
            + "execution_path=\"path4\",chat_id=\"chat4\",user_hash=\"user1\" "
            + "1773241200000000000",
            // INSIDE #5: 2026-03-12T11:00:00Z
            "mcp_analytics,deployment=gpt-3.5,mcp_method=tools/list,project_id=proj1 "
            + "execution_path=\"path5\",chat_id=\"chat5\",user_hash=\"user2\" "
            + "1773313200000000000",
            // INSIDE #6: 2026-03-12T15:00:00Z
            "mcp_analytics,deployment=gpt-4,mcp_method=tools/call,project_id=proj2 "
            + "execution_path=\"path6\",chat_id=\"chat6\",user_hash=\"user1\" "
            + "1773327600000000000",
            // INSIDE #7: 2026-03-13T09:00:00Z
            "mcp_analytics,deployment=gpt-3.5,mcp_method=tools/call,project_id=proj2 "
            + "execution_path=\"path7\",chat_id=\"chat7\",user_hash=\"user2\" "
            + "1773392400000000000"
    );

    // mcp_analytics record with a UUID-shaped project_id, timestamped outside
    // every other test's time range. Used by UuidLiteralFilterTests to verify
    // that a UUID-shaped string literal is accepted as a filter value against a
    // STRING tag column. Lives on mcp_analytics (not analytics) because Flux's
    // distinct(column:) doesn't always respect the upstream range() filter — a
    // UUID project_id added to analytics would leak into pre-existing distinct
    // tests like distinctWithContainsFilter.
    private static final String UUID_PROJECT_ID = "a36d8a75-aa7d-4185-a84d-566066cf91f2";
    private static final List<String> UUID_PROJECT_RECORDS = List.of(
            // 2026-03-15T10:00:00Z
            "mcp_analytics,deployment=gpt-4,mcp_method=tools/call,project_id=" + UUID_PROJECT_ID + " "
            + "execution_path=\"path_uuid\",chat_id=\"chat_uuid\",user_hash=\"user1\" "
            + "1773568800000000000"
    );

    // Time range: [2026-03-11T13:33:38.680Z, 2026-03-13T13:33:38.680Z)
    // 6 records total: 4 inside the range, 2 outside
    private static final List<String> ANALYTICS_RECORDS = List.of(
            // OUTSIDE (before range): 2026-03-10T12:00:00Z
            "analytics,deployment=gpt-4,model=gpt-4,project_id=proj1 "
            + "user_hash=\"user1\",price=0.08,deployment_price=0.07,"
            + "prompt_tokens=300i,completion_tokens=100i "
            + "1773144000000000000",
            // INSIDE #1: 2026-03-11T14:00:00Z
            "analytics,deployment=gpt-4,model=gpt-4,project_id=proj1 "
            + "user_hash=\"user1\",price=0.05,deployment_price=0.04,"
            + "prompt_tokens=200i,completion_tokens=80i "
            + "1773237600000000000",
            // INSIDE #2: 2026-03-12T10:00:00Z
            "analytics,deployment=gpt-4,model=gpt-4,project_id=proj2 "
            + "user_hash=\"user2\",price=0.10,deployment_price=0.09,"
            + "prompt_tokens=100i,completion_tokens=50i "
            + "1773309600000000000",
            // INSIDE #3: 2026-03-12T18:00:00Z
            "analytics,deployment=gpt-3.5,model=gpt-3.5,project_id=proj1 "
            + "user_hash=\"user1\",price=0.02,deployment_price=0.01,"
            + "prompt_tokens=50i,completion_tokens=30i "
            + "1773338400000000000",
            // INSIDE #4: 2026-03-13T10:00:00Z
            "analytics,deployment=gpt-3.5,model=gpt-3.5,project_id=proj2 "
            + "user_hash=\"user2\",price=0.03,deployment_price=0.05,"
            + "prompt_tokens=150i,completion_tokens=60i "
            + "1773396000000000000",
            // OUTSIDE (after range): 2026-03-13T14:00:00Z
            "analytics,deployment=gpt-4,model=gpt-4,project_id=proj1 "
            + "user_hash=\"user3\",price=0.15,deployment_price=0.12,"
            + "prompt_tokens=400i,completion_tokens=200i "
            + "1773410400000000000"
    );

    protected static final List<String> TEST_RECORDS;

    static {
        var all = new ArrayList<>(ANALYTICS_RECORDS);
        all.addAll(MCP_RECORDS_NO_PROJECT);
        all.addAll(MCP_RECORDS_WITH_PROJECT);
        all.addAll(UUID_PROJECT_RECORDS);
        TEST_RECORDS = List.copyOf(all);
    }

    private static final String TIME_GTE = """
            {"$gte": {"left": "_time", "right": "'2026-03-11T13:33:38.680Z'"}}""";

    private static final String TIME_LT = """
            {"$lt": {"left": "_time", "right": "'2026-03-13T13:33:38.680Z'"}}""";

    private static final String TIME_FILTER = """
            "$and": [%s, %s]""".formatted(TIME_GTE, TIME_LT);

    protected abstract Engine getEngine();

    protected Data queryFromJson(String json) throws Exception {
        return queryFromJson(json, true);
    }

    protected Data queryFromJson(String json, boolean fillGaps) throws Exception {
        var engine = getEngine();
        var languageConverter = new LanguageConverter(engine);
        var dto = QUERY_MAPPER.readValue(json, CompletableDto.class);
        var completable = languageConverter.convert(dto, engine.getTables());
        return engine.getData(completable, fillGaps);
    }

    protected static List<String> columnNames(Data data) {
        return data.getExpressions().stream()
                .map(expr -> (expr instanceof com.epam.aidial.expressions.Column col) ? col.getName() : expr.toString())
                .toList();
    }

    @Nested
    class SimpleSelectTests {

        @Test
        void simpleSelectWithTimeAlias() throws Exception {
            var data = queryFromJson("""
                    {
                      "expressions": ["_time as completion_time", "deployment"],
                      "from": "analytics",
                      "where": {%s},
                      "orderBy": [{"$asc": "_time"}],
                      "limit": 1
                    }""".formatted(TIME_FILTER));

            assertThat(columnNames(data)).containsExactly("completion_time", "deployment");
            assertThat(data.getData()).containsExactly(
                    List.of(Instant.parse("2026-03-11T14:00:00Z"), "gpt-4")
            );
        }

        @Test
        void simpleSelectWithProjectIdAndDeploymentFilter() throws Exception {
            var data = queryFromJson("""
                    {
                      "expressions": ["deployment", "project_id", "price"],
                      "from": "analytics",
                      "where": {
                        "$and": [
                          %s, %s,
                          {"$eq": {"left": "project_id", "right": "'proj1'"}},
                          {"$eq": {"left": "deployment", "right": "'gpt-3.5'"}}
                        ]
                      }
                    }""".formatted(TIME_GTE, TIME_LT));

            assertThat(columnNames(data)).containsExactly("deployment", "project_id", "price");
            assertThat(data.getData()).containsExactly(
                    List.of("gpt-3.5", "proj1", 0.02)
            );
        }

    }

    @Nested
    class AggregationTests {

        @Test
        void windowAggregation() throws Exception {
            // Use 1-day window over ~2 day range
            // Time range: [2026-03-11T13:33:38.680Z, 2026-03-13T13:33:38.680Z)
            // DATE_BIN aligns to epoch, so 1-day buckets start at midnight UTC:
            //   2026-03-11T00:00:00Z (record #1 at 14:00)
            //   2026-03-12T00:00:00Z (records #2 at 10:00, #3 at 18:00)
            //   2026-03-13T00:00:00Z (record #4 at 10:00)
            var data = queryFromJson("""
                    {
                      "expressions": ["window(_time, 1, 'd') as time", "count() as requests"],
                      "from": "analytics",
                      "groupBy": ["window(_time, 1, 'd')"],
                      "where": {%s},
                      "orderBy": [{"$asc": "time"}]
                    }""".formatted(TIME_FILTER));

            assertThat(columnNames(data)).containsExactly("time", "requests");
            assertThat(data.getData()).containsExactly(
                    List.of(Instant.parse("2026-03-11T00:00:00Z"), 1L),
                    List.of(Instant.parse("2026-03-12T00:00:00Z"), 2L),
                    List.of(Instant.parse("2026-03-13T00:00:00Z"), 1L)
            );
        }

        @Test
        void windowAggregationFillsGaps() throws Exception {
            // Use 8-hour window over ~2 day range
            // Time range: [2026-03-11T13:33:38.680Z, 2026-03-13T13:33:38.680Z)
            // 8-hour buckets aligned to epoch:
            //   2026-03-11T08:00:00Z  -> record #1 at 14:00 -> count=1
            //   2026-03-11T16:00:00Z  -> no data             -> count=0 (gap filled)
            //   2026-03-12T00:00:00Z  -> no data             -> count=0 (gap filled)
            //   2026-03-12T08:00:00Z  -> record #2 at 10:00 -> count=1
            //   2026-03-12T16:00:00Z  -> record #3 at 18:00 -> count=1
            //   2026-03-13T00:00:00Z  -> no data             -> count=0 (gap filled)
            //   2026-03-13T08:00:00Z  -> record #4 at 10:00 -> count=1
            var data = queryFromJson("""
                    {
                      "expressions": ["window(_time, 8, 'h') as time", "count() as requests"],
                      "from": "analytics",
                      "groupBy": ["window(_time, 8, 'h')"],
                      "where": {%s},
                      "orderBy": [{"$asc": "time"}]
                    }""".formatted(TIME_FILTER));

            assertThat(columnNames(data)).containsExactly("time", "requests");
            assertThat(data.getData()).containsExactly(
                    List.of(Instant.parse("2026-03-11T08:00:00Z"), 1L),
                    List.of(Instant.parse("2026-03-11T16:00:00Z"), 0L),
                    List.of(Instant.parse("2026-03-12T00:00:00Z"), 0L),
                    List.of(Instant.parse("2026-03-12T08:00:00Z"), 1L),
                    List.of(Instant.parse("2026-03-12T16:00:00Z"), 1L),
                    List.of(Instant.parse("2026-03-13T00:00:00Z"), 0L),
                    List.of(Instant.parse("2026-03-13T08:00:00Z"), 1L)
            );
        }

        @Test
        void totalCount() throws Exception {
            var data = queryFromJson("""
                    {
                      "expressions": ["count() as cnt"],
                      "from": "analytics",
                      "where": {%s}
                    }""".formatted(TIME_FILTER));

            assertThat(columnNames(data)).containsExactly("cnt");
            assertThat(data.getData()).containsExactly(List.of(4L));
        }

        @Test
        void sumTokens() throws Exception {
            var data = queryFromJson("""
                    {
                      "expressions": ["sum(prompt_tokens) as total_prompt_tokens", "sum(completion_tokens) as total_completion_tokens"],
                      "from": "analytics",
                      "where": {%s}
                    }""".formatted(TIME_FILTER));

            assertThat(columnNames(data)).containsExactly("total_prompt_tokens", "total_completion_tokens");
            assertThat(data.getData()).containsExactly(List.of(500L, 220L));
        }

        @Test
        void sumDeploymentPrice() throws Exception {
            var data = queryFromJson("""
                    {
                      "expressions": ["sum(deployment_price) as total_deployment_price"],
                      "from": "analytics",
                      "where": {%s}
                    }""".formatted(TIME_FILTER));

            assertThat(columnNames(data)).containsExactly("total_deployment_price");
            assertThat(data.getData())
                    .usingRecursiveComparison()
                    .withComparatorForType(DOUBLE_COMPARATOR, Double.class)
                    .isEqualTo(List.of(List.of(0.19)));
        }

        @Test
        void countDistinctUsers() throws Exception {
            var data = queryFromJson("""
                    {
                      "expressions": ["count() as cnt"],
                      "from": {
                        "distinct": "true",
                        "expressions": ["user_hash"],
                        "from": "analytics",
                        "where": {%s}
                      }
                    }""".formatted(TIME_FILTER));

            assertThat(columnNames(data)).containsExactly("cnt");
            assertThat(data.getData()).containsExactly(List.of(2L));
        }

    }

    @Nested
    class MissingColumnTests {

        @Test
        void countWithGroupBy_whenSchemaColumnAbsentFromSomeData() throws Exception {
            // Some mcp_analytics records have NO project_id tag, but the schema declares it.
            // count() must work regardless — it should not depend on project_id existing
            // in every record.
            var data = queryFromJson("""
                    {
                      "expressions": ["deployment", "count() as cnt"],
                      "from": "mcp_analytics",
                      "groupBy": ["deployment"],
                      "where": {%s},
                      "orderBy": [{"$desc": "count()"}]
                    }""".formatted(TIME_FILTER));

            // orderBy desc count(): gpt-4(4) > gpt-3.5(3)
            assertThat(columnNames(data)).containsExactly("deployment", "cnt");
            assertThat(data.getData()).containsExactly(
                    List.of("gpt-4", 4L),
                    List.of("gpt-3.5", 3L)
            );
        }

        @Test
        void totalCount_whenSchemaColumnAbsentFromSomeData() throws Exception {
            // count() without group-by on a table where project_id is absent from some records.
            var data = queryFromJson("""
                    {
                      "expressions": ["count() as cnt"],
                      "from": "mcp_analytics",
                      "where": {%s}
                    }""".formatted(TIME_FILTER));

            assertThat(columnNames(data)).containsExactly("cnt");
            assertThat(data.getData()).containsExactly(List.of(7L));
        }

    }

    @Nested
    class McpProjectAggregationTests {

        @Test
        void combinedToolCallsAndMcpCallsPerProject() throws Exception {
            // Single query with both tool_calls (conditional) and mcp_calls (total) per project.
            // Uses SUM(CASE WHEN mcp_method = 'tools/call' THEN 1 ELSE 0 END) for tool_calls.
            // proj1: tool_calls=1 (#4), mcp_calls=2 (#4, #5)
            // proj2: tool_calls=2 (#6, #7), mcp_calls=2 (#6, #7)
            var data = queryFromJson("""
                    {
                      "expressions": [
                        "project_id",
                        "sum(case when mcp_method = 'tools/call' then 1 else 0 end) as tool_calls",
                        "count() as mcp_calls"
                      ],
                      "from": "mcp_analytics",
                      "groupBy": ["project_id"],
                      "where": {
                        "$and": [
                          %s, %s,
                          {"$or": [
                            {"$eq": {"left": "project_id", "right": "'proj1'"}},
                            {"$eq": {"left": "project_id", "right": "'proj2'"}}
                          ]}
                        ]
                      },
                      "orderBy": [{"$desc": "count()"}]
                    }""".formatted(TIME_GTE, TIME_LT));

            assertThat(columnNames(data)).containsExactly("project_id", "tool_calls", "mcp_calls");
            assertThat(data.getData()).containsExactlyInAnyOrder(
                    List.of("proj1", 1L, 2L),
                    List.of("proj2", 2L, 2L)
            );
        }

        @Test
        void toolCallsForProjectAndDeployment() throws Exception {
            // project_id=proj1 AND deployment=gpt-3.5 on mcp_analytics → only MCP #5.
            // #5 has mcp_method=tools/list, so tool_calls=0, mcp_calls=1.
            // Exercises a two-aggregation query with no group-by where the two aggregations
            // take different build paths: count() uses the field-filter fast path, while
            // sum(case when <tag>) uses the pivot path. The two branches must still join
            // correctly through the synthetic key.
            var data = queryFromJson("""
                    {
                      "expressions": [
                        "sum(case when mcp_method = 'tools/call' then 1 else 0 end) as tool_calls",
                        "count() as mcp_calls"
                      ],
                      "from": "mcp_analytics",
                      "where": {
                        "$and": [
                          %s, %s,
                          {"$eq": {"left": "project_id", "right": "'proj1'"}},
                          {"$eq": {"left": "deployment", "right": "'gpt-3.5'"}}
                        ]
                      }
                    }""".formatted(TIME_GTE, TIME_LT));

            assertThat(columnNames(data)).containsExactly("tool_calls", "mcp_calls");
            assertThat(data.getData()).containsExactly(List.of(0L, 1L));
        }

        @Test
        void multipleAggregationsWithNullGroupByColumn() throws Exception {
            // Query multiple aggregations grouped by project_id WITHOUT filtering by project_id.
            // Records #1-#3 have no project_id tag (null) → should appear with null project_id.
            // null:  tool_calls=2 (#1 tools/call, #3 tools/call), mcp_calls=3 (#1, #2, #3)
            // proj1: tool_calls=1 (#4 tools/call),                 mcp_calls=2 (#4, #5)
            // proj2: tool_calls=2 (#6 tools/call, #7 tools/call),  mcp_calls=2 (#6, #7)
            var data = queryFromJson("""
                    {
                      "expressions": [
                        "project_id",
                        "sum(case when mcp_method = 'tools/call' then 1 else 0 end) as tool_calls",
                        "count() as mcp_calls"
                      ],
                      "from": "mcp_analytics",
                      "groupBy": ["project_id"],
                      "where": {
                        "$and": [%s, %s]
                      }
                    }""".formatted(TIME_GTE, TIME_LT));

            assertThat(columnNames(data)).containsExactly("project_id", "tool_calls", "mcp_calls");
            assertThat(data.getData()).containsExactlyInAnyOrder(
                    Arrays.asList(null, 2L, 3L),
                    List.of("proj1", 1L, 2L),
                    List.of("proj2", 2L, 2L)
            );
        }

    }

    @Nested
    class GroupByTests {

        @Test
        void groupByDeployment() throws Exception {
            var data = queryFromJson("""
                    {
                      "expressions": [
                        "deployment", "count() as cnt", "sum(price) as money",
                        "sum(prompt_tokens) as tokens_p", "sum(completion_tokens) as tokens_c"
                      ],
                      "from": "analytics",
                      "groupBy": ["deployment"],
                      "where": {%s}
                    }""".formatted(TIME_FILTER));

            assertThat(columnNames(data)).containsExactly("deployment", "cnt", "money", "tokens_p", "tokens_c");
            assertThat(data.getData())
                    .usingRecursiveComparison()
                    .withComparatorForType(DOUBLE_COMPARATOR, Double.class)
                    .ignoringCollectionOrder()
                    .isEqualTo(List.of(
                            List.of("gpt-4", 2L, 0.15, 300L, 130L),
                            List.of("gpt-3.5", 2L, 0.05, 200L, 90L)
                    ));
        }

        @Test
        void groupByProjectId() throws Exception {
            var data = queryFromJson("""
                    {
                      "expressions": [
                        "project_id", "count() as cnt", "sum(price) as money",
                        "sum(prompt_tokens) as tokens_p", "sum(completion_tokens) as tokens_c"
                      ],
                      "from": "analytics",
                      "groupBy": ["project_id"],
                      "where": {%s}
                    }""".formatted(TIME_FILTER));

            assertThat(columnNames(data)).containsExactly("project_id", "cnt", "money", "tokens_p", "tokens_c");
            assertThat(data.getData())
                    .usingRecursiveComparison()
                    .withComparatorForType(DOUBLE_COMPARATOR, Double.class)
                    .ignoringCollectionOrder()
                    .isEqualTo(List.of(
                            List.of("proj1", 2L, 0.07, 250L, 110L),
                            List.of("proj2", 2L, 0.13, 250L, 110L)
                    ));
        }

        @Test
        void aggregateWithFieldFilterAndGroupByTagAndField() throws Exception {
            // Filter on field (user_hash), group by tag (deployment) + field (user_hash).
            // user_hash is in groupBy but NOT in expressions — verifies extra
            // group-by columns don't shift result positions.
            var data = queryFromJson("""
                    {
                      "expressions": ["deployment", "count() as cnt"],
                      "from": "analytics",
                      "where": {
                        "$and": [
                          %s, %s,
                          {"$ne": {"left": "user_hash", "right": "'user2'"}}
                        ]
                      },
                      "groupBy": ["deployment", "user_hash"],
                      "orderBy": [{"$desc": "count()"}]
                    }""".formatted(TIME_GTE, TIME_LT));

            // After filtering out user_hash=user2: records #1 (gpt-4,user1) and #3 (gpt-3.5,user1)
            // Both map to distinct (deployment, user_hash) groups with count=1.
            // Both have count=1 so orderBy desc count() doesn't disambiguate.
            assertThat(columnNames(data)).containsExactly("deployment", "cnt");
            assertThat(data.getData()).containsExactlyInAnyOrder(
                    List.of("gpt-4", 1L),
                    List.of("gpt-3.5", 1L)
            );
        }

        @Test
        void groupByAliasInExpressionsAndOrderBy() throws Exception {
            // groupBy references an alias defined in expressions ("proj" for project_id)
            // and orderBy references the count alias ("cnt"). Both must resolve correctly.
            // 4 in-range records — proj1: #1, #3 → 2; proj2: #2, #4 → 2.
            var data = queryFromJson("""
                    {
                      "expressions": ["project_id as proj", "count() as cnt"],
                      "from": "analytics",
                      "groupBy": ["proj"],
                      "where": {%s},
                      "orderBy": [{"$desc": "cnt"}]
                    }""".formatted(TIME_FILTER));

            assertThat(columnNames(data)).containsExactly("proj", "cnt");
            assertThat(data.getData()).containsExactlyInAnyOrder(
                    List.of("proj1", 2L),
                    List.of("proj2", 2L)
            );
        }

    }

    @Nested
    class WindowColumnAggregationTests {

        @Test
        void windowAndColumnAggregation() throws Exception {
            // 4 in-range records across 3 days and 2 deployments.
            // GROUP BY 1-day window + deployment:
            //   day1 (03-11): gpt-4=1, gpt-3.5=0 (gap filled)
            //   day2 (03-12): gpt-4=1, gpt-3.5=1
            //   day3 (03-13): gpt-4=0 (gap filled), gpt-3.5=1
            var data = queryFromJson("""
                    {
                      "expressions": ["window(_time, 1, 'd') as time", "deployment", "count() as requests"],
                      "from": "analytics",
                      "groupBy": ["window(_time, 1, 'd')", "deployment"],
                      "where": {%s}
                    }""".formatted(TIME_FILTER));

            assertThat(columnNames(data)).containsExactly("time", "deployment", "requests");
            assertThat(data.getData()).containsExactlyInAnyOrder(
                    List.of(Instant.parse("2026-03-11T00:00:00Z"), "gpt-4", 1L),
                    List.of(Instant.parse("2026-03-11T00:00:00Z"), "gpt-3.5", 0L),
                    List.of(Instant.parse("2026-03-12T00:00:00Z"), "gpt-4", 1L),
                    List.of(Instant.parse("2026-03-12T00:00:00Z"), "gpt-3.5", 1L),
                    List.of(Instant.parse("2026-03-13T00:00:00Z"), "gpt-4", 0L),
                    List.of(Instant.parse("2026-03-13T00:00:00Z"), "gpt-3.5", 1L)
            );
        }

    }

    @Nested
    class FilterTests {

        @Test
        void equalityFilter() throws Exception {
            var data = queryFromJson("""
                    {
                      "expressions": ["count() as cnt"],
                      "from": "analytics",
                      "where": {
                        "$and": [
                          %s, %s,
                          {"$eq": {"left": "deployment", "right": "'gpt-4'"}}
                        ]
                      }
                    }""".formatted(TIME_GTE, TIME_LT));

            assertThat(columnNames(data)).containsExactly("cnt");
            assertThat(data.getData()).containsExactly(List.of(2L));
        }

        @Test
        void notEqualFilter() throws Exception {
            var data = queryFromJson("""
                    {
                      "expressions": ["count() as cnt"],
                      "from": "analytics",
                      "where": {
                        "$and": [
                          %s, %s,
                          {"$ne": {"left": "deployment", "right": "'gpt-4'"}}
                        ]
                      }
                    }""".formatted(TIME_GTE, TIME_LT));

            assertThat(columnNames(data)).containsExactly("cnt");
            assertThat(data.getData()).containsExactly(List.of(2L));
        }

        @Test
        void orFilter() throws Exception {
            var data = queryFromJson("""
                    {
                      "expressions": ["count() as cnt"],
                      "from": "analytics",
                      "where": {
                        "$and": [
                          %s, %s,
                          {"$or": [
                            {"$eq": {"left": "project_id", "right": "'proj1'"}},
                            {"$eq": {"left": "project_id", "right": "'proj2'"}}
                          ]}
                        ]
                      }
                    }""".formatted(TIME_GTE, TIME_LT));

            assertThat(columnNames(data)).containsExactly("cnt");
            assertThat(data.getData()).containsExactly(List.of(4L));
        }

        @Test
        void doubleEqualityFilterCount() throws Exception {
            // project_id=proj1 AND deployment=gpt-4 → only INSIDE #1 matches.
            var data = queryFromJson("""
                    {
                      "expressions": ["count() as cnt"],
                      "from": "analytics",
                      "where": {
                        "$and": [
                          %s, %s,
                          {"$eq": {"left": "project_id", "right": "'proj1'"}},
                          {"$eq": {"left": "deployment", "right": "'gpt-4'"}}
                        ]
                      }
                    }""".formatted(TIME_GTE, TIME_LT));

            assertThat(columnNames(data)).containsExactly("cnt");
            assertThat(data.getData()).containsExactly(List.of(1L));
        }

        @Test
        void equalityAndNotEqualFilter() throws Exception {
            // project_id=proj1 AND deployment!=gpt-4 → only INSIDE #3 (gpt-3.5, proj1) matches.
            var data = queryFromJson("""
                    {
                      "expressions": ["count() as cnt"],
                      "from": "analytics",
                      "where": {
                        "$and": [
                          %s, %s,
                          {"$eq": {"left": "project_id", "right": "'proj1'"}},
                          {"$ne": {"left": "deployment", "right": "'gpt-4'"}}
                        ]
                      }
                    }""".formatted(TIME_GTE, TIME_LT));

            assertThat(columnNames(data)).containsExactly("cnt");
            assertThat(data.getData()).containsExactly(List.of(1L));
        }

        @Test
        void inFilter() throws Exception {
            var data = queryFromJson("""
                    {
                      "expressions": ["count() as cnt"],
                      "from": "analytics",
                      "where": {
                        "$and": [
                          %s, %s,
                          {"$in": {"left": "user_hash", "right": ["'user1'", "'user2'"]}}
                        ]
                      }
                    }""".formatted(TIME_GTE, TIME_LT));

            assertThat(columnNames(data)).containsExactly("cnt");
            assertThat(data.getData()).containsExactly(List.of(4L));
        }

        @Test
        void notInFilter() throws Exception {
            var data = queryFromJson("""
                    {
                      "expressions": ["count() as cnt"],
                      "from": "analytics",
                      "where": {
                        "$and": [
                          %s, %s,
                          {"$nin": {"left": "user_hash", "right": ["'user1'"]}}
                        ]
                      }
                    }""".formatted(TIME_GTE, TIME_LT));

            assertThat(columnNames(data)).containsExactly("cnt");
            assertThat(data.getData()).containsExactly(List.of(2L));
        }

    }

    @Nested
    class PartialStringSearchTests {

        @Test
        void containsFilter() throws Exception {
            var data = queryFromJson("""
                    {
                      "expressions": ["count() as cnt"],
                      "from": "analytics",
                      "where": {
                        "$and": [
                          %s, %s,
                          {"$contains": {"left": "project_id", "right": "'oj'"}}
                        ]
                      }
                    }""".formatted(TIME_GTE, TIME_LT));

            assertThat(columnNames(data)).containsExactly("cnt");
            assertThat(data.getData()).containsExactly(List.of(4L));
        }

        @Test
        void containsFilterNarrow() throws Exception {
            var data = queryFromJson("""
                    {
                      "expressions": ["count() as cnt"],
                      "from": "analytics",
                      "where": {
                        "$and": [
                          %s, %s,
                          {"$contains": {"left": "project_id", "right": "'1'"}}
                        ]
                      }
                    }""".formatted(TIME_GTE, TIME_LT));

            assertThat(columnNames(data)).containsExactly("cnt");
            assertThat(data.getData()).containsExactly(List.of(2L));
        }

        @Test
        void notContainsFilter() throws Exception {
            var data = queryFromJson("""
                    {
                      "expressions": ["count() as cnt"],
                      "from": "analytics",
                      "where": {
                        "$and": [
                          %s, %s,
                          {"$not_contains": {"left": "deployment", "right": "'4'"}}
                        ]
                      }
                    }""".formatted(TIME_GTE, TIME_LT));

            assertThat(columnNames(data)).containsExactly("cnt");
            assertThat(data.getData()).containsExactly(List.of(2L));
        }

        @Test
        void startsWithFilter() throws Exception {
            var data = queryFromJson("""
                    {
                      "expressions": ["count() as cnt"],
                      "from": "analytics",
                      "where": {
                        "$and": [
                          %s, %s,
                          {"$starts_with": {"left": "deployment", "right": "'gpt-4'"}}
                        ]
                      }
                    }""".formatted(TIME_GTE, TIME_LT));

            assertThat(columnNames(data)).containsExactly("cnt");
            assertThat(data.getData()).containsExactly(List.of(2L));
        }

        @Test
        void endsWithFilter() throws Exception {
            var data = queryFromJson("""
                    {
                      "expressions": ["count() as cnt"],
                      "from": "analytics",
                      "where": {
                        "$and": [
                          %s, %s,
                          {"$ends_with": {"left": "deployment", "right": "'3.5'"}}
                        ]
                      }
                    }""".formatted(TIME_GTE, TIME_LT));

            assertThat(columnNames(data)).containsExactly("cnt");
            assertThat(data.getData()).containsExactly(List.of(2L));
        }

        @Test
        void likeContainsFilter() throws Exception {
            var data = queryFromJson("""
                    {
                      "expressions": ["count() as cnt"],
                      "from": "analytics",
                      "where": {
                        "$and": [
                          %s, %s,
                          {"$like": {"left": "project_id", "right": "'%%oj1%%'"}}
                        ]
                      }
                    }""".formatted(TIME_GTE, TIME_LT));

            assertThat(columnNames(data)).containsExactly("cnt");
            assertThat(data.getData()).containsExactly(List.of(2L));
        }

        @Test
        void likeStartsWithFilter() throws Exception {
            var data = queryFromJson("""
                    {
                      "expressions": ["count() as cnt"],
                      "from": "analytics",
                      "where": {
                        "$and": [
                          %s, %s,
                          {"$like": {"left": "deployment", "right": "'gpt-4%%'"}}
                        ]
                      }
                    }""".formatted(TIME_GTE, TIME_LT));

            assertThat(columnNames(data)).containsExactly("cnt");
            assertThat(data.getData()).containsExactly(List.of(2L));
        }

        @Test
        void likeEndsWithFilter() throws Exception {
            var data = queryFromJson("""
                    {
                      "expressions": ["count() as cnt"],
                      "from": "analytics",
                      "where": {
                        "$and": [
                          %s, %s,
                          {"$like": {"left": "deployment", "right": "'%%3.5'"}}
                        ]
                      }
                    }""".formatted(TIME_GTE, TIME_LT));

            assertThat(columnNames(data)).containsExactly("cnt");
            assertThat(data.getData()).containsExactly(List.of(2L));
        }

        @Test
        void distinctWithContainsFilter() throws Exception {
            var data = queryFromJson("""
                    {
                      "distinct": true,
                      "expressions": ["project_id"],
                      "from": "analytics",
                      "where": {
                        "$and": [
                          %s, %s,
                          {"$contains": {"left": "project_id", "right": "'1'"}}
                        ]
                      }
                    }""".formatted(TIME_GTE, TIME_LT));

            assertThat(columnNames(data)).containsExactly("project_id");
            assertThat(data.getData()).containsExactly(List.of("proj1"));
        }

        @Test
        void distinctWithStartsWithFilter() throws Exception {
            var data = queryFromJson("""
                    {
                      "distinct": true,
                      "expressions": ["deployment"],
                      "from": "analytics",
                      "where": {
                        "$and": [
                          %s, %s,
                          {"$starts_with": {"left": "deployment", "right": "'gpt-3'"}}
                        ]
                      }
                    }""".formatted(TIME_GTE, TIME_LT));

            assertThat(columnNames(data)).containsExactly("deployment");
            assertThat(data.getData()).containsExactly(List.of("gpt-3.5"));
        }

        @Test
        void startsWithAndEqualityFilter() throws Exception {
            // deployment STARTS_WITH 'gpt-4' AND project_id='proj1' → only INSIDE #1.
            var data = queryFromJson("""
                    {
                      "expressions": ["count() as cnt"],
                      "from": "analytics",
                      "where": {
                        "$and": [
                          %s, %s,
                          {"$starts_with": {"left": "deployment", "right": "'gpt-4'"}},
                          {"$eq": {"left": "project_id", "right": "'proj1'"}}
                        ]
                      }
                    }""".formatted(TIME_GTE, TIME_LT));

            assertThat(columnNames(data)).containsExactly("cnt");
            assertThat(data.getData()).containsExactly(List.of(1L));
        }

        @Test
        void distinctWithEndsWithFilter() throws Exception {
            var data = queryFromJson("""
                    {
                      "distinct": true,
                      "expressions": ["deployment"],
                      "from": "analytics",
                      "where": {
                        "$and": [
                          %s, %s,
                          {"$ends_with": {"left": "deployment", "right": "'3.5'"}}
                        ]
                      }
                    }""".formatted(TIME_GTE, TIME_LT));

            assertThat(columnNames(data)).containsExactly("deployment");
            assertThat(data.getData()).containsExactly(List.of("gpt-3.5"));
        }
    }

    @Nested
    class CaseInsensitiveSearchTests {

        @Test
        void containsFilterCaseInsensitive() throws Exception {
            // "GPT" (uppercase) should match "gpt-4" and "gpt-3.5"
            var data = queryFromJson("""
                    {
                      "expressions": ["count() as cnt"],
                      "from": "analytics",
                      "where": {
                        "$and": [
                          %s, %s,
                          {"$contains": {"left": "deployment", "right": "'GPT'"}}
                        ]
                      }
                    }""".formatted(TIME_GTE, TIME_LT));

            assertThat(columnNames(data)).containsExactly("cnt");
            assertThat(data.getData()).containsExactly(List.of(4L));
        }

        @Test
        void notContainsFilterCaseInsensitive() throws Exception {
            // NOT_CONTAINS "GPT" (uppercase) should exclude all deployments
            var data = queryFromJson("""
                    {
                      "distinct": true,
                      "expressions": ["deployment"],
                      "from": "analytics",
                      "where": {
                        "$and": [
                          %s, %s,
                          {"$not_contains": {"left": "deployment", "right": "'GPT'"}}
                        ]
                      }
                    }""".formatted(TIME_GTE, TIME_LT));

            assertThat(columnNames(data)).containsExactly("deployment");
            assertThat(data.getData()).isEmpty();
        }

        @Test
        void startsWithFilterCaseInsensitive() throws Exception {
            // "GPT-4" (uppercase) should match "gpt-4"
            var data = queryFromJson("""
                    {
                      "distinct": true,
                      "expressions": ["deployment"],
                      "from": "analytics",
                      "where": {
                        "$and": [
                          %s, %s,
                          {"$starts_with": {"left": "deployment", "right": "'GPT-4'"}}
                        ]
                      }
                    }""".formatted(TIME_GTE, TIME_LT));

            assertThat(columnNames(data)).containsExactly("deployment");
            assertThat(data.getData()).containsExactly(List.of("gpt-4"));
        }

        @Test
        void endsWithFilterCaseInsensitive() throws Exception {
            // "PROJ1" (uppercase) should match "proj1"
            var data = queryFromJson("""
                    {
                      "distinct": true,
                      "expressions": ["project_id"],
                      "from": "analytics",
                      "where": {
                        "$and": [
                          %s, %s,
                          {"$ends_with": {"left": "project_id", "right": "'PROJ1'"}}
                        ]
                      }
                    }""".formatted(TIME_GTE, TIME_LT));

            assertThat(columnNames(data)).containsExactly("project_id");
            assertThat(data.getData()).containsExactly(List.of("proj1"));
        }

        @Test
        void likeFilterCaseInsensitive() throws Exception {
            // LIKE '%OJ1%' (uppercase) should match "proj1"
            var data = queryFromJson("""
                    {
                      "expressions": ["count() as cnt"],
                      "from": "analytics",
                      "where": {
                        "$and": [
                          %s, %s,
                          {"$like": {"left": "project_id", "right": "'%%OJ1%%'"}}
                        ]
                      }
                    }""".formatted(TIME_GTE, TIME_LT));

            assertThat(columnNames(data)).containsExactly("cnt");
            assertThat(data.getData()).containsExactly(List.of(2L));
        }

        @Test
        void containsFilterNoMatch() throws Exception {
            var data = queryFromJson("""
                    {
                      "distinct": true,
                      "expressions": ["project_id"],
                      "from": "analytics",
                      "where": {
                        "$and": [
                          %s, %s,
                          {"$contains": {"left": "project_id", "right": "'xyz'"}}
                        ]
                      }
                    }""".formatted(TIME_GTE, TIME_LT));

            assertThat(columnNames(data)).containsExactly("project_id");
            assertThat(data.getData()).isEmpty();
        }
    }

    @Nested
    class PaginationTests {

        @Test
        void limitResults() throws Exception {
            var data = queryFromJson("""
                    {
                      "expressions": ["deployment", "price"],
                      "from": "analytics",
                      "where": {%s},
                      "limit": 2
                    }""".formatted(TIME_FILTER));

            assertThat(columnNames(data)).containsExactly("deployment", "price");
            assertThat(data.getData()).hasSize(2);
        }

        @Test
        void limitWithOffset() throws Exception {
            // offset=2 with limit=2 on 4 in-range rows should return exactly 2 rows
            var data = queryFromJson("""
                    {
                      "expressions": ["deployment", "price"],
                      "from": "analytics",
                      "where": {%s},
                      "limit": 2,
                      "offset": 2
                    }""".formatted(TIME_FILTER));

            assertThat(columnNames(data)).containsExactly("deployment", "price");
            assertThat(data.getData()).hasSize(2);

            // offset=3 with limit=2 on 4 in-range rows should return only 1 row
            var tailData = queryFromJson("""
                    {
                      "expressions": ["deployment", "price"],
                      "from": "analytics",
                      "where": {%s},
                      "limit": 2,
                      "offset": 3
                    }""".formatted(TIME_FILTER));

            assertThat(tailData.getData()).hasSize(1);
        }
    }

    @Nested
    class EdgeCaseTests {

        @Test
        void emptyResultSet() throws Exception {
            var data = queryFromJson("""
                    {
                      "distinct": true,
                      "expressions": ["deployment"],
                      "from": "analytics",
                      "where": {
                        "$and": [
                          %s, %s,
                          {"$eq": {"left": "deployment", "right": "'nonexistent'"}}
                        ]
                      }
                    }""".formatted(TIME_GTE, TIME_LT));

            assertThat(columnNames(data)).containsExactly("deployment");
            assertThat(data.getData()).isEmpty();
        }

        @Test
        void countWithNoMatchingFilter() throws Exception {
            var data = queryFromJson("""
                    {
                      "expressions": ["count() as cnt"],
                      "from": "analytics",
                      "where": {
                        "$and": [
                          %s, %s,
                          {"$eq": {"left": "deployment", "right": "'nonexistent'"}}
                        ]
                      }
                    }""".formatted(TIME_GTE, TIME_LT));

            assertThat(columnNames(data)).containsExactly("cnt");
            assertThat(data.getData()).containsExactly(List.of(0L));
        }

        @Test
        void sumWithNoMatchingFilter() throws Exception {
            var data = queryFromJson("""
                    {
                      "expressions": ["sum(deployment_price) as total"],
                      "from": "analytics",
                      "where": {
                        "$and": [
                          %s, %s,
                          {"$eq": {"left": "deployment", "right": "'nonexistent'"}}
                        ]
                      }
                    }""".formatted(TIME_GTE, TIME_LT));

            assertThat(columnNames(data)).containsExactly("total");
            assertThat(data.getData()).containsExactly(List.of(0.0));
        }

        @Test
        void multipleAggregationsWithNoMatchingFilter() throws Exception {
            var data = queryFromJson("""
                    {
                      "expressions": ["count() as cnt", "sum(deployment_price) as money", "sum(prompt_tokens) as tokens"],
                      "from": "analytics",
                      "where": {
                        "$and": [
                          %s, %s,
                          {"$eq": {"left": "deployment", "right": "'nonexistent'"}}
                        ]
                      }
                    }""".formatted(TIME_GTE, TIME_LT));

            assertThat(columnNames(data)).containsExactly("cnt", "money", "tokens");
            assertThat(data.getData()).containsExactly(List.of(0L, 0.0, 0L));
        }

        @Test
        void doubleEqualityNoIntersection() throws Exception {
            // deployment=gpt-3.5 matches #3,#4 (user_hash=user1,user2).
            // user_hash=user3 appears only on the OUT-OF-RANGE record.
            // AND of both inside the time range → empty intersection.
            var data = queryFromJson("""
                    {
                      "expressions": ["count() as cnt"],
                      "from": "analytics",
                      "where": {
                        "$and": [
                          %s, %s,
                          {"$eq": {"left": "deployment", "right": "'gpt-3.5'"}},
                          {"$eq": {"left": "user_hash", "right": "'user3'"}}
                        ]
                      }
                    }""".formatted(TIME_GTE, TIME_LT));

            assertThat(columnNames(data)).containsExactly("cnt");
            assertThat(data.getData()).containsExactly(List.of(0L));
        }

        @Test
        void expressionAliasesPreserved() throws Exception {
            var data = queryFromJson("""
                    {
                      "expressions": ["sum(price) as money", "count() as requests"],
                      "from": "analytics",
                      "where": {%s}
                    }""".formatted(TIME_FILTER));

            assertThat(columnNames(data)).containsExactly("money", "requests");
            assertThat(data.getData()).hasSize(1);
        }
    }

    @Nested
    class UuidLiteralFilterTests {

        // Time range covering only the UUID_PROJECT_RECORDS row at 2026-03-15T10:00:00Z.
        private static final String UUID_TIME_GTE = """
                {"$gte": {"left": "_time", "right": "'2026-03-15T00:00:00Z'"}}""";
        private static final String UUID_TIME_LT = """
                {"$lt": {"left": "_time", "right": "'2026-03-16T00:00:00Z'"}}""";

        @Test
        void equalityFilterWithUuidLiteralAgainstStringColumn() throws Exception {
            // Reproduces the analytics-UI bug: project_id is a STRING tag, but a
            // UUID-shaped literal is auto-typed as Type.UUID by enterString_literal.
            // Without the STRING/UUID coercion in ValidationUtils.isSubType this query
            // throws "Comparision STRING (project_id) and UUID (...) types using
            // EQUALS operator is unsupported." With the fix it matches exactly one row.
            var data = queryFromJson("""
                    {
                      "expressions": ["project_id", "count() as cnt"],
                      "from": "mcp_analytics",
                      "groupBy": ["project_id"],
                      "where": {
                        "$and": [
                          %s, %s,
                          {"$eq": {"left": "project_id", "right": "'%s'"}}
                        ]
                      }
                    }""".formatted(UUID_TIME_GTE, UUID_TIME_LT, UUID_PROJECT_ID));

            assertThat(columnNames(data)).containsExactly("project_id", "cnt");
            assertThat(data.getData()).containsExactly(List.of(UUID_PROJECT_ID, 1L));
        }
    }

    @Nested
    class GapFillingDisabledTests {

        @Test
        void windowAggregationNoGapFill() throws Exception {
            // Same 8-hour window query as windowAggregationFillsGaps, but with fillGaps=false.
            // Should return only buckets that have actual data (no zero-filled rows).
            var data = queryFromJson("""
                    {
                      "expressions": ["window(_time, 8, 'h') as time", "count() as requests"],
                      "from": "analytics",
                      "groupBy": ["window(_time, 8, 'h')"],
                      "where": {%s},
                      "orderBy": [{"$asc": "time"}]
                    }""".formatted(TIME_FILTER), false);

            assertThat(columnNames(data)).containsExactly("time", "requests");
            assertThat(data.getData()).containsExactly(
                    List.of(Instant.parse("2026-03-11T08:00:00Z"), 1L),
                    List.of(Instant.parse("2026-03-12T08:00:00Z"), 1L),
                    List.of(Instant.parse("2026-03-12T16:00:00Z"), 1L),
                    List.of(Instant.parse("2026-03-13T08:00:00Z"), 1L)
            );
        }

        @Test
        void windowAndColumnAggregationNoGapFill() throws Exception {
            // Same query as windowAndColumnAggregation, but with fillGaps=false.
            // Should return only rows with actual data (no zero-filled deployment rows).
            var data = queryFromJson("""
                    {
                      "expressions": ["window(_time, 1, 'd') as time", "deployment", "count() as requests"],
                      "from": "analytics",
                      "groupBy": ["window(_time, 1, 'd')", "deployment"],
                      "where": {%s}
                    }""".formatted(TIME_FILTER), false);

            assertThat(columnNames(data)).containsExactly("time", "deployment", "requests");
            assertThat(data.getData()).containsExactlyInAnyOrder(
                    List.of(Instant.parse("2026-03-11T00:00:00Z"), "gpt-4", 1L),
                    List.of(Instant.parse("2026-03-12T00:00:00Z"), "gpt-4", 1L),
                    List.of(Instant.parse("2026-03-12T00:00:00Z"), "gpt-3.5", 1L),
                    List.of(Instant.parse("2026-03-13T00:00:00Z"), "gpt-3.5", 1L)
            );
        }
    }

}
