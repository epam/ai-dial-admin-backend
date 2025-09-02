package com.epam.aidial.cfg.logger;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.util.Strings;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
public class ConfigUpdaterLoggerLevel {

    private final LoggerConfigSource configSource;

    @Scheduled(fixedRateString = "${logger.configuration.interval}", timeUnit = TimeUnit.SECONDS)
    public void updateAll() {
        try {
            log.debug("Updating loggers levels...");
            LoggerLevelsDto config = configSource.readConfig();
            log.trace("config read: {} ", config);
            Map<String, LoggerLevelDto> loggerLevels = config.getLoggerLevels();
            log.trace("loggerLevels: {}", loggerLevels);
            if (loggerLevels.isEmpty()) {
                return;
            }

            config.getLoggerLevels().forEach(this::setLoggerLevel);
        } catch (Exception ex) {
            log.warn("error while updating loggers levels ", ex);
        }
    }

    private void setLoggerLevel(String loggerName, LoggerLevelDto loggerLevel) {
        if (Strings.isBlank(loggerName)) {
            throw new IllegalArgumentException("Wrong logger name: " + loggerName);
        }
        Objects.requireNonNull(loggerLevel, "No logger level given");

        log.trace("start setting logger level loggerName: {}, loggerLevel: {}", loggerName, loggerLevel);
        if (loggerLevel.getValidTill() != null && (!Strings.isBlank(loggerLevel.getConfiguredLevel()))) {
            final var till = Instant.ofEpochMilli(loggerLevel.getValidTill());
            if (till.isAfter(Instant.now())) {
                final var level = Level.valueOf(loggerLevel.getConfiguredLevel());
                Configurator.setAllLevels(loggerName, level);
                log.trace("Updated logger {} to configured level {}, valid till {}", loggerName, level, till);

                return;
            }
        }

        if (loggerLevel.getDefaultLevel() != null) {
            final var level = Level.valueOf(loggerLevel.getDefaultLevel());
            Configurator.setAllLevels(loggerName, level);
            log.trace("Updated logger {} to default level {}", loggerName, level);
        } else {
            log.trace("Skip level for logger {}", loggerName);
        }

    }
}
