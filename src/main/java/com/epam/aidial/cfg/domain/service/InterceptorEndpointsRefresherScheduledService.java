package com.epam.aidial.cfg.domain.service;

import com.epam.aidial.cfg.client.dto.DeploymentInfoDto;
import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dao.jpa.InterceptorJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@LogExecution
@RequiredArgsConstructor
public class InterceptorEndpointsRefresherScheduledService {

    private final ExternalDeploymentScheduledService deploymentService;
    private final InterceptorJpaRepository interceptorJpaRepository;

    @Scheduled(fixedDelayString = "${interceptor.endpoints.refresh.interval}")
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

        for (var interceptorEntity : interceptorEntities) {
            var interceptorContainerEntity = interceptorEntity.getInterceptorContainer();
            String containerId = interceptorContainerEntity.getContainerId();

            DeploymentInfoDto deploymentInfo = deploymentService.getById(containerId);
            validateDeploymentInfo(deploymentInfo, containerId);

            String containerUrl = deploymentInfo.getUrl();
            String completionEndpoint = containerUrl + interceptorContainerEntity.getCompletionEndpointPath();
            String configurationEndpoint = containerUrl + interceptorContainerEntity.getConfigurationEndpointPath();

            interceptorEntity.setEndpoint(completionEndpoint);
            interceptorEntity.setConfigurationEndpoint(configurationEndpoint);
        }

        interceptorJpaRepository.saveAll(interceptorEntities);
    }

    private void validateDeploymentInfo(DeploymentInfoDto deploymentInfo, String containerId) {
        if (deploymentInfo == null) {
            throw new IllegalArgumentException("Container with ID '%s' not found in cache".formatted(containerId));
        }

        String deploymentUrl = deploymentInfo.getUrl();
        if (StringUtils.isBlank(deploymentUrl)) {
            throw new IllegalArgumentException(
                "Container URL is not present, please check if it is deployed. Container ID: %s".formatted(containerId)
            );
        }
    }
}
