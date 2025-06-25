package com.epam.aidial.cfg.service.impl.storage;

import com.epam.aidial.cfg.service.transfer.VersionAwareFieldFilter;
import com.epam.aidial.core.config.Config;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RequiredArgsConstructor
@Slf4j
public class ConfigSourceFile implements ConfigSource {

    private final VersionAwareFieldFilter versionAwareFieldFilter;
    private final ObjectMapper objectMapper;
    private final String outputFilePath;

    @Override
    public Map<String, String> readRawConfig() {
        File file = new File(outputFilePath);
        if (file.exists()) {
            try {
                log.debug("Read configuration from file: {}", file);
                var content = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
                return Map.of(outputFilePath, content);
            } catch (IOException e) {
                throw new RuntimeException("Error reading config file: " + file, e);
            }
        }

        log.debug("Configuration directory {} doesn't exist", file);
        return Map.of();
    }

    @Override
    public Config readConfig() {
        File file = new File(outputFilePath);
        if (file.exists()) {
            try {
                log.debug("Read configuration from file: {}", file);
                return objectMapper.readValue(file, Config.class);

            } catch (IOException e) {
                throw new RuntimeException("Error reading config file: " + file, e);
            }
        }

        log.debug("Configuration directory {} doesn't exist", file);
        return new Config();
    }

    @Override
    public void writeConfig(Config configBody, boolean createResources) {
        try {
            Config versionedConfig = versionAwareFieldFilter.filterForTargetVersion(configBody);
            createDirectoryIfNeed();
            objectMapper.writeValue(new File(outputFilePath), versionedConfig);
        } catch (IOException e) {
            log.error("Can't serialize configuration", e);
        }
    }

    private void createDirectoryIfNeed() {
        String[] split = outputFilePath.split("(?=[^/]+$)");
        String directory = split[0];
        File file = new File(directory);
        file.mkdirs();
    }

}
