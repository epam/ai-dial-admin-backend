package com.epam.aidial.cfg.domain.validator;

import com.epam.aidial.cfg.domain.model.ApplicationTypeSchema;
import com.epam.aidial.cfg.domain.model.route.DependentRoute;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

@Slf4j
@Component
public class ApplicationTypeSchemaValidator {

    private final IdFieldValidator idFieldValidator;
    private final RouteValidator routeValidator;
    private final DisplayFieldsValidator displayFieldsValidator;

    private final String applicationTypeSchemaIdValidationPattern;

    public ApplicationTypeSchemaValidator(IdFieldValidator idFieldValidator,
                                          RouteValidator routeValidator,
                                          DisplayFieldsValidator displayFieldsValidator,
                                          @Value("${validation.applicationTypeSchema.id:}") String applicationTypeSchemaIdValidationPattern) {
        this.idFieldValidator = idFieldValidator;
        this.routeValidator = routeValidator;
        this.displayFieldsValidator = displayFieldsValidator;
        this.applicationTypeSchemaIdValidationPattern = applicationTypeSchemaIdValidationPattern;
    }

    public void validateCreation(ApplicationTypeSchema applicationTypeSchema) {
        final String schemaId = applicationTypeSchema.getSchemaId();

        idFieldValidator.validateId("ApplicationTypeSchema", schemaId, "schemaId");
        if (StringUtils.isEmpty(applicationTypeSchemaIdValidationPattern)) {
            log.debug("ApplicationTypeSchema id validation pattern is empty, skipping validation for schema id: {}", schemaId);
        } else if (!Pattern.matches(applicationTypeSchemaIdValidationPattern, schemaId)) {
            throw new IllegalArgumentException("ApplicationTypeSchema ID '" + schemaId
                    + "' does not match the required pattern: " + applicationTypeSchemaIdValidationPattern);
        }
        displayFieldsValidator.validateDisplayName(applicationTypeSchema.getApplicationTypeDisplayName());
        validateRoutes(applicationTypeSchema.getApplicationTypeRoutes());
    }

    public void validateUpdate(String schemaId, ApplicationTypeSchema applicationTypeSchema) {
        if (!Objects.equals(schemaId, applicationTypeSchema.getSchemaId())) {
            throw new IllegalArgumentException("Schema id can not be updated for application type schema "
                    + "with schema id: '" + schemaId + "'. New schema id: '" + applicationTypeSchema.getSchemaId() + "'");
        }
        displayFieldsValidator.validateDisplayName(applicationTypeSchema.getApplicationTypeDisplayName());
        validateRoutes(applicationTypeSchema.getApplicationTypeRoutes());
    }

    private void validateRoutes(List<DependentRoute> routes) {
        if (CollectionUtils.isNotEmpty(routes)) {
            routes.forEach(routeValidator::validateDependentRoute);
        }
    }
}
