package com.epam.aidial.metric.config;

import com.epam.aidial.cfg.features.IsMetricsEnabledCondition;
import com.epam.aidial.cfg.utils.ResourceUtils;
import com.epam.aidial.metric.component.EngineFactoryManager;
import com.epam.aidial.metric.model.configuration.DatasetDeclaration;
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
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Slf4j
@Configuration
@Conditional(IsMetricsEnabledCondition.class)
public class MetricDatasetConfiguration {

    private static final Map<String, String> DEFAULT_CONFIGS = Map.of(
            "influx2", "/metric.config.influx2.json",
            "influx3", "/metric.config.influx3.json"
    );

    @Bean
    public List<Engine> engines(
            DatasetDeclaration datasetDeclaration,
            EngineFactoryManager engineFactoryManager
    ) {
        return List.of(engineFactoryManager.build(datasetDeclaration));
    }

    @Bean("influxOkHttpClientBuilder")
    public OkHttpClient.Builder influxOkHttpClientBuilder(
            @Value("${metrics.influx.connectTimeout}") int connectTimeout,
            @Value("${metrics.influx.readTimeout}") int readTimeout,
            @Value("${metrics.influx.writeTimeout}") int writeTimeout) {
        return new OkHttpClient().newBuilder()
                .connectTimeout(connectTimeout, TimeUnit.SECONDS)
                .readTimeout(readTimeout, TimeUnit.SECONDS)
                .writeTimeout(writeTimeout, TimeUnit.SECONDS);
    }

    @Bean
    @Scope(SCOPE_PROTOTYPE)
    public PlaceholderResolver placeholderResolver() {
        return new PlaceholderResolver(System::getenv);
    }

    @Bean
    @Scope(SCOPE_PROTOTYPE)
    @SneakyThrows
    public DatasetDeclaration getDatasetDeclaration(
            @Value("${metrics.config.content}") String content,
            @Value("${metrics.config.file}") String fileName,
            @Value("${metrics.config.type}") String configType,
            PlaceholderResolver placeholderResolver,
            ObjectMapper objectMapper
    ) {
        var rawConfiguration = getConfigurationFromContent(content)
                .or(() -> getConfigurationFromFile(fileName))
                .orElseGet(() -> getDefaultConfiguration(configType));

        var resolvedConfiguration = placeholderResolver.resolvePlaceholders(rawConfiguration);

        return objectMapper.readValue(resolvedConfiguration, DatasetDeclaration.class);
    }

    private Optional<String> getConfigurationFromContent(String content) {
        if (StringUtils.isEmpty(content)) {
            return Optional.empty();
        }

        log.info("Dataset configuration is provided via content");
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
    private String getDefaultConfiguration(String configType) {
        var configPath = DEFAULT_CONFIGS.get(configType);
        if (configPath == null) {
            throw new IllegalArgumentException("Unsupported metrics config type: %s. Supported types: %s"
                    .formatted(configType, DEFAULT_CONFIGS.keySet()));
        }
        var content = ResourceUtils.readResource(configPath);
        log.info("Dataset configuration is provided via default configuration: {}", configPath);
        return content;
    }

}
