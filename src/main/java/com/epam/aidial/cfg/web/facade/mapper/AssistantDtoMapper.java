package com.epam.aidial.cfg.web.facade.mapper;

import com.epam.aidial.cfg.domain.model.Assistant;
import com.epam.aidial.cfg.dto.AssistantDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {LimitDtoMapper.class, RoleBasedDtoMapper.class})
public interface AssistantDtoMapper {

    @RoleBasedDtoMapper.ToDomain
    @Mapping(target = "deployment.name", source = "name")
    Assistant toDomain(AssistantDto entity);

    @RoleBasedDtoMapper.ToDto
    @Mapping(target = "name", source = "deployment.name")
    @Mapping(target = "defaults", source = "defaults", qualifiedByName = "mapDefaults")
    AssistantDto toDto(Assistant domain);
}
