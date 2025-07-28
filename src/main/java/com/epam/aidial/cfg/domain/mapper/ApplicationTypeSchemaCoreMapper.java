package com.epam.aidial.cfg.domain.mapper;

import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.epam.aidial.cfg.domain.model.ApplicationTypeSchema;
import com.epam.aidial.cfg.dto.ApplicationTypeSchemaDto;
import com.epam.aidial.cfg.web.facade.mapper.InstantMapper;
import com.epam.aidial.core.config.CoreApplicationTypeSchema;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Slf4j
@Mapper(componentModel = "spring")
public abstract class ApplicationTypeSchemaCoreMapper {

    private final ObjectMapper objectMapper = JsonMapperConfiguration.createJsonMapper();

    public String mapToCoreString(ApplicationTypeSchema applicationTypeSchema) {
        if (applicationTypeSchema == null) {
            return null;
        }

        var typeSchema = mapToCoreApplicationTypeSchema(applicationTypeSchema);
        return toApplicationTypeSchemaAsString(typeSchema);
    }

    @Mapping(target = "id", source = "schemaId")
    abstract CoreApplicationTypeSchema mapToCoreApplicationTypeSchema(ApplicationTypeSchema applicationTypeSchema);

    public ApplicationTypeSchema mapToSchema(String applicationTypeSchema) {
        if (StringUtils.isEmpty(applicationTypeSchema)) {
            return null;
        }

        ApplicationTypeSchemaDto dto = toDto(applicationTypeSchema);
        Long createdAt = null;
        Long updatedAt = null;

        if (dto != null) {
            createdAt = dto.getCreatedAt() != null ? InstantMapper.mapInstantToLong(dto.getCreatedAt()) : null;
            updatedAt = dto.getCreatedAt() != null ? InstantMapper.mapInstantToLong(dto.getCreatedAt()) : null;
        }

        return mapToApplicationTypeSchema(dto, createdAt, updatedAt);
    }

    @Mapping(target = "schemaId", source = "dto.id")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    abstract ApplicationTypeSchema mapToApplicationTypeSchema(ApplicationTypeSchemaDto dto, Long createdAt, Long updatedAt);

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
