package com.epam.aidial.cfg.service.config.transfer;

import com.epam.aidial.cfg.configuration.AutoImportOnBootstrapProperties;
import com.epam.aidial.cfg.domain.service.DatabaseService;
import com.epam.aidial.cfg.model.ConfigImportOptions;
import com.epam.aidial.cfg.security.aspect.RunAsSystemUser;
import com.epam.aidial.cfg.service.config.transfer.exporter.CoreConfigRetriever;
import com.epam.aidial.cfg.service.config.transfer.importer.ConfigImporter;
import com.epam.aidial.core.config.Config;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(value = "config.import.autoImportOnBootstrap.enabled", havingValue = "true")
public class CoreConfigAutoImportOnBootstrapService {

    private final DatabaseService databaseService;
    private final CoreConfigRetriever coreConfigRetriever;
    private final ConfigImporter configImporter;
    private final CoreConfigAutoImportOnBootstrapLock coreConfigAutoImportOnBootstrapLock;
    private final JsonConfigMerger jsonConfigMerger;
    private final AutoImportOnBootstrapProperties properties;

    @EventListener(ApplicationReadyEvent.class)
    @RunAsSystemUser
    public void autoImportCoreConfig() {
        try {
            if (databaseService.isInitializedEmptyDatabase()) {
                log.info("Auto import of core config started");
                doImport();
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

    private void doImport() {
        List<String> filePaths = properties.getFilePaths();
        if (filePaths.isEmpty()) {
            Config config = coreConfigRetriever.getConfig(true);
            configImporter.importConfigWithOverride(config);
            return;
        }

        if (properties.getStrategy() == MultiFileImportStrategy.MERGE_JSON) {
            Config merged = jsonConfigMerger.merge(filePaths);
            configImporter.importConfigWithOverride(merged);
        } else {
            var importOptions = new ConfigImportOptions(properties.getConflictResolutionPolicy());
            for (String path : filePaths) {
                log.info("Sequential import of config file: {}", path);
                Config config = jsonConfigMerger.merge(List.of(path));
                configImporter.importConfig(config, importOptions);
            }
        }
    }
}
