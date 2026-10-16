package com.epam.aidial.core.config.validation;

import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.dialect.Dialect;
import com.networknt.schema.dialect.Dialects;
import com.networknt.schema.keyword.NonValidationKeyword;
import lombok.Getter;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Map;

import static com.epam.aidial.core.metaschemas.MetaSchemaHolder.SCHEMA_REGISTRY_CONFIG;
import static com.epam.aidial.core.metaschemas.MetaSchemaHolder.getDialectBuilder;

@Getter
public class CustomApplicationConformToTypeSchemaValidationContext {

    private static final Dialect DIAL_DIALECT = getDialectBuilder()
            .keyword(new NonValidationKeyword("dial:meta"))
            .keyword(new NonValidationKeyword("dial:file"))
            .keyword(new NonValidationKeyword("dial:resource"))
            .build();

    private final SchemaRegistry schemaRegistry;
    private final ObjectMapper mapper;

    public CustomApplicationConformToTypeSchemaValidationContext(Map<String, String> applicationTypeSchemas) {
        schemaRegistry = SchemaRegistry.withDialects(
                List.of(Dialects.getDraft7(), DIAL_DIALECT),
                builder -> builder.schemas(applicationTypeSchemas).schemaRegistryConfig(SCHEMA_REGISTRY_CONFIG)
        );
        mapper = JsonMapper.builder().build();
    }
}
