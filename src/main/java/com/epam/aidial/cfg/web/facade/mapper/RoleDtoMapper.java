package com.epam.aidial.cfg.web.facade.mapper;

import com.epam.aidial.cfg.domain.model.Role;
import com.epam.aidial.cfg.dto.RoleDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {RoleLimitDtoMapper.class, RoleShareResourceLimitDtoMapper.class, InstantMapper.class})
public interface RoleDtoMapper {

    @Mapping(target = "keys", source = "grantedKeys")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Role toDomain(RoleDto entity);

    @Mapping(target = "grantedKeys", source = "keys")
    @Mapping(target = "createdAt", source = "createdAt", qualifiedByName = "longToInstant")
    @Mapping(target = "updatedAt", source = "updatedAt", qualifiedByName = "longToInstant")
    RoleDto toDto(Role domain);
}
