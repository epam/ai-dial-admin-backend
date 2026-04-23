package com.epam.aidial.cfg.domain.service;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dao.jpa.InterceptorJpaRepository;
import com.epam.aidial.cfg.dao.model.InterceptorEntity;
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
public class InterceptorRefreshService {

    private final InterceptorJpaRepository interceptorJpaRepository;
    private final ContainerEndpointResolver containerEndpointResolver;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void refreshEndpoints(InterceptorEntity interceptorEntity) {
        var interceptorContainerEntity = interceptorEntity.getInterceptorContainer();
        if (interceptorContainerEntity == null) {
            return;
        }
        try {
            containerEndpointResolver.processContainerEndpoints(interceptorEntity);
            interceptorJpaRepository.save(interceptorEntity);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to refresh container endpoints for interceptor '{}', container '{}'. "
                    + "Retaining existing endpoints. Reason: {}",
                    interceptorEntity.getName(), interceptorContainerEntity.getContainerId(), e.getMessage());
        }
    }
}
