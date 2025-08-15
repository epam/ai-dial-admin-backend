package com.epam.aidial.cfg.domain.mapper;

import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.epam.aidial.cfg.domain.model.ApplicationTypeSchema;
import com.epam.aidial.core.config.CoreApplicationTypeSchema;
import com.epam.aidial.core.config.CoreApplicationTypeSchemaRoute;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Mapper(componentModel = "spring", uses = ApplicationTypeSchemaRouteCoreMapper.class)
public abstract class ApplicationTypeSchemaCoreMapper {

    private final ObjectMapper objectMapper = JsonMapperConfiguration.createJsonMapper();

    @Autowired
    private ApplicationTypeSchemaRouteCoreMapper applicationTypeSchemaRouteCoreMapper;

    public String mapToCoreString(ApplicationTypeSchema applicationTypeSchema) {
        if (applicationTypeSchema == null) {
            return null;
        }
        var routes = applicationTypeSchemaRouteCoreMapper.map(applicationTypeSchema.getApplicationTypeRoutes());
        var typeSchema = mapToCoreApplicationTypeSchema(applicationTypeSchema, routes);
        return toApplicationTypeSchemaAsString(typeSchema);
    }

    protected Map<String, String> map(Map<String, ApplicationTypeSchema> schemas) {
        return schemas.entrySet().stream()
            .map(entry -> Pair.of(entry.getKey(), mapToCoreString(entry.getValue())))
            .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
    }

    @Mapping(target = "id", source = "applicationTypeSchema.schemaId")
    @Mapping(target = "applicationTypeRoutes", source = "routes")
    abstract CoreApplicationTypeSchema mapToCoreApplicationTypeSchema(ApplicationTypeSchema applicationTypeSchema,
                                                                      LinkedHashMap<String, CoreApplicationTypeSchemaRoute> routes);

    public ApplicationTypeSchema mapToSchema(String applicationTypeSchema) {
        if (StringUtils.isEmpty(applicationTypeSchema)) {
            return null;
        }
        CoreApplicationTypeSchema coreApplicationTypeSchema = toCoreApplicationTypeSchema(applicationTypeSchema);
        return mapToApplicationTypeSchema(coreApplicationTypeSchema);
    }

    @Mapping(target = "schemaId", source = "id")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "applications", ignore = true)
    @Mapping(target = "topics", ignore = true)
    abstract ApplicationTypeSchema mapToApplicationTypeSchema(CoreApplicationTypeSchema coreApplicationTypeSchema);

    private String toApplicationTypeSchemaAsString(CoreApplicationTypeSchema applicationTypeSchema) {
        if (applicationTypeSchema == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(applicationTypeSchema);
        } catch (JsonProcessingException e) {
            log.warn("Error converting CoreApplicationTypeSchema to JSON string: {}", applicationTypeSchema, e);
            throw new RuntimeException("Error converting CoreApplicationTypeSchema to JSON string", e);
        }
    }

    private CoreApplicationTypeSchema toCoreApplicationTypeSchema(String applicationTypeSchema) {
        if (StringUtils.isEmpty(applicationTypeSchema)) {
            return null;
        }
        try {
            return objectMapper.readValue(applicationTypeSchema, CoreApplicationTypeSchema.class);
        } catch (JsonProcessingException e) {
            log.warn("Error converting JSON to CoreApplicationTypeSchema: {}", applicationTypeSchema, e);
            throw new RuntimeException("Error converting JSON to CoreApplicationTypeSchema", e);
        }
    }

}
