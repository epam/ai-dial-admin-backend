package com.epam.aidial.cfg.domain.service;

import com.epam.aidial.cfg.client.dto.DeploymentInfoDto;
import com.epam.aidial.cfg.dao.jpa.InterceptorJpaRepository;
import com.epam.aidial.cfg.dao.mapper.InterceptorEntityMapper;
import com.epam.aidial.cfg.dao.model.InterceptorEntity;
import com.epam.aidial.cfg.domain.model.Interceptor;
import com.epam.aidial.cfg.domain.model.source.InterceptorContainerSource;
import com.epam.aidial.cfg.domain.validator.InterceptorValidator;
import com.epam.aidial.cfg.exception.EntityAlreadyExistsException;
import com.epam.aidial.cfg.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@Service("coreInterceptorService")
@RequiredArgsConstructor
public class InterceptorService {

    private static final String NOT_FOUND_MESSAGE_TEMPLATE = "Interceptor with name '%s' does not exist";

    private final ExternalDeploymentScheduledService deploymentService;
    private final InterceptorJpaRepository interceptorJpaRepository;
    private final InterceptorValidator interceptorValidator;
    private final InterceptorEntityMapper mapper;
    private final HistoryService historyService;

    @Transactional(readOnly = true)
    public Collection<Interceptor> getAll() {
        return interceptorJpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Interceptor get(String interceptorName) {
        return Optional.ofNullable(interceptorName)
                .flatMap(interceptorJpaRepository::findById)
                .map(mapper::toDomain)
                .orElseThrow(() -> new EntityNotFoundException(NOT_FOUND_MESSAGE_TEMPLATE.formatted(interceptorName)));
    }

    @Transactional
    public void create(Interceptor interceptor) {
        interceptorValidator.validateCreation(interceptor);
        assertNotExists(interceptor.getName());
        resolveEndpointsIfContainerSource(interceptor);
        Optional.of(interceptor)
                .map(domainModel -> mapper.toEntity(domainModel, new InterceptorEntity()))
                .ifPresent(interceptorJpaRepository::save);
    }

    @Transactional
    public void update(String interceptorName, Interceptor interceptor) {
        interceptorValidator.validateUpdate(interceptorName, interceptor);
        resolveEndpointsIfContainerSource(interceptor);
        InterceptorEntity interceptorEntity = interceptorJpaRepository.findById(interceptorName)
                .orElseThrow(() -> new EntityNotFoundException(NOT_FOUND_MESSAGE_TEMPLATE.formatted(interceptorName)));
        Optional.of(interceptor)
                .map(domainModel -> mapper.toEntity(domainModel, interceptorEntity))
                .ifPresent(interceptorJpaRepository::save);
    }

    @Transactional
    public void delete(String interceptorName) {
        assertExists(interceptorName);
        interceptorJpaRepository.deleteById(interceptorName);
    }

    @Transactional(readOnly = true)
    public boolean exists(String interceptorName) {
        return interceptorJpaRepository.existsById(interceptorName);
    }

    @Transactional(readOnly = true)
    public Interceptor getSnapshot(String interceptorName, Integer revision) {
        var entity = historyService.entitySnapshotAtRevision(revision, interceptorName, InterceptorEntity.class);
        return mapper.toDomain(entity);
    }

    @Transactional(readOnly = true)
    public Collection<Interceptor> getAllAtRevision(Integer revision) {
        return historyService.getEntitiesAtRevision(revision, InterceptorEntity.class)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Transactional
    public void refreshInterceptorEndpoints() {
        var interceptorEntities = interceptorJpaRepository.findByContainerIdIsNotNull();

        for (var interceptorEntity : interceptorEntities) {
            var interceptorContainerEntity = interceptorEntity.getInterceptorContainer();
            String containerId = interceptorContainerEntity.getContainerId();

            DeploymentInfoDto deploymentInfo = deploymentService.getById(containerId);
            validateDeploymentInfo(deploymentInfo, containerId);

            // TODO [VPA]: extract this logic
            String containerUrl = deploymentInfo.getUrl();
            String completionEndpoint = containerUrl
                    + Optional.ofNullable(interceptorContainerEntity.getCompletionEndpointPath()).orElse("");
            String configurationEndpoint = containerUrl
                    + Optional.ofNullable(interceptorContainerEntity.getConfigurationEndpointPath()).orElse("");

            interceptorEntity.setEndpoint(completionEndpoint);
            interceptorEntity.setConfigurationEndpoint(configurationEndpoint);
        }

        interceptorJpaRepository.saveAll(interceptorEntities);
    }

    // TODO [VPA]: use validator class
    private void validateDeploymentInfo(DeploymentInfoDto deploymentInfo, String containerId) {
        if (deploymentInfo == null) {
            throw new IllegalArgumentException("Container with ID '%s' not found in cache".formatted(containerId));
        }

        String deploymentUrl = deploymentInfo.getUrl();
        if (StringUtils.isBlank(deploymentUrl)) {
            throw new IllegalArgumentException(
                "Container URL is not present, please check if it is deployed. Container ID: %s".formatted(containerId)
            );
        }
    }

    private void resolveEndpointsIfContainerSource(Interceptor interceptor) {
        if (!(interceptor.getSource() instanceof InterceptorContainerSource containerSource)) {
            return;
        }

        // TODO [VPA]: extract this logic
        DeploymentInfoDto deploymentInfo = deploymentService.getById(containerSource.getContainerId());
        String containerUrl = deploymentInfo.getUrl();

        String completionEndpoint = containerUrl
                + Optional.ofNullable(containerSource.getCompletionEndpointPath()).orElse("");
        String configurationEndpoint = containerUrl
                + Optional.ofNullable(containerSource.getConfigurationEndpointPath()).orElse("");

        interceptor.setEndpoint(completionEndpoint);
        interceptor.setConfigurationEndpoint(configurationEndpoint);
    }

    private void assertExists(String name) {
        boolean exists = interceptorJpaRepository.existsById(name);
        if (!exists) {
            throw new EntityNotFoundException(NOT_FOUND_MESSAGE_TEMPLATE.formatted(name));
        }
    }

    private void assertNotExists(String name) {
        if (interceptorJpaRepository.existsById(name)) {
            throw new EntityAlreadyExistsException("Interceptor with name " + name + " already exists");
        }
    }
}
