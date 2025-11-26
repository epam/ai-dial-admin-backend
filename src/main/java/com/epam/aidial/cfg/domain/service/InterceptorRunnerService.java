package com.epam.aidial.cfg.domain.service;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dao.jpa.InterceptorJpaRepository;
import com.epam.aidial.cfg.dao.jpa.InterceptorRunnerJpaRepository;
import com.epam.aidial.cfg.dao.mapper.InterceptorRunnerEntityMapper;
import com.epam.aidial.cfg.dao.model.FeaturesEntity;
import com.epam.aidial.cfg.dao.model.InterceptorEntity;
import com.epam.aidial.cfg.dao.model.InterceptorRunnerEntity;
import com.epam.aidial.cfg.domain.model.DomainObjectWithHash;
import com.epam.aidial.cfg.domain.model.InterceptorRunner;
import com.epam.aidial.cfg.domain.validator.InterceptorRunnerValidator;
import com.epam.aidial.cfg.exception.EntityAlreadyExistsException;
import com.epam.aidial.cfg.exception.EntityNotFoundException;
import com.epam.aidial.cfg.exception.OptimisticLockConflictException;
import com.epam.aidial.cfg.service.hashing.HashCalculator;
import com.google.api.client.util.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.SetUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.epam.aidial.cfg.service.hashing.HashCalculator.ANY_HASH;

@LogExecution
@Service("coreInterceptorRunnerService")
@RequiredArgsConstructor
@Slf4j
public class InterceptorRunnerService {

    private static final String NOT_FOUND_MESSAGE_TEMPLATE = "Interceptor Runner with name '%s' does not exist";

    private final InterceptorRunnerJpaRepository interceptorRunnerJpaRepository;
    private final InterceptorRunnerEntityMapper mapper;
    private final InterceptorRunnerValidator interceptorRunnerValidator;
    private final HistoryService historyService;
    private final InterceptorJpaRepository interceptorJpaRepository;
    private final HashCalculator calculator;

    @Transactional(readOnly = true)
    public Collection<InterceptorRunner> getAll() {
        return StreamSupport.stream(interceptorRunnerJpaRepository.findAll().spliterator(), false)
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Collection<InterceptorRunner> getAllByNames(List<String> names) {
        return StreamSupport.stream(interceptorRunnerJpaRepository.findAllById(names).spliterator(), false)
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public InterceptorRunner get(String interceptorRunnerName) {
        return tryGet(interceptorRunnerName)
                .orElseThrow(() -> new EntityNotFoundException(NOT_FOUND_MESSAGE_TEMPLATE.formatted(interceptorRunnerName)));
    }

    @Transactional(readOnly = true)
    public DomainObjectWithHash<InterceptorRunner> getInterceptorRunnerWithHash(String id) {
        var interceptorRunner = get(id);
        return new DomainObjectWithHash<>(interceptorRunner, calculator.calculateHash(interceptorRunner));
    }

    @Transactional(readOnly = true)
    public Optional<InterceptorRunner> tryGet(String interceptorRunnerName) {
        return Optional.ofNullable(interceptorRunnerName)
                .flatMap(interceptorRunnerJpaRepository::findById)
                .map(mapper::toDomain);
    }

    @Transactional
    public void create(InterceptorRunner interceptorRunner) {
        interceptorRunnerValidator.validateCreation(interceptorRunner);
        assertNotExists(interceptorRunner.getName());
        Optional.of(interceptorRunner)
                .map(domainModel -> toEntity(domainModel, new InterceptorRunnerEntity()))
                .ifPresent(interceptorRunnerJpaRepository::save);
    }

    @Transactional
    public void update(String interceptorRunnerName, InterceptorRunner interceptorRunner) {
        performUpdate(interceptorRunnerName, interceptorRunner, ANY_HASH);
    }

    @Transactional
    public String update(String interceptorRunnerName, InterceptorRunner interceptorRunner, String hash) {
        if (hash == null) {
            throw new IllegalArgumentException(String.format(
                    "Hash must not be null. Use \"*\" to skip optimistic check. InterceptorRunner:%s.", interceptorRunnerName));
        }
        var savedInterceptorRunner = performUpdate(interceptorRunnerName, interceptorRunner, hash);
        return calculator.calculateHash(mapper.toDomain(savedInterceptorRunner));
    }

    private InterceptorRunnerEntity performUpdate(String interceptorRunnerName, InterceptorRunner interceptorRunner, String hash) {
        interceptorRunnerValidator.validateUpdate(interceptorRunnerName, interceptorRunner);
        var interceptorRunnerEntity = findByInterceptorRunnerName(interceptorRunnerName);
        assertNotConcurrencyOverwrite(interceptorRunnerEntity, hash);
        return interceptorRunnerJpaRepository.save(toEntity(interceptorRunner, interceptorRunnerEntity));
    }

    @Transactional
    public void delete(String interceptorRunnerName, boolean removeInterceptor) {
        InterceptorRunnerEntity interceptorRunnerEntity = findByInterceptorRunnerName(interceptorRunnerName);
        List<InterceptorEntity> interceptors = interceptorRunnerEntity.getInterceptors();

        if (CollectionUtils.isEmpty(interceptors)) {
            interceptorRunnerJpaRepository.delete(interceptorRunnerEntity);
            return;
        }

        if (removeInterceptor) {
            removeInterceptorsFromRunner(interceptorRunnerEntity, interceptors);
        } else {
            detachInterceptorsFromRunner(interceptorRunnerEntity, interceptors);
        }
    }

    @Transactional(readOnly = true)
    public boolean exists(String interceptorRunnerName) {
        return interceptorRunnerJpaRepository.existsById(interceptorRunnerName);
    }

    @Transactional(readOnly = true)
    public InterceptorRunner getSnapshot(String interceptorRunnerName, Integer revision) {
        var entity = historyService.entitySnapshotAtRevision(revision, interceptorRunnerName, InterceptorRunnerEntity.class);
        return mapper.toDomain(entity);
    }

    @Transactional(readOnly = true)
    public Collection<InterceptorRunner> getAllAtRevision(Number revision) {
        return historyService.getEntitiesAtRevision(revision, InterceptorRunnerEntity.class)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    private void assertNotConcurrencyOverwrite(InterceptorRunnerEntity entity, String expectedHash) {
        if (ANY_HASH.equals(expectedHash)) {
            return;
        }
        var currentHash = calculator.calculateHash(mapper.toDomain(entity));
        if (!expectedHash.equals(currentHash)) {
            log.debug("Optimistic lock conflict on update: interceptorRunnerName={}, expectedHash={}, currentHash={}",
                    entity.getName(), expectedHash, currentHash);
            throw new OptimisticLockConflictException(String.format("Optimistic lock conflict on update: interceptorRunnerName:'"
                    + "%s'. Reload the data.", entity.getName()));
        }
    }

    @Transactional
    public void rollbackInterceptorRunners(Number revision) {
        Iterable<InterceptorEntity> interceptors = interceptorJpaRepository.findAll();
        interceptors.forEach(entity -> entity.setInterceptorRunner(null));
        interceptorJpaRepository.saveAllAndFlush(interceptors);

        Collection<InterceptorRunner> interceptorRunners = getAllAtRevision(revision);
        interceptorRunnerJpaRepository.deleteAllExcept(interceptorRunners.stream().map(InterceptorRunner::getName).toList());

        for (InterceptorRunner interceptorRunner : interceptorRunners) {
            InterceptorRunnerEntity entity = interceptorRunnerJpaRepository.findById(interceptorRunner.getName()).orElseGet(InterceptorRunnerEntity::new);
            InterceptorRunnerEntity interceptorRunnerEntity = toEntity(interceptorRunner, entity);
            interceptorRunnerJpaRepository.save(interceptorRunnerEntity);
        }
    }

    private InterceptorRunnerEntity findByInterceptorRunnerName(String interceptorRunnerName) {
        return interceptorRunnerJpaRepository.findById(interceptorRunnerName)
                .orElseThrow(() -> new EntityNotFoundException(NOT_FOUND_MESSAGE_TEMPLATE.formatted(interceptorRunnerName)));
    }

    private void assertNotExists(String name) {
        if (interceptorRunnerJpaRepository.existsById(name)) {
            throw new EntityAlreadyExistsException("Interceptor Runner with name '%s' already exists".formatted(name));
        }
    }

    private void removeInterceptorsFromRunner(InterceptorRunnerEntity interceptorRunnerEntity, List<InterceptorEntity> interceptors) {
        interceptorJpaRepository.deleteAll(interceptors);
        interceptorRunnerJpaRepository.delete(interceptorRunnerEntity);
    }

    private void detachInterceptorsFromRunner(InterceptorRunnerEntity interceptorRunnerEntity, List<InterceptorEntity> interceptors) {
        interceptors.forEach(interceptor -> {
            interceptor.setInterceptorRunner(null);
            interceptor.setEndpoint(interceptorRunnerEntity.getCompletionEndpoint());

            if (interceptor.getFeatures() == null) {
                interceptor.setFeatures(new FeaturesEntity());
            }
            interceptor.getFeatures().setConfigurationEndpoint(interceptorRunnerEntity.getConfigurationEndpoint());
        });
        interceptorRunnerJpaRepository.delete(interceptorRunnerEntity);
    }

    private InterceptorRunnerEntity toEntity(InterceptorRunner domain, InterceptorRunnerEntity entity) {
        List<InterceptorEntity> interceptors = findInterceptorsByNames(domain.getInterceptors());
        return mapper.toEntity(domain, entity, interceptors);
    }

    private List<InterceptorEntity> findInterceptorsByNames(List<String> names) {
        if (names == null) {
            return null;
        }

        if (names.isEmpty()) {
            return List.of();
        }

        List<InterceptorEntity> existingInterceptors = Lists.newArrayList(interceptorJpaRepository.findAllById(names));
        Set<String> existingInterceptorsNames = existingInterceptors.stream()
                .map(InterceptorEntity::getId)
                .collect(Collectors.toSet());

        Set<String> namesDiff = SetUtils.difference(new HashSet<>(names), existingInterceptorsNames);
        if (!namesDiff.isEmpty()) {
            throw new EntityNotFoundException("Unable to find Interceptors: " + namesDiff);
        }

        return existingInterceptors;
    }

}