package com.epam.aidial.cfg.domain.service;

import com.epam.aidial.cfg.client.deployment.manager.DeploymentManagerClient;
import com.epam.aidial.cfg.client.dto.DeploymentInfoDto;
import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.exception.DeploymentClientNotExistsException;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@LogExecution
public class DeploymentManagerService {

    private final DeploymentManagerClient deploymentManagerClient;
    private final String deploymentClientUrl;

    private final Cache<String, DeploymentInfoDto> deploymentCache;

    public DeploymentManagerService(DeploymentManagerClient deploymentManagerClient,
                                    @Value("${plugins.deployment.manager.cache.expiration.interval}") long cacheExpirationInterval,
                                    @Value("${plugins.deployment.manager.client.url}") String deploymentClientUrl) {
        this.deploymentManagerClient = deploymentManagerClient;
        this.deploymentClientUrl = deploymentClientUrl;
        this.deploymentCache = CacheBuilder.newBuilder()
                .expireAfterWrite(cacheExpirationInterval, TimeUnit.MILLISECONDS)
                .build();
    }

    public DeploymentInfoDto getById(String id) {
        try {
            return deploymentCache.get(id, () -> {
                log.debug("Deployment '{}' is not present in cache, loading from deployment manager client", id);
                return getDeploymentInfoDto(id);
            });
        } catch (DeploymentClientNotExistsException deploymentClientNotExistsException) {
            throw deploymentClientNotExistsException;
        } catch (Exception e) {
            if (e.getCause() instanceof FeignException.NotFound) {
                log.warn("Deployment not found by ID '{}'", id);
            } else {
                log.error("Failed to get deployment by ID '{}'", id, e);
            }
            return null;
        }
    }

    private DeploymentInfoDto getDeploymentInfoDto(String id) {
        if ("url-placeholder".equals(deploymentClientUrl)) {
            throw new DeploymentClientNotExistsException();
        }
        return deploymentManagerClient.getDeployment(id);
    }
}