package com.epam.aidial.cfg.web.facade.mapper;

import com.epam.aidial.cfg.domain.model.ResourceAuthSettings;
import com.epam.aidial.cfg.dto.ResourceAuthSettingsDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ResourceAuthSettingsDtoMapper {

    @Mapping(target = "globalAuthStatus", ignore = true)
    @Mapping(target = "userLevelAuthStatus", ignore = true)
    @Mapping(target = "appLevelAuthStatus", ignore = true)
    ResourceAuthSettingsDto toDto(ResourceAuthSettings domain);

    ResourceAuthSettings toDomain(ResourceAuthSettingsDto dto);
}
