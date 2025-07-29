package com.epam.aidial.cfg.domain.service;

import com.epam.aidial.cfg.client.deployment.manager.DeploymentManagerClient;
import com.epam.aidial.cfg.client.dto.DeploymentInfoDto;
import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.exception.DeploymentClientNotExistsException;
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
public class DeploymentManagerService {

    private final DeploymentManagerClient deploymentManagerClient;
    private final String deploymentClientUrl;

    private final Cache<UUID, DeploymentInfoDto> deploymentCache;

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
            return deploymentCache.get(UUID.fromString(id), () -> {
                log.debug("Deployment '{}' is not present in cache, loading from deployment manager client", id);
                return getDeploymentInfoDto(id);
            });
        } catch (DeploymentClientNotExistsException deploymentClientNotExistsException) {
            throw deploymentClientNotExistsException;
        } catch (Exception e) {
            log.error("Failed to get deployment by ID '{}'", id, e);
            return null;
        }
    }

    private DeploymentInfoDto getDeploymentInfoDto(String id) {
        if (StringUtils.isBlank(deploymentClientUrl)) {
            throw new DeploymentClientNotExistsException();
        }
        return deploymentManagerClient.getDeployment(id);
    }
}