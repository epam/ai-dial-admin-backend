package com.epam.aidial.cfg.service.impl.storage;


import com.epam.aidial.core.config.Config;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;

@RequiredArgsConstructor
@Slf4j
public class ConfigSourceFile implements ConfigSource {

    private final ObjectMapper objectMapper;
    private final String outputFilePath;

    @Override
    public Config readConfig() { // TODO do we need this???
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
    public void writeConfig(Config configBody) {
        try {
            createDirectotyIfNeed();
            objectMapper.writeValue(new File(outputFilePath), configBody);
        } catch (IOException e) {
            log.error("Can't serialize configuration", e);
        }
    }

    private void createDirectotyIfNeed() {
        String[] split = outputFilePath.split("(?=[^/]+$)");
        String directory = split[0];
        File file = new File(directory);
        file.mkdirs();
    }

}
