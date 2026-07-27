package com.epam.aidial.cfg.web.facade.mapper;

import com.epam.aidial.cfg.domain.model.CatalogSchema;
import com.epam.aidial.cfg.dto.CatalogSchemaDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {InstantMapper.class})
public interface CatalogSchemaDtoMapper {

    @Mapping(target = "id", source = "schemaId")
    CatalogSchemaDto toDto(CatalogSchema domain);

    @Mapping(target = "schemaId", source = "id")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    CatalogSchema toDomain(CatalogSchemaDto dto);
}
