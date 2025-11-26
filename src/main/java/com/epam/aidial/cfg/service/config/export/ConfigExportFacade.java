package com.epam.aidial.cfg.service.config.export;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.service.config.normalizer.CoreConfigNormalizer;
import com.epam.aidial.core.config.Config;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@LogExecution
public class ConfigExportFacade {

    private final CoreConfigAggregatorService configService;
    private final ConfigExportService configExportService;

    private final boolean createResources;

    public ConfigExportFacade(CoreConfigAggregatorService configService,
                              ConfigExportService configExportService,
                              @Value("${config.export.createResources}") boolean createResources) {
        this.configService = configService;
        this.configExportService = configExportService;
        this.createResources = createResources;
    }

    public void exportCurrentConfig() {
        Config config = configService.getConfig();
        log.debug("Exporting current Configuration settings.");
        configExportService.export(config, createResources);
    }

}
