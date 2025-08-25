package com.epam.aidial.cfg.web.facade.mapper;

import com.epam.aidial.cfg.domain.model.Limit;
import com.epam.aidial.cfg.domain.model.RoleLimit;
import com.epam.aidial.cfg.domain.model.RoleShareResourceLimit;
import com.epam.aidial.cfg.domain.model.ShareResourceLimit;
import com.epam.aidial.cfg.dto.LimitDto;
import com.epam.aidial.cfg.dto.ShareResourceLimitDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public abstract class RoleBasedDtoMapper {

    @Autowired
    private LimitDtoMapper limitDtoMapper;
    @Autowired
    private ShareResourceLimitDtoMapper shareResourceLimitDtoMapper;

    @Mapping(target = "deployment.roleLimits", source = "roleLimits")
    @Mapping(target = "deployment.roleShareResourceLimits", source = "roleShareResourceLimits")
    @Mapping(target = "deployment.isPublic", source = "isPublic")
    @Mapping(target = "deployment.defaultRoleLimit", source = "defaultRoleLimit")
    @Mapping(target = "deployment.defaultRoleShareResourceLimit", source = "defaultRoleShareResourceLimit")
    public @interface ToDomain {
    }

    @Mapping(target = "roleLimits", source = "deployment.roleLimits")
    @Mapping(target = "roleShareResourceLimits", source = "deployment.roleShareResourceLimits")
    @Mapping(target = "isPublic", source = "deployment.isPublic")
    @Mapping(target = "defaultRoleLimit", source = "deployment.defaultRoleLimit")
    @Mapping(target = "defaultRoleShareResourceLimit", source = "deployment.defaultRoleShareResourceLimit")
    public @interface ToDto {
    }

    public Map<String, LimitDto> mapRoleLimitsToDto(List<RoleLimit> roleLimits) {
        if (roleLimits == null) {
            return null;
        }
        Map<String, LimitDto> result = new HashMap<>();
        for (RoleLimit roleLimit : roleLimits) {
            LimitDto limitDto = limitDtoMapper.toLimitDto(roleLimit.getLimit());
            limitDto.setEnabled(roleLimit.isEnabled());
            result.put(roleLimit.getRole(), limitDto);
        }
        return result;
    }

    public Map<String, ShareResourceLimitDto> mapRoleShareResourceLimitsToDto(List<RoleShareResourceLimit> roleShareResourceLimits) {
        if (roleShareResourceLimits == null) {
            return null;
        }
        Map<String, ShareResourceLimitDto> result = new HashMap<>();
        for (RoleShareResourceLimit roleShareResourceLimit : roleShareResourceLimits) {
            ShareResourceLimitDto shareResourceLimitDto = shareResourceLimitDtoMapper.toShareResourceLimitDto(roleShareResourceLimit.getLimit());
            result.put(roleShareResourceLimit.getRole(), shareResourceLimitDto);
        }
        return result;
    }

    public List<RoleLimit> mapRoleLimitsToDomain(Map<String, LimitDto> value) {
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

    public List<RoleShareResourceLimit> mapRoleShareResourceLimitsToDomain(Map<String, ShareResourceLimitDto> value) {
        if (value == null) {
            return null;
        }
        return value.entrySet()
                .stream()
                .map(e -> {
                    ShareResourceLimit shareResourceLimit = shareResourceLimitDtoMapper.toShareResourceLimit(e.getValue());
                    RoleShareResourceLimit roleShareResourceLimit = new RoleShareResourceLimit();
                    roleShareResourceLimit.setRole(e.getKey());
                    roleShareResourceLimit.setLimit(shareResourceLimit);
                    return roleShareResourceLimit;
                })
                .collect(Collectors.toList());
    }

    @Named("mapDefaults")
    public Map<String, String> mapDefaults(Map<String, Object> value) {
        if (value == null) {
            return null;
        }
        return value.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> Objects.toString(entry.getValue())));
    }
}
