package com.epam.aidial.cfg.web.facade.mapper;

import com.epam.aidial.cfg.domain.model.ToolSet;
import com.epam.aidial.cfg.dto.ResourceAuthSettingsDto;
import com.epam.aidial.cfg.dto.ToolSetDto;
import com.epam.aidial.cfg.model.ResourceAuthSettings;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", uses = {
        LimitDtoMapper.class, RoleBasedDtoMapper.class, InstantMapper.class,
        ResourceAuthSettingsDtoMapper.class, ToolSetSourceDtoMapper.class
})
public abstract class ToolSetDtoMapper {

    @Autowired
    private ResourceAuthSettingsDtoMapper resourceAuthSettingsDtoMapper;

    @RoleBasedDtoMapper.ToDomain
    @Mapping(target = "deployment.name", source = "name")
    @Mapping(target = "deployment.authSettings", source = "authSettings")
    @Mapping(target = "deployment.forwardPerRequestKey", source = "forwardPerRequestKey")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    public abstract ToolSet toDomain(ToolSetDto dto);

    @RoleBasedDtoMapper.ToDto
    @Mapping(target = "name", source = "deployment.name")
    @Mapping(target = "authSettings", source = "deployment.authSettings")
    @Mapping(target = "forwardPerRequestKey", source = "deployment.forwardPerRequestKey")
    public abstract ToolSetDto toDto(ToolSet domain);

    @RoleBasedDtoMapper.ToDto
    @Mapping(target = "name", source = "deployment.name")
    @Mapping(target = "authSettings", source = "domain", qualifiedByName = "toResourceAuthSettingsDto")
    @Mapping(target = "forwardPerRequestKey", source = "deployment.forwardPerRequestKey")
    public abstract ToolSetDto toDto(ToolSet domain, @Context ResourceAuthSettings resourceAuthSettings);

    @Named("toResourceAuthSettingsDto")
    ResourceAuthSettingsDto toResourceAuthSettingsDto(ToolSet domain, @Context ResourceAuthSettings resourceAuthSettings) {
        ResourceAuthSettingsDto dto = resourceAuthSettingsDtoMapper.toDto(domain.getDeployment().getAuthSettings());

        if (resourceAuthSettings == null) {
            return dto;
        }

        var globalAuthStatus = mapResourceAuthStatus(resourceAuthSettings.getGlobalAuthStatus());
        var userLevelAuthStatus = mapResourceAuthStatus(resourceAuthSettings.getUserLevelAuthStatus());
        var appLevelAuthStatus = mapResourceAuthStatus(resourceAuthSettings.getAppLevelAuthStatus());

        if (dto == null && globalAuthStatus == null && userLevelAuthStatus == null && appLevelAuthStatus == null) {
            return null;
        }

        if (dto == null) {
            dto = new ResourceAuthSettingsDto();
        }

        dto.setGlobalAuthStatus(globalAuthStatus);
        dto.setUserLevelAuthStatus(userLevelAuthStatus);
        dto.setAppLevelAuthStatus(appLevelAuthStatus);

        return dto;
    }

    private ResourceAuthSettingsDto.ResourceAuthStatus mapResourceAuthStatus(ResourceAuthSettings.ResourceAuthStatus status) {
        return status == null ? null : ResourceAuthSettingsDto.ResourceAuthStatus.valueOf(status.name());
    }
}