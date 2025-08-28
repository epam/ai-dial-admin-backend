package com.epam.aidial.cfg.service.export;

import com.epam.aidial.cfg.client.CoreConfigClient;
import com.epam.aidial.cfg.configuration.logging.LogExecution;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@LogExecution
@ConditionalOnProperty(value = "config.reload.enabled", havingValue = "true")
public class CoreConfigReloadService {

    private final CoreConfigClient coreConfigClient;
    private final ConfigExportFacade configExportFacade;
    private final long delayReloadMilliseconds;

    public CoreConfigReloadService(CoreConfigClient coreConfigClient,
                                   ConfigExportFacade configExportFacade,
                                   @Value("${config.reload.delay}") long delayReloadMilliseconds) {
        this.coreConfigClient = coreConfigClient;
        this.configExportFacade = configExportFacade;
        this.delayReloadMilliseconds = delayReloadMilliseconds;
    }

    public void reloadConfig() throws Exception {
        try {
            configExportFacade.exportCurrentConfig();
            TimeUnit.MILLISECONDS.sleep(delayReloadMilliseconds);
            coreConfigClient.reload();
        } catch (Exception exception) {
            log.error("Failed reload : {}", exception.getMessage(), exception);
            throw exception;
        }
    }

}
