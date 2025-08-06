package com.epam.aidial.cfg.domain.mapper;

import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.epam.aidial.cfg.domain.model.ApplicationTypeSchema;
import com.epam.aidial.cfg.domain.model.Route;
import com.epam.aidial.cfg.domain.service.RouteService;
import com.epam.aidial.cfg.dto.ApplicationTypeSchemaDto;
import com.epam.aidial.core.config.CoreApplicationTypeSchema;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Mapper(componentModel = "spring", uses = {RouteCoreMapper.class, RouteService.class})
public abstract class ApplicationTypeSchemaCoreMapper {

    private final ObjectMapper objectMapper = JsonMapperConfiguration.createJsonMapper();

    @Autowired
    private RouteService routeService;

    public String mapToCoreString(ApplicationTypeSchema applicationTypeSchema, List<Route> routes) {
        if (applicationTypeSchema == null) {
            return null;
        }

        var typeSchema = mapToCoreApplicationTypeSchema(applicationTypeSchema, routes);
        return toApplicationTypeSchemaAsString(typeSchema);
    }

    protected Map<String, String> map(Map<String, ApplicationTypeSchema> schemas) {
        return schemas.entrySet().stream()
            .map(entry -> {
                ApplicationTypeSchema schema = entry.getValue();
                List<Route> routes = (List<Route>) routeService.getAllById(schema.getRoutes());
                String schemaStr = mapToCoreString(schema, routes);
                return Pair.of(entry.getKey(), schemaStr);
            })
            .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
    }

    @Mapping(target = "id", source = "applicationTypeSchema.schemaId")
    @Mapping(target = "applicationTypeRoutes", source = "routes")
    abstract CoreApplicationTypeSchema mapToCoreApplicationTypeSchema(ApplicationTypeSchema applicationTypeSchema, List<Route> routes);

    public ApplicationTypeSchema mapToSchema(String applicationTypeSchema) {
        if (StringUtils.isEmpty(applicationTypeSchema)) {
            return null;
        }
        ApplicationTypeSchemaDto dto = toDto(applicationTypeSchema);
        return mapToApplicationTypeSchema(dto);
    }

    @Mapping(target = "schemaId", source = "id")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    abstract ApplicationTypeSchema mapToApplicationTypeSchema(ApplicationTypeSchemaDto dto);

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

    private ApplicationTypeSchemaDto toDto(String applicationTypeSchema) {
        if (StringUtils.isEmpty(applicationTypeSchema)) {
            return null;
        }
        try {
            return objectMapper.readValue(applicationTypeSchema, ApplicationTypeSchemaDto.class);
        } catch (JsonProcessingException e) {
            log.warn("Error converting JSON to ApplicationTypeSchemaDto: {}", applicationTypeSchema, e);
            throw new RuntimeException("Error converting JSON to ApplicationTypeSchemaDto", e);
        }
    }

}
