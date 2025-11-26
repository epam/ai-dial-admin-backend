package com.epam.aidial.cfg.service.config.transfer.importer;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.mapper.ApplicationTypeSchemaCoreMapper;
import com.epam.aidial.cfg.domain.model.ApplicationTypeSchema;
import com.epam.aidial.cfg.domain.model.ImportAction;
import com.epam.aidial.cfg.domain.model.ImportComponent;
import com.epam.aidial.cfg.domain.service.ApplicationTypeSchemaService;
import com.epam.aidial.cfg.service.config.export.ConflictResolutionPolicy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Service;

import java.util.Collection;
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
@LogExecution
@RequiredArgsConstructor
public class ApplicationTypeSchemaImporter {

    private final ApplicationTypeSchemaService service;
    private final ApplicationTypeSchemaCoreMapper mapper;

    public Collection<ImportComponent<ApplicationTypeSchema>> importSchemas(Map<String, String> schemas,
                                                                            ConflictResolutionPolicy resolutionPolicy) {
        if (MapUtils.isNotEmpty(schemas)) {
            Map<String, ApplicationTypeSchema> applicationTypeSchemas = schemas.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, entry -> map(entry.getValue())));
            return importAdminSchemas(applicationTypeSchemas, resolutionPolicy);
        }
        return Collections.emptyList();
    }

    public Collection<ImportComponent<ApplicationTypeSchema>> importAdminSchemas(Map<String, ApplicationTypeSchema> schemas,
                                                                                 ConflictResolutionPolicy resolutionPolicy) {
        if (MapUtils.isNotEmpty(schemas)) {
            return schemas.entrySet().stream()
                    .map((schemaEntry) -> {
                        var schema = schemaEntry.getValue();
                        return process(schemaEntry.getKey(), schema, resolutionPolicy);
                    })
                    .toList();
        }
        return Collections.emptyList();
    }

    private ImportComponent<ApplicationTypeSchema> process(String schemaId,
                                                           ApplicationTypeSchema schema,
                                                           ConflictResolutionPolicy resolutionPolicy) {
        Optional<ApplicationTypeSchema> existingApplicationTypeSchema = service.tryGet(schemaId);
        if (existingApplicationTypeSchema.isPresent()) {
            ImportAction importAction = handleExisting(schema, resolutionPolicy, schemaId);
            return new ImportComponent<>(importAction, existingApplicationTypeSchema.get(), schema);
        } else {
            service.create(schema);
            return new ImportComponent<>(CREATE, null, schema);
        }
    }

    private ImportAction handleExisting(ApplicationTypeSchema newSchema, ConflictResolutionPolicy resolutionPolicy, String schemaId) {
        return switch (resolutionPolicy) {
            case SKIP -> SKIP; // Do nothing, the existing applicationTypeSchema will remain unchanged.
            case OVERRIDE -> {
                service.update(schemaId, newSchema);
                yield UPDATE;
            }
        };
    }

    private ApplicationTypeSchema map(String schema) {
        return mapper.mapToSchema(schema);
    }

    public List<ImportComponent<ApplicationTypeSchema>> getActualImportedApplicationTypeSchemas(Collection<ImportComponent<ApplicationTypeSchema>> importComponents) {
        List<String> ids = importComponents.stream()
                .map(ImportComponent::getNext)
                .map(ApplicationTypeSchema::getSchemaId)
                .toList();
        Map<String, ApplicationTypeSchema> importedApplicationTypeSchemasByIds = service.getAllByIds(ids)
                .stream()
                .collect(Collectors.toMap(ApplicationTypeSchema::getSchemaId, Function.identity()));

        return importComponents.stream()
                .map(importComponent -> {
                    var next = importedApplicationTypeSchemasByIds.get(importComponent.getNext().getSchemaId());
                    var prev = importComponent.getPrev();
                    clearTxDependentFields(next);
                    clearTxDependentFields(prev);
                    return new ImportComponent<>(importComponent.getImportAction(), prev, next);
                })
                .toList();
    }

    private void clearTxDependentFields(ApplicationTypeSchema applicationTypeSchema) {
        if (applicationTypeSchema != null) {
            applicationTypeSchema.setCreatedAt(null);
            applicationTypeSchema.setUpdatedAt(null);
        }
    }
}
