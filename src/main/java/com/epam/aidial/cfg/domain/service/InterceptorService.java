package com.epam.aidial.cfg.domain.service;

import com.epam.aidial.cfg.dao.jpa.InterceptorJpaRepository;
import com.epam.aidial.cfg.dao.mapper.InterceptorEntityMapper;
import com.epam.aidial.cfg.dao.model.InterceptorEntity;
import com.epam.aidial.cfg.domain.model.DomainObjectWithHash;
import com.epam.aidial.cfg.domain.model.Interceptor;
import com.epam.aidial.cfg.domain.model.source.InterceptorContainerSource;
import com.epam.aidial.cfg.domain.util.ContainerEndpointResolver;
import com.epam.aidial.cfg.domain.validator.InterceptorValidator;
import com.epam.aidial.cfg.exception.EntityAlreadyExistsException;
import com.epam.aidial.cfg.exception.EntityNotFoundException;
import com.epam.aidial.cfg.exception.OptimisticLockConflictException;
import com.epam.aidial.cfg.service.hashing.HashCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.epam.aidial.cfg.service.hashing.HashCalculator.ANY_HASH;

@Slf4j
@Service("coreInterceptorService")
@RequiredArgsConstructor
public class InterceptorService {

    private static final String NOT_FOUND_MESSAGE_TEMPLATE = "Interceptor with name '%s' does not exist";

    private final ContainerEndpointResolver endpointResolver;
    private final InterceptorRefreshService interceptorRefreshService;
    private final InterceptorJpaRepository interceptorJpaRepository;
    private final InterceptorValidator interceptorValidator;
    private final InterceptorEntityMapper mapper;
    private final HistoryService historyService;
    private final HashCalculator calculator;

    @Transactional(readOnly = true)
    public Collection<Interceptor> getAll() {
        return interceptorJpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Collection<Interceptor> getAllByNames(List<String> names) {
        return interceptorJpaRepository.findAllById(names).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Interceptor get(String interceptorName) {
        return tryGet(interceptorName)
                .orElseThrow(() -> new EntityNotFoundException(NOT_FOUND_MESSAGE_TEMPLATE.formatted(interceptorName)));
    }

    @Transactional(readOnly = true)
    public Optional<Interceptor> tryGet(String interceptorName) {
        return Optional.ofNullable(interceptorName)
                .flatMap(interceptorJpaRepository::findById)
                .map(mapper::toDomain);
    }

    @Transactional(readOnly = true)
    public DomainObjectWithHash<Interceptor> getInterceptorWithHash(String interceptorName) {
        var interceptor = get(interceptorName);
        return new DomainObjectWithHash<>(interceptor, calculator.calculateHash(interceptor));
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
        performUpdate(interceptorName, interceptor, ANY_HASH);
    }

    @Transactional
    public String update(String interceptorName, Interceptor interceptor, String hash) {
        if (hash == null) {
            throw new IllegalArgumentException(String.format(
                    "Hash must not be null. Use \"*\" to skip optimistic check. Interceptor:%s.", interceptorName));
        }
        var savedInterceptor = performUpdate(interceptorName, interceptor, hash);
        return calculator.calculateHash(mapper.toDomain(savedInterceptor));
    }

    private InterceptorEntity performUpdate(String interceptorName, Interceptor interceptor, String hash) {
        interceptorValidator.validateUpdate(interceptorName, interceptor);
        resolveEndpointsIfContainerSource(interceptor);
        var interceptorEntity = interceptorJpaRepository.findById(interceptorName)
                .orElseThrow(() -> new EntityNotFoundException(NOT_FOUND_MESSAGE_TEMPLATE.formatted(interceptorName)));
        assertNotConcurrencyOverwrite(interceptorEntity, hash);
        return interceptorJpaRepository.save(mapper.toEntity(interceptor, interceptorEntity));
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

    private void assertNotConcurrencyOverwrite(InterceptorEntity entity, String expectedHash) {
        if (ANY_HASH.equals(expectedHash)) {
            return;
        }
        var currentHash = calculator.calculateHash(mapper.toDomain(entity));
        if (!expectedHash.equals(currentHash)) {
            log.debug("Optimistic lock conflict on update: interceptorName={}, expectedHash={}, currentHash={}",
                    entity.getName(), expectedHash, currentHash);
            throw new OptimisticLockConflictException(String.format("Optimistic lock conflict on update: interceptorName:'"
                            + "%s'. Reload the data.", entity.getName()));
        }
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
        if (!(interceptor.getSource() instanceof InterceptorContainerSource)) {
            return;
        }
        endpointResolver.processContainerEndpoints(interceptor);
    }

    @Transactional(readOnly = true)
    public void assertExists(String name) {
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
