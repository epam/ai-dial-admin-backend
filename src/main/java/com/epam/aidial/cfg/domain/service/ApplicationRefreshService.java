package com.epam.aidial.cfg.domain.service;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dao.jpa.ApplicationJpaRepository;
import com.epam.aidial.cfg.dao.model.ApplicationEntity;
import com.epam.aidial.cfg.domain.util.ContainerEndpointResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@LogExecution
@RequiredArgsConstructor
public class ApplicationRefreshService {

    private final ApplicationJpaRepository applicationJpaRepository;
    private final ContainerEndpointResolver containerEndpointResolver;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void refreshEndpoints(ApplicationEntity entity) {
        var containerEntity = entity.getApplicationContainer();
        if (containerEntity == null) {
            return;
        }
        try {
            containerEndpointResolver.processContainerEndpoints(entity);
            applicationJpaRepository.save(entity);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to refresh container endpoints for application '{}', container '{}'. "
                    + "Retaining existing endpoints. Reason: {}",
                    entity.getDeploymentName(), containerEntity.getContainerId(), e.getMessage());
        }
    }
}
