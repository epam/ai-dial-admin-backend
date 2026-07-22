package com.epam.aidial.cfg.domain.service;

import com.epam.aidial.cfg.client.deployment.manager.DeploymentManagerClient;
import com.epam.aidial.cfg.client.dto.DeploymentInfoDto;
import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.exception.DeploymentClientNotExistsException;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
            DeploymentInfoDto cached = deploymentCache.getIfPresent(id);
            if (cached != null) {
                return cached;
            }

            log.debug("Deployment '{}' is not present in cache, loading from deployment manager client", id);
            DeploymentInfoDto info = getDeploymentInfoDto(id);

            // Cache only fully-deployed deployments. The deployment manager can report a deployment
            // before its URL is assigned (status becomes RUNNING slightly before the URL is populated).
            // Caching a blank-URL result for the whole expiration window would make creation from a
            // freshly-running container keep failing with "Container URL is not present" long after the
            // URL is actually available, since retries would keep hitting the stale cache entry.
            if (info != null && StringUtils.isNotBlank(info.getUrl())) {
                deploymentCache.put(id, info);
            }
            return info;
        } catch (DeploymentClientNotExistsException deploymentClientNotExistsException) {
            throw deploymentClientNotExistsException;
        } catch (Exception e) {
            if (e instanceof FeignException.NotFound || e.getCause() instanceof FeignException.NotFound) {
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