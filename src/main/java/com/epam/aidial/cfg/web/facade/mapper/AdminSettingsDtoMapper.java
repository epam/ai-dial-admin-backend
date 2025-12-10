package com.epam.aidial.cfg.web.facade.mapper;

import com.epam.aidial.cfg.domain.model.AdminSettings;
import com.epam.aidial.cfg.dto.AdminSettingsDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AdminSettingsDtoMapper {

    AdminSettings toDomain(AdminSettingsDto adminSettingsDto);

    AdminSettingsDto toDto(AdminSettings adminSettings);
}
