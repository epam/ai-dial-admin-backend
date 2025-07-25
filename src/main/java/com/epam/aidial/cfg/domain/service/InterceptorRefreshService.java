package com.epam.aidial.cfg.domain.service;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dao.jpa.InterceptorJpaRepository;
import com.epam.aidial.cfg.dao.model.InterceptorEntity;
import com.epam.aidial.cfg.domain.util.InterceptorEndpointResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@LogExecution
@RequiredArgsConstructor
public class InterceptorRefreshService {

    private final InterceptorJpaRepository interceptorJpaRepository;
    private final InterceptorEndpointResolver interceptorEndpointResolver;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void refreshEndpoints(InterceptorEntity interceptorEntity) {
        var interceptorContainerEntity = interceptorEntity.getInterceptorContainer();
        if (interceptorContainerEntity == null) {
            return;
        }
        interceptorEndpointResolver.processContainerEndpoints(interceptorEntity);
        interceptorJpaRepository.save(interceptorEntity);
    }
}