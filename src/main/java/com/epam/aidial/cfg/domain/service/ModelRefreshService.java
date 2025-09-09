package com.epam.aidial.cfg.domain.service;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dao.jpa.ModelJpaRepository;
import com.epam.aidial.cfg.dao.model.ModelEntity;
import com.epam.aidial.cfg.domain.util.ContainerEndpointResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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
        containerEndpointResolver.processContainerEndpoints(modelEntity);
        modelJpaRepository.save(modelEntity);
    }
}