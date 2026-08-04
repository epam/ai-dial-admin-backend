package com.epam.aidial.core.config.validation;

import com.epam.aidial.core.metaschemas.CatalogMetaSchemaHolder;
import com.networknt.schema.InputFormat;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class CatalogSchemaConformToMetaSchemaValidator {

    private static final JsonSchemaFactory SCHEMA_FACTORY = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
    private static final JsonSchema SCHEMA = SCHEMA_FACTORY.getSchema(CatalogMetaSchemaHolder.getCatalogMetaSchema());

    public static boolean isValid(String schema) {
        return getValidationMessages(schema).isEmpty();
    }

    public static Set<ValidationMessage> getValidationMessages(String schema) {
        return SCHEMA.validate(schema, InputFormat.JSON);
    }

    public static String getValidationErrors(String schema) {
        var validations = getValidationMessages(schema);
        if (validations.isEmpty()) {
            return null;
        }
        String validationsMessage = validations.stream()
                .map(ValidationMessage::getMessage)
                .collect(Collectors.joining(", "));

        String message = "ApplicationTypeSchema doesn't conform to meta schema: " + validationsMessage;
        log.warn(message);
        return message;
    }
}