package com.epam.aidial.cfg.domain.service;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@LogExecution
@RequiredArgsConstructor
public class InterceptorEndpointsRefresherScheduledService {

    private final InterceptorService interceptorService;

    @Scheduled(fixedDelayString = "${interceptor.endpoints.refresh.interval}")
    public void refreshInterceptorEndpoints() {
        try {
            log.info("Refreshing interceptor endpoints where source is container");
            interceptorService.refreshInterceptorEndpoints();
            log.info("Successfully refreshed interceptor endpoints");
        } catch (Exception e) {
            log.error("Failed to refresh interceptor endpoints", e);
        }
    }
}
