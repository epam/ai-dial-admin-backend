package com.epam.aidial.cfg.client.mapper;

import com.epam.aidial.cfg.client.dto.RouteDto;
import com.epam.aidial.cfg.model.RouteResource;
import org.mapstruct.Mapper;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface RouteMapper {

    RouteResource toRoute(RouteDto dto);

    default List<RouteResource> toRouteList(Map<String, RouteDto> routeDtos) {
        if (routeDtos == null) {
            return List.of();
        }

        return routeDtos.entrySet().stream()
                .map(e -> {
                    RouteDto dto = e.getValue();
                    RouteResource route = toRoute(dto);
                    route.setName(e.getKey());
                    return route;
                })
                .toList();
    }

    RouteDto toRouteDto(RouteResource domain);

    default Map<String, RouteDto> map(List<RouteResource> routes) {
        if (routes == null) {
            return Map.of();
        }

        return routes.stream()
                .collect(Collectors.toMap(
                        RouteResource::getName,
                        this::toRouteDto
                ));
    }

    default Long map(Instant instant) {
        return instant != null ? instant.toEpochMilli() : null;
    }

    default Instant map(Long value) {
        return value != null ? Instant.ofEpochMilli(value) : null;
    }
}
