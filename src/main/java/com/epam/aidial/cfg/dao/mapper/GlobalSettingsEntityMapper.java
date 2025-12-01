package com.epam.aidial.cfg.dao.mapper;

import com.epam.aidial.cfg.dao.model.GlobalSettingsEntity;
import com.epam.aidial.cfg.domain.model.GlobalSettings;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public abstract class GlobalSettingsEntityMapper {

    public GlobalSettingsEntity toGlobalSettingsEntity(GlobalSettings globalSettings, GlobalSettingsEntity entity) {
        return update(globalSettings, entity);
    }

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "id", constant = "1")
    public abstract GlobalSettingsEntity update(GlobalSettings globalSettings, @MappingTarget GlobalSettingsEntity entity);

    public abstract GlobalSettings toDomain(GlobalSettingsEntity globalSettingsEntity);
}