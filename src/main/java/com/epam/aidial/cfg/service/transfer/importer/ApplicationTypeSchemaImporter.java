package com.epam.aidial.cfg.service.transfer.importer;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.mapper.ApplicationTypeSchemaCoreMapper;
import com.epam.aidial.cfg.domain.model.ApplicationTypeSchema;
import com.epam.aidial.cfg.domain.model.ImportAction;
import com.epam.aidial.cfg.domain.model.ImportComponent;
import com.epam.aidial.cfg.domain.service.ApplicationTypeSchemaService;
import com.epam.aidial.cfg.service.export.ConflictResolutionPolicy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
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
                                                                            ConflictResolutionPolicy resolutionPolicy,
                                                                            boolean isPreview) {
        if (MapUtils.isNotEmpty(schemas)) {
            Map<String, ApplicationTypeSchema> applicationTypeSchemas = schemas.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, entry -> map(entry.getValue())));
            return importAdminSchemas(applicationTypeSchemas, resolutionPolicy, isPreview);
        }
        return Collections.emptyList();
    }

    public Collection<ImportComponent<ApplicationTypeSchema>> importAdminSchemas(Map<String, ApplicationTypeSchema> schemas,
                                                                                 ConflictResolutionPolicy resolutionPolicy,
                                                                                 boolean isPreview) {
        if (MapUtils.isNotEmpty(schemas)) {
            return schemas.entrySet().stream()
                    .map((schemaEntry) -> {
                        var schema = schemaEntry.getValue();
                        var importAction = process(schemaEntry.getKey(), schema, resolutionPolicy, isPreview);
                        return new ImportComponent<>(importAction, schema);
                    })
                    .toList();
        }
        return Collections.emptyList();
    }

    private ImportAction process(String schemaId,
                                 ApplicationTypeSchema schema,
                                 ConflictResolutionPolicy resolutionPolicy,
                                 boolean isPreview) {
        if (service.exists(schemaId)) {
            return handleExisting(schema, resolutionPolicy, schemaId, isPreview);
        } else {
            return create(schema, isPreview);
        }
    }

    private ImportAction handleExisting(ApplicationTypeSchema newSchema, ConflictResolutionPolicy resolutionPolicy, String schemaId, boolean isPreview) {
        switch (resolutionPolicy) {
            case SKIP -> {
                // Do nothing, the existing applicationTypeSchema will remain unchanged.
                return SKIP;
            }
            case OVERRIDE -> {
                if (!isPreview) {
                    service.update(schemaId, newSchema);
                }
                return UPDATE;
            }
            default -> throw new IllegalArgumentException("Unexpected resolutionPolicy: " + resolutionPolicy);
        }
    }

    private ImportAction create(ApplicationTypeSchema schema, boolean isPreview) {
        if (!isPreview) {
            service.create(schema);
        }
        return CREATE;
    }

    private ApplicationTypeSchema map(String schema) {
        return mapper.mapToSchema(schema);
    }
}
