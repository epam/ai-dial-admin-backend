package com.epam.aidial.cfg.service.config.reload;

import com.epam.aidial.cfg.client.BackendTokenAuthenticatedCoreClient;
import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.exception.CoreConfigReloadException;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
@LogExecution
@ConditionalOnProperty(value = "config.autoReload.enabled", havingValue = "true")
public class CoreConfigReloadScheduler {

    private final BackendTokenAuthenticatedCoreClient backendTokenAuthenticatedCoreClient;
    private final ConfigReloadErrorHandler errorHandler;
    private final CoreConfigReloadCache coreConfigReloadCache;

    @Scheduled(fixedDelayString = "${config.autoReload.schedule.delayMs}")
    public void reloadCoreConfig() {
        try {
            JsonNode config = backendTokenAuthenticatedCoreClient.reload();
            coreConfigReloadCache.put(config);
            errorHandler.setLastErrorMessage(null);
        } catch (Exception e) {
            log.error("Failed to reload configuration in DIAL Core", e);
            String lastErrorMessage = e.getMessage() == null ? "An unknown error occurred during config reload" : e.getMessage();
            errorHandler.setLastErrorMessage(lastErrorMessage);
            throw new CoreConfigReloadException("Failed to reload configuration in DIAL Core");
        }
    }

}