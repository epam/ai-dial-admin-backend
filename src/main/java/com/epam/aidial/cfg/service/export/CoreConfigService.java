package com.epam.aidial.cfg.service.export;

import com.epam.aidial.cfg.client.CoreConfigClient;
import com.epam.aidial.cfg.configuration.logging.LogExecution;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@LogExecution
public class CoreConfigService {

    private final CoreConfigClient coreConfigClient;
    private final ConfigExportScheduler configExportScheduler;
    private final long delayReloadMilliseconds;

    public CoreConfigService(CoreConfigClient coreConfigClient,
                             ConfigExportScheduler configExportScheduler,
                             @Value("${config.export.delayConfigReload}") long delayReloadMilliseconds) {
        this.coreConfigClient = coreConfigClient;
        this.configExportScheduler = configExportScheduler;
        this.delayReloadMilliseconds = delayReloadMilliseconds;
    }

    public void reloadConfig() throws Exception {
        try {
            configExportScheduler.exportCurrentConfig();
            TimeUnit.MILLISECONDS.sleep(delayReloadMilliseconds);
            coreConfigClient.reload();
        } catch (Exception exception) {
            log.error("Failed reload : {}", exception.getMessage(), exception);
            throw exception;
        }
    }

}
