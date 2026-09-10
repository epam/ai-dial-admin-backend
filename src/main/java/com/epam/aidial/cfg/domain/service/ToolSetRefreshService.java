package com.epam.aidial.cfg.domain.service;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dao.jpa.ToolSetJpaRepository;
import com.epam.aidial.cfg.dao.model.ToolSetEntity;
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
public class ToolSetRefreshService {

    private final ToolSetJpaRepository toolSetJpaRepository;
    private final ContainerEndpointResolver containerEndpointResolver;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void refreshEndpoints(ToolSetEntity entity) {
        var containerEntity = entity.getToolSetContainer();
        if (containerEntity == null) {
            return;
        }
        try {
            containerEndpointResolver.processContainerEndpoints(entity);
            toolSetJpaRepository.save(entity);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to refresh container endpoints for toolset '{}', container '{}'. "
                    + "Retaining existing endpoints. Reason: {}",
                    entity.getDeploymentName(), containerEntity.getContainerId(), e.getMessage());
        }
    }
}
