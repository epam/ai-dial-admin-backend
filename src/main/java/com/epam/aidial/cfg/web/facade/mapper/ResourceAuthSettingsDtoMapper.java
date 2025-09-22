package com.epam.aidial.cfg.web.facade.mapper;

import com.epam.aidial.cfg.domain.model.ResourceAuthSettings;
import com.epam.aidial.cfg.dto.ResourceAuthSettingsDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ResourceAuthSettingsDtoMapper {

    ResourceAuthSettingsDto toDto(ResourceAuthSettings domain);

    ResourceAuthSettings toDomain(ResourceAuthSettingsDto dto);
}
