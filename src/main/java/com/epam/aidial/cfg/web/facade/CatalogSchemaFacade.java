package com.epam.aidial.cfg.web.facade;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.model.CatalogSchema;
import com.epam.aidial.cfg.domain.service.CatalogSchemaService;
import com.epam.aidial.cfg.dto.CatalogSchemaDto;
import com.epam.aidial.cfg.dto.CoreWithDomainHash;
import com.epam.aidial.cfg.dto.DtoWithDomainHash;
import com.epam.aidial.cfg.dto.EntitySyncStateDto;
import com.epam.aidial.cfg.service.core.CoreCatalogSchemaService;
import com.epam.aidial.cfg.web.facade.mapper.CatalogSchemaDtoMapper;
import com.epam.aidial.cfg.web.facade.mapper.EntitySyncStateDtoMapper;
import com.epam.aidial.core.config.CoreCatalogSchema;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@LogExecution
public class CatalogSchemaFacade {

    private final CatalogSchemaService schemaService;
    private final CatalogSchemaDtoMapper mapper;
    private final CoreCatalogSchemaService coreSchemaService;
    private final EntitySyncStateDtoMapper entitySyncStateDtoMapper;

    public Collection<CatalogSchemaDto> getAll() {
        return schemaService.getAll()
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    public CatalogSchemaDto get(String id) {
        CatalogSchema catalogSchema = schemaService.get(id);
        return mapper.toDto(catalogSchema);
    }

    public DtoWithDomainHash<CatalogSchemaDto> getSchemaWithHash(String id) {
        var schemaWithHash = schemaService.getSchemaWithHash(id);
        return new DtoWithDomainHash<>(mapper.toDto(schemaWithHash.model()), schemaWithHash.hash());
    }

    public CoreWithDomainHash<CoreCatalogSchema> getCoreSchemaWithHash(String id) {
        return coreSchemaService.getCoreSchemaWithHash(id);
    }

    public EntitySyncStateDto getSyncState(String schemaId, String hash) {
        var syncState = coreSchemaService.getSyncState(schemaId, hash);
        return entitySyncStateDtoMapper.toDto(syncState);
    }

    public void create(CatalogSchemaDto schemaDto) {
        Optional.of(schemaDto)
                .map(mapper::toDomain)
                .ifPresent(schemaService::create);
    }

    public String update(String id, CatalogSchemaDto dto, String hash) {
        CatalogSchema value = mapper.toDomain(dto);
        return schemaService.update(id, value, hash);
    }

    public String updateCore(String id, @Valid CoreCatalogSchema coreCatalogSchema, String hash) {
        return coreSchemaService.updateSchema(id, coreCatalogSchema, hash);
    }

    public void delete(String schemaId) {
        schemaService.delete(schemaId);
    }

    public CatalogSchemaDto getSnapshot(String id, Integer revision) {
        CatalogSchema catalogSchema = schemaService.getSnapshot(id, revision);
        return mapper.toDto(catalogSchema);
    }

    public Collection<CatalogSchemaDto> getAllAtRevision(Integer revision) {
        return schemaService.getAllAtRevision(revision)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }
}
