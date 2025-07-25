package com.epam.aidial.cfg.domain.service;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@LogExecution
@RequiredArgsConstructor
public class EndpointsRefresherScheduledService {

    private final InterceptorService interceptorService;

    @Value("${external.deployment.endpoint.refresh.enabled}")
    private boolean enableEndpointsRefresh;

    // TODO [VPA]: use system user
    @Scheduled(fixedDelayString = "${external.deployment.endpoint.refresh.interval}")
    public void refreshEndpoints() {
        if (!enableEndpointsRefresh) {
            log.debug("External deployment endpoints refresh is disabled");
            return;
        }

        try {
            log.debug("Refreshing interceptor endpoints where source is container");
            interceptorService.refreshEndpoints();
            log.debug("Successfully refreshed interceptor endpoints");
        } catch (Exception e) {
            log.warn("Failed to refresh interceptor endpoints", e);
        }
    }
}
