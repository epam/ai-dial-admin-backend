package com.epam.aidial.cfg.web.facade.mapper;

import com.epam.aidial.cfg.domain.model.ResourceAuthSettings;
import com.epam.aidial.cfg.domain.model.SecuredResource;
import com.epam.aidial.cfg.domain.model.ToolSet;
import com.epam.aidial.cfg.dto.ResourceAuthSettingsDto;
import com.epam.aidial.cfg.dto.ToolSetDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", uses = {
        LimitDtoMapper.class, RoleBasedDtoMapper.class, InstantMapper.class,
        ShareResourceLimitDtoMapper.class, ResourceAuthSettingsDtoMapper.class
})
public abstract class ToolSetDtoMapper {

    @Autowired
    private ResourceAuthSettingsDtoMapper authSettingsDtoMapper;

    public ToolSet toDomain(ToolSetDto dto) {
        ToolSet toolSet = toDomainInner(dto);
        ResourceAuthSettings authSettings = authSettingsDtoMapper.toDomain(dto.getAuthSettings());
        SecuredResource securedResource = new SecuredResource(toolSet.getDeployment(), authSettings);
        toolSet.setDeployment(securedResource);
        return toolSet;
    }

    @RoleBasedDtoMapper.ToDomain
    @Mapping(target = "deployment.name", source = "name")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    protected abstract ToolSet toDomainInner(ToolSetDto dto);

    @RoleBasedDtoMapper.ToDto
    @Mapping(target = "name", source = "deployment.name")
    @Mapping(target = "authSettings", source = "domain", qualifiedByName = "deploymentToAuthSettings")
    public abstract ToolSetDto toDto(ToolSet domain);

    @Named("deploymentToAuthSettings")
    protected ResourceAuthSettingsDto deploymentToAuthSettings(ToolSet toolSet) {
        var authSettings = ((SecuredResource) toolSet.getDeployment()).getAuthSettings();
        return authSettingsDtoMapper.toDto(authSettings);
    }

}
