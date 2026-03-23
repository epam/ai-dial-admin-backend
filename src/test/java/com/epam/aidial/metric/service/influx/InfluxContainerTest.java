package com.epam.aidial.metric.service.influx;

import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.epam.aidial.cfg.utils.ResourceUtils;
import com.epam.aidial.metric.config.InfluxDatasetConfiguration;
import com.epam.aidial.metric.model.configuration.DatasetDeclaration;
import com.epam.aidial.metric.model.configuration.influx.InfluxDatasetDeclaration;
import com.epam.aidial.metric.service.AbstractInfluxContainerTest;
import com.epam.aidial.ql.Engine;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.InfluxDBClientOptions;
import com.influxdb.client.domain.WritePrecision;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.containers.InfluxDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
class InfluxContainerTest extends AbstractInfluxContainerTest {

    private static final ObjectMapper OBJECT_MAPPER = JsonMapperConfiguration.createJsonMapper();
    private static final String BUCKET = "analytics-realtime";
    private static final String ORG = "test-org";
    private static final String TOKEN = "test-admin-token";

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
        influxClient = InfluxDBClientFactory.create(
                InfluxDBClientOptions.builder()
                        .url(INFLUXDB.getUrl())
                        .org(ORG)
                        .authenticateToken(TOKEN.toCharArray())
                        .build());

        influxClient.getWriteApiBlocking()
                .writeRecords(BUCKET, ORG, WritePrecision.NS, TEST_RECORDS);
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
        datasetConfiguration.setDefaultPageSize(50);

        var queryBuilderFactory = new FluxQueryBuilderFactory(datasetDeclaration, datasetConfiguration);
        engine = new InfluxEngine(datasetDeclaration, influxClient, queryBuilderFactory);
    }

    @Override
    protected Engine getEngine() {
        return engine;
    }
}
