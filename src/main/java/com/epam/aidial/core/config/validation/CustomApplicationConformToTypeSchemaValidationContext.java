package com.epam.aidial.core.config.validation;

import com.epam.aidial.core.config.Config;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonMetaSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.NonValidationKeyword;
import com.networknt.schema.SpecVersion;
import lombok.Getter;

import static com.epam.aidial.core.metaschemas.MetaSchemaHolder.getMetaschemaBuilder;

@Getter
public class CustomApplicationConformToTypeSchemaValidationContext {

    private static final JsonMetaSchema DIAL_META_SCHEMA = getMetaschemaBuilder()
            .keyword(new NonValidationKeyword("dial:meta"))
            .keyword(new NonValidationKeyword("dial:file"))
            .build();

    private final JsonSchemaFactory schemaFactory;
    private final ObjectMapper mapper;

    public CustomApplicationConformToTypeSchemaValidationContext(Config value) {
        schemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7, builder ->
                builder.schemaLoaders(loaders -> loaders.schemas(value.getApplicationTypeSchemas()))
                        .metaSchema(DIAL_META_SCHEMA)
        );
        mapper = new ObjectMapper();
    }
}
