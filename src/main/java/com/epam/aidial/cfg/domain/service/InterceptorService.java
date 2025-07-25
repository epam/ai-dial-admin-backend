package com.epam.aidial.cfg.domain.service;

import com.epam.aidial.cfg.dao.jpa.InterceptorJpaRepository;
import com.epam.aidial.cfg.dao.mapper.InterceptorEntityMapper;
import com.epam.aidial.cfg.dao.model.InterceptorEntity;
import com.epam.aidial.cfg.domain.model.Interceptor;
import com.epam.aidial.cfg.domain.model.source.InterceptorContainerSource;
import com.epam.aidial.cfg.domain.util.InterceptorEndpointUtil;
import com.epam.aidial.cfg.domain.validator.DeploymentInfoValidator;
import com.epam.aidial.cfg.domain.validator.InterceptorValidator;
import com.epam.aidial.cfg.exception.EntityAlreadyExistsException;
import com.epam.aidial.cfg.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service("coreInterceptorService")
@RequiredArgsConstructor
public class InterceptorService {

    private static final String NOT_FOUND_MESSAGE_TEMPLATE = "Interceptor with name '%s' does not exist";

    private final ExternalDeploymentService deploymentService;
    private final InterceptorRefreshService interceptorRefreshService;
    private final InterceptorJpaRepository interceptorJpaRepository;
    private final DeploymentInfoValidator deploymentInfoValidator;
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

    @Transactional(readOnly = true)
    public void refreshEndpoints() {
        var interceptorEntities = interceptorJpaRepository.findByContainerIdIsNotNull();
        List<String> successfulInterceptors = new ArrayList<>();
        List<String> failedInterceptors = new ArrayList<>();

        for (var interceptorEntity : interceptorEntities) {
            String interceptorName = interceptorEntity.getName();
            try {
                interceptorRefreshService.refreshEndpoints(interceptorEntity);
                successfulInterceptors.add(interceptorName);
            } catch (Exception e) {
                log.error("Failed to refresh endpoints for interceptor '{}'", interceptorName, e);
                failedInterceptors.add(interceptorName);
            }
        }

        if (!failedInterceptors.isEmpty()) {
            log.warn("Failed to refresh endpoints for {} interceptors: {}",
                    failedInterceptors.size(), String.join(", ", failedInterceptors));
        }

        if (!successfulInterceptors.isEmpty()) {
            log.debug("Successfully refreshed endpoints for {} interceptors: {}",
                    successfulInterceptors.size(), String.join(", ", successfulInterceptors));
        }
    }

    private void resolveEndpointsIfContainerSource(Interceptor interceptor) {
        if (!(interceptor.getSource() instanceof InterceptorContainerSource containerSource)) {
            return;
        }

        InterceptorEndpointUtil.processContainerEndpoints(
                deploymentService,
                deploymentInfoValidator,
                containerSource.getContainerId(),
                containerSource,
                InterceptorContainerSource::getCompletionEndpointPath,
                InterceptorContainerSource::getConfigurationEndpointPath,
                (target, endpoints) -> {
                    target.setEndpoint(endpoints[0]);
                    target.setConfigurationEndpoint(endpoints[1]);
                },
                interceptor
        );
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
