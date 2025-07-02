package com.epam.aidial.cfg.configuration;

import com.epam.aidial.cfg.exception.InvalidVersionException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.regex.Pattern;
import javax.annotation.PostConstruct;

@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "config.version")
public class CoreConfigVersionProperties {

    private static final Pattern VERSION_PATTERN = Pattern.compile("^\\d+\\.\\d+\\.\\d+$");
    private static final String LATEST_VERSION = "latest";

    private String target;

    @Value("${config.version.autoDetect.enabled}")
    private boolean enable;

    @Value("${config.version.autoDetect.maxAttempts}")
    private int maxRetries;

    @Value("${config.version.autoDetect.period}")
    private long retryDelayMs;

    @PostConstruct
    public void validateConfiguration() {
        log.info("Initializing core config version properties. Target: {}", target);

        if (!LATEST_VERSION.equals(target) && !VERSION_PATTERN.matcher(target).matches()) {
            throw new InvalidVersionException("Invalid CORE_CONFIG_VERSION format: " + target
                + ". Expected format: X.Y.Z (e.g., '0.23.0', '1.0.1', '2.0.3') or 'latest'");
        }

        log.info("Core config version validation successful for version: {}", target);
    }
}