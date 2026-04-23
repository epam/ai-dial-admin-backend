package com.epam.aidial.cfg.domain.service;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dao.jpa.AdapterJpaRepository;
import com.epam.aidial.cfg.dao.model.AdapterEntity;
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
public class AdapterRefreshService {

    private final AdapterJpaRepository adapterJpaRepository;
    private final ContainerEndpointResolver containerEndpointResolver;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void refreshEndpoints(AdapterEntity entity) {
        var containerEntity = entity.getAdapterContainer();
        if (containerEntity == null) {
            return;
        }
        try {
            containerEndpointResolver.processContainerEndpoints(entity);
            adapterJpaRepository.save(entity);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to refresh container endpoints for adapter '{}', container '{}'. "
                    + "Retaining existing endpoints. Reason: {}",
                    entity.getName(), containerEntity.getContainerId(), e.getMessage());
        }
    }
}
