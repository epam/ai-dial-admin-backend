package com.epam.aidial.cfg.domain.service;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dao.jpa.ModelJpaRepository;
import com.epam.aidial.cfg.dao.model.ModelEntity;
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
public class ModelRefreshService {

    private final ModelJpaRepository modelJpaRepository;
    private final ContainerEndpointResolver containerEndpointResolver;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void refreshEndpoints(ModelEntity modelEntity) {
        var modelContainerEntity = modelEntity.getModelContainer();
        if (modelContainerEntity == null) {
            return;
        }
        try {
            containerEndpointResolver.processContainerEndpoints(modelEntity);
            modelJpaRepository.save(modelEntity);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to refresh container endpoints for model '{}', container '{}'. "
                    + "Retaining existing endpoints. Reason: {}",
                    modelEntity.getDeploymentName(), modelContainerEntity.getContainerId(), e.getMessage());
        }
    }
}
