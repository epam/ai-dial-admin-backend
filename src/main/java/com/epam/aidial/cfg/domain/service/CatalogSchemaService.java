package com.epam.aidial.cfg.domain.service;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dao.jpa.CatalogSchemaJpaRepository;
import com.epam.aidial.cfg.dao.mapper.CatalogSchemaEntityMapper;
import com.epam.aidial.cfg.dao.model.CatalogSchemaEntity;
import com.epam.aidial.cfg.domain.model.CatalogSchema;
import com.epam.aidial.cfg.domain.model.DomainObjectWithHash;
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
@Service
@RequiredArgsConstructor
@Slf4j
public class CatalogSchemaService {

    private static final String NOT_FOUND_MESSAGE_TEMPLATE = "Catalog schema with schema id %s does not exist";

    private final CatalogSchemaJpaRepository jpaRepository;
    private final CatalogSchemaEntityMapper mapper;
    private final HistoryService historyService;
    private final HashCalculator calculator;

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
        String schemaId = catalogSchema.getSchemaId();

        if (jpaRepository.existsById(schemaId)) {
            throw new EntityAlreadyExistsException("Catalog schema with id %s already exists".formatted(schemaId));
        }

        CatalogSchemaEntity entity = new CatalogSchemaEntity();
        entity.setSchemaId(schemaId);
        mapper.update(catalogSchema, entity);
        jpaRepository.save(entity);

        log.info("Created catalog schema with id: {}", schemaId);
    }

    @Transactional
    public String update(String id, CatalogSchema catalogSchema, String hash) {
        CatalogSchemaEntity entity = jpaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(NOT_FOUND_MESSAGE_TEMPLATE.formatted(id)));

        CatalogSchema existingSchema = mapper.toDomain(entity);
        String expectedHash = calculator.calculateHash(existingSchema);

        if (!ANY_HASH.equals(hash) && !expectedHash.equals(hash)) {
            throw new OptimisticLockConflictException("Hash mismatch for catalog schema with id: " + id);
        }

        mapper.update(catalogSchema, entity);
        jpaRepository.save(entity);

        CatalogSchema updated = mapper.toDomain(entity);
        String newHash = calculator.calculateHash(updated);

        log.info("Updated catalog schema with id: {}", id);
        return newHash;
    }

    @Transactional
    public void delete(String schemaId) {
        if (!jpaRepository.existsById(schemaId)) {
            throw new EntityNotFoundException(NOT_FOUND_MESSAGE_TEMPLATE.formatted(schemaId));
        }

        jpaRepository.deleteById(schemaId);
        log.info("Deleted catalog schema with id: {}", schemaId);
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

    private Optional<CatalogSchema> tryGet(String id) {
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
}
