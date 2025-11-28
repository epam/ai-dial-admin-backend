package com.epam.aidial.cfg.web.facade.mapper;

import com.epam.aidial.cfg.domain.model.GlobalSettings;
import com.epam.aidial.cfg.dto.GlobalSettingsDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface GlobalSettingsDtoMapper {

    GlobalSettings toDomain(GlobalSettingsDto entity);

    GlobalSettingsDto toDto(GlobalSettings domain);
}