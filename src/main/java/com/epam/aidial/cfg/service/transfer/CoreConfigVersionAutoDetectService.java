package com.epam.aidial.cfg.service.transfer;

import com.epam.aidial.cfg.client.CoreConfigClient;
import com.epam.aidial.cfg.configuration.CoreConfigVersionProperties;
import com.epam.aidial.cfg.configuration.logging.LogExecution;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@Service
@Slf4j
@LogExecution
@RequiredArgsConstructor
public class CoreConfigVersionAutoDetectService {

    private final CoreConfigClient coreConfigClient;
    private final CoreConfigVersionProperties coreConfigVersionProperties;
    private final AtomicReference<String> cachedVersion = new AtomicReference<>();

    /**
     * Gets the Core version with retry mechanism.
     * If all retries fail, falls back to the target version from properties.
     * If auto-detection is disabled, returns the target version directly.
     *
     * @return the Core version
     */
    public String getVersion() {
        if (!coreConfigVersionProperties.isAutoDetectEnabled()) {
            log.debug("Core version auto-detection is disabled. Using target version: {}",
                    coreConfigVersionProperties.getTarget());
            return coreConfigVersionProperties.getTarget();
        }

        String version = cachedVersion.get();
        if (version != null) {
            log.debug("Using cached Core version: {}", version);
            return version;
        }

        try {
            version = attemptToGetVersionWithRetries();
            cachedVersion.set(version);
            return version;
        } catch (Exception e) {
            return fallbackToTargetVersion(e);
        }
    }

    private String attemptToGetVersionWithRetries() throws Exception {
        Exception lastException = null;
        int maxRetries = coreConfigVersionProperties.getMaxRetries();

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                return tryToGetVersion(attempt);
            } catch (Exception e) {
                lastException = e;
                log.warn("Failed to get Core version on attempt {}/{}: {}",
                        attempt, maxRetries, e.getMessage());

                if (attempt < maxRetries) {
                    waitBeforeNextRetry();
                }
            }
        }

        throw Objects.requireNonNull(lastException);
    }

    private String tryToGetVersion(int attempt) {
        log.info("Attempting to get Core version, attempt {}/{}", attempt, coreConfigVersionProperties.getMaxRetries());
        String version = coreConfigClient.getVersion();
        log.info("Successfully retrieved Core version: {}", version);
        return version;
    }

    private void waitBeforeNextRetry() {
        try {
            long retryDelayMilliseconds = coreConfigVersionProperties.getRetryDelayMs();
            log.info("Waiting {} ms before next retry", retryDelayMilliseconds);
            TimeUnit.MILLISECONDS.sleep(retryDelayMilliseconds);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            log.warn("Retry delay was interrupted", ie);
        }
    }

    private String fallbackToTargetVersion(Exception lastException) {
        String targetVersion = coreConfigVersionProperties.getTarget();
        cachedVersion.set(targetVersion);
        log.warn("All attempts to get Core version failed, falling back to target version: {}", targetVersion);
        log.debug("Last exception: ", lastException);
        return targetVersion;
    }
}