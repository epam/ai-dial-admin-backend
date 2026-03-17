package com.epam.aidial.metric.service;

import com.epam.aidial.ql.Engine;
import com.epam.aidial.ql.LanguageConverter;
import com.epam.aidial.ql.deserializers.json.QueryLanguageModule;
import com.epam.aidial.ql.dto.CompletableDto;
import com.epam.aidial.ql.model.Data;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

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

    // Time range: [2026-03-11T13:33:38.680Z, 2026-03-13T13:33:38.680Z)
    // 6 records total: 4 inside the range, 2 outside
    protected static final List<String> TEST_RECORDS = List.of(
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

    private static final String TIME_FILTER = """
            "$and":[{"$gte":{"left":"_time","right":"'2026-03-11T13:33:38.680Z'"}},\
            {"$lt":{"left":"_time","right":"'2026-03-13T13:33:38.680Z'"}}]""";

    protected abstract Engine getEngine();

    @Test
    void windowAggregation() throws Exception {
        var data = queryFromJson("""
                {"expressions":["window(_time, 1, 'm') as time","count() as requests"],\
                "from":"analytics",\
                "groupBy":["window(_time, 1, 'm')"],\
                "where":{%s}}""".formatted(TIME_FILTER));

        assertThat(data.getData()).hasSize(4);
        for (var row : data.getData()) {
            assertThat(row.get(1)).isEqualTo(1L);
        }
    }

    @Test
    void countDistinctUsers() throws Exception {
        var data = queryFromJson("""
                {"expressions":["count()"],\
                "from":{"distinct":"true","expressions":["user_hash"],\
                "from":"analytics",\
                "where":{%s}}}""".formatted(TIME_FILTER));

        assertThat(data.getData()).hasSize(1);
        assertThat(data.getData().get(0).get(0)).isEqualTo(2L);
    }

    @Test
    void totalCount() throws Exception {
        var data = queryFromJson("""
                {"expressions":["count()"],\
                "from":"analytics",\
                "where":{%s}}""".formatted(TIME_FILTER));

        assertThat(data.getData()).hasSize(1);
        assertThat(data.getData().get(0).get(0)).isEqualTo(4L);
    }

    @Test
    void sumTokens() throws Exception {
        var data = queryFromJson("""
                {"expressions":["sum(prompt_tokens)","sum(completion_tokens)"],\
                "from":"analytics",\
                "where":{%s}}""".formatted(TIME_FILTER));

        assertThat(data.getData()).hasSize(1);
        var row = data.getData().get(0);
        assertThat(row.get(0)).isEqualTo(500L);
        assertThat(row.get(1)).isEqualTo(220L);
    }

    @Test
    void sumDeploymentPrice() throws Exception {
        var data = queryFromJson("""
                {"expressions":["sum(deployment_price)"],\
                "from":"analytics",\
                "where":{%s}}""".formatted(TIME_FILTER));

        assertThat(data.getData()).hasSize(1);
        assertThat((Double) data.getData().get(0).get(0)).isCloseTo(0.19, offset(0.001));
    }

    @Test
    void groupByDeployment() throws Exception {
        var data = queryFromJson("""
                {"expressions":["deployment","count()","sum(price) as money","sum(prompt_tokens) as tokens_p","sum(completion_tokens) as tokens_c"],\
                "from":"analytics",\
                "groupBy":["deployment"],\
                "where":{%s}}""".formatted(TIME_FILTER));

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
                {"expressions":["project_id","count()","sum(price) as money",\
                "sum(prompt_tokens) as tokens_p","sum(completion_tokens) as tokens_c"],\
                "from":"analytics",\
                "groupBy":["project_id"],\
                "where":{%s}}""".formatted(TIME_FILTER));

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

    private Data queryFromJson(String json) throws Exception {
        var engine = getEngine();
        var languageConverter = new LanguageConverter(engine);
        var dto = QUERY_MAPPER.readValue(json, CompletableDto.class);
        var completable = languageConverter.convert(dto, engine.getTables());
        return engine.getData(completable);
    }
}
