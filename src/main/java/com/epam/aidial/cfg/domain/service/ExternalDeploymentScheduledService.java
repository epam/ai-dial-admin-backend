package com.epam.aidial.cfg.domain.service;

import com.epam.aidial.cfg.client.deployment.manager.DeploymentClient;
import com.epam.aidial.cfg.client.dto.DeploymentInfoDto;
import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.networknt.schema.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@LogExecution
public class ExternalDeploymentScheduledService {

    private final DeploymentClient deploymentClient;
    private final String deploymentClientUrl;

    private final Cache<UUID, DeploymentInfoDto> deploymentCache;

    public ExternalDeploymentScheduledService(DeploymentClient deploymentClient, 
                                              @Value("${deployment.cache.expiration.interval}") long cacheExpirationInterval,
                                              @Value("${deployment.client.url}") String deploymentClientUrl) {
        this.deploymentClient = deploymentClient;
        this.deploymentClientUrl = deploymentClientUrl;
        this.deploymentCache = CacheBuilder.newBuilder()
                .expireAfterWrite(cacheExpirationInterval, TimeUnit.MILLISECONDS)
                .build();
    }

    public DeploymentInfoDto getById(String id) {
        try {
            return deploymentCache.get(UUID.fromString(id), () -> {
                log.debug("Deployment '{}' is not present in cache, loading from deployment client", id);
                return getDeploymentInfoDto(id);
            });
        } catch (Exception e) {
            log.error("Failed to get deployment by ID '{}'", id, e);
            return null;
        }
    }

    public DeploymentInfoDto getByIdUncached(String id) {
        try {
            DeploymentInfoDto deploymentInfo = getDeploymentInfoDto(id);
            deploymentCache.put(deploymentInfo.getId(), deploymentInfo);
            return deploymentInfo;
        } catch (Exception e) {
            String message = e.getMessage();
            if (message != null && message.contains("Deploy not found by id")) {
                log.warn("Deployment not found by ID '{}'", id);
                return null;
            }
            log.error("Failed to get deployment by ID '%s'".formatted(id), e);
            throw e;
        }
    }

    private DeploymentInfoDto getDeploymentInfoDto(String id) {
        if (StringUtils.isBlank(deploymentClientUrl)) {
            throw new RuntimeException("Deployment client does not exist or URL is not specified");
        }
        return deploymentClient.getDeployment(id);
    }
}