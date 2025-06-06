package com.epam.aidial.cfg.web.facade.mapper;

import com.epam.aidial.cfg.domain.model.Deployment;
import com.epam.aidial.cfg.domain.model.Limit;
import com.epam.aidial.cfg.domain.model.RoleLimit;
import com.epam.aidial.cfg.dto.LimitDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public abstract class RoleBasedDtoMapper {

    @Autowired
    private LimitDtoMapper limitDtoMapper;

    @Mapping(target = "deployment.roleLimits", source = "roleLimits")
    @Mapping(target = "deployment.isPublic", source = "isPublic")
    @Mapping(target = "deployment.defaultRoleLimit", source = "defaultRoleLimit")
    public @interface ToDomain {
    }

    @Mapping(target = "roleLimits", source = "deployment")
    @Mapping(target = "isPublic", source = "deployment.isPublic")
    @Mapping(target = "defaultRoleLimit", source = "deployment.defaultRoleLimit")
    public @interface ToDto {
    }

    public Map<String, LimitDto> mapRolesToDto(Deployment value) {
        if (value == null || value.getRoleLimits() == null) {
            return null;
        }
        Map<String, LimitDto> result = new HashMap<>();
        for (RoleLimit roleLimit : value.getRoleLimits()) {
            LimitDto limitDto = limitDtoMapper.toLimitDto(roleLimit.getLimit());
            limitDto.setEnabled(roleLimit.isEnabled());
            result.put(roleLimit.getRole(), limitDto);
        }
        return result;
    }

    public List<RoleLimit> mapToRoles(Map<String, LimitDto> value) {
        if (value == null) {
            return null;
        }
        return value.entrySet()
                .stream()
                .map(e -> {
                    Limit limit = limitDtoMapper.toLimit(e.getValue());
                    RoleLimit roleLimit = new RoleLimit();
                    roleLimit.setRole(e.getKey());
                    roleLimit.setLimit(limit);
                    return roleLimit;
                })
                .collect(Collectors.toList());
    }
}
