package com.epam.aidial.cfg.domain.mapper;

import com.epam.aidial.cfg.dao.jpa.RouteJpaRepository;
import com.epam.aidial.cfg.dao.mapper.RouteEntityMapper;
import com.epam.aidial.cfg.domain.model.Route;
import com.epam.aidial.cfg.domain.model.Upstream;
import com.epam.aidial.cfg.exception.EntityNotFoundException;
import com.epam.aidial.core.config.CoreRole;
import com.epam.aidial.core.config.CoreRoute;
import com.epam.aidial.core.config.CoreUpstream;
import org.apache.commons.collections4.CollectionUtils;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

@Mapper(
        componentModel = "spring",
        uses = {
                RoleLimitMapper.class,
        }
)
public abstract class RouteCoreMapper {

    @Autowired
    private RoleLimitMapper roleLimitMapper;
    @Autowired
    private RouteEntityMapper mapper;
    @Autowired
    private RouteJpaRepository routeJpaRepository;

    @Mapping(target = "name", source = "deployment.name")
    @Mapping(target = "userRoles", source = "deployment")
    public abstract CoreRoute mapRoute(Route route);

    @Mapping(target = "deployment.name", source = "route.name")
    @Mapping(target = "description", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "applicationName", ignore = true)
    @Mapping(target = "applicationTypeSchemaId", ignore = true)
    public abstract Route mapRoute(CoreRoute route, Map<String, CoreRole> roles);

    public LinkedHashMap<String, CoreRoute> mapRoutes(List<Route> routes) {
        LinkedHashMap<String, CoreRoute> coreRoutes = new LinkedHashMap<>();
        if (CollectionUtils.isEmpty(routes)) {
            return null;
        }
        for (Route route : routes) {
            var coreRoute = mapRoute(route);
            coreRoutes.put(coreRoute.getName(), coreRoute);
        }
        return coreRoutes;
    }

    public List<String> mapRoutes(LinkedHashMap<String, CoreRoute> routes) {
        return routes != null ? new ArrayList<>(routes.keySet()) : new ArrayList<>();
    }

    public LinkedHashMap<String, CoreRoute> map(List<String> routes) {
        LinkedHashMap<String, CoreRoute> coreRoutes = new LinkedHashMap<>();
        if (CollectionUtils.isEmpty(routes)) {
            return null;
        }
        for (String routeName : routes) {
            var coreRoute = findRoute(routeName);
            coreRoutes.put(routeName, coreRoute);
        }
        return coreRoutes;
    }

    private CoreRoute findRoute(String routeName) {
        return Optional.ofNullable(routeName)
                .flatMap(routeJpaRepository::findById)
                .map(mapper::toCoreRoute)
                .orElseThrow(() -> new EntityNotFoundException("Route with name %s does not exist".formatted(routeName)));
    }

    @Mapping(target = "id", ignore = true)
    public abstract Upstream mapUpstream(CoreUpstream upstream);

    public abstract List<Pattern> mapPaths(List<String> paths);

    public Pattern mapPath(String path) {
        return Pattern.compile(path);
    }

    public String mapToString(Pattern path) {
        if (path == null) {
            return null;
        }
        return path.pattern();
    }

    @AfterMapping
    public void mapRoles(@MappingTarget Route route, CoreRoute coreEntity, Map<String, CoreRole> roles) {
        roleLimitMapper.mapRoles(route.getDeployment(), coreEntity.getUserRoles(), coreEntity.getName(), roles);
    }

}
