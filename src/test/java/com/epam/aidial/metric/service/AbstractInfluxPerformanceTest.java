package com.epam.aidial.metric.service;

import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.epam.aidial.metric.web.dto.DataQuery;
import com.epam.aidial.metric.web.dto.JsonDataQuery;
import com.epam.aidial.ql.Engine;
import com.epam.aidial.ql.LanguageConverter;
import com.epam.aidial.ql.model.Data;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Shared performance test suite for InfluxDB 2 and InfluxDB 3 metrics extraction.
 *
 * <p>Seeds 50,000 deterministically-generated {@code analytics} records spread uniformly
 * across a 48-hour window, then executes the seven dashboard queries (provided by the
 * task) against the engine under test. Each query is warmed up once and measured across
 * five runs; min/p50/p95/max/mean wall-clock nanos are logged via SLF4J.
 *
 * <p>Data is generated only once per JVM (static initializer) so both engines ingest
 * identical line-protocol bytes, and both test classes reuse the same {@link #PERF_RECORDS}
 * list without regenerating it.
 *
 * <p>Tagged {@code "performance"} so the default {@code test} Gradle task excludes it;
 * run via the dedicated {@code perfTest} task.
 */
@Tag("performance")
public abstract class AbstractInfluxPerformanceTest {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractInfluxPerformanceTest.class);

    private static final ObjectMapper QUERY_MAPPER = JsonMapperConfiguration.createJsonMapper();

    protected static final int RECORD_COUNT = 100_000;
    protected static final int WARMUP_RUNS = 0;
    protected static final int MEASURED_RUNS = 1;

    // Data generation parameters — kept deterministic so engines compare apples to apples.
    private static final long SEED = 42L;
    private static final Instant RANGE_START = Instant.parse("2026-04-13T12:40:00Z");
    private static final Instant RANGE_END = Instant.parse("2026-04-15T12:40:00Z");

    private static final List<String> DEPLOYMENTS = List.of(
            "gpt-4", "gpt-4o", "gpt-3.5", "claude-3-opus", "claude-3-sonnet",
            "claude-3-haiku", "gemini-pro", "llama-3-70b", "mistral-large", "cohere-command");
    private static final List<String> PARENT_DEPLOYMENTS = List.of(
            "orchestrator-app", "summarizer-app", "router-app", "plugin-host", "standalone");
    private static final List<String> LANGUAGES = List.of(
            "en", "es", "fr", "de", "it", "pt", "ja", "ko", "zh", "ru");
    private static final List<String> UPSTREAMS = List.of(
            "https://upstream-a.example/v1", "https://upstream-b.example/v1",
            "https://upstream-c.example/v1", "https://upstream-d.example/v1",
            "https://upstream-e.example/v1");
    private static final List<String> TOPICS = List.of(
            "coding", "writing", "math", "science", "history", "translation",
            "summarization", "analysis", "chat", "support", "legal", "medical",
            "finance", "education", "travel", "food", "sports", "music", "art", "other");
    private static final List<String> TITLES = List.of(
            "engineer", "manager", "analyst", "designer", "scientist",
            "consultant", "director", "student", "researcher", "admin");
    private static final int PROJECT_COUNT = 50;
    private static final int USER_COUNT = 1000;
    private static final int CHAT_COUNT = 5_000;
    private static final int EXECUTION_PATH_COUNT = 100;
    private static final char[] HEX = "0123456789abcdef".toCharArray();

    /**
     * Deterministically-generated {@code analytics} records in line-protocol format with every
     * schema tag and field populated. Built once per JVM and shared across subclasses.
     */
    protected static final List<String> PERF_RECORDS = generateRecords();

    // --- Query JSON (verbatim from task, with only time literals factored for readability) ---

    private static final String Q_WINDOW_COUNT = """
            {"$type":"json","fillGaps":true,"query":{"expressions":["window(_time, 15, 'm') as time","count() as requests"],"from":"analytics","groupBy":["window(_time, 15, 'm')"],"orderBy":[{"$asc":"time"}],"where":{"$and":[{"$gte":{"left":"_time","right":"'2026-04-13T12:40:00.401Z'"}},{"$lt":{"left":"_time","right":"'2026-04-15T12:40:00.401Z'"}}]}}}""";

    private static final String Q_DISTINCT_USER_COUNT = """
            {"$type":"json","query":{"expressions":["count()"],"from":{"distinct":"true","expressions":["user_hash"],"from":"analytics","where":{"$and":[{"$gte":{"left":"_time","right":"'2026-04-13T12:40:00.403Z'"}},{"$lt":{"left":"_time","right":"'2026-04-15T12:40:00.403Z'"}}]}}}}""";

    private static final String Q_TOTAL_COUNT = """
            {"$type":"json","query":{"expressions":["count()"],"from":"analytics","where":{"$and":[{"$gte":{"left":"_time","right":"'2026-04-13T12:40:00.404Z'"}},{"$lt":{"left":"_time","right":"'2026-04-15T12:40:00.404Z'"}}]}}}""";

    private static final String Q_SUM_TOKENS = """
            {"$type":"json","query":{"expressions":["sum(prompt_tokens)","sum(completion_tokens)"],"from":"analytics","where":{"$and":[{"$gte":{"left":"_time","right":"'2026-04-13T12:40:00.404Z'"}},{"$lt":{"left":"_time","right":"'2026-04-15T12:40:00.404Z'"}}]}}}""";

    private static final String Q_SUM_DEPLOYMENT_PRICE = """
            {"$type":"json","query":{"expressions":["sum(deployment_price)"],"from":"analytics","where":{"$and":[{"$gte":{"left":"_time","right":"'2026-04-13T12:40:00.404Z'"}},{"$lt":{"left":"_time","right":"'2026-04-15T12:40:00.404Z'"}}]}}}""";

    private static final String Q_GROUP_BY_DEPLOYMENT = """
            {"$type":"json","query":{"expressions":["deployment","count()","sum(deployment_price) as money","sum(price) as aggregated_money","sum(prompt_tokens) as tokens_p","sum(completion_tokens) as tokens_c"],"from":"analytics","groupBy":["deployment"],"where":{"$and":[{"$gte":{"left":"_time","right":"'2026-04-13T12:40:00.405Z'"}},{"$lt":{"left":"_time","right":"'2026-04-15T12:40:00.405Z'"}}]}}}""";

    private static final String Q_GROUP_BY_PROJECT = """
            {"$type":"json","query":{"expressions":["project_id","count()","sum(deployment_price) as money","sum(prompt_tokens) as tokens_p","sum(completion_tokens) as tokens_c"],"from":"analytics","groupBy":["project_id"],"where":{"$and":[{"$gte":{"left":"_time","right":"'2026-04-13T12:40:00.405Z'"}},{"$lt":{"left":"_time","right":"'2026-04-15T12:40:00.405Z'"}}]}}}""";

    protected abstract Engine getEngine();

    protected abstract String getEngineLabel();

    @Test
    void windowCount15mGapfilled() throws Exception {
        measure("window_count_15m_gapfilled", Q_WINDOW_COUNT);
    }

    @Test
    void distinctUserCount() throws Exception {
        measure("distinct_user_count", Q_DISTINCT_USER_COUNT);
    }

    @Test
    void totalCount() throws Exception {
        measure("total_count", Q_TOTAL_COUNT);
    }

    @Test
    void sumPromptCompletionTokens() throws Exception {
        measure("sum_prompt_completion_tokens", Q_SUM_TOKENS);
    }

    @Test
    void sumDeploymentPrice() throws Exception {
        measure("sum_deployment_price", Q_SUM_DEPLOYMENT_PRICE);
    }

    @Test
    void groupByDeploymentFullRollup() throws Exception {
        measure("group_by_deployment_full_rollup", Q_GROUP_BY_DEPLOYMENT);
    }

    @Test
    void groupByProjectFullRollup() throws Exception {
        measure("group_by_project_full_rollup", Q_GROUP_BY_PROJECT);
    }

    /**
     * Parses {@code json} through the standard query-language pipeline, warms up the engine
     * once, then executes the query {@link #MEASURED_RUNS} times recording wall-clock nanos.
     * Logs min/p50/p95/max/mean and asserts the last result is non-empty.
     */
    private void measure(String queryLabel, String json) throws Exception {
        var engine = getEngine();
        var languageConverter = new LanguageConverter(engine);
        var dataQuery = QUERY_MAPPER.readValue(json, DataQuery.class);
        if (!(dataQuery instanceof JsonDataQuery jsonQuery)) {
            throw new IllegalArgumentException("Only $type=\"json\" queries are supported: " + queryLabel);
        }
        var fillGaps = jsonQuery.isFillGaps();
        var completable = languageConverter.convert(jsonQuery.getQuery(), engine.getTables());

        // Warmup — excluded from statistics to let the connection pool / caches settle.
        Data last = null;
        for (int i = 0; i < WARMUP_RUNS; i++) {
            last = engine.getData(completable, fillGaps);
        }

        long[] elapsed = new long[MEASURED_RUNS];
        for (int i = 0; i < MEASURED_RUNS; i++) {
            long start = System.nanoTime();
            last = engine.getData(completable, fillGaps);
            elapsed[i] = System.nanoTime() - start;
        }

        logStats(queryLabel, elapsed);

        assertThat(last).as("query result").isNotNull();
        assertThat(last.getData()).as("query rows").isNotNull();
    }

    private void logStats(String queryLabel, long[] elapsedNanos) {
        long[] sorted = elapsedNanos.clone();
        Arrays.sort(sorted);
        long min = sorted[0];
        long max = sorted[sorted.length - 1];
        long p50 = sorted[sorted.length / 2];
        long p95 = sorted[(int) Math.min(sorted.length - 1L, Math.ceil(0.95 * sorted.length) - 1)];
        long total = 0;
        for (long v : elapsedNanos) {
            total += v;
        }
        long mean = total / elapsedNanos.length;

        LOG.info("[perf][{}] {} runs={} min={}ms p50={}ms p95={}ms max={}ms mean={}ms",
                getEngineLabel(), queryLabel, elapsedNanos.length,
                toMillis(min), toMillis(p50), toMillis(p95), toMillis(max), toMillis(mean));
    }

    private static String toMillis(long nanos) {
        return String.format("%.2f", nanos / 1_000_000.0);
    }

    /**
     * Generates {@link #RECORD_COUNT} {@code analytics} line-protocol records, deterministically
     * seeded. Every tag and field declared in the {@code analytics} schema is populated so each
     * record has the full column footprint of a real {@code /chat/completions} or {@code /embeddings}
     * log entry. Timestamps are monotonically spaced across the 48-hour range; tag/field values are
     * drawn uniformly from the configured cardinalities.
     */
    private static List<String> generateRecords() {
        Random random = new Random(SEED);
        long startNanos = RANGE_START.getEpochSecond() * 1_000_000_000L + RANGE_START.getNano();
        long endNanos = RANGE_END.getEpochSecond() * 1_000_000_000L + RANGE_END.getNano();
        long stepNanos = (endNanos - startNanos) / RECORD_COUNT;

        List<String> records = new ArrayList<>(RECORD_COUNT);
        StringBuilder sb = new StringBuilder(512);

        for (int i = 0; i < RECORD_COUNT; i++) {
            // Tags
            String deployment = DEPLOYMENTS.get(random.nextInt(DEPLOYMENTS.size()));
            String model = DEPLOYMENTS.get(random.nextInt(DEPLOYMENTS.size()));
            String parentDeployment = PARENT_DEPLOYMENTS.get(random.nextInt(PARENT_DEPLOYMENTS.size()));
            String executionPath = "path-" + random.nextInt(EXECUTION_PATH_COUNT);
            String traceId = randomHex(random, 32);
            String coreSpanId = randomHex(random, 16);
            String coreParentSpanId = randomHex(random, 16);
            String projectId = "proj-" + random.nextInt(PROJECT_COUNT);
            String language = LANGUAGES.get(random.nextInt(LANGUAGES.size()));
            String upstream = UPSTREAMS.get(random.nextInt(UPSTREAMS.size()));
            String topic = TOPICS.get(random.nextInt(TOPICS.size()));
            String title = TITLES.get(random.nextInt(TITLES.size()));
            String responseId = randomHex(random, 32);

            // Fields
            String userHash = "user-" + random.nextInt(USER_COUNT);
            int promptTokens = 10 + random.nextInt(991);                        // 10..1000
            int cachedPromptTokens = random.nextInt(promptTokens + 1);          // 0..prompt_tokens
            int completionTokens = 10 + random.nextInt(491);                    // 10..500
            int numberRequestMessages = 1 + random.nextInt(20);                 // 1..20
            String chatId = "chat-" + random.nextInt(CHAT_COUNT);
            double deploymentPrice = 0.001 + random.nextDouble() * 0.099;
            double price = deploymentPrice + random.nextDouble() * 0.05;        // price >= deployment_price
            long timestamp = startNanos + i * stepNanos;

            sb.setLength(0);
            // Measurement + tags
            sb.append("analytics,deployment=").append(deployment)
                    .append(",model=").append(model)
                    .append(",parent_deployment=").append(parentDeployment)
                    .append(",execution_path=").append(executionPath)
                    .append(",trace_id=").append(traceId)
                    .append(",core_span_id=").append(coreSpanId)
                    .append(",core_parent_span_id=").append(coreParentSpanId)
                    .append(",project_id=").append(projectId)
                    .append(",language=").append(language)
                    .append(",upstream=").append(escapeTagValue(upstream))
                    .append(",topic=").append(topic)
                    .append(",title=").append(title)
                    .append(",response_id=").append(responseId)
                    // Fields
                    .append(" user_hash=\"").append(userHash).append('"')
                    .append(",deployment_price=").append(deploymentPrice)
                    .append(",price=").append(price)
                    .append(",number_request_messages=").append(numberRequestMessages).append('i')
                    .append(",chat_id=\"").append(chatId).append('"')
                    .append(",prompt_tokens=").append(promptTokens).append('i')
                    .append(",cached_prompt_tokens=").append(cachedPromptTokens).append('i')
                    .append(",completion_tokens=").append(completionTokens).append('i')
                    .append(' ').append(timestamp);
            records.add(sb.toString());
        }

        return Collections.unmodifiableList(records);
    }

    private static String randomHex(Random random, int length) {
        char[] buf = new char[length];
        for (int i = 0; i < length; i++) {
            buf[i] = HEX[random.nextInt(16)];
        }
        return new String(buf);
    }

    /** Line-protocol tag values must escape commas, spaces, and equals signs. */
    private static String escapeTagValue(String value) {
        StringBuilder out = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == ',' || c == ' ' || c == '=') {
                out.append('\\');
            }
            out.append(c);
        }
        return out.toString();
    }
}
