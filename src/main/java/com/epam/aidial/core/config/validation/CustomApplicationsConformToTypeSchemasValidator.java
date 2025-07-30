package com.epam.aidial.core.config.validation;

import com.epam.aidial.core.config.Config;
import com.epam.aidial.core.config.CoreApplication;
import com.networknt.schema.ValidationMessage;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.util.Map;
import java.util.Set;

@Slf4j
public class CustomApplicationsConformToTypeSchemasValidator implements ConstraintValidator<CustomApplicationsConformToTypeSchemas, Config> {

    @Override
    public boolean isValid(Config value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        var validationContext = new CustomApplicationConformToTypeSchemaValidationContext(value);

        for (Map.Entry<String, CoreApplication> entry : value.getApplications().entrySet()) {
            CoreApplication application = entry.getValue();
            Set<ValidationMessage> validationResults = CustomApplicationConformToTypeSchemaValidator.validate(
                    application,
                    validationContext
            );
            if (!validationResults.isEmpty()) {
                String logMessage = validationResults.stream()
                        .map(ValidationMessage::getMessage)
                        .reduce((a, b) -> a + ", " + b)
                        .orElse("Unknown validation error");
                URI schemaId = application.getApplicationTypeSchemaId();
                log.error("Application {} does not conform to schema {}: {}", entry.getKey(), schemaId, logMessage);
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                        .addPropertyNode("applications")
                        .addContainerElementNode(entry.getKey(), Map.class, 0)
                        .addConstraintViolation();
                return false;
            }
        }
        return true;
    }
}