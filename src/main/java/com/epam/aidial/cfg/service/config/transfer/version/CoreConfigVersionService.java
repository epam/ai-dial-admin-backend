package com.epam.aidial.cfg.service.config.transfer.version;

import com.epam.aidial.cfg.configuration.CoreConfigVersionProperties;
import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.model.AdminSettings;
import com.epam.aidial.cfg.domain.service.AdminSettingsService;
import com.epam.aidial.cfg.model.CoreConfigVersions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import static com.epam.aidial.cfg.service.config.transfer.version.CoreConfigVersionAutoDetectService.AUTO_DETECT_FAILED_CORE_VERSION;

@Service
@RequiredArgsConstructor
@LogExecution
@Slf4j
public class CoreConfigVersionService {

    private final AdminSettingsService adminSettingsService;
    private final CoreConfigVersionAutoDetectService coreConfigVersionAutoDetectService;
    private final CoreConfigVersionProperties coreConfigVersionProperties;

    public String getVersionForExport() {
        AdminSettings adminSettings = adminSettingsService.getAdminSettings();
        String manuallySetCoreConfigVersion = adminSettings.getCoreConfigVersion();
        if (StringUtils.isNotBlank(manuallySetCoreConfigVersion)) {
            log.debug("Core version for export is manually set version: {}", manuallySetCoreConfigVersion);
            return manuallySetCoreConfigVersion;
        }

        String autoDetectedVersion = coreConfigVersionAutoDetectService.getVersion();
        if (StringUtils.isNotBlank(autoDetectedVersion) && !autoDetectedVersion.equals(AUTO_DETECT_FAILED_CORE_VERSION)) {
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
            coreConfigVersions.setManuallySetVersion(manuallySetCoreConfigVersion);
        }

        String autoDetectedVersion = coreConfigVersionAutoDetectService.getVersion();
        if (StringUtils.isNotBlank(autoDetectedVersion)) {
            coreConfigVersions.setAutoDetectedVersion(autoDetectedVersion);
        }

        String defaultVersion = coreConfigVersionProperties.getTarget();
        if (StringUtils.isNotBlank(defaultVersion)) {
            coreConfigVersions.setDefaultVersion(defaultVersion);
        }

        return coreConfigVersions;
    }
}
