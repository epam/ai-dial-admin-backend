package com.epam.aidial.cfg.service.config.transfer.version;

import com.epam.aidial.cfg.configuration.CoreConfigVersionProperties;
import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.model.AdminSettings;
import com.epam.aidial.cfg.domain.service.AdminSettingsService;
import com.epam.aidial.cfg.model.CoreConfigVersions;
import com.epam.aidial.cfg.service.config.transfer.VersionedSchemaLoader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@LogExecution
@Slf4j
public class CoreConfigVersionService {

    private final AdminSettingsService adminSettingsService;
    private final CoreConfigAutoDetectedVersionCache coreConfigAutoDetectedVersionCache;
    private final CoreConfigVersionProperties coreConfigVersionProperties;
    private final VersionedSchemaLoader schemaLoader;

    public String getVersionForExport() {
        AdminSettings adminSettings = adminSettingsService.getAdminSettings();
        String manuallySetCoreConfigVersion = adminSettings.getCoreConfigVersion();
        if (StringUtils.isNotBlank(manuallySetCoreConfigVersion)) {
            log.debug("Core version for export is manually set version: {}", manuallySetCoreConfigVersion);
            return manuallySetCoreConfigVersion;
        }

        String autoDetectedVersion = coreConfigAutoDetectedVersionCache.getVersion();
        if (StringUtils.isNotBlank(autoDetectedVersion)) {
            log.debug("Core version for export is auto-detected version: {}", autoDetectedVersion);
            return autoDetectedVersion;
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

        String autoDetectedVersion = coreConfigAutoDetectedVersionCache.getVersion();
        if (StringUtils.isNotBlank(autoDetectedVersion)) {
            coreConfigVersions.setAutoDetectedVersion(schemaLoader.getEffectiveVersion(autoDetectedVersion));
        }

        String defaultVersion = coreConfigVersionProperties.getTarget();
        if (StringUtils.isNotBlank(defaultVersion)) {
            coreConfigVersions.setDefaultVersion(schemaLoader.getEffectiveVersion(defaultVersion));
        }

        return coreConfigVersions;
    }
}
