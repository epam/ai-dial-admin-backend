package com.epam.aidial.cfg.web.facade.mapper;

import com.epam.aidial.cfg.domain.model.ApplicationTypeSchema;
import com.epam.aidial.cfg.dto.ApplicationTypeSchemaDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {InstantMapper.class, RouteDtoMapper.class})
public interface ApplicationTypeSchemaDtoMapper {

    @Mapping(target = "schemaId", source = "id")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    ApplicationTypeSchema toDomain(ApplicationTypeSchemaDto dto);

    @Mapping(target = "id", source = "schemaId")
    ApplicationTypeSchemaDto toDto(ApplicationTypeSchema domain);
}
