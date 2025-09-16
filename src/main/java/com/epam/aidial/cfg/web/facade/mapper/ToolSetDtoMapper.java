package com.epam.aidial.cfg.web.facade.mapper;

import com.epam.aidial.cfg.domain.model.ToolSet;
import com.epam.aidial.cfg.dto.ToolSetDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {
        LimitDtoMapper.class, RoleBasedDtoMapper.class, InstantMapper.class,
        ShareResourceLimitDtoMapper.class, ResourceAuthSettingsDtoMapper.class
})
public abstract class ToolSetDtoMapper {

    @RoleBasedDtoMapper.ToDomain
    @Mapping(target = "deployment.name", source = "name")
    @Mapping(target = "deployment.authSettings", source = "authSettings")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    public abstract ToolSet toDomain(ToolSetDto dto);

    @RoleBasedDtoMapper.ToDto
    @Mapping(target = "name", source = "deployment.name")
    @Mapping(target = "authSettings", source = "deployment.authSettings")
    public abstract ToolSetDto toDto(ToolSet domain);

}
