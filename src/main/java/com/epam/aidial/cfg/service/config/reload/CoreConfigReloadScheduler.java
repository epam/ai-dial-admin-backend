package com.epam.aidial.cfg.service.config.reload;

import com.epam.aidial.cfg.client.BackendTokenAuthenticatedCoreClient;
import com.epam.aidial.cfg.configuration.logging.LogExecution;
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

    @Scheduled(fixedDelayString = "${config.autoReload.schedule.delayMs}")
    public void reloadCoreConfig() {
        try {
            backendTokenAuthenticatedCoreClient.reload();
            errorHandler.setLastErrorMessage(null);
        } catch (Exception e) {
            log.error("Error during config reload", e);
            String lastErrorMessage = e.getMessage() == null ? "An unknown error occurred during config reload" : e.getMessage();
            errorHandler.setLastErrorMessage(lastErrorMessage);
        }
    }

}
