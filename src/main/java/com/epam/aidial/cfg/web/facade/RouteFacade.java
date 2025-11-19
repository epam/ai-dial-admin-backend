package com.epam.aidial.cfg.web.facade;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.model.route.Route;
import com.epam.aidial.cfg.domain.service.RouteService;
import com.epam.aidial.cfg.dto.CoreWithDomainHash;
import com.epam.aidial.cfg.dto.DtoWithDomainHash;
import com.epam.aidial.cfg.dto.route.RouteDto;
import com.epam.aidial.cfg.service.core.CoreRouteService;
import com.epam.aidial.cfg.web.facade.mapper.RouteDtoMapper;
import com.epam.aidial.core.config.CoreRoute;
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
    private final CoreRouteService coreRouteService;

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

    public DtoWithDomainHash<RouteDto> getRouteWithHash(String toolSetName) {
        var routeWithHash = routeService.getRouteWithHash(toolSetName);
        RouteDto dto = mapper.toDto(routeWithHash.model());
        return new DtoWithDomainHash<>(dto, routeWithHash.hash());
    }

    public CoreWithDomainHash<CoreRoute> getCoreRouteWithHash(String routeName) {
        return coreRouteService.getCoreRouteWithHash(routeName);
    }

    public void createRoute(RouteDto routeDto) {
        Optional.of(routeDto)
                .map(mapper::toDomain)
                .ifPresent(routeService::create);
    }

    public String updateRoute(String routeName, RouteDto routeDto, String hash) {
        Route value = mapper.toDomain(routeDto);
        return routeService.update(routeName, value, hash);
    }

    public String updateRoute(String routeName, CoreRoute coreRoute, String hash) {
        return coreRouteService.updateRoute(routeName, coreRoute, hash);
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
}
