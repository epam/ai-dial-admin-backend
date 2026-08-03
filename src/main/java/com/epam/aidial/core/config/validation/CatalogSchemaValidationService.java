package com.epam.aidial.core.config.validation;

import com.epam.aidial.cfg.domain.mapper.CatalogSchemaCoreMapper;
import com.epam.aidial.cfg.domain.service.CatalogSchemaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CatalogSchemaValidationService {
    private final CatalogSchemaService catalogSchemaService;
    private final CatalogSchemaCoreMapper catalogSchemaCoreMapper;

    public void validateCatalogProperties(URI schemaId,
                                          Map<String, Object> catalogProperties,
                                          String deploymentName,
                                          String deploymentType,
                                          Map<String, String> catalogSchemas) {
        if (schemaId == null) {
            return;
        }
        String schemaIdAsString = schemaId.toString();
        String configSchema = catalogSchemas.get(schemaIdAsString);
        CatalogSchemaValidationContext validationCtx = getValidationContext(configSchema, schemaIdAsString);
        var validationMessages = CatalogSchemaValidator.validate(catalogProperties,
                schemaId, validationCtx);
        if (!validationMessages.isEmpty()) {
            throw new IllegalArgumentException(deploymentType + ": " + deploymentName + " doesn't conform to its catalog schema. Details: " + validationMessages);
        }
    }

    private CatalogSchemaValidationContext getValidationContext(String configSchema, String schemaIdAsString) {
        if (configSchema != null) {
            return new CatalogSchemaValidationContext(Map.of(schemaIdAsString, configSchema));
        } else {
            var schema = catalogSchemaService.get(schemaIdAsString);
            var schemaAsCoreString = catalogSchemaCoreMapper.mapToCoreString(schema);
            return new CatalogSchemaValidationContext(Map.of(schemaIdAsString, schemaAsCoreString));
        }
    }
}
