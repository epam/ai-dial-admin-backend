package com.epam.aidial.cfg.domain.service;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Conditional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@LogExecution
@RequiredArgsConstructor
@Conditional(EndpointsRefreshEnabledCondition.class)
public class EndpointsRefresherScheduledService {

    private final InterceptorService interceptorService;

    // TODO [VPA]: use system user
    @Scheduled(fixedDelayString = "${external.deployment.endpoint.refresh.interval}")
    public void refreshEndpoints() {
        try {
            log.debug("Refreshing interceptor endpoints where source is container");
            interceptorService.refreshEndpoints();
            log.debug("Successfully refreshed interceptor endpoints");
        } catch (Exception e) {
            log.warn("Failed to refresh interceptor endpoints", e);
        }
    }
}
