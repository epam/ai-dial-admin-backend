package com.epam.aidial.cfg.service.export;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.service.normalizer.CoreConfigNormalizer;
import com.epam.aidial.core.config.Config;
import lombok.RequiredArgsConstructor;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by Aliaksei Kurnosau on 9/9/24.
 */
@RequiredArgsConstructor
@Service
@Slf4j
@LogExecution
public class ConfigExportScheduler {

    private final CoreConfigAggregatorService configService;
    private final ConfigExportService configExportService;
    private final List<CoreConfigNormalizer> normalizers;
    private final ConfigExportErrorHandler errorHandler;

    @Value("${config.export.createResources}")
    private boolean createResources;

    @Scheduled(fixedRateString = "${config.export.syncPeriod}")
    @Synchronized
    public void exportCurrentConfig() {
        try {
            Config config = configService.getConfig();
            log.debug("Exporting current Configuration settings.");
            normalizers.forEach(n -> n.normalize(config));
            configExportService.export(config, createResources);
            errorHandler.setLastErrorMessage(null);
        } catch (Exception e) {
            log.error("Can't export current configuration", e);
            String lastErrorMessage = e.getMessage() == null ? "An unknown error occurred during config export" : e.getMessage();
            errorHandler.setLastErrorMessage(lastErrorMessage);
        }
    }

}
