package com.epam.aidial.metric.config;

import com.epam.aidial.cfg.features.IsMetricsEnabledCondition;
import com.epam.aidial.cfg.utils.ResourceUtils;
import com.epam.aidial.metric.component.EngineFactoryManager;
import com.epam.aidial.metric.model.configuration.DatasetsConfiguration;
import com.epam.aidial.metric.util.PlaceholderResolver;
import com.epam.aidial.ql.Engine;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.common.util.StringUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.NoSuchFileException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Slf4j
@Configuration
@Conditional(IsMetricsEnabledCondition.class)
public class MetricDatasetConfiguration {

    private static final String DEFAULT_CONFIG_FILE_NAME = "/metric.config.json";

    @Bean
    public List<Engine> engines(
            DatasetsConfiguration datasetsConfiguration,
            EngineFactoryManager engineFactoryManager
    ) {
        return datasetsConfiguration.getDatasets().stream().map(engineFactoryManager::build).toList();
    }

    @Bean("influxOkHttpClientBuilder")
    public OkHttpClient.Builder influxOkHttpClientBuilder() {
        return new OkHttpClient().newBuilder()
                .connectTimeout(10, TimeUnit.SECONDS) // TODO: make configurable
                .readTimeout(60, TimeUnit.SECONDS) // TODO: make configurable
                .writeTimeout(60, TimeUnit.SECONDS); // TODO: make configurable
    }

    @Bean
    @Scope(SCOPE_PROTOTYPE)
    public PlaceholderResolver placeholderResolver() {
        return new PlaceholderResolver(System::getenv);
    }

    @Bean
    @Scope(SCOPE_PROTOTYPE)
    @SneakyThrows
    public DatasetsConfiguration getDatasetConfiguration(
            @Value("${metrics.configFile.contentEnvVar}") String envVar,
            @Value("${metrics.configFile.location}") String fileName,
            PlaceholderResolver placeholderResolver,
            ObjectMapper objectMapper
    ) {
        var rawConfiguration = getConfigurationFromEnvVar(envVar)
                .or(() -> getConfigurationFromFile(fileName))
                .orElseGet(this::getDefaultConfiguration);

        var resolvedConfiguration = placeholderResolver.resolvePlaceholders(rawConfiguration);

        return objectMapper.readValue(resolvedConfiguration, DatasetsConfiguration.class);
    }

    @SneakyThrows
    private Optional<String> getConfigurationFromEnvVar(String envVar) {
        var content = System.getenv(envVar);
        if (content == null) {
            return Optional.empty();
        }

        log.info("Dataset configuration is provided via environment variable: {}", envVar);
        return Optional.of(content);
    }

    @SneakyThrows
    private Optional<String> getConfigurationFromFile(String fileName) {
        if (StringUtils.isEmpty(fileName)) {
            return Optional.empty();
        }
        var file = new File(fileName);

        try {
            var content = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            log.info("Dataset configuration is provided via file: {}", fileName);
            return Optional.of(content);
        } catch (NoSuchFileException e) {
            log.info("Dataset configuration file not found. File name: {}", fileName);
            return Optional.empty();
        }
    }

    @SneakyThrows
    private String getDefaultConfiguration() {
        var content = ResourceUtils.readResource(DEFAULT_CONFIG_FILE_NAME);
        log.info("Dataset configuration is provided via default configuration: {}", DEFAULT_CONFIG_FILE_NAME);
        return content;
    }

}
