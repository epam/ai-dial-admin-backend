package com.epam.aidial.cfg.service.transfer;

import com.epam.aidial.cfg.client.AnonymousCoreConfigClient;
import com.epam.aidial.cfg.configuration.CoreConfigVersionProperties;
import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.utils.CoreConfigVersionNormalizer;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@LogExecution
public class CoreConfigVersionAutoDetectService {

    private static final String CURRENT_VERSION_CACHE_KEY = "current-version";

    private final AnonymousCoreConfigClient coreConfigClient;
    private final CoreConfigVersionProperties coreConfigVersionProperties;
    private final Cache<String, String> versionCache;

    public CoreConfigVersionAutoDetectService(AnonymousCoreConfigClient coreConfigClient, CoreConfigVersionProperties coreConfigVersionProperties) {
        this.coreConfigClient = coreConfigClient;
        this.coreConfigVersionProperties = coreConfigVersionProperties;
        this.versionCache = CacheBuilder.newBuilder()
                .expireAfterWrite(coreConfigVersionProperties.getCacheExpirationMs(), TimeUnit.MILLISECONDS)
                .build();
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
            if (StringUtils.isBlank(target)) {
                throw new IllegalStateException("Core target version is undefined");
            }
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
            return version;
        } catch (Exception e) {
            log.error("All attempts to get Core version failed. Reason: ", e);
            throw e;
        }
    }

    private String getVersionFromCore() {
        log.debug("Attempting to get version from Core");
        String version;
        try {
            version = CoreConfigVersionNormalizer.normalizeCoreVersion(coreConfigClient.getVersion());
            log.info("Successfully retrieved Core version: {}", version);
        } catch (Exception e) {
            log.info("Unable to retrieve Core version. Will try to use target version", e);
            String target = coreConfigVersionProperties.getTarget();

            if (StringUtils.isBlank(target)) {
                throw new IllegalStateException("Core target version is undefined");
            }

            version = target;
        }

        return version;
    }
}