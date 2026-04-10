package com.epam.aidial.cfg.domain.service;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.security.aspect.RunAsInternalUser;
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

    private final InterceptorService interceptorService;
    private final ApplicationService applicationService;
    private final ToolSetService toolSetService;
    private final AdapterService adapterService;
    private final ModelService modelService;

    @Scheduled(fixedDelayString = "${plugins.deployment.manager.endpoint.refresh.interval}")
    @RunAsInternalUser
    public void refreshEndpoints() {
        refreshEndpoints(interceptorService::refreshEndpoints, "interceptor");
        refreshEndpoints(applicationService::refreshEndpoints, "application");
        refreshEndpoints(adapterService::refreshEndpoints, "adapter");
        refreshEndpoints(toolSetService::refreshEndpoints, "toolset");
        refreshEndpoints(modelService::refreshEndpoints, "model");
        refreshEndpoints(modelService::refreshEndpoints, "model");
    }

    private void refreshEndpoints(Runnable refreshAction, String logEntity) {
        try {
            log.debug("Refreshing {} endpoints where source is container", logEntity);
            refreshAction.run();
            log.debug("Successfully refreshed {} endpoints", logEntity);
        } catch (Exception e) {
            log.warn("Failed to refresh {} endpoints", logEntity, e);
        }
    }
}
