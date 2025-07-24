package com.epam.aidial.cfg.domain.service;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dao.jpa.InterceptorJpaRepository;
import com.epam.aidial.cfg.dao.model.InterceptorContainerEntity;
import com.epam.aidial.cfg.dao.model.InterceptorEntity;
import com.epam.aidial.cfg.domain.util.InterceptorEndpointUtil;
import com.epam.aidial.cfg.domain.validator.DeploymentInfoValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@LogExecution
@RequiredArgsConstructor
public class InterceptorRefreshService {

    private final ExternalDeploymentScheduledService deploymentService;
    private final InterceptorJpaRepository interceptorJpaRepository;
    private final DeploymentInfoValidator deploymentInfoValidator;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void refreshEndpoints(InterceptorEntity interceptorEntity) {
        var interceptorContainerEntity = interceptorEntity.getInterceptorContainer();
        if (interceptorContainerEntity == null) {
            return;
        }

        String containerId = interceptorContainerEntity.getContainerId();

        InterceptorEndpointUtil.processContainerEndpoints(
                deploymentService,
                deploymentInfoValidator,
                containerId,
                interceptorContainerEntity,
                InterceptorContainerEntity::getCompletionEndpointPath,
                InterceptorContainerEntity::getConfigurationEndpointPath,
                (entity, endpoints) -> {
                    entity.setEndpoint(endpoints[0]);
                    entity.setConfigurationEndpoint(endpoints[1]);
                },
                interceptorEntity
        );

        interceptorJpaRepository.save(interceptorEntity);
    }
}