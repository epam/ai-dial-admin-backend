package com.epam.aidial.cfg.domain.service;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dao.jpa.ApplicationJpaRepository;
import com.epam.aidial.cfg.dao.jpa.CatalogSchemaJpaRepository;
import com.epam.aidial.cfg.dao.jpa.ModelJpaRepository;
import com.epam.aidial.cfg.dao.jpa.ToolSetJpaRepository;
import com.epam.aidial.cfg.dao.mapper.CatalogSchemaEntityMapper;
import com.epam.aidial.cfg.dao.model.ApplicationEntity;
import com.epam.aidial.cfg.dao.model.CatalogSchemaEntity;
import com.epam.aidial.cfg.dao.model.ModelEntity;
import com.epam.aidial.cfg.dao.model.ToolSetEntity;
import com.epam.aidial.cfg.domain.model.CatalogSchema;
import com.epam.aidial.cfg.domain.model.DomainObjectWithHash;
import com.epam.aidial.cfg.domain.validator.CatalogSchemaValidator;
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
@Service
@RequiredArgsConstructor
@Slf4j
public class CatalogSchemaService {

    private static final String NOT_FOUND_MESSAGE_TEMPLATE = "Catalog schema with schema id %s does not exist";

    private final CatalogSchemaJpaRepository jpaRepository;
    private final CatalogSchemaEntityMapper mapper;
    private final HistoryService historyService;
    private final HashCalculator calculator;
    private final ApplicationJpaRepository applicationJpaRepository;
    private final ModelJpaRepository modelJpaRepository;
    private final ToolSetJpaRepository toolSetJpaRepository;
    private final CatalogSchemaValidator catalogSchemaValidator;

    @Transactional(readOnly = true)
    public Collection<CatalogSchema> getAll() {
        return StreamSupport.stream(jpaRepository.findAll().spliterator(), false)
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Collection<CatalogSchema> getAllByIds(List<String> ids) {
        return StreamSupport.stream(jpaRepository.findAllById(ids).spliterator(), false)
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CatalogSchema get(String id) {
        return tryGet(id)
                .orElseThrow(() -> new EntityNotFoundException(NOT_FOUND_MESSAGE_TEMPLATE.formatted(id)));
    }

    @Transactional(readOnly = true)
    public DomainObjectWithHash<CatalogSchema> getSchemaWithHash(String id) {
        CatalogSchema catalogSchema = get(id);
        String hash = calculator.calculateHash(catalogSchema);
        return new DomainObjectWithHash<>(catalogSchema, hash);
    }

    @Transactional
    public void create(CatalogSchema catalogSchema) {
        catalogSchemaValidator.validateCreation(catalogSchema);
        assertNotExists(catalogSchema.getSchemaId());
        Optional.of(catalogSchema)
                .map(domainModel -> toEntity(domainModel, new CatalogSchemaEntity()))
                .ifPresent(jpaRepository::save);
    }

    @Transactional
    public void update(String schemaId, CatalogSchema schema) {
        update(schemaId, schema, ANY_HASH);
    }

    @Transactional
    public String update(String schemaId, CatalogSchema catalogSchema, String hash) {
        if (hash == null) {
            throw new IllegalArgumentException(String.format(
                    "Hash must not be null. Use \"*\" to skip optimistic check. Schema:%s.", schemaId));
        }
        var savedSchema = performUpdate(schemaId, catalogSchema, hash);
        return calculator.calculateHash(mapper.toDomain(savedSchema));
    }

    private CatalogSchemaEntity performUpdate(String schemaId, CatalogSchema schema, String hash) {
        catalogSchemaValidator.validateUpdate(schemaId, schema);
        CatalogSchemaEntity catalogSchemaEntity = findBySchemaId(schemaId);
        assertNotConcurrencyOverwrite(catalogSchemaEntity, hash);
        return jpaRepository.save(toEntity(schema, catalogSchemaEntity));
    }

    @Transactional
    public void delete(String schemaId) {
        if (!jpaRepository.existsById(schemaId)) {
            throw new EntityNotFoundException(NOT_FOUND_MESSAGE_TEMPLATE.formatted(schemaId));
        }
        jpaRepository.deleteById(schemaId);
    }

    @Transactional(readOnly = true)
    public CatalogSchema getSnapshot(String id, Integer revision) {
        CatalogSchemaEntity entity = historyService.entitySnapshotAtRevision(revision, id, CatalogSchemaEntity.class);
        return mapper.toDomain(entity);
    }

    @Transactional(readOnly = true)
    public Collection<CatalogSchema> getAllAtRevision(Number revision) {
        List<CatalogSchemaEntity> entities = historyService.getEntitiesAtRevision(revision, CatalogSchemaEntity.class);
        return entities.stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    private void assertNotConcurrencyOverwrite(CatalogSchemaEntity entity, String expectedHash) {
        if (ANY_HASH.equals(expectedHash)) {
            return;
        }
        var currentHash = calculator.calculateHash(mapper.toDomain(entity));
        if (!expectedHash.equals(currentHash)) {
            throw OptimisticLockConflictException.onUpdate("CatalogSchemaEntity", entity.getSchemaId(), expectedHash, currentHash);
        }
    }

    @Transactional(readOnly = true)
    public Optional<CatalogSchema> tryGet(String id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Transactional
    public void rollbackCatalogSchemas(Number revision) {
        Collection<CatalogSchema> catalogSchemas = getAllAtRevision(revision);
        List<String> ids = catalogSchemas.stream().map(CatalogSchema::getSchemaId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(ids)) {
            jpaRepository.deleteAll();
        } else {
            Iterable<CatalogSchemaEntity> catalogSchemasToDelete = jpaRepository.findByIdNotIn(ids);
            jpaRepository.deleteAll(catalogSchemasToDelete);
        }
    }

    private void assertNotExists(String schemaId) {
        if (jpaRepository.existsById(schemaId)) {
            throw new EntityAlreadyExistsException("Catalog schema with schema id " + schemaId + " already exists");
        }
    }

    private CatalogSchemaEntity findBySchemaId(String schemaId) {
        return jpaRepository.findById(schemaId)
                .orElseThrow(() -> new EntityNotFoundException("Catalog with schema id " + schemaId + " does not exist"));
    }

    private CatalogSchemaEntity toEntity(CatalogSchema domain, CatalogSchemaEntity entity) {
        List<ApplicationEntity> applications = findApplicationsByNames(domain.getApplications());
        List<ModelEntity> models = findModelsByNames(domain.getModels());
        List<ToolSetEntity> toolSets = findToolSetsByNames(domain.getToolSets());
        return mapper.toEntity(domain, entity, applications, models, toolSets);
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

    private List<ModelEntity> findModelsByNames(List<String> names) {
        if (CollectionUtils.isEmpty(names)) {
            return List.of();
        }

        List<ModelEntity> existingModels = Lists.newArrayList(modelJpaRepository.findAllById(names));
        Set<String> existingModelsNames = existingModels.stream()
                .map(ModelEntity::getDeploymentName)
                .collect(Collectors.toSet());

        Set<String> namesDiff = SetUtils.difference(new HashSet<>(names), existingModelsNames);
        if (!namesDiff.isEmpty()) {
            throw new EntityNotFoundException("Unable to find interceptors: " + namesDiff);
        }

        return existingModels;
    }

    private List<ToolSetEntity> findToolSetsByNames(List<String> names) {
        if (CollectionUtils.isEmpty(names)) {
            return List.of();
        }

        List<ToolSetEntity> existingToolSets = Lists.newArrayList(toolSetJpaRepository.findAllById(names));
        Set<String> existingToolSetsNames = existingToolSets.stream()
                .map(ToolSetEntity::getDeploymentName)
                .collect(Collectors.toSet());

        Set<String> namesDiff = SetUtils.difference(new HashSet<>(names), existingToolSetsNames);
        if (!namesDiff.isEmpty()) {
            throw new EntityNotFoundException("Unable to find interceptors: " + namesDiff);
        }

        return existingToolSets;
    }
}
