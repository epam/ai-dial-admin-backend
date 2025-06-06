package com.epam.aidial.cfg.web.facade.mapper;

import com.epam.aidial.cfg.domain.model.ApplicationTypeSchema;
import com.epam.aidial.cfg.dto.ApplicationTypeSchemaDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ApplicationTypeSchemaDtoMapper {

    @Mapping(target = "schemaId", source = "id")
    ApplicationTypeSchema toDomain(ApplicationTypeSchemaDto dto);

    @Mapping(target = "id", source = "schemaId")
    ApplicationTypeSchemaDto toDto(ApplicationTypeSchema domain);
}
