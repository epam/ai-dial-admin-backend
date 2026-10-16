package com.epam.aidial.core.config.validation;

import com.epam.aidial.cfg.client.dto.ApplicationResourceDto;
import com.epam.aidial.cfg.domain.model.Application;
import com.epam.aidial.cfg.domain.model.source.ApplicationSchemaSource;
import com.epam.aidial.core.config.CoreApplication;
import com.networknt.schema.Error;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.SchemaRegistry;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.util.List;
import java.util.Map;

public class CustomApplicationConformToTypeSchemaValidator {

    public static List<Error> validate(CoreApplication application,
                                       CustomApplicationConformToTypeSchemaValidationContext validationContext) {
        return validateApplicationProperties(
                application.getApplicationProperties(),
                application.getApplicationTypeSchemaId(),
                validationContext
        );
    }

    public static List<Error> validate(Application application,
                                       CustomApplicationConformToTypeSchemaValidationContext validationContext) {
        URI schemaId = application.getSource() instanceof ApplicationSchemaSource schemaSource
                ? schemaSource.getApplicationTypeSchemaId() : null;
        return validateApplicationProperties(
                application.getApplicationProperties(),
                schemaId,
                validationContext
        );
    }

    public static List<Error> validate(ApplicationResourceDto application,
                                       CustomApplicationConformToTypeSchemaValidationContext validationContext) {
        String schemaIdAsString = application.getApplicationTypeSchemaId();
        if (schemaIdAsString == null) {
            return List.of();
        }

        URI schemaId;
        try {
            schemaId = URI.create(schemaIdAsString);
        } catch (Exception e) {
            Error error = Error.builder().message(e.getMessage()).build();
            return List.of(error);
        }

        return validateApplicationProperties(
                application.getApplicationProperties(),
                schemaId,
                validationContext
        );
    }

    private static List<Error> validateApplicationProperties(Map<String, Object> applicationProperties,
                                                             URI schemaId,
                                                             CustomApplicationConformToTypeSchemaValidationContext validationContext) {
        if (schemaId == null) {
            return List.of();
        }

        if (applicationProperties == null) {
            return List.of();
        }

        SchemaRegistry schemaRegistry = validationContext.getSchemaRegistry();
        ObjectMapper mapper = validationContext.getMapper();

        JsonNode applicationNode = mapper.valueToTree(applicationProperties);
        Schema schema = schemaRegistry.getSchema(SchemaLocation.of(schemaId.toString()));

        return schema.validate(applicationNode);
    }
}
