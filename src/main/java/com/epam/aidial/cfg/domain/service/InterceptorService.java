package com.epam.aidial.cfg.domain.service;

import com.epam.aidial.cfg.dao.jpa.ApplicationJpaRepository;
import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dao.jpa.InterceptorJpaRepository;
import com.epam.aidial.cfg.dao.jpa.InterceptorRunnerJpaRepository;
import com.epam.aidial.cfg.dao.jpa.ModelJpaRepository;
import com.epam.aidial.cfg.dao.mapper.InterceptorContainerEntityMapper;
import com.epam.aidial.cfg.dao.mapper.InterceptorEntityMapper;
import com.epam.aidial.cfg.dao.model.ApplicationEntity;
import com.epam.aidial.cfg.dao.model.InterceptorContainerEntity;
import com.epam.aidial.cfg.dao.model.InterceptorEntity;
import com.epam.aidial.cfg.domain.model.DomainObjectWithHash;
import com.epam.aidial.cfg.dao.model.InterceptorRunnerEntity;
import com.epam.aidial.cfg.dao.model.ModelEntity;
import com.epam.aidial.cfg.domain.model.Interceptor;
import com.epam.aidial.cfg.domain.model.source.InterceptorContainerSource;
import com.epam.aidial.cfg.domain.model.source.InterceptorRunnerSource;
import com.epam.aidial.cfg.domain.model.source.InterceptorSource;
import com.epam.aidial.cfg.domain.util.ContainerEndpointResolver;
import com.epam.aidial.cfg.domain.validator.InterceptorValidator;
import com.epam.aidial.cfg.exception.EntityAlreadyExistsException;
import com.epam.aidial.cfg.exception.EntityNotFoundException;
import com.google.api.client.util.Lists;
import com.epam.aidial.cfg.exception.OptimisticLockConflictException;
import com.epam.aidial.cfg.service.hashing.HashCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.epam.aidial.cfg.service.hashing.HashCalculator.ANY_HASH;

@LogExecution
@Slf4j
@Service
@RequiredArgsConstructor
public class InterceptorService {

    private static final String NOT_FOUND_MESSAGE_TEMPLATE = "Interceptor with name '%s' does not exist";

    private final ContainerEndpointResolver endpointResolver;
    private final InterceptorRefreshService interceptorRefreshService;
    private final InterceptorJpaRepository interceptorJpaRepository;
    private final ApplicationJpaRepository applicationJpaRepository;
    private final ModelJpaRepository modelJpaRepository;
    private final InterceptorRunnerJpaRepository interceptorRunnerJpaRepository;
    private final InterceptorValidator interceptorValidator;
    private final InterceptorEntityMapper mapper;
    private final InterceptorContainerEntityMapper interceptorContainerEntityMapper;
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
                .map(domainModel -> toEntity(domainModel, new InterceptorEntity()))
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
        return interceptorJpaRepository.save(toEntity(interceptor, interceptorEntity));
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
    public Collection<Interceptor> getAllAtRevision(Number revision) {
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

    @Transactional
    public void rollbackInterceptors(Number revision) {
        Collection<Interceptor> interceptors = getAllAtRevision(revision);
        interceptorJpaRepository.deleteAllExcept(interceptors.stream().map(Interceptor::getName).toList());

        for (Interceptor interceptor : interceptors) {
            InterceptorEntity entity = interceptorJpaRepository.findById(interceptor.getName()).orElseGet(InterceptorEntity::new);
            InterceptorEntity interceptorEntity = toEntity(interceptor, entity);
            interceptorJpaRepository.save(interceptorEntity);
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

    private InterceptorEntity toEntity(Interceptor domain, InterceptorEntity entity) {
        Pair<List<ApplicationEntity>, List<ModelEntity>> applicationsAndModels = findApplicationsAndModelsByNames(domain.getEntities());

        InterceptorRunnerEntity interceptorRunner = null;
        InterceptorContainerEntity interceptorContainer = null;

        InterceptorSource source = domain.getSource();
        if (source != null) {
            if (source instanceof InterceptorRunnerSource runnerSource) {
                interceptorRunner = findInterceptorRunnerEntityByName(runnerSource.getRunnerName());
            } else if (source instanceof InterceptorContainerSource containerSource) {
                interceptorContainer = interceptorContainerEntityMapper.toEntity(containerSource);
            }
        }

        return mapper.toEntity(domain, entity, applicationsAndModels.getLeft(), applicationsAndModels.getRight(),
                interceptorRunner, interceptorContainer);

    }

    private Pair<List<ApplicationEntity>, List<ModelEntity>> findApplicationsAndModelsByNames(List<String> names) {
        if (CollectionUtils.isEmpty(names)) {
            return Pair.of(List.of(), List.of());
        }

        List<ApplicationEntity> applications = Lists.newArrayList(applicationJpaRepository.findAllById(names));
        Set<String> existingApplications = applications.stream()
                .map(application -> application.getDeployment().getName())
                .collect(Collectors.toSet());
        List<ModelEntity> models = Lists.newArrayList(modelJpaRepository.findAllById(names));
        Set<String> existingModels = models.stream()
                .map(model -> model.getDeployment().getName())
                .collect(Collectors.toSet());

        Set<String> namesDiff = SetUtils.difference(new HashSet<>(names), SetUtils.union(existingApplications, existingModels));
        if (!namesDiff.isEmpty()) {
            throw new EntityNotFoundException("unable to find neither applications nor models: " + namesDiff);
        }

        return Pair.of(applications, models);
    }

    private InterceptorRunnerEntity findInterceptorRunnerEntityByName(String name) {
        if (StringUtils.isBlank(name)) {
            return null;
        }
        return interceptorRunnerJpaRepository.findById(name)
                .orElseThrow(() -> new EntityNotFoundException("Unable to find Interceptor Runner with name: '%s'".formatted(name)));
    }
}
