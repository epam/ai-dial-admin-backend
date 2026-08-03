package com.epam.aidial.cfg.domain.validator;

import com.epam.aidial.cfg.domain.model.CatalogSchema;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.regex.Pattern;

@Slf4j
@Component
public class CatalogSchemaValidator {
    private final IdFieldValidator idFieldValidator;
    private final DisplayFieldsValidator displayFieldsValidator;
    private final String catalogSchemaIdValidationPattern;

    public CatalogSchemaValidator(IdFieldValidator idFieldValidator,
                                  DisplayFieldsValidator displayFieldsValidator,
                                  @Value("${validation.catalogSchema.id:}") String catalogSchemaIdValidationPattern) {
        this.idFieldValidator = idFieldValidator;
        this.displayFieldsValidator = displayFieldsValidator;
        this.catalogSchemaIdValidationPattern = catalogSchemaIdValidationPattern;
    }

    public void validateCreation(CatalogSchema catalogSchema) {
        final String schemaId = catalogSchema.getSchemaId();

        idFieldValidator.validateId("CatalogSchema", schemaId, "schemaId");
        if (StringUtils.isEmpty(catalogSchemaIdValidationPattern)) {
            log.debug("CatalogSchema id validation pattern is empty, skipping validation for schema id: {}", schemaId);
        } else if (!Pattern.matches(catalogSchemaIdValidationPattern, schemaId)) {
            throw new IllegalArgumentException("CatalogSchema ID '" + schemaId
                    + "' does not match the required pattern: " + catalogSchemaIdValidationPattern);
        }
        displayFieldsValidator.validateDisplayName(catalogSchema.getCatalogDisplayName(), "CatalogSchema", schemaId);
    }

    public void validateUpdate(String schemaId, CatalogSchema catalogSchema) {
        if (!Objects.equals(schemaId, catalogSchema.getSchemaId())) {
            throw new IllegalArgumentException("Schema id can not be updated for catalog schema "
                    + "with schema id: '" + schemaId + "'. New schema id: '" + catalogSchema.getSchemaId() + "'");
        }
        displayFieldsValidator.validateDisplayName(catalogSchema.getCatalogDisplayName(), "CatalogSchema", schemaId);
    }

}
