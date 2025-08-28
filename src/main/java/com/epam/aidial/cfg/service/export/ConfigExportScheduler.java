package com.epam.aidial.cfg.service.export;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.service.transfer.CoreConfigAutoImportOnBootstrapLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Created by Aliaksei Kurnosau on 9/9/24.
 */
@RequiredArgsConstructor
@Service
@Slf4j
@LogExecution
@ConditionalOnProperty(value = "config.export.enabled", havingValue = "true")
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class ConfigExportScheduler {

    private final Optional<CoreConfigAutoImportOnBootstrapLock> coreConfigAutoImportLock;
    private final ConfigExportFacade configExportFacade;
    private final ConfigExportErrorHandler errorHandler;

    @Scheduled(fixedDelayString = "${config.export.syncPeriod}")
    public void exportCurrentConfig() {
        try {
            if (coreConfigAutoImportLock.isPresent() && !coreConfigAutoImportLock.get().isAutoImportFinished()) {
                log.info("Skipping export of current configuration since auto import of core configuration is not finished");
                return;
            }

            configExportFacade.exportCurrentConfig();
            errorHandler.setLastErrorMessage(null);
        } catch (Exception e) {
            log.error("Can't export current configuration", e);
            String lastErrorMessage = e.getMessage() == null ? "An unknown error occurred during config export" : e.getMessage();
            errorHandler.setLastErrorMessage(lastErrorMessage);
        }
    }

}
