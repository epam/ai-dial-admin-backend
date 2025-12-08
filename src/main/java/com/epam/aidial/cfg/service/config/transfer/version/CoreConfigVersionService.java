package com.epam.aidial.cfg.service.config.transfer.version;

import com.epam.aidial.cfg.client.AnonymousCoreConfigClient;
import com.epam.aidial.cfg.configuration.CoreConfigVersionProperties;
import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.model.AdminSettings;
import com.epam.aidial.cfg.domain.service.AdminSettingsService;
import com.epam.aidial.cfg.model.CoreConfigVersions;
import com.epam.aidial.cfg.service.config.transfer.VersionedSchemaLoader;
import com.epam.aidial.cfg.utils.CoreConfigVersionNormalizer;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@LogExecution
@Slf4j
public class CoreConfigVersionService {

    private static final String CURRENT_VERSION_CACHE_KEY = "current-version";
    private static final String AUTO_DETECT_FAILED_CORE_VERSION = "-1";

    private final AnonymousCoreConfigClient coreConfigClient;
    private final AdminSettingsService adminSettingsService;
    private final CoreConfigVersionProperties coreConfigVersionProperties;
    private final VersionedSchemaLoader schemaLoader;
    private final Cache<String, String> versionCache;

    public CoreConfigVersionService(AnonymousCoreConfigClient coreConfigClient,
                                    AdminSettingsService adminSettingsService,
                                    CoreConfigVersionProperties coreConfigVersionProperties,
                                    VersionedSchemaLoader schemaLoader) {
        this.coreConfigClient = coreConfigClient;
        this.adminSettingsService = adminSettingsService;
        this.coreConfigVersionProperties = coreConfigVersionProperties;
        this.schemaLoader = schemaLoader;
        this.versionCache = CacheBuilder.newBuilder()
                .expireAfterWrite(coreConfigVersionProperties.getCacheExpirationMs(), TimeUnit.MILLISECONDS)
                .build();
    }

    public String getVersionForExport() {
        AdminSettings adminSettings = adminSettingsService.getAdminSettings();
        String manuallySetCoreConfigVersion = adminSettings.getCoreConfigVersion();
        if (StringUtils.isNotBlank(manuallySetCoreConfigVersion)) {
            log.debug("Core version for export is manually set version: {}", manuallySetCoreConfigVersion);
            return manuallySetCoreConfigVersion;
        }

        if (coreConfigVersionProperties.isAutoDetectEnabled()) {
            String autoDetectedVersionFromCache = versionCache.getIfPresent(CURRENT_VERSION_CACHE_KEY);
            if (StringUtils.isNotBlank(autoDetectedVersionFromCache)) {
                log.debug("Core version for export is auto-detected version from cache: {}", autoDetectedVersionFromCache);
                return autoDetectedVersionFromCache;
            }

            String autoDetectedVersionFromCore = getVersionFromCore();
            if (StringUtils.isNotBlank(autoDetectedVersionFromCore) && !autoDetectedVersionFromCore.equals(AUTO_DETECT_FAILED_CORE_VERSION)) {
                versionCache.put(CURRENT_VERSION_CACHE_KEY, autoDetectedVersionFromCore);
                log.debug("Core version for export is auto-detected version from core: {}", autoDetectedVersionFromCore);
                return autoDetectedVersionFromCore;
            }
        }

        String defaultVersion = coreConfigVersionProperties.getTarget();
        if (StringUtils.isNotBlank(defaultVersion)) {
            log.debug("Core version for export is default version: {}", defaultVersion);
            return defaultVersion;
        }

        throw new IllegalStateException("Core version for export is undefined");
    }

    public CoreConfigVersions getVersions() {
        CoreConfigVersions coreConfigVersions = new CoreConfigVersions();

        AdminSettings adminSettings = adminSettingsService.getAdminSettings();
        String manuallySetCoreConfigVersion = adminSettings.getCoreConfigVersion();
        if (StringUtils.isNotBlank(manuallySetCoreConfigVersion)) {
            coreConfigVersions.setManuallySetVersion(schemaLoader.getEffectiveVersion(manuallySetCoreConfigVersion));
        }

        if (coreConfigVersionProperties.isAutoDetectEnabled()) {
            String autoDetectedVersionFromCache = versionCache.getIfPresent(CURRENT_VERSION_CACHE_KEY);
            if (StringUtils.isNotBlank(autoDetectedVersionFromCache)) {
                coreConfigVersions.setAutoDetectedVersion(autoDetectedVersionFromCache);
            } else {
                String autoDetectedVersionFromCore = getVersionFromCore();
                if (StringUtils.isNotBlank(autoDetectedVersionFromCore)) {
                    if (!autoDetectedVersionFromCore.equals(AUTO_DETECT_FAILED_CORE_VERSION)) {
                        versionCache.put(CURRENT_VERSION_CACHE_KEY, autoDetectedVersionFromCore);
                        coreConfigVersions.setAutoDetectedVersion(schemaLoader.getEffectiveVersion(autoDetectedVersionFromCore));
                    } else {
                        coreConfigVersions.setAutoDetectedVersion(autoDetectedVersionFromCore);
                    }
                }
            }
        }

        String defaultVersion = coreConfigVersionProperties.getTarget();
        if (StringUtils.isNotBlank(defaultVersion)) {
            coreConfigVersions.setDefaultVersion(schemaLoader.getEffectiveVersion(defaultVersion));
        }

        return coreConfigVersions;
    }

    private String getVersionFromCore() {
        try {
            log.debug("Attempting to get version from Core");

            String coreVersion = coreConfigClient.getVersion();
            String normalizedCoreVersion = CoreConfigVersionNormalizer.normalizeCoreVersion(coreVersion);

            log.info("Successfully get version from Core: {}, normalized version: {}", coreVersion, normalizedCoreVersion);

            return normalizedCoreVersion;
        } catch (Exception e) {
            log.info("Unable to get version from Core", e);
            return AUTO_DETECT_FAILED_CORE_VERSION;
        }
    }
}
