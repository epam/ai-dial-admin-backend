package com.epam.aidial.cfg.web.facade.mapper;

import com.epam.aidial.cfg.domain.model.RoleShareResourceLimit;
import com.epam.aidial.cfg.dto.ShareResourceLimitDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = ShareResourceLimitDtoMapper.class)
public interface RoleShareResourceLimitDtoMapper {

    @Mapping(target = "role", ignore = true)
    @Mapping(target = "deploymentName", source = "deploymentName")
    @Mapping(target = "limit", source = "limitDto")
    RoleShareResourceLimit toShareResourceLimitWithoutRole(ShareResourceLimitDto limitDto, String deploymentName);

    @Mapping(target = "maxAcceptedUsers", source = "limit.maxAcceptedUsers")
    @Mapping(target = "invitationTtl", source = "limit.invitationTtl")
    ShareResourceLimitDto toShareResourceLimitDto(RoleShareResourceLimit roleShareResourceLimitDto);

    default Map<String, ShareResourceLimitDto> toLimitDtos(List<RoleShareResourceLimit> value) {
        if (value == null) {
            return null;
        }
        var map = new HashMap<String, ShareResourceLimitDto>();
        for (RoleShareResourceLimit roleLimit : value) {
            ShareResourceLimitDto limitDto = toShareResourceLimitDto(roleLimit);
            map.put(roleLimit.getDeploymentName(), limitDto);
        }
        return map;
    }

    default List<RoleShareResourceLimit> toShareResourceLimits(Map<String, ShareResourceLimitDto> value) {
        if (value == null) {
            return null;
        }
        return value.entrySet()
                .stream()
                .map(e -> toShareResourceLimitWithoutRole(e.getValue(), e.getKey()))
                .collect(Collectors.toList());
    }
}
