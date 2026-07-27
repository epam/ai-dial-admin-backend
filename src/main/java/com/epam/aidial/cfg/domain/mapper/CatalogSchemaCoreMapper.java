package com.epam.aidial.cfg.domain.mapper;

import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.epam.aidial.cfg.domain.model.CatalogSchema;
import com.epam.aidial.core.config.CoreCatalogSchema;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Mapper(componentModel = "spring")
public abstract class CatalogSchemaCoreMapper {

    private final ObjectMapper objectMapper = JsonMapperConfiguration.createJsonMapper();

    public String mapToCoreString(CatalogSchema catalogSchema) {
        if (catalogSchema == null) {
            return null;
        }
        var coreSchema = mapToCoreCatalogSchema(catalogSchema);
        return toCatalogSchemaAsString(coreSchema);
    }

    protected Map<String, String> map(Map<String, CatalogSchema> schemas) {
        if (schemas == null) {
            return null;
        }
        return schemas.entrySet().stream()
                .map(entry -> Pair.of(entry.getKey(), mapToCoreString(entry.getValue())))
                .collect(Collectors.toMap(
                        Pair::getLeft,
                        Pair::getRight,
                        (a, b) -> {
                            throw new IllegalStateException("Duplicated catalog schemas found: %s".formatted(a));
                        },
                        LinkedHashMap::new
                ));
    }

    @Mapping(target = "id", source = "schemaId")
    public abstract CoreCatalogSchema mapToCoreCatalogSchema(CatalogSchema catalogSchema);

    public CatalogSchema mapToSchema(String catalogSchema) {
        if (StringUtils.isEmpty(catalogSchema)) {
            return null;
        }
        CoreCatalogSchema coreCatalogSchema = toCoreCatalogSchema(catalogSchema);
        return mapToCatalogSchema(coreCatalogSchema);
    }

    @Mapping(target = "schemaId", source = "id")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "topics", ignore = true)
    abstract CatalogSchema mapToCatalogSchema(CoreCatalogSchema coreCatalogSchema);

    public String toCatalogSchemaAsString(CoreCatalogSchema catalogSchema) {
        if (catalogSchema == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(catalogSchema);
        } catch (JsonProcessingException e) {
            log.warn("Error converting CoreCatalogSchema to JSON string: {}", catalogSchema, e);
            throw new RuntimeException("Error converting CoreCatalogSchema to JSON string", e);
        }
    }

    private CoreCatalogSchema toCoreCatalogSchema(String catalogSchema) {
        if (StringUtils.isEmpty(catalogSchema)) {
            return null;
        }
        try {
            return objectMapper.readValue(catalogSchema, CoreCatalogSchema.class);
        } catch (JsonProcessingException e) {
            log.warn("Error converting JSON to CoreCatalogSchema: {}", catalogSchema, e);
            throw new RuntimeException("Error converting JSON to CoreCatalogSchema", e);
        }
    }
}
