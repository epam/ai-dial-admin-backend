package com.epam.aidial.cfg.logger;

import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.Map;

@RequiredArgsConstructor
@Slf4j
public class LoggerConfigSourceJsonFile implements LoggerConfigSource {

    private final ObjectMapper objectMapper = JsonMapperConfiguration.createJsonMapper();

    private final String loggersPath;

    public LoggerLevelsDto readConfig() {
        File file = new File(loggersPath);
        if (file.exists()) {
            try {
                log.debug("Read configuration from file: {}", file);

                return objectMapper.readValue(file, LoggerLevelsDto.class);

            } catch (IOException e) {
                throw new IllegalStateException("Error reading config file: " + loggersPath, e);
            }
        }

        log.debug("Configuration directory {} doesn't exist", file);
        return new LoggerLevelsDto(Map.of());
    }

}
