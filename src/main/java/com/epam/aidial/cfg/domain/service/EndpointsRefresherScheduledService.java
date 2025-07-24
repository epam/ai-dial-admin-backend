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

    @Value("${endpoints.refresh.enabled}")
    private boolean enableEndpointsRefresh;

    // TODO [VPA]: use system user
    @Scheduled(fixedDelayString = "${endpoints.refresh.interval}")
    public void refreshEndpoints() {
        if (!enableEndpointsRefresh) {
            log.debug("Endpoints refresh is disabled");
            return;
        }

        try {
            log.info("Refreshing interceptor endpoints where source is container");
            interceptorService.refreshEndpoints();
            log.info("Successfully refreshed interceptor endpoints");
        } catch (Exception e) {
            log.error("Failed to refresh interceptor endpoints", e);
        }
    }
}
