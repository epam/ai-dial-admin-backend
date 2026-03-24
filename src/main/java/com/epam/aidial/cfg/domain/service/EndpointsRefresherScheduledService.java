package com.epam.aidial.cfg.domain.service;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.security.SystemSecurityContextExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@LogExecution
@RequiredArgsConstructor
@ConditionalOnProperty(value = "plugins.deployment.manager.endpoint.refresh.enabled", havingValue = "true")
public class EndpointsRefresherScheduledService {

    private final SystemSecurityContextExecutor systemSecurityContextExecutor;
    private final InterceptorService interceptorService;
    private final ToolSetService toolSetService;
    private final AdapterService adapterService;
    private final ModelService modelService;

    @Scheduled(fixedDelayString = "${plugins.deployment.manager.endpoint.refresh.interval}")
    public void refreshEndpoints() {
        systemSecurityContextExecutor.runAsSystemUser(() -> {
            refreshEndpoints(interceptorService::refreshEndpoints, "interceptor");
            refreshEndpoints(adapterService::refreshEndpoints, "adapter");
            refreshEndpoints(toolSetService::refreshEndpoints, "toolset");
            refreshEndpoints(modelService::refreshEndpoints, "model");
        });
    }

    private void refreshEndpoints(Runnable refreshAction, String logEntity) {
        try {
            log.debug("Refreshing %s endpoints where source is container".formatted(logEntity));
            refreshAction.run();
            log.debug("Successfully refreshed %s endpoints".formatted(logEntity));
        } catch (Exception e) {
            log.warn("Failed to refresh %s endpoints".formatted(logEntity), e);
        }
    }
}
