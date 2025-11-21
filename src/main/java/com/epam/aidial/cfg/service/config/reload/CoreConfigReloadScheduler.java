package com.epam.aidial.cfg.service.config.reload;

import com.epam.aidial.cfg.client.CoreConfigClient;
import com.epam.aidial.cfg.configuration.logging.LogExecution;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
@LogExecution
@ConditionalOnProperty(value = "config.reload.enabled", havingValue = "true")
public class CoreConfigReloadScheduler {

    private final CoreConfigClient coreConfigClient;
    private final ConfigReloadErrorHandler errorHandler;

    //@Scheduled(fixedDelayString = "${config.reload.schedule.delayMs}")
    public void reloadCoreConfig() {
        try {
            coreConfigClient.reload();
            errorHandler.setLastErrorMessage(null);
        } catch (Exception e) {
            log.error("Error during config reload", e);
            String lastErrorMessage = e.getMessage() == null ? "An unknown error occurred during config reload" : e.getMessage();
            errorHandler.setLastErrorMessage(lastErrorMessage);
        }
    }

}
