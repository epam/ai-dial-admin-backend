package com.epam.aidial.cfg.domain.service;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dao.jpa.ApplicationJpaRepository;
import com.epam.aidial.cfg.dao.jpa.ApplicationTypeSchemaJpaRepository;
import com.epam.aidial.cfg.dao.mapper.ApplicationTypeSchemaEntityMapper;
import com.epam.aidial.cfg.dao.model.ApplicationEntity;
import com.epam.aidial.cfg.dao.model.ApplicationTypeSchemaEntity;
import com.epam.aidial.cfg.domain.model.ApplicationTypeSchema;
import com.epam.aidial.cfg.domain.model.DomainObjectWithHash;
import com.epam.aidial.cfg.domain.validator.ApplicationTypeSchemaValidator;
import com.epam.aidial.cfg.exception.EntityAlreadyExistsException;
import com.epam.aidial.cfg.exception.EntityNotFoundException;
import com.epam.aidial.cfg.exception.OptimisticLockConflictException;
import com.epam.aidial.cfg.service.hashing.HashCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.epam.aidial.cfg.service.hashing.HashCalculator.ANY_HASH;

@LogExecution
@Service("coreApplicationTypeSchemaService")
@RequiredArgsConstructor
@Slf4j
public class ApplicationTypeSchemaService {

    private static final String NOT_FOUND_MESSAGE_TEMPLATE = "Application type schema with schema id %s does not exist";

    private final ApplicationTypeSchemaJpaRepository jpaRepository;
    private final ApplicationTypeSchemaEntityMapper mapper;
    private final ApplicationJpaRepository applicationJpaRepository;
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
                .map(domainModel -> mapper.toEntity(domainModel, new ApplicationTypeSchemaEntity()))
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
        return jpaRepository.save(mapper.toEntity(schema, applicationTypeSchemaEntity));
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
    public void assertExists(String schemaId) {
        boolean exists = jpaRepository.existsById(schemaId);
        if (!exists) {
            throw new EntityNotFoundException(NOT_FOUND_MESSAGE_TEMPLATE.formatted(schemaId));
        }
    }

    @Transactional(readOnly = true)
    public ApplicationTypeSchema getSnapshot(String id, Integer revision) {
        var entity = historyService.entitySnapshotAtRevision(revision, id, ApplicationTypeSchemaEntity.class);
        return mapper.toDomain(entity);
    }

    @Transactional(readOnly = true)
    public Collection<ApplicationTypeSchema> getAllAtRevision(Integer revision) {
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
            log.debug("Optimistic lock conflict on update: schemaId={}, expectedHash={}, currentHash={}",
                    entity.getSchemaId(), expectedHash, currentHash);
            throw new OptimisticLockConflictException(String.format("Optimistic lock conflict on update: schemaId:'"
                    + "%s'. Reload the data.", entity.getSchemaId()));
        }
    }

    private ApplicationTypeSchemaEntity findBySchemaId(String schemaId) {
        return jpaRepository.findById(schemaId)
                .orElseThrow(() -> new EntityNotFoundException("Application type schema with schema id " + schemaId + " does not exist"));
    }

    private void removeApplicationsFromSchema(ApplicationTypeSchemaEntity schemaEntity, List<ApplicationEntity> applications) {
        applicationJpaRepository.deleteAll(applications);
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
}
