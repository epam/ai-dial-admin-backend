package com.epam.aidial.cfg.web.facade.mapper;

import com.epam.aidial.cfg.domain.model.Role;
import com.epam.aidial.cfg.dto.RoleDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {RoleLimitDtoMapper.class, RoleShareResourceLimitDtoMapper.class})
public interface RoleDtoMapper {

    @Mapping(target = "keys", source = "grantedKeys")
    Role toDomain(RoleDto entity);

    @Mapping(target = "grantedKeys", source = "keys")
    RoleDto toDto(Role domain);
}
