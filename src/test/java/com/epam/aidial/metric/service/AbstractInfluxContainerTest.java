package com.epam.aidial.metric.service;

import com.epam.aidial.ql.Engine;
import com.epam.aidial.ql.LanguageConverter;
import com.epam.aidial.ql.deserializers.json.QueryLanguageModule;
import com.epam.aidial.ql.dto.CompletableDto;
import com.epam.aidial.ql.model.Data;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;

/**
 * Shared test suite for InfluxDB 2 and InfluxDB 3 metrics extraction.
 * Both engines must produce identical results from the same line-protocol data
 * when queried with the same JSON statements.
 */
public abstract class AbstractInfluxContainerTest {

    private static final ObjectMapper QUERY_MAPPER = new ObjectMapper();

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
        var engine = getEngine();
        var languageConverter = new LanguageConverter(engine);
        var dto = QUERY_MAPPER.readValue(json, CompletableDto.class);
        var completable = languageConverter.convert(dto, engine.getTables());
        return engine.getData(completable);
    }

    protected static List<String> columnNames(Data data) {
        return data.getExpressions().stream()
                .map(expr -> (expr instanceof com.epam.aidial.expressions.Column col) ? col.getName() : expr.toString())
                .toList();
    }

    @Nested
    class AggregationTests {

        @Test
        void windowAggregation() throws Exception {
            var data = queryFromJson("""
                    {
                      "expressions": ["window(_time, 1, 'm') as time", "count() as requests"],
                      "from": "analytics",
                      "groupBy": ["window(_time, 1, 'm')"],
                      "where": {%s}
                    }""".formatted(TIME_FILTER));

            assertThat(data.getData()).hasSize(4);
            for (var row : data.getData()) {
                assertThat(row.get(1)).isEqualTo(1L);
            }
        }

        @Test
        void totalCount() throws Exception {
            var data = queryFromJson("""
                    {
                      "expressions": ["count()"],
                      "from": "analytics",
                      "where": {%s}
                    }""".formatted(TIME_FILTER));

            assertThat(data.getData()).hasSize(1);
            assertThat(data.getData().get(0).get(0)).isEqualTo(4L);
        }

        @Test
        void sumTokens() throws Exception {
            var data = queryFromJson("""
                    {
                      "expressions": ["sum(prompt_tokens)", "sum(completion_tokens)"],
                      "from": "analytics",
                      "where": {%s}
                    }""".formatted(TIME_FILTER));

            assertThat(data.getData()).hasSize(1);
            var row = data.getData().get(0);
            assertThat(row.get(0)).isEqualTo(500L);
            assertThat(row.get(1)).isEqualTo(220L);
        }

        @Test
        void sumDeploymentPrice() throws Exception {
            var data = queryFromJson("""
                    {
                      "expressions": ["sum(deployment_price)"],
                      "from": "analytics",
                      "where": {%s}
                    }""".formatted(TIME_FILTER));

            assertThat(data.getData()).hasSize(1);
            assertThat((Double) data.getData().get(0).get(0)).isCloseTo(0.19, offset(0.001));
        }

        @Test
        void countDistinctUsers() throws Exception {
            var data = queryFromJson("""
                    {
                      "expressions": ["count()"],
                      "from": {
                        "distinct": "true",
                        "expressions": ["user_hash"],
                        "from": "analytics",
                        "where": {%s}
                      }
                    }""".formatted(TIME_FILTER));

            assertThat(data.getData()).hasSize(1);
            assertThat(data.getData().get(0).get(0)).isEqualTo(2L);
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
                      "expressions": ["deployment", "count()"],
                      "from": "mcp_analytics",
                      "groupBy": ["deployment"],
                      "where": {%s},
                      "orderBy": [{"$desc": "count()"}]
                    }""".formatted(TIME_FILTER));

            assertThat(data.getData()).hasSize(2);

            var byDeployment = data.getData().stream()
                    .collect(Collectors.toMap(row -> (String) row.get(0), row -> row));

            assertThat(byDeployment.get("gpt-4").get(1)).isEqualTo(4L);
            assertThat(byDeployment.get("gpt-3.5").get(1)).isEqualTo(3L);
        }

        @Test
        void totalCount_whenSchemaColumnAbsentFromSomeData() throws Exception {
            // count() without group-by on a table where project_id is absent from some records.
            var data = queryFromJson("""
                    {
                      "expressions": ["count()"],
                      "from": "mcp_analytics",
                      "where": {%s}
                    }""".formatted(TIME_FILTER));

            assertThat(data.getData()).hasSize(1);
            assertThat(data.getData().get(0).get(0)).isEqualTo(7L);
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

    }

    @Nested
    class GroupByTests {

        @Test
        void groupByDeployment() throws Exception {
            var data = queryFromJson("""
                    {
                      "expressions": [
                        "deployment", "count()", "sum(price) as money",
                        "sum(prompt_tokens) as tokens_p", "sum(completion_tokens) as tokens_c"
                      ],
                      "from": "analytics",
                      "groupBy": ["deployment"],
                      "where": {%s}
                    }""".formatted(TIME_FILTER));

            assertThat(data.getData()).hasSize(2);

            var byDeployment = data.getData().stream()
                    .collect(Collectors.toMap(row -> (String) row.get(0), row -> row));

            var gpt4 = byDeployment.get("gpt-4");
            assertThat(gpt4.get(1)).isEqualTo(2L);
            assertThat((Double) gpt4.get(2)).isCloseTo(0.15, offset(0.001));
            assertThat(gpt4.get(3)).isEqualTo(300L);
            assertThat(gpt4.get(4)).isEqualTo(130L);

            var gpt35 = byDeployment.get("gpt-3.5");
            assertThat(gpt35.get(1)).isEqualTo(2L);
            assertThat((Double) gpt35.get(2)).isCloseTo(0.05, offset(0.001));
            assertThat(gpt35.get(3)).isEqualTo(200L);
            assertThat(gpt35.get(4)).isEqualTo(90L);
        }

        @Test
        void groupByProjectId() throws Exception {
            var data = queryFromJson("""
                    {
                      "expressions": [
                        "project_id", "count()", "sum(price) as money",
                        "sum(prompt_tokens) as tokens_p", "sum(completion_tokens) as tokens_c"
                      ],
                      "from": "analytics",
                      "groupBy": ["project_id"],
                      "where": {%s}
                    }""".formatted(TIME_FILTER));

            assertThat(data.getData()).hasSize(2);

            var byProject = data.getData().stream()
                    .collect(Collectors.toMap(row -> (String) row.get(0), row -> row));

            var proj1 = byProject.get("proj1");
            assertThat(proj1.get(1)).isEqualTo(2L);
            assertThat((Double) proj1.get(2)).isCloseTo(0.07, offset(0.001));
            assertThat(proj1.get(3)).isEqualTo(250L);
            assertThat(proj1.get(4)).isEqualTo(110L);

            var proj2 = byProject.get("proj2");
            assertThat(proj2.get(1)).isEqualTo(2L);
            assertThat((Double) proj2.get(2)).isCloseTo(0.13, offset(0.001));
            assertThat(proj2.get(3)).isEqualTo(250L);
            assertThat(proj2.get(4)).isEqualTo(110L);
        }

        @Test
        void aggregateWithFieldFilterAndGroupByTagAndField() throws Exception {
            // Filter on field (user_hash), group by tag (deployment) + field (user_hash).
            // user_hash is in groupBy but NOT in expressions — verifies extra
            // group-by columns don't shift result positions.
            var data = queryFromJson("""
                    {
                      "expressions": ["deployment", "count()"],
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
            // Both map to distinct (deployment, user_hash) groups with count=1
            assertThat(data.getData()).hasSize(2);

            var counts = data.getData().stream()
                    .map(row -> row.get(1))
                    .toList();
            assertThat(counts).containsExactly(1L, 1L);
        }

    }

    @Nested
    class WindowColumnAggregationTests {

        @Test
        void windowAndColumnAggregation() throws Exception {
            // 4 in-range records across 3 days and 2 deployments.
            // GROUP BY 1-day window + deployment:
            //   day1 (03-11): gpt-4=1
            //   day2 (03-12): gpt-4=1, gpt-3.5=1
            //   day3 (03-13): gpt-3.5=1
            var data = queryFromJson("""
                    {
                      "expressions": ["window(_time, 1, 'd') as time", "deployment", "count() as requests"],
                      "from": "analytics",
                      "groupBy": ["window(_time, 1, 'd')", "deployment"],
                      "where": {%s}
                    }""".formatted(TIME_FILTER));

            assertThat(data.getData()).hasSize(4);

            for (var row : data.getData()) {
                assertThat(row.get(2)).isEqualTo(1L);
            }
        }

    }

    @Nested
    class FilterTests {

        @Test
        void equalityFilter() throws Exception {
            var data = queryFromJson("""
                    {
                      "expressions": ["count()"],
                      "from": "analytics",
                      "where": {
                        "$and": [
                          %s, %s,
                          {"$eq": {"left": "deployment", "right": "'gpt-4'"}}
                        ]
                      }
                    }""".formatted(TIME_GTE, TIME_LT));

            assertThat(data.getData()).hasSize(1);
            assertThat(data.getData().get(0).get(0)).isEqualTo(2L);
        }

        @Test
        void notEqualFilter() throws Exception {
            var data = queryFromJson("""
                    {
                      "expressions": ["count()"],
                      "from": "analytics",
                      "where": {
                        "$and": [
                          %s, %s,
                          {"$ne": {"left": "deployment", "right": "'gpt-4'"}}
                        ]
                      }
                    }""".formatted(TIME_GTE, TIME_LT));

            assertThat(data.getData()).hasSize(1);
            assertThat(data.getData().get(0).get(0)).isEqualTo(2L);
        }

        @Test
        void orFilter() throws Exception {
            var data = queryFromJson("""
                    {
                      "expressions": ["count()"],
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

            assertThat(data.getData()).hasSize(1);
            assertThat(data.getData().get(0).get(0)).isEqualTo(4L);
        }

    }

    @Nested
    class PartialStringSearchTests {

        @Test
        void containsFilter() throws Exception {
            var data = queryFromJson("""
                    {
                      "expressions": ["count()"],
                      "from": "analytics",
                      "where": {
                        "$and": [
                          %s, %s,
                          {"$contains": {"left": "project_id", "right": "'oj'"}}
                        ]
                      }
                    }""".formatted(TIME_GTE, TIME_LT));

            assertThat(data.getData()).hasSize(1);
            assertThat(data.getData().get(0).get(0)).isEqualTo(4L);
        }

        @Test
        void containsFilterNarrow() throws Exception {
            var data = queryFromJson("""
                    {
                      "expressions": ["count()"],
                      "from": "analytics",
                      "where": {
                        "$and": [
                          %s, %s,
                          {"$contains": {"left": "project_id", "right": "'1'"}}
                        ]
                      }
                    }""".formatted(TIME_GTE, TIME_LT));

            assertThat(data.getData()).hasSize(1);
            assertThat(data.getData().get(0).get(0)).isEqualTo(2L);
        }

        @Test
        void notContainsFilter() throws Exception {
            var data = queryFromJson("""
                    {
                      "expressions": ["count()"],
                      "from": "analytics",
                      "where": {
                        "$and": [
                          %s, %s,
                          {"$not_contains": {"left": "deployment", "right": "'4'"}}
                        ]
                      }
                    }""".formatted(TIME_GTE, TIME_LT));

            assertThat(data.getData()).hasSize(1);
            assertThat(data.getData().get(0).get(0)).isEqualTo(2L);
        }

        @Test
        void startsWithFilter() throws Exception {
            var data = queryFromJson("""
                    {
                      "expressions": ["count()"],
                      "from": "analytics",
                      "where": {
                        "$and": [
                          %s, %s,
                          {"$starts_with": {"left": "deployment", "right": "'gpt-4'"}}
                        ]
                      }
                    }""".formatted(TIME_GTE, TIME_LT));

            assertThat(data.getData()).hasSize(1);
            assertThat(data.getData().get(0).get(0)).isEqualTo(2L);
        }

        @Test
        void endsWithFilter() throws Exception {
            var data = queryFromJson("""
                    {
                      "expressions": ["count()"],
                      "from": "analytics",
                      "where": {
                        "$and": [
                          %s, %s,
                          {"$ends_with": {"left": "deployment", "right": "'3.5'"}}
                        ]
                      }
                    }""".formatted(TIME_GTE, TIME_LT));

            assertThat(data.getData()).hasSize(1);
            assertThat(data.getData().get(0).get(0)).isEqualTo(2L);
        }

        @Test
        void likeContainsFilter() throws Exception {
            var data = queryFromJson("""
                    {
                      "expressions": ["count()"],
                      "from": "analytics",
                      "where": {
                        "$and": [
                          %s, %s,
                          {"$like": {"left": "project_id", "right": "'%%oj1%%'"}}
                        ]
                      }
                    }""".formatted(TIME_GTE, TIME_LT));

            assertThat(data.getData()).hasSize(1);
            assertThat(data.getData().get(0).get(0)).isEqualTo(2L);
        }

        @Test
        void likeStartsWithFilter() throws Exception {
            var data = queryFromJson("""
                    {
                      "expressions": ["count()"],
                      "from": "analytics",
                      "where": {
                        "$and": [
                          %s, %s,
                          {"$like": {"left": "deployment", "right": "'gpt-4%%'"}}
                        ]
                      }
                    }""".formatted(TIME_GTE, TIME_LT));

            assertThat(data.getData()).hasSize(1);
            assertThat(data.getData().get(0).get(0)).isEqualTo(2L);
        }

        @Test
        void likeEndsWithFilter() throws Exception {
            var data = queryFromJson("""
                    {
                      "expressions": ["count()"],
                      "from": "analytics",
                      "where": {
                        "$and": [
                          %s, %s,
                          {"$like": {"left": "deployment", "right": "'%%3.5'"}}
                        ]
                      }
                    }""".formatted(TIME_GTE, TIME_LT));

            assertThat(data.getData()).hasSize(1);
            assertThat(data.getData().get(0).get(0)).isEqualTo(2L);
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

            assertThat(data.getData()).hasSize(1);
            assertThat(data.getData().get(0).get(0)).isEqualTo("proj1");
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

            assertThat(data.getData()).hasSize(1);
            assertThat(data.getData().get(0).get(0)).isEqualTo("gpt-3.5");
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

            assertThat(data.getData()).hasSize(1);
            assertThat(data.getData().get(0).get(0)).isEqualTo("gpt-3.5");
        }
    }

    @Nested
    class CaseInsensitiveSearchTests {

        @Test
        void containsFilterCaseInsensitive() throws Exception {
            // "GPT" (uppercase) should match "gpt-4" and "gpt-3.5"
            var data = queryFromJson("""
                    {
                      "expressions": ["count()"],
                      "from": "analytics",
                      "where": {
                        "$and": [
                          %s, %s,
                          {"$contains": {"left": "deployment", "right": "'GPT'"}}
                        ]
                      }
                    }""".formatted(TIME_GTE, TIME_LT));

            assertThat(data.getData()).hasSize(1);
            assertThat(data.getData().get(0).get(0)).isEqualTo(4L);
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

            assertThat(data.getData()).hasSize(1);
            assertThat(data.getData().get(0).get(0)).isEqualTo("gpt-4");
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

            assertThat(data.getData()).hasSize(1);
            assertThat(data.getData().get(0).get(0)).isEqualTo("proj1");
        }

        @Test
        void likeFilterCaseInsensitive() throws Exception {
            // LIKE '%OJ1%' (uppercase) should match "proj1"
            var data = queryFromJson("""
                    {
                      "expressions": ["count()"],
                      "from": "analytics",
                      "where": {
                        "$and": [
                          %s, %s,
                          {"$like": {"left": "project_id", "right": "'%%OJ1%%'"}}
                        ]
                      }
                    }""".formatted(TIME_GTE, TIME_LT));

            assertThat(data.getData()).hasSize(1);
            assertThat(data.getData().get(0).get(0)).isEqualTo(2L);
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

            assertThat(data.getData()).isEmpty();
        }

        @Test
        void countWithNoMatchingFilter() throws Exception {
            var data = queryFromJson("""
                    {
                      "expressions": ["count()"],
                      "from": "analytics",
                      "where": {
                        "$and": [
                          %s, %s,
                          {"$eq": {"left": "deployment", "right": "'nonexistent'"}}
                        ]
                      }
                    }""".formatted(TIME_GTE, TIME_LT));

            assertThat(data.getData()).hasSize(1);
            assertThat(data.getData().get(0).get(0)).isEqualTo(0L);
        }

        @Test
        void expressionAliasesPreserved() throws Exception {
            var data = queryFromJson("""
                    {
                      "expressions": ["sum(price) as money", "count() as requests"],
                      "from": "analytics",
                      "where": {%s}
                    }""".formatted(TIME_FILTER));

            assertThat(data.getExpressions()).hasSize(2);
            var aliasNames = data.getExpressions().stream()
                    .map(Object::toString)
                    .toList();
            assertThat(aliasNames).anyMatch(name -> name.contains("money"));
            assertThat(aliasNames).anyMatch(name -> name.contains("requests"));
        }
    }

}
