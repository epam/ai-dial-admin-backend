package com.epam.aidial.core.config.validation;

import com.epam.aidial.core.metaschemas.MetaSchemaHolder;
import com.networknt.schema.InputFormat;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;

public class SchemaConformToMetaSchemaValidator {

    private static final JsonSchemaFactory SCHEMA_FACTORY = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
    private static final JsonSchema SCHEMA = SCHEMA_FACTORY.getSchema(MetaSchemaHolder.getCustomApplicationMetaSchema());

    public static boolean isValid(String schema) {
        return SCHEMA.validate(schema, InputFormat.JSON).isEmpty();
    }
}