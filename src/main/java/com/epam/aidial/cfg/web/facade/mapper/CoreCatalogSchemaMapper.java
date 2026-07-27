package com.epam.aidial.cfg.web.facade.mapper;

import com.epam.aidial.cfg.dto.CatalogSchemaDto;
import com.epam.aidial.core.config.CoreCatalogSchema;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {InstantMapper.class})
public interface CoreCatalogSchemaMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "topics", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    CatalogSchemaDto toDto(CoreCatalogSchema coreCatalogSchema);

    @Mapping(target = "id", source = "id")
    CoreCatalogSchema toCoreSchema(CatalogSchemaDto dto);
}
