package com.epam.aidial.metric.service.influx3;

import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.epam.aidial.cfg.utils.ResourceUtils;
import com.epam.aidial.metric.config.Influx3DatasetConfiguration;
import com.epam.aidial.metric.model.configuration.DatasetDeclaration;
import com.epam.aidial.metric.model.configuration.influx3.Influx3DatasetDeclaration;
import com.epam.aidial.metric.service.AbstractInfluxContainerTest;
import com.epam.aidial.ql.Engine;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.influxdb.v3.client.InfluxDBClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
class Influx3ContainerTest extends AbstractInfluxContainerTest {

    private static final ObjectMapper OBJECT_MAPPER = JsonMapperConfiguration.createJsonMapper();
    private static final String DATABASE = "analytics_db";
    private static final int INFLUXDB3_PORT = 8181;

    @Container
    private static final GenericContainer<?> INFLUX3 =
            new GenericContainer<>(DockerImageName.parse("influxdb:3.8.3-core"))
                    .withExposedPorts(INFLUXDB3_PORT)
                    .withCommand("serve", "--node-id=1", "--object-store=memory", "--without-auth")
                    .waitingFor(Wait.forListeningPort());

    private static InfluxDBClient influx3Client;
    private Influx3Engine engine;

    @BeforeAll
    static void seedData() {
        var url = "http://" + INFLUX3.getHost() + ":" + INFLUX3.getMappedPort(INFLUXDB3_PORT);
        influx3Client = InfluxDBClient.getInstance(url, null, DATABASE);

        for (var record : TEST_RECORDS) {
            influx3Client.writeRecord(record);
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
        datasetConfiguration.setDefaultPageSize(50);

        var queryBuilderFactory = new SqlQueryBuilderFactory(datasetDeclaration, datasetConfiguration);
        engine = new Influx3Engine(datasetDeclaration, influx3Client, queryBuilderFactory);
    }

    @Override
    protected Engine getEngine() {
        return engine;
    }
}
