package com.epam.aidial.cfg.service.transfer;

import com.epam.aidial.cfg.domain.service.DatabaseService;
import com.epam.aidial.cfg.service.transfer.exporter.CoreConfigRetriever;
import com.epam.aidial.cfg.service.transfer.importer.ConfigImporter;
import com.epam.aidial.core.config.Config;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(value = "config.import.autoImportOnBootstrap.enabled", havingValue = "true")
public class CoreConfigAutoImportOnBootstrapService {

    private final DatabaseService databaseService;
    private final CoreConfigRetriever coreConfigRetriever;
    private final ConfigImporter configImporter;
    private final CoreConfigAutoImportOnBootstrapLock coreConfigAutoImportOnBootstrapLock;

    @EventListener(ApplicationReadyEvent.class)
    public void autoImportCoreConfig() {
        try {
            if (databaseService.isInitializedEmptyDatabase()) {
                log.info("Auto import of core config started");
                Config config = coreConfigRetriever.getConfig(true);
                configImporter.importConfigWithOverride(config);
                log.info("Auto import of core config finished");
            } else {
                log.info("Database is not empty. Skipping auto import of core config");
            }
            coreConfigAutoImportOnBootstrapLock.finishAutoImport();
        } catch (Exception exception) {
            log.error("Auto import of core config failed", exception);
            throw exception;
        }
    }

}