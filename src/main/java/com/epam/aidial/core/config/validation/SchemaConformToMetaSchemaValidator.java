package com.epam.aidial.core.config.validation;

import com.epam.aidial.core.metaschemas.MetaSchemaHolder;
import com.networknt.schema.InputFormat;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import lombok.extern.slf4j.Slf4j;

import java.util.stream.Collectors;

@Slf4j
public class SchemaConformToMetaSchemaValidator {

    private static final JsonSchemaFactory SCHEMA_FACTORY = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
    private static final JsonSchema SCHEMA = SCHEMA_FACTORY.getSchema(MetaSchemaHolder.getCustomApplicationMetaSchema());

    public static boolean isValid(String schema) {
        var validations = SCHEMA.validate(schema, InputFormat.JSON);
        if (!validations.isEmpty()) {
            String validationsMessage = validations.stream()
                    .map(ValidationMessage::getMessage)
                    .collect(Collectors.joining(","));
            String message = "Application type schema doesn't conform to meta schema: %s".formatted(validationsMessage);
            log.warn(message);
            return false;
        }
        return true;
    }
}