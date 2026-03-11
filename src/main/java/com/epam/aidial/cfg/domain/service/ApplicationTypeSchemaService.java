package com.epam.aidial.cfg.domain.service;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dao.jpa.ApplicationJpaRepository;
import com.epam.aidial.cfg.dao.jpa.ApplicationTypeSchemaJpaRepository;
import com.epam.aidial.cfg.dao.jpa.InterceptorJpaRepository;
import com.epam.aidial.cfg.dao.mapper.ApplicationTypeSchemaEntityMapper;
import com.epam.aidial.cfg.dao.model.ApplicationEntity;
import com.epam.aidial.cfg.dao.model.ApplicationTypeSchemaEntity;
import com.epam.aidial.cfg.dao.model.InterceptorEntity;
import com.epam.aidial.cfg.domain.model.ApplicationTypeSchema;
import com.epam.aidial.cfg.domain.model.DomainObjectWithHash;
import com.epam.aidial.cfg.domain.validator.ApplicationTypeSchemaValidator;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.epam.aidial.cfg.service.hashing.HashCalculator.ANY_HASH;

@LogExecution
@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicationTypeSchemaService {

    private static final String NOT_FOUND_MESSAGE_TEMPLATE = "Application type schema with schema id %s does not exist";

    private final ApplicationTypeSchemaJpaRepository jpaRepository;
    private final ApplicationTypeSchemaEntityMapper mapper;
    private final ApplicationJpaRepository applicationJpaRepository;
    private final InterceptorJpaRepository interceptorJpaRepository;
    private final ApplicationTypeSchemaValidator applicationTypeSchemaValidator;
    private final HistoryService historyService;
    private final HashCalculator calculator;

    @Transactional(readOnly = true)
    public Collection<ApplicationTypeSchema> getAll() {
        return StreamSupport.stream(jpaRepository.findAll().spliterator(), false)
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Collection<ApplicationTypeSchema> getAllByIds(List<String> ids) {
        return StreamSupport.stream(jpaRepository.findAllById(ids).spliterator(), false)
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ApplicationTypeSchema get(String id) {
        return tryGet(id)
                .orElseThrow(() -> new EntityNotFoundException(NOT_FOUND_MESSAGE_TEMPLATE.formatted(id)));
    }

    @Transactional(readOnly = true)
    public Optional<ApplicationTypeSchema> tryGet(String id) {
        return Optional.ofNullable(id)
                .flatMap(jpaRepository::findById)
                .map(mapper::toDomain);
    }

    @Transactional(readOnly = true)
    public DomainObjectWithHash<ApplicationTypeSchema> getSchemaWithHash(String id) {
        var applicationTypeSchema = get(id);
        return new DomainObjectWithHash<>(applicationTypeSchema, calculator.calculateHash(applicationTypeSchema));
    }

    @Transactional
    public void create(ApplicationTypeSchema applicationTypeSchema) {
        applicationTypeSchemaValidator.validateCreation(applicationTypeSchema);
        assertNotExists(applicationTypeSchema.getSchemaId());
        Optional.of(applicationTypeSchema)
                .map(domainModel -> toEntity(domainModel, new ApplicationTypeSchemaEntity()))
                .ifPresent(jpaRepository::save);
    }

    @Transactional
    public void update(String schemaId, ApplicationTypeSchema schema) {
        performUpdate(schemaId, schema, ANY_HASH);
    }

    @Transactional
    public String update(String schemaId, ApplicationTypeSchema schema, String hash) {
        if (hash == null) {
            throw new IllegalArgumentException(String.format(
                    "Hash must not be null. Use \"*\" to skip optimistic check. Schema:%s.", schemaId));
        }
        var savedSchema = performUpdate(schemaId, schema, hash);
        return calculator.calculateHash(mapper.toDomain(savedSchema));
    }

    private ApplicationTypeSchemaEntity performUpdate(String schemaId, ApplicationTypeSchema schema, String hash) {
        applicationTypeSchemaValidator.validateUpdate(schemaId, schema);
        ApplicationTypeSchemaEntity applicationTypeSchemaEntity = findBySchemaId(schemaId);
        assertNotConcurrencyOverwrite(applicationTypeSchemaEntity, hash);
        return jpaRepository.save(toEntity(schema, applicationTypeSchemaEntity));
    }

    @Transactional
    public void delete(String id, boolean removeApplication) {
        ApplicationTypeSchemaEntity applicationTypeSchema = findBySchemaId(id);
        List<ApplicationEntity> applications = applicationTypeSchema.getApplications();

        if (CollectionUtils.isEmpty(applications)) {
            jpaRepository.delete(applicationTypeSchema);
            return;
        }

        if (removeApplication) {
            removeApplicationsFromSchema(applicationTypeSchema, applications);
        } else {
            detachApplicationsFromSchema(applicationTypeSchema, applications);
        }
    }

    @Transactional(readOnly = true)
    public boolean exists(String schemaId) {
        return jpaRepository.existsById(schemaId);
    }

    @Transactional(readOnly = true)
    public ApplicationTypeSchema getSnapshot(String id, Integer revision) {
        var entity = historyService.entitySnapshotAtRevision(revision, id, ApplicationTypeSchemaEntity.class);
        return mapper.toDomain(entity);
    }

    @Transactional(readOnly = true)
    public Collection<ApplicationTypeSchema> getAllAtRevision(Number revision) {
        return historyService.getEntitiesAtRevision(revision, ApplicationTypeSchemaEntity.class)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    private void assertNotConcurrencyOverwrite(ApplicationTypeSchemaEntity entity, String expectedHash) {
        if (ANY_HASH.equals(expectedHash)) {
            return;
        }
        var currentHash = calculator.calculateHash(mapper.toDomain(entity));
        if (!expectedHash.equals(currentHash)) {
            throw OptimisticLockConflictException.onUpdate("ApplicationTypeSchema", entity.getSchemaId(), expectedHash, currentHash);
        }
    }

    @Transactional
    public void rollbackApplicationTypeSchemas(Number revision) {
        Collection<ApplicationTypeSchema> applicationTypeSchemas = getAllAtRevision(revision);
        List<String> ids = applicationTypeSchemas.stream().map(ApplicationTypeSchema::getSchemaId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(ids)) {
            jpaRepository.deleteAll();
        } else {
            Iterable<ApplicationTypeSchemaEntity> applicationTypeSchemasToDelete = jpaRepository.findByIdNotIn(ids);
            jpaRepository.deleteAll(applicationTypeSchemasToDelete);
        }

        Set<String> allInterceptorNames = interceptorJpaRepository.findAllNames();
        for (ApplicationTypeSchema domain : applicationTypeSchemas) {
            domain.getInterceptors().removeIf(interceptor -> !allInterceptorNames.contains(interceptor));
            ApplicationTypeSchemaEntity entity = jpaRepository.findById(domain.getSchemaId()).orElseGet(ApplicationTypeSchemaEntity::new);
            ApplicationTypeSchemaEntity applicationTypeSchemaEntity = toEntity(domain, entity);
            jpaRepository.save(applicationTypeSchemaEntity);
        }
    }

    private ApplicationTypeSchemaEntity findBySchemaId(String schemaId) {
        return jpaRepository.findById(schemaId)
                .orElseThrow(() -> new EntityNotFoundException("Application type schema with schema id " + schemaId + " does not exist"));
    }

    private void removeApplicationsFromSchema(ApplicationTypeSchemaEntity schemaEntity, List<ApplicationEntity> applications) {
        // list copy is needed due to {@link ApplicationEntity#preRemove()} where list of schema applications is modified
        applicationJpaRepository.deleteAll(new ArrayList<>(applications));
        jpaRepository.delete(schemaEntity);
    }

    private void detachApplicationsFromSchema(ApplicationTypeSchemaEntity schemaEntity, List<ApplicationEntity> applications) {
        applications.forEach(app -> {
            app.setApplicationTypeSchema(null);
            app.setEndpoint(schemaEntity.getSchemaId());
        });
        jpaRepository.delete(schemaEntity);
    }

    private void assertNotExists(String schemaId) {
        if (jpaRepository.existsById(schemaId)) {
            throw new EntityAlreadyExistsException("Application type schema with schema id " + schemaId + " already exists");
        }
    }

    private ApplicationTypeSchemaEntity toEntity(ApplicationTypeSchema domain, ApplicationTypeSchemaEntity entity) {
        List<ApplicationEntity> applications = findApplicationsByNames(domain.getApplications());
        List<InterceptorEntity> interceptors = findInterceptorsByNames(domain.getInterceptors());
        return mapper.toEntity(domain, entity, applications, interceptors);
    }

    private List<ApplicationEntity> findApplicationsByNames(List<String> names) {
        if (names == null) {
            return null;
        }

        if (names.isEmpty()) {
            return List.of();
        }

        List<ApplicationEntity> existingApplications = Lists.newArrayList(applicationJpaRepository.findAllById(names));
        Set<String> existingApplicationsNames = existingApplications.stream()
                .map(ApplicationEntity::getDeploymentName)
                .collect(Collectors.toSet());

        Set<String> namesDiff = SetUtils.difference(new HashSet<>(names), existingApplicationsNames);
        if (!namesDiff.isEmpty()) {
            throw new EntityNotFoundException("Unable to find applications: " + namesDiff);
        }

        return existingApplications;
    }

    private List<InterceptorEntity> findInterceptorsByNames(List<String> names) {
        if (CollectionUtils.isEmpty(names)) {
            return List.of();
        }

        List<InterceptorEntity> existingInterceptors = Lists.newArrayList(interceptorJpaRepository.findAllById(names));
        Set<String> existingInterceptorsNames = existingInterceptors.stream()
                .map(InterceptorEntity::getName)
                .collect(Collectors.toSet());

        Set<String> namesDiff = SetUtils.difference(new HashSet<>(names), existingInterceptorsNames);
        if (!namesDiff.isEmpty()) {
            throw new EntityNotFoundException("Unable to find interceptors: " + namesDiff);
        }

        return existingInterceptors;
    }
}
