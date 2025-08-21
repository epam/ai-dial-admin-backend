package com.epam.aidial.cfg.web.facade.mapper;

import com.epam.aidial.cfg.domain.model.ToolSet;
import com.epam.aidial.cfg.dto.ToolSetDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {
    LimitDtoMapper.class, RoleBasedDtoMapper.class, InstantMapper.class
})
public interface ToolSetDtoMapper {

    @RoleBasedDtoMapper.ToDomain
    @Mapping(target = "deployment.name", source = "name")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    ToolSet toDomain(ToolSetDto entity);

    @RoleBasedDtoMapper.ToDto
    @Mapping(target = "name", source = "deployment.name")
    ToolSetDto toDto(ToolSet domain);

}
