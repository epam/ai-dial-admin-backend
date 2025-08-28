package com.epam.aidial.cfg.service.export;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.service.normalizer.CoreConfigNormalizer;
import com.epam.aidial.core.config.Config;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@LogExecution
public class ConfigExportFacade {

    private final CoreConfigAggregatorService configService;
    private final ConfigExportService configExportService;
    private final List<CoreConfigNormalizer> normalizers;

    private final boolean createResources;

    public ConfigExportFacade(CoreConfigAggregatorService configService,
                              ConfigExportService configExportService,
                              List<CoreConfigNormalizer> normalizers,
                              @Value("${config.export.createResources}") boolean createResources) {
        this.configService = configService;
        this.configExportService = configExportService;
        this.normalizers = normalizers;
        this.createResources = createResources;
    }

    public void exportCurrentConfig() {
        Config config = configService.getConfig();
        log.debug("Exporting current Configuration settings.");
        normalizers.forEach(n -> n.normalize(config));
        configExportService.export(config, createResources);
    }

}
