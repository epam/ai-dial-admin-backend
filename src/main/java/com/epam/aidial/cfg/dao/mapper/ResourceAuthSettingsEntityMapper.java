package com.epam.aidial.cfg.dao.mapper;

import com.epam.aidial.cfg.dao.model.ResourceAuthSettingsEntity;
import com.epam.aidial.cfg.domain.model.ResourceAuthSettings;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ResourceAuthSettingsEntityMapper {

    ResourceAuthSettingsEntity toEntity(ResourceAuthSettings domain);

    ResourceAuthSettings toDomain(ResourceAuthSettingsEntity entity);
}
