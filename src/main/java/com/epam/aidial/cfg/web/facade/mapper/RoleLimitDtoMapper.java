package com.epam.aidial.cfg.web.facade.mapper;

import com.epam.aidial.cfg.domain.model.RoleLimit;
import com.epam.aidial.cfg.dto.LimitDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = LimitDtoMapper.class)
public interface RoleLimitDtoMapper {

    @Mapping(target = "role", ignore = true)
    @Mapping(target = "deploymentName", source = "deploymentName")
    @Mapping(target = "limit", source = "limitDto")
    RoleLimit toLimitWithoutRole(LimitDto limitDto, String deploymentName);

    @Mapping(target = "minute", source = "limit.minute")
    @Mapping(target = "day", source = "limit.day")
    @Mapping(target = "week", source = "limit.week")
    @Mapping(target = "month", source = "limit.month")
    @Mapping(target = "requestHour", source = "limit.requestHour")
    @Mapping(target = "requestDay", source = "limit.requestDay")
    LimitDto toLimitDto(RoleLimit roleLimitDto);

    default Map<String, LimitDto> toLimitDtos(List<RoleLimit> value) {
        if (value == null) {
            return null;
        }
        var map = new HashMap<String, LimitDto>();
        for (RoleLimit roleLimit : value) {
            LimitDto limitDto = toLimitDto(roleLimit);
            map.put(roleLimit.getDeploymentName(), limitDto);
        }
        return map;
    }

    default List<RoleLimit> toLimits(Map<String, LimitDto> value) {
        if (value == null) {
            return null;
        }
        return value.entrySet()
                .stream()
                .map(e -> toLimitWithoutRole(e.getValue(), e.getKey()))
                .collect(Collectors.toList());
    }
}
