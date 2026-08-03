package com.epam.aidial.core.config.validation;

import com.epam.aidial.core.metaschemas.CatalogMetaSchemaHolder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonMetaSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.NonValidationKeyword;
import com.networknt.schema.SpecVersion;
import lombok.Getter;

import java.util.Map;

@Getter
public class CatalogSchemaValidationContext {

    private static final JsonMetaSchema CATALOG_DIAL_META_SCHEMA = CatalogMetaSchemaHolder.getMetaschemaBuilder()
            .keyword(new NonValidationKeyword("dial:file"))
            .build();

    private final JsonSchemaFactory schemaFactory;
    private final ObjectMapper mapper;

    public CatalogSchemaValidationContext(Map<String, String> catalogSchemas) {
        schemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7, builder ->
                builder.schemaLoaders(loaders -> loaders.schemas(catalogSchemas))
                        .metaSchema(CATALOG_DIAL_META_SCHEMA)
        );
        mapper = new ObjectMapper();
    }
}
