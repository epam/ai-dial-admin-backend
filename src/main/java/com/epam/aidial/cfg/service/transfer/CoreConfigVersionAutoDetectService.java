package com.epam.aidial.cfg.service.transfer;

import com.epam.aidial.cfg.client.AnonymousCoreConfigClient;
import com.epam.aidial.cfg.configuration.CoreConfigVersionProperties;
import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@Service
@Slf4j
@LogExecution
public class CoreConfigVersionAutoDetectService {

    private static final String CURRENT_VERSION_CACHE_KEY = "current-version";

    private final AnonymousCoreConfigClient coreConfigClient;
    private final CoreConfigVersionProperties coreConfigVersionProperties;
    private final Cache<String, String> versionCache;
    private final AtomicReference<String> nonExpiringCachedVersion;

    public CoreConfigVersionAutoDetectService(AnonymousCoreConfigClient coreConfigClient, CoreConfigVersionProperties coreConfigVersionProperties) {
        this.coreConfigClient = coreConfigClient;
        this.coreConfigVersionProperties = coreConfigVersionProperties;
        this.versionCache = CacheBuilder.newBuilder()
                .expireAfterWrite(coreConfigVersionProperties.getCacheExpirationMs(), TimeUnit.MILLISECONDS)
                .build();
        this.nonExpiringCachedVersion = new AtomicReference<>();
    }

    /**
     * Gets the Core version with retry mechanism.
     * If all retries fail, falls back to the target version from properties.
     * If auto-detection is disabled, returns the target version directly.
     *
     * @return the Core version
     */
    public String getVersion() {
        if (!coreConfigVersionProperties.isAutoDetectEnabled()) {
            String target = coreConfigVersionProperties.getTarget();
            log.debug("Core version auto-detection is disabled. Using target version: {}", target);
            return target;
        }

        String version = versionCache.getIfPresent(CURRENT_VERSION_CACHE_KEY);
        if (version != null) {
            log.debug("Using cached Core version: {}", version);
            return version;
        }

        try {
            version = getVersionFromCore();
            versionCache.put(CURRENT_VERSION_CACHE_KEY, version);
            nonExpiringCachedVersion.set(version);
            return version;
        } catch (Exception e) {
            log.warn("Failed to get Core version. Reason: ", e);
            return fallbackToTargetVersion();
        }
    }

    private String getVersionFromCore() {
        log.debug("Attempting to get version from Core");
        String version = coreConfigClient.getVersion();
        log.info("Successfully retrieved Core version: {}", version);
        return version;
    }

    private String fallbackToTargetVersion() {
        String version = nonExpiringCachedVersion.get();
        if (version != null) {
            log.debug("Using the last successfully retrieved Core version: {}", version);
            return version;
        }

        String targetVersion = coreConfigVersionProperties.getTarget();
        versionCache.put(CURRENT_VERSION_CACHE_KEY, targetVersion);
        log.warn("All attempts to get Core version failed, falling back to target version: {}", targetVersion);

        return targetVersion;
    }
}