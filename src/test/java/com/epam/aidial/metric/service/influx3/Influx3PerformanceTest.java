package com.epam.aidial.metric.service.influx3;

import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.epam.aidial.cfg.utils.ResourceUtils;
import com.epam.aidial.metric.config.Influx3DatasetConfiguration;
import com.epam.aidial.metric.model.configuration.DatasetDeclaration;
import com.epam.aidial.metric.model.configuration.influx3.Influx3DatasetDeclaration;
import com.epam.aidial.metric.service.AbstractInfluxPerformanceTest;
import com.epam.aidial.metric.service.WindowGapFiller;
import com.epam.aidial.ql.Engine;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.influxdb.v3.client.InfluxDBClient;
import com.influxdb.v3.client.config.ClientConfig;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

@Testcontainers
class Influx3PerformanceTest extends AbstractInfluxPerformanceTest {

    private static final ObjectMapper OBJECT_MAPPER = JsonMapperConfiguration.createJsonMapper();
    private static final String DATABASE = "analytics_db";
    private static final int INFLUXDB3_PORT = 8181;
    private static final int WRITE_CHUNK = 5_000;

    @Container
    private static final GenericContainer<?> INFLUX3 =
            new GenericContainer<>(DockerImageName.parse("influxdb:3.8.3-core"))
                    .withExposedPorts(INFLUXDB3_PORT)
                    .withCommand(
                            "serve",
                            "--node-id=1",
                            "--object-store=file",
                            "--data-dir=/var/lib/influxdb3/data",
                            "--without-auth")
                    .waitingFor(Wait.forListeningPort());

    private static InfluxDBClient influx3Client;
    private Influx3Engine engine;

    @BeforeAll
    static void seedData() {
        var url = "http://" + INFLUX3.getHost() + ":" + INFLUX3.getMappedPort(INFLUXDB3_PORT);
        // Explicit long timeouts so perf queries over 100k rows aren't killed by the default.
        var config = new ClientConfig.Builder()
                .host(url)
                .database(DATABASE)
                .timeout(Duration.ofMinutes(5))
                .writeTimeout(Duration.ofMinutes(5))
                .queryTimeout(Duration.ofMinutes(5))
                .build();
        influx3Client = InfluxDBClient.getInstance(config);

        for (int i = 0; i < PERF_RECORDS.size(); i += WRITE_CHUNK) {
            int end = Math.min(i + WRITE_CHUNK, PERF_RECORDS.size());
            influx3Client.writeRecords(PERF_RECORDS.subList(i, end));
        }
    }

    @AfterAll
    static void cleanup() throws Exception {
        if (influx3Client != null) {
            influx3Client.close();
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        var testMetricConfig = ResourceUtils.readResource("/metrics/metric.config.influx3.json");
        var datasetDeclaration = (Influx3DatasetDeclaration) OBJECT_MAPPER.readValue(
                testMetricConfig, DatasetDeclaration.class);

        var datasetConfiguration = new Influx3DatasetConfiguration();
        // Page size large enough that the 48h / 15m window query (~192 rows) returns in a single page.
        datasetConfiguration.setDefaultPageSize(1_000);

        var queryBuilderFactory = new SqlQueryBuilderFactory(datasetDeclaration, datasetConfiguration);
        var windowGapFiller = new WindowGapFiller(10_000);
        engine = new Influx3Engine(datasetDeclaration, influx3Client, queryBuilderFactory, windowGapFiller);
    }

    @Override
    protected Engine getEngine() {
        return engine;
    }

    @Override
    protected String getEngineLabel() {
        return "influx3";
    }
}
