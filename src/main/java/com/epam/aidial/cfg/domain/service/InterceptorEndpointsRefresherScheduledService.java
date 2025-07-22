package com.epam.aidial.cfg.domain.service;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dao.jpa.InterceptorJpaRepository;
import com.epam.aidial.cfg.dto.DeploymentInfoDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@LogExecution
@RequiredArgsConstructor
public class InterceptorEndpointsRefresherScheduledService {

    private final ExternalDeploymentScheduledService deploymentService;
    private final InterceptorJpaRepository interceptorJpaRepository;

    @Scheduled(fixedDelayString = "${interceptor.endpoints.refresh.interval:360000}")
    public void refreshInterceptorEndpoints() {
        try {
            log.info("Refreshing interceptor endpoints where source is container");
            refreshInterceptorEndpointsInternal();
            log.info("Successfully refreshed interceptor endpoints");
        } catch (Exception e) {
            log.error("Failed to refresh interceptor endpoints", e);
        }
    }

    private void refreshInterceptorEndpointsInternal() {
        var interceptorEntities = interceptorJpaRepository.findByContainerIdIsNotNull();
        for (var interceptor : interceptorEntities) {
            DeploymentInfoDto deploymentInfo = deploymentService.getById(interceptor.getContainerId());
            String url = deploymentInfo.getUrl();

            String endpoint = interceptor.getEndpoint();
            if (endpoint != null && !endpoint.startsWith(url)) {
                // TODO [VPA]: endpoint suffix is lost
                interceptor.setEndpoint(url);
            }

            String configurationEndpoint = interceptor.getConfigurationEndpoint();
            if (configurationEndpoint != null && !configurationEndpoint.startsWith(url)) {
                // TODO [VPA]: configurationEndpoint suffix is lost
                interceptor.setConfigurationEndpoint(url);
            }
        }
        interceptorJpaRepository.saveAll(interceptorEntities);
    }
}
