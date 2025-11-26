package com.epam.aidial.core.config.validation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonMetaSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.NonValidationKeyword;
import com.networknt.schema.SpecVersion;
import lombok.Getter;

import java.util.Map;

import static com.epam.aidial.core.metaschemas.MetaSchemaHolder.getMetaschemaBuilder;

@Getter
public class CustomApplicationConformToTypeSchemaValidationContext {

    private static final JsonMetaSchema DIAL_META_SCHEMA = getMetaschemaBuilder()
            .keyword(new NonValidationKeyword("dial:meta"))
            .keyword(new NonValidationKeyword("dial:file"))
            .keyword(new NonValidationKeyword("dial:resource"))
            .build();

    private final JsonSchemaFactory schemaFactory;
    private final ObjectMapper mapper;

    public CustomApplicationConformToTypeSchemaValidationContext(Map<String, String> applicationTypeSchemas) {
        schemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7, builder ->
                builder.schemaLoaders(loaders -> loaders.schemas(applicationTypeSchemas))
                        .metaSchema(DIAL_META_SCHEMA)
        );
        mapper = new ObjectMapper();
    }
}
