package com.epam.aidial.cfg.web.facade.mapper;

import com.epam.aidial.cfg.domain.model.Addon;
import com.epam.aidial.cfg.dto.AddonDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {LimitDtoMapper.class, RoleBasedDtoMapper.class})
public interface AddonDtoMapper {

    @RoleBasedDtoMapper.ToDomain
    @Mapping(target = "createdAt", source = "createdAtMs")
    @Mapping(target = "updatedAt", source = "updatedAtMs")
    @Mapping(target = "deployment.name", source = "name")
    Addon toDomain(AddonDto dto);

    @RoleBasedDtoMapper.ToDto
    @Mapping(target = "createdAtMs", source = "createdAt")
    @Mapping(target = "updatedAtMs", source = "updatedAt")
    @Mapping(target = "name", source = "deployment.name")
    AddonDto toDto(Addon domain);
}
