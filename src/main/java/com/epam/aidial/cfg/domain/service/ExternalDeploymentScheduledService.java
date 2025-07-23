package com.epam.aidial.cfg.domain.service;

import com.epam.aidial.cfg.client.DeploymentClient;
import com.epam.aidial.cfg.client.dto.DeploymentInfoDto;
import com.epam.aidial.cfg.client.dto.DeploymentTypeDto;
import com.epam.aidial.cfg.configuration.logging.LogExecution;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@LogExecution
@RequiredArgsConstructor
public class ExternalDeploymentScheduledService {

    private final DeploymentClient deploymentClient;

    private final Map<UUID, DeploymentInfoDto> deploymentCache = new ConcurrentHashMap<>();

    // TODO [VPA]: use system user
    @Scheduled(fixedDelayString = "${deployment.cache.refresh.interval}")
    public void refreshDeploymentCache() {
        try {
            log.info("Refreshing deployment cache");
            deploymentCache.clear();
            // interceptors only, other types are not needed for now
            deploymentClient.getDeployments(DeploymentTypeDto.INTERCEPTOR)
                    .forEach(deployment -> deploymentCache.put(deployment.getId(), deployment));
            log.info("Deployment cache refreshed successfully. Cache size: {}", deploymentCache.size());
        } catch (Exception e) {
            log.error("Failed to refresh deployment cache", e);
        }
    }

    public DeploymentInfoDto getById(String id) {
        return deploymentCache.get(UUID.fromString(id));
    }

    public DeploymentInfoDto getByIdUncached(String id) {
        try {
            DeploymentInfoDto deploymentInfo = deploymentClient.getDeployment(id);
            deploymentCache.put(deploymentInfo.getId(), deploymentInfo);
            return deploymentInfo;
        } catch (Exception e) {
            if (e.getMessage().contains("Deploy not found by id")) {
                log.warn("Deployment not found by ID '{}'", id);
                return null;
            }
            log.error("Failed to get deployment by ID '%s'".formatted(id), e);
            throw e;
        }
    }
}