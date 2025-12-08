package com.epam.aidial.cfg.service.config.transfer.version;

import com.epam.aidial.cfg.client.AnonymousCoreConfigClient;
import com.epam.aidial.cfg.configuration.CoreConfigVersionProperties;
import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.utils.CoreConfigVersionNormalizer;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@LogExecution
public class CoreConfigVersionAutoDetectService {

    public static final String AUTO_DETECT_FAILED_CORE_VERSION = "-1";

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
     *
     * @return the Core version or {@code null} in case auto-detection is disabled or {@code -1} in case core does not respond successfully
     */
    public String getVersion() {
        if (!coreConfigVersionProperties.isAutoDetectEnabled()) {
            return null;
        }

        String autoDetectedVersionFromCache = versionCache.getIfPresent(CURRENT_VERSION_CACHE_KEY);
        if (autoDetectedVersionFromCache != null) {
            log.debug("Using cached Core version: {}", autoDetectedVersionFromCache);
            return autoDetectedVersionFromCache;
        }

        String autoDetectedVersionFromCore = getVersionFromCore();
        if (!autoDetectedVersionFromCore.equals(AUTO_DETECT_FAILED_CORE_VERSION)) {
            versionCache.put(CURRENT_VERSION_CACHE_KEY, autoDetectedVersionFromCore);
        }

        return autoDetectedVersionFromCore;
    }

    private String getVersionFromCore() {
        try {
            log.debug("Attempting to get version from Core");

            String coreVersion = coreConfigClient.getVersion();
            String normalizedCoreVersion = CoreConfigVersionNormalizer.normalizeCoreVersion(coreVersion);

            log.info("Successfully get version from Core: {}, normalized version: {}", coreVersion, normalizedCoreVersion);

            return normalizedCoreVersion;
        } catch (Exception e) {
            log.warn("Unable to get version from Core", e);
            return AUTO_DETECT_FAILED_CORE_VERSION;
        }
    }
}