package com.epam.aidial.cfg.client.mapper;

import com.epam.aidial.cfg.client.dto.RouteDto;
import com.epam.aidial.cfg.client.dto.UpstreamDto;
import com.epam.aidial.cfg.model.Route;
import com.epam.aidial.cfg.model.Upstream;
import org.mapstruct.Mapper;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface RouteMapper {

    Route toRoute(RouteDto dto);

    Upstream toUpstream(UpstreamDto dto);

    default List<Route> toRouteList(Map<String, RouteDto> routeDtos) {
        return routeDtos.entrySet().stream()
                .map(e -> {
                    RouteDto dto = e.getValue();
                    Route route = toRoute(dto);
                    route.setName(e.getKey());
                    return route;
                })
                .toList();
    }

    RouteDto toRouteDto(Route domain);

    default Map<String, RouteDto> map(List<Route> routes) {
        if (routes == null) {
            return Map.of();
        }

        return routes.stream()
                .collect(Collectors.toMap(
                        Route::getName,
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
