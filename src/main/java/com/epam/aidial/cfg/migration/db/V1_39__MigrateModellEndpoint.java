package com.epam.aidial.cfg.migration.db;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
//CHECKSTYLE:OFF
public class V1_39__MigrateModellEndpoint extends BaseJavaMigration {

    //CHECKSTYLE:ON
    @Override
    public void migrate(Context context) throws Exception {

        Connection connection = context.getConnection();

        String adaptersJson = System.getenv("CONFIG_ENV_ADAPTERS_JSON");
        List<AdapterDto> adapters = readAdaptersFromEnvVar(adaptersJson);

        insertAdapters(connection, adapters);

        Map<String, AdapterDto> adaptersByBaseEndpoint = adapters.stream()
                .collect(Collectors.toMap(AdapterDto::getBaseEndpoint, Function.identity()));

        try (Statement statement = connection.createStatement(); ResultSet result = statement.executeQuery("select deployment_name, endpoint, type from model_entity")) {

            try (var preparedStatement = connection.prepareStatement("update model_entity set adapter_name=? where deployment_name=?")) {
                while (result.next()) {
                    String deploymentName = result.getString("deployment_name");
                    String endpoint = result.getString("endpoint");
                    int rawType = result.getInt("type");
                    ModelTypeEntity type = mapType(rawType);
                    log.debug("found model {}, endpoint {}", deploymentName, endpoint);

                    if (StringUtils.isEmpty(endpoint)) {
                        continue;
                    }

                    String adapterEndpoint = extractAdapterEndpoint(endpoint, deploymentName, type);
                    log.info("model endpoint {}, extracted adapter endpoint {}", endpoint, adapterEndpoint);

                    if (StringUtils.isEmpty(adapterEndpoint)) {
                        continue;
                    }

                    AdapterDto adapterDto = adaptersByBaseEndpoint.computeIfAbsent(adapterEndpoint, baseEndpoint -> createAdapter(baseEndpoint, connection));

                    preparedStatement.setString(1, adapterDto.getName());
                    preparedStatement.setString(2, deploymentName);
                    preparedStatement.addBatch();
                }
                preparedStatement.executeBatch();
            }
        }
    }

    private ModelTypeEntity mapType(int rawType) {
        for (ModelTypeEntity type : ModelTypeEntity.values()) {
            if (type.ordinal() == rawType) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unable to map " + rawType + " to ModelTypeEntity");
    }

    @SneakyThrows
    private AdapterDto createAdapter(String endpoint, Connection connection) {
        AdapterDto adapterDto = new AdapterDto();
        adapterDto.setName(UUID.randomUUID().toString());
        adapterDto.setBaseEndpoint(endpoint);
        insertAdapters(connection, List.of(adapterDto));
        return adapterDto;
    }

    private static void insertAdapters(Connection connection, List<AdapterDto> adapters) throws SQLException {
        try (var preparedStatement = connection.prepareStatement("insert into adapter_entity (base_endpoint,name) values (?,?)")) {
            for (AdapterDto adapter : adapters) {
                log.debug("add adapter: {}", adapter);
                preparedStatement.setString(1, adapter.getBaseEndpoint());
                preparedStatement.setString(2, adapter.getName());
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
    }

    private static List<AdapterDto> readAdaptersFromEnvVar(String adaptersJson) throws JsonProcessingException {
        if (StringUtils.isNotBlank(adaptersJson)) {
            return new ObjectMapper().readValue(adaptersJson, new TypeReference<List<AdapterDto>>() {
            });
        }
        return List.of();
    }

    enum ModelTypeEntity {
        CHAT, COMPLETION, EMBEDDING
    }

    @Data
    static class AdapterDto {

        private String name;
        private String baseEndpoint;
        private String description;
    }

    public static String extractAdapterEndpoint(String modelEndpoint, String name, ModelTypeEntity type) {
        String result = StringUtils.removeEnd(modelEndpoint, "/");
        if (result.endsWith("chat/completions")) {
            result = StringUtils.removeEnd(result, "chat/completions");
        } else if (result.endsWith("embeddings")) {
            result = StringUtils.removeEnd(result, "embeddings");
        } else {
            log.warn("model endpoint {} doesn't end with chat/completions or embeddings", modelEndpoint);
            return modelEndpoint;
        }
        result = StringUtils.removeEnd(result, "/");

        String modelName = StringUtils.substringAfterLast(result, "/");

        result = StringUtils.removeEnd(result, modelName);

        return result;
    }
}
