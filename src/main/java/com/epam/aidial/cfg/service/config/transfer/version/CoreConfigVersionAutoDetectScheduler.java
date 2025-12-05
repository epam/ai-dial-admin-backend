package com.epam.aidial.cfg.service.config.transfer.version;

import com.epam.aidial.cfg.client.AnonymousCoreConfigClient;
import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.utils.CoreConfigVersionNormalizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@LogExecution
@RequiredArgsConstructor
@ConditionalOnProperty(value = "config.version.autoDetect.enabled", havingValue = "true")
public class CoreConfigVersionAutoDetectScheduler {

    private final AnonymousCoreConfigClient coreConfigClient;
    private final CoreConfigAutoDetectedVersionCache coreConfigAutoDetectedVersionCache;

    @Scheduled(fixedDelayString = "${config.version.autoDetect.schedule.delayMs}")
    public void retrieveCoreVersionAndSaveInCache() {
        try {
            log.debug("Attempting to get version from Core");

            String coreVersion = coreConfigClient.getVersion();
            String normalizedCoreVersion = CoreConfigVersionNormalizer.normalizeCoreVersion(coreVersion);
            coreConfigAutoDetectedVersionCache.putVersion(normalizedCoreVersion);

            log.info("Successfully get version from Core: {}, normalized version: {}", coreVersion, normalizedCoreVersion);
        } catch (Exception e) {
            log.info("Unable to get version from Core", e);
        }
    }
}