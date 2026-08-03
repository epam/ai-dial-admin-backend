package com.epam.aidial.cfg.service.config.transfer.importer;

import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.epam.aidial.cfg.domain.mapper.CatalogSchemaCoreMapper;
import com.epam.aidial.cfg.domain.model.CatalogSchema;
import com.epam.aidial.cfg.domain.model.ImportAction;
import com.epam.aidial.cfg.domain.model.ImportComponent;
import com.epam.aidial.cfg.domain.service.CatalogSchemaService;
import com.epam.aidial.cfg.service.config.export.ConflictResolutionPolicy;
import com.epam.aidial.cfg.web.facade.mapper.CatalogSchemaDtoMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.epam.aidial.cfg.domain.model.ImportAction.CREATE;
import static com.epam.aidial.cfg.domain.model.ImportAction.SKIP;
import static com.epam.aidial.cfg.domain.model.ImportAction.UPDATE;

@Service
@Slf4j
public class CatalogSchemaImporter {

    private final CatalogSchemaService catalogSchemaService;
    private final CatalogSchemaDtoMapper catalogSchemaDtoMapper;
    private final CatalogSchemaCoreMapper mapper;
    private final ObjectMapper objectMapper = JsonMapperConfiguration.createJsonMapper();

    public CatalogSchemaImporter(CatalogSchemaService catalogSchemaService,
                                 CatalogSchemaDtoMapper catalogSchemaDtoMapper, CatalogSchemaCoreMapper mapper) {
        this.catalogSchemaService = catalogSchemaService;
        this.catalogSchemaDtoMapper = catalogSchemaDtoMapper;
        this.mapper = mapper;
    }

    public List<ImportComponent<CatalogSchema>> importCatalogSchemas(Map<String, String> catalogSchemas,
                                                                     ConflictResolutionPolicy resolutionPolicy) {
        if (MapUtils.isNotEmpty(catalogSchemas)) {
            Map<String, CatalogSchema> schemas = catalogSchemas.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, entry -> map(entry.getValue())));
            return importAdminCatalogSchemas(schemas, resolutionPolicy);
        }
        return Collections.emptyList();
    }

    public List<ImportComponent<CatalogSchema>> importAdminCatalogSchemas(Map<String, CatalogSchema> catalogSchemas,
                                                                          ConflictResolutionPolicy resolutionPolicy) {
        if (MapUtils.isNotEmpty(catalogSchemas)) {
            return catalogSchemas.entrySet().stream()
                    .map((schemaEntry) -> {
                        var schema = schemaEntry.getValue();
                        return process(schemaEntry.getKey(), schema, resolutionPolicy);
                    })
                    .toList();
        }
        return Collections.emptyList();
    }

    private CatalogSchema map(String schema) {
        return mapper.mapToSchema(schema);
    }

    private ImportComponent<CatalogSchema> process(String schemaId,
                                                   CatalogSchema schema,
                                                   ConflictResolutionPolicy resolutionPolicy) {
        Optional<CatalogSchema> existingCatalogSchema = catalogSchemaService.tryGet(schemaId);
        if (existingCatalogSchema.isPresent()) {
            ImportAction importAction = handleExisting(schema, resolutionPolicy, schemaId);
            return new ImportComponent<>(importAction, existingCatalogSchema.get(), schema);
        } else {
            catalogSchemaService.create(schema);
            return new ImportComponent<>(CREATE, null, schema);
        }
    }

    private ImportAction handleExisting(CatalogSchema newSchema, ConflictResolutionPolicy resolutionPolicy, String schemaId) {
        return switch (resolutionPolicy) {
            case SKIP -> SKIP; // Do nothing, the existing applicationTypeSchema will remain unchanged.
            case OVERRIDE -> {
                catalogSchemaService.update(schemaId, newSchema);
                yield UPDATE;
            }
        };
    }

    public List<ImportComponent<CatalogSchema>> getActualImportedCatalogSchemas(List<ImportComponent<CatalogSchema>> importedSchemas) {
        List<String> ids = importedSchemas.stream()
                .map(ImportComponent::getNext)
                .map(CatalogSchema::getSchemaId)
                .toList();
        Map<String, CatalogSchema> importedCatalogSchemaByIds = catalogSchemaService.getAllByIds(ids)
                .stream()
                .collect(Collectors.toMap(CatalogSchema::getSchemaId, Function.identity()));

        return importedSchemas.stream()
                .map(importComponent -> {
                    var next = importedCatalogSchemaByIds.get(importComponent.getNext().getSchemaId());
                    var prev = importComponent.getPrev();
                    clearTxDependentFields(next);
                    clearTxDependentFields(prev);
                    return new ImportComponent<>(importComponent.getImportAction(), prev, next);
                })
                .toList();
    }

    private void clearTxDependentFields(CatalogSchema catalogSchema) {
        if (catalogSchema != null) {
            catalogSchema.setCreatedAt(null);
            catalogSchema.setUpdatedAt(null);
        }
    }
}
