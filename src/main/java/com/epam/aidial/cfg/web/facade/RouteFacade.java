package com.epam.aidial.cfg.web.facade;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.model.route.Route;
import com.epam.aidial.cfg.domain.service.RouteService;
import com.epam.aidial.cfg.dto.ShareResourceLimitDto;
import com.epam.aidial.cfg.dto.route.RouteDto;
import com.epam.aidial.cfg.web.facade.mapper.RouteDtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@LogExecution
public class RouteFacade {

    private final RouteService routeService;
    private final RouteDtoMapper mapper;

    public Collection<RouteDto> getAllRoutes() {
        return routeService.getAll()
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    public RouteDto getRoute(String routeName) {
        Route route = routeService.get(routeName);
        return mapper.toDto(route);
    }

    public void createRoute(RouteDto routeDto) {
        setDefaultRoleShareResourceLimitIfMissing(routeDto);
        Optional.of(routeDto)
                .map(mapper::toDomain)
                .ifPresent(routeService::create);
    }

    public void updateRoute(String routeName, RouteDto routeDto) {
        setDefaultRoleShareResourceLimitIfMissing(routeDto);
        Route value = mapper.toDomain(routeDto);
        routeService.update(routeName, value);
    }

    public void deleteRoute(String routeName) {
        routeService.delete(routeName);
    }

    public RouteDto getSnapshot(String routeName, Integer revision) {
        Route route = routeService.getSnapshot(routeName, revision);
        return mapper.toDto(route);
    }

    public Collection<RouteDto> getAllAtRevision(Integer revision) {
        return routeService.getAllAtRevision(revision)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    private void setDefaultRoleShareResourceLimitIfMissing(RouteDto routeDto) {
        ShareResourceLimitDto defaultRoleShareResourceLimit = routeDto.getDefaultRoleShareResourceLimit();
        if (defaultRoleShareResourceLimit == null) {
            defaultRoleShareResourceLimit = new ShareResourceLimitDto();
            routeDto.setDefaultRoleShareResourceLimit(defaultRoleShareResourceLimit);
        }
    }
}
