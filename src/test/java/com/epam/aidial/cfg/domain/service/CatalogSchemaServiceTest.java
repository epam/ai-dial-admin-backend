package com.epam.aidial.cfg.domain.service;

import com.epam.aidial.cfg.dao.jpa.CatalogSchemaJpaRepository;
import com.epam.aidial.cfg.dao.mapper.CatalogSchemaEntityMapper;
import com.epam.aidial.cfg.dao.model.CatalogSchemaEntity;
import com.epam.aidial.cfg.domain.model.CatalogSchema;
import com.epam.aidial.cfg.domain.validator.CatalogSchemaValidator;
import com.epam.aidial.cfg.exception.EntityAlreadyExistsException;
import com.epam.aidial.cfg.exception.EntityNotFoundException;
import com.epam.aidial.cfg.exception.OptimisticLockConflictException;
import com.epam.aidial.cfg.service.hashing.HashCalculator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CatalogSchemaServiceTest {

    private static final String TEST_SCHEMA_ID = "test-schema-id";
    private static final String TEST_HASH = "test-hash";

    @Mock
    private CatalogSchemaJpaRepository jpaRepository;

    @Mock
    private CatalogSchemaEntityMapper mapper;

    @Mock
    private HistoryService historyService;

    @Mock
    private HashCalculator calculator;

    @Mock
    private CatalogSchemaValidator validator;

    @InjectMocks
    private CatalogSchemaService catalogSchemaService;

    @Test
    void testGetAll() {
        CatalogSchemaEntity entity = createEntity();
        CatalogSchema schema = createDomain();

        when(jpaRepository.findAll()).thenReturn(List.of(entity));
        when(mapper.toDomain(entity)).thenReturn(schema);

        var result = catalogSchemaService.getAll();

        assertEquals(1, result.size());
        verify(jpaRepository).findAll();
    }

    @Test
    void testGet() {
        CatalogSchemaEntity entity = createEntity();
        CatalogSchema schema = createDomain();

        when(jpaRepository.findById(TEST_SCHEMA_ID)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(schema);

        CatalogSchema result = catalogSchemaService.get(TEST_SCHEMA_ID);

        assertNotNull(result);
        assertEquals(TEST_SCHEMA_ID, result.getSchemaId());
    }

    @Test
    void testGetNotFound() {
        when(jpaRepository.findById(TEST_SCHEMA_ID)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> catalogSchemaService.get(TEST_SCHEMA_ID));
    }

    @Test
    void testGetSchemaWithHash() {
        CatalogSchemaEntity entity = createEntity();
        CatalogSchema schema = createDomain();

        when(jpaRepository.findById(TEST_SCHEMA_ID)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(schema);
        when(calculator.calculateHash(schema)).thenReturn(TEST_HASH);

        var result = catalogSchemaService.getSchemaWithHash(TEST_SCHEMA_ID);

        assertNotNull(result);
        assertEquals(TEST_HASH, result.hash());
        assertEquals(schema, result.model());
    }

    @Test
    void testCreate() {
        CatalogSchema schema = createDomain();
        CatalogSchemaEntity entity = createEntity();

        when(jpaRepository.existsById(TEST_SCHEMA_ID)).thenReturn(false);
        when(mapper.toEntity(any(), any(), any(), any(), any())).thenReturn(entity);
        when(jpaRepository.save(any(CatalogSchemaEntity.class))).thenReturn(entity);

        catalogSchemaService.create(schema);

        verify(jpaRepository).save(any(CatalogSchemaEntity.class));
    }

    @Test
    void testCreateAlreadyExists() {
        CatalogSchema schema = createDomain();

        when(jpaRepository.existsById(TEST_SCHEMA_ID)).thenReturn(true);

        assertThrows(EntityAlreadyExistsException.class, () -> catalogSchemaService.create(schema));
    }

    @Test
    void testUpdate() {
        CatalogSchemaEntity entity = createEntity();
        CatalogSchema schema = createDomain();
        CatalogSchema updatedSchema = createDomain();
        updatedSchema.setSchema("{\"updated\": true}");

        when(jpaRepository.findById(TEST_SCHEMA_ID)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(schema, updatedSchema);
        when(calculator.calculateHash(schema)).thenReturn(TEST_HASH);
        when(calculator.calculateHash(updatedSchema)).thenReturn("new-hash");
        when(mapper.toEntity(any(), any(), any(), any(), any())).thenReturn(entity);
        when(jpaRepository.save(entity)).thenReturn(entity);

        String newHash = catalogSchemaService.update(TEST_SCHEMA_ID, schema, TEST_HASH);

        assertEquals("new-hash", newHash);
        verify(jpaRepository).save(entity);
    }

    @Test
    void testUpdateWithHashMismatch() {
        CatalogSchemaEntity entity = createEntity();
        CatalogSchema schema = createDomain();

        when(jpaRepository.findById(TEST_SCHEMA_ID)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(schema);
        when(calculator.calculateHash(schema)).thenReturn(TEST_HASH);

        assertThrows(OptimisticLockConflictException.class,
                () -> catalogSchemaService.update(TEST_SCHEMA_ID, schema, "wrong-hash"));
    }

    @Test
    void testDelete() {
        when(jpaRepository.existsById(TEST_SCHEMA_ID)).thenReturn(true);

        catalogSchemaService.delete(TEST_SCHEMA_ID);

        verify(jpaRepository, times(1)).deleteById(TEST_SCHEMA_ID);
    }

    @Test
    void testDeleteNotFound() {
        when(jpaRepository.existsById(TEST_SCHEMA_ID)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> catalogSchemaService.delete(TEST_SCHEMA_ID));
    }

    @Test
    void testGetSnapshot() {
        CatalogSchemaEntity entity = createEntity();
        CatalogSchema schema = createDomain();

        when(historyService.entitySnapshotAtRevision(1, TEST_SCHEMA_ID, CatalogSchemaEntity.class)).thenReturn(entity);
        when(mapper.toDomain(entity)).thenReturn(schema);

        CatalogSchema result = catalogSchemaService.getSnapshot(TEST_SCHEMA_ID, 1);

        assertNotNull(result);
        assertEquals(TEST_SCHEMA_ID, result.getSchemaId());
    }

    @Test
    void testGetAllAtRevision() {
        CatalogSchemaEntity entity = createEntity();
        CatalogSchema schema = createDomain();

        when(historyService.getEntitiesAtRevision(1, CatalogSchemaEntity.class)).thenReturn(List.of(entity));
        when(mapper.toDomain(entity)).thenReturn(schema);

        var result = catalogSchemaService.getAllAtRevision(1);

        assertEquals(1, result.size());
    }

    private CatalogSchemaEntity createEntity() {
        CatalogSchemaEntity entity = new CatalogSchemaEntity();
        entity.setSchemaId(TEST_SCHEMA_ID);
        entity.setSchema("{\"type\": \"object\"}");
        entity.setCatalogEntityType(CatalogSchemaEntity.CatalogEntityTypeEntity.MODEL);
        entity.setCatalogDisplayName("Model Catalog");
        entity.setDefaultLocale("en");
        return entity;
    }

    private CatalogSchema createDomain() {
        CatalogSchema schema = new CatalogSchema();
        schema.setSchemaId(TEST_SCHEMA_ID);
        schema.setSchema("{\"type\": \"object\"}");
        schema.setCatalogEntityType(CatalogSchema.CatalogEntityType.MODEL);
        schema.setCatalogDisplayName("Model Catalog");
        schema.setDefaultLocale("en");
        return schema;
    }
}
