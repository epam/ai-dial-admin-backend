package com.epam.aidial.core.config.validation;

import com.epam.aidial.cfg.client.dto.ApplicationResourceDto;
import com.epam.aidial.cfg.domain.model.Application;
import com.epam.aidial.cfg.domain.model.source.ApplicationSchemaSource;
import com.epam.aidial.core.config.CoreApplication;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.ValidationMessage;

import java.net.URI;
import java.util.Map;
import java.util.Set;

public class CustomApplicationConformToTypeSchemaValidator {

    public static Set<ValidationMessage> validate(CoreApplication application,
                                                  CustomApplicationConformToTypeSchemaValidationContext validationContext) {
        return validateApplicationProperties(
                application.getApplicationProperties(),
                application.getApplicationTypeSchemaId(),
                validationContext
        );
    }

    public static Set<ValidationMessage> validate(Application application,
                                                  CustomApplicationConformToTypeSchemaValidationContext validationContext) {
        URI schemaId = application.getSource() instanceof ApplicationSchemaSource schemaSource
                ? schemaSource.getApplicationTypeSchemaId() : null;
        return validateApplicationProperties(
                application.getApplicationProperties(),
                schemaId,
                validationContext
        );
    }

    public static Set<ValidationMessage> validate(ApplicationResourceDto application,
                                                  CustomApplicationConformToTypeSchemaValidationContext validationContext) {
        String schemaIdAsString = application.getApplicationTypeSchemaId();
        if (schemaIdAsString == null) {
            return Set.of();
        }

        URI schemaId;
        try {
            schemaId = URI.create(schemaIdAsString);
        } catch (Exception e) {
            ValidationMessage validationMessage = ValidationMessage.builder().message(e.getMessage()).build();
            return Set.of(validationMessage);
        }

        return validateApplicationProperties(
                application.getApplicationProperties(),
                schemaId,
                validationContext
        );
    }

    private static Set<ValidationMessage> validateApplicationProperties(Map<String, Object> applicationProperties,
                                                                        URI schemaId,
                                                                        CustomApplicationConformToTypeSchemaValidationContext validationContext) {
        if (schemaId == null) {
            return Set.of();
        }

        if (applicationProperties == null) {
            return Set.of();
        }

        JsonSchemaFactory schemaFactory = validationContext.getSchemaFactory();
        ObjectMapper mapper = validationContext.getMapper();

        JsonNode applicationNode = mapper.valueToTree(applicationProperties);
        JsonSchema schema = schemaFactory.getSchema(schemaId);

        return schema.validate(applicationNode);
    }
}
