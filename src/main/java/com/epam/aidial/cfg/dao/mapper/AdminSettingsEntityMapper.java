package com.epam.aidial.cfg.dao.mapper;

import com.epam.aidial.cfg.dao.model.AdminSettingsEntity;
import com.epam.aidial.cfg.domain.model.AdminSettings;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public abstract class AdminSettingsEntityMapper {

    public abstract AdminSettings toDomain(AdminSettingsEntity adminSettingsEntity);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "id", ignore = true)
    public abstract AdminSettingsEntity toEntity(AdminSettings adminSettings, @MappingTarget AdminSettingsEntity adminSettingsEntity);
}
