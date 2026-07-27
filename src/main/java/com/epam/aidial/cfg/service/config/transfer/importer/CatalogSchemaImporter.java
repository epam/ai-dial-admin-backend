package com.epam.aidial.cfg.service.config.transfer.importer;

import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.epam.aidial.cfg.domain.model.CatalogSchema;
import com.epam.aidial.cfg.domain.model.ImportAction;
import com.epam.aidial.cfg.domain.model.ImportComponent;
import com.epam.aidial.cfg.domain.service.CatalogSchemaService;
import com.epam.aidial.cfg.dto.CatalogSchemaDto;
import com.epam.aidial.cfg.exception.EntityNotFoundException;
import com.epam.aidial.cfg.service.config.export.ConflictResolutionPolicy;
import com.epam.aidial.cfg.web.facade.mapper.CatalogSchemaDtoMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.epam.aidial.cfg.service.hashing.HashCalculator.ANY_HASH;

@Service
@Slf4j
public class CatalogSchemaImporter {

    private final CatalogSchemaService catalogSchemaService;
    private final CatalogSchemaDtoMapper catalogSchemaDtoMapper;
    private final ObjectMapper objectMapper = JsonMapperConfiguration.createJsonMapper();

    public CatalogSchemaImporter(CatalogSchemaService catalogSchemaService,
                                 CatalogSchemaDtoMapper catalogSchemaDtoMapper) {
        this.catalogSchemaService = catalogSchemaService;
        this.catalogSchemaDtoMapper = catalogSchemaDtoMapper;
    }

    public List<ImportComponent<CatalogSchema>> importCatalogSchemas(Map<String, String> catalogSchemas,
                                                                     ConflictResolutionPolicy resolutionPolicy) {
        if (MapUtils.isNotEmpty(catalogSchemas)) {
            Map<String, CatalogSchema> schemas = catalogSchemas.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, entry -> fromJson(entry.getValue())));
            return importAdminCatalogSchemas(schemas, resolutionPolicy);
        }
        return Collections.emptyList();
    }

    public List<ImportComponent<CatalogSchema>> importAdminCatalogSchemas(Map<String, CatalogSchema> catalogSchemas,
                                                                          ConflictResolutionPolicy resolutionPolicy) {
        if (MapUtils.isNotEmpty(catalogSchemas)) {
            return catalogSchemas.values().stream()
                    .map(catalogSchema -> importCatalogSchema(catalogSchema, resolutionPolicy))
                    .toList();
        }
        return Collections.emptyList();
    }

    private CatalogSchema fromJson(String jsonSchema) {
        try {
            CatalogSchemaDto dto = objectMapper.readValue(jsonSchema, CatalogSchemaDto.class);
            return catalogSchemaDtoMapper.toDomain(dto);
        } catch (Exception e) {
            log.error("Error parsing catalog schema JSON: {}", jsonSchema, e);
            throw new RuntimeException("Error parsing catalog schema JSON", e);
        }
    }

    private ImportComponent<CatalogSchema> importCatalogSchema(CatalogSchema catalogSchema,
                                                               ConflictResolutionPolicy resolutionPolicy) {
        String schemaId = catalogSchema.getSchemaId();
        CatalogSchema existing = null;

        try {
            existing = catalogSchemaService.get(schemaId);
        } catch (EntityNotFoundException e) {
            // Schema doesn't exist, will create it
        }

        if (existing == null) {
            catalogSchemaService.create(catalogSchema);
            CatalogSchema created = catalogSchemaService.get(schemaId);
            return new ImportComponent<>(ImportAction.CREATE, null, created);
        }

        if (resolutionPolicy == ConflictResolutionPolicy.SKIP) {
            return new ImportComponent<>(ImportAction.SKIP, existing, existing);
        }

        // OVERRIDE policy
        catalogSchemaService.update(schemaId, catalogSchema, ANY_HASH);
        CatalogSchema updated = catalogSchemaService.get(schemaId);
        return new ImportComponent<>(ImportAction.UPDATE, existing, updated);
    }

    public List<ImportComponent<CatalogSchema>> getActualImportedCatalogSchemas(List<ImportComponent<CatalogSchema>> importedSchemas) {
        return importedSchemas.stream()
                .map(component -> {
                    String schemaId = component.getNext() != null ? component.getNext().getSchemaId() : component.getPrev().getSchemaId();
                    CatalogSchema actualSchema = catalogSchemaService.get(schemaId);
                    return new ImportComponent<>(component.getImportAction(), component.getPrev(), actualSchema);
                })
                .toList();
    }
}
