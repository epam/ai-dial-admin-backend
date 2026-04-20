package com.epam.aidial.metric.service.influx;

import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.epam.aidial.cfg.utils.ResourceUtils;
import com.epam.aidial.metric.config.InfluxDatasetConfiguration;
import com.epam.aidial.metric.model.configuration.DatasetDeclaration;
import com.epam.aidial.metric.model.configuration.influx.InfluxDatasetDeclaration;
import com.epam.aidial.metric.service.AbstractInfluxPerformanceTest;
import com.epam.aidial.metric.service.WindowGapFiller;
import com.epam.aidial.ql.Engine;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.InfluxDBClientOptions;
import com.influxdb.client.domain.WritePrecision;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.containers.InfluxDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

@Testcontainers
class InfluxPerformanceTest extends AbstractInfluxPerformanceTest {

    private static final ObjectMapper OBJECT_MAPPER = JsonMapperConfiguration.createJsonMapper();

    private static final String BUCKET = "analytics-realtime";
    private static final String ORG = "test-org";
    private static final String TOKEN = "test-admin-token";
    private static final int WRITE_CHUNK = 5_000;

    @Container
    private static final InfluxDBContainer<?> INFLUXDB =
            new InfluxDBContainer<>(DockerImageName.parse("influxdb:2.7"))
                    .withAdminToken(TOKEN)
                    .withOrganization(ORG)
                    .withBucket(BUCKET);

    private static InfluxDBClient influxClient;
    private InfluxEngine engine;

    @BeforeAll
    static void seedData() {
        // Default OkHttp read timeout is 10s — Flux queries over 100k rows can exceed it.
        // Bump read/write/connect timeouts so perf queries aren't killed mid-execution.
        var httpClient = new OkHttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(30))
                .readTimeout(Duration.ofMinutes(5))
                .writeTimeout(Duration.ofMinutes(5));

        influxClient = InfluxDBClientFactory.create(
                InfluxDBClientOptions.builder()
                        .url(INFLUXDB.getUrl())
                        .org(ORG)
                        .authenticateToken(TOKEN.toCharArray())
                        .okHttpClient(httpClient)
                        .build());

        var writeApi = influxClient.getWriteApiBlocking();
        for (int i = 0; i < PERF_RECORDS.size(); i += WRITE_CHUNK) {
            int end = Math.min(i + WRITE_CHUNK, PERF_RECORDS.size());
            writeApi.writeRecords(BUCKET, ORG, WritePrecision.NS, PERF_RECORDS.subList(i, end));
        }
    }

    @AfterAll
    static void cleanup() {
        if (influxClient != null) {
            influxClient.close();
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        var testMetricConfig = ResourceUtils.readResource("/metrics/metric.config.influx2.json");
        var datasetDeclaration = (InfluxDatasetDeclaration) OBJECT_MAPPER.readValue(
                testMetricConfig, DatasetDeclaration.class);

        var datasetConfiguration = new InfluxDatasetConfiguration();
        // Page size large enough that the 48h / 15m window query (~192 rows) returns in a single page.
        datasetConfiguration.setDefaultPageSize(1_000);

        var queryBuilderFactory = new FluxQueryBuilderFactory(datasetDeclaration, datasetConfiguration);
        var windowGapFiller = new WindowGapFiller(10_000);
        engine = new InfluxEngine(datasetDeclaration, influxClient, queryBuilderFactory, windowGapFiller);
    }

    @Override
    protected Engine getEngine() {
        return engine;
    }

    @Override
    protected String getEngineLabel() {
        return "influx2";
    }
}
