package com.epam.aidial.core.config.validation;

import com.epam.aidial.core.metaschemas.MetaSchemaHolder;
import com.networknt.schema.Error;
import com.networknt.schema.InputFormat;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.SpecificationVersion;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class SchemaConformToMetaSchemaValidator {

    private static final SchemaRegistry SCHEMA_REGISTRY = SchemaRegistry.withDefaultDialect(
            SpecificationVersion.DRAFT_7, builder -> builder.schemaRegistryConfig(MetaSchemaHolder.SCHEMA_REGISTRY_CONFIG));
    private static final Schema SCHEMA = SCHEMA_REGISTRY.getSchema(MetaSchemaHolder.getCustomApplicationMetaSchema());

    public static boolean isValid(String schema) {
        return getValidationMessages(schema).isEmpty();
    }

    public static List<Error> getValidationMessages(String schema) {
        return SCHEMA.validate(schema, InputFormat.JSON);
    }


    public static String getValidationErrors(String schema) {
        var validations = getValidationMessages(schema);
        if (validations.isEmpty()) {
            return null;
        }
        String validationsMessage = validations.stream()
                .map(ValidationErrors::format)
                .collect(Collectors.joining(", "));

        String message = "ApplicationTypeSchema doesn't conform to meta schema: " + validationsMessage;
        log.warn(message);
        return message;
    }
}