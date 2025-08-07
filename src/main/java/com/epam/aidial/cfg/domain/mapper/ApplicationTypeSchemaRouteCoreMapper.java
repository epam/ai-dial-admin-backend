package com.epam.aidial.cfg.domain.mapper;

import com.epam.aidial.cfg.domain.model.Route;
import com.epam.aidial.cfg.domain.service.RouteService;
import com.epam.aidial.core.config.CoreApplicationTypeSchemaRoute;
import org.apache.commons.collections4.CollectionUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Pattern;

@Mapper(
        componentModel = "spring",
        uses = RoleLimitMapper.class
)
public abstract class ApplicationTypeSchemaRouteCoreMapper {

    @Autowired
    private RouteService routeService;

    @Mapping(target = "userRoles", source = "deployment")
    public abstract CoreApplicationTypeSchemaRoute mapRoute(Route route);

    public LinkedHashMap<String, CoreApplicationTypeSchemaRoute> mapRoutes(List<Route> routes) {
        LinkedHashMap<String, CoreApplicationTypeSchemaRoute> coreRoutes = new LinkedHashMap<>();
        if (CollectionUtils.isEmpty(routes)) {
            return null;
        }
        for (Route route : routes) {
            var coreRoute = mapRoute(route);
            coreRoutes.put(route.getDeployment().getName(), coreRoute);
        }
        return coreRoutes;
    }

    public LinkedHashMap<String, CoreApplicationTypeSchemaRoute> map(List<String> routes) {
        LinkedHashMap<String, CoreApplicationTypeSchemaRoute> coreRoutes = new LinkedHashMap<>();
        if (CollectionUtils.isEmpty(routes)) {
            return null;
        }
        for (String routeName : routes) {
            var route = routeService.get(routeName);
            var coreRoute = mapRoute(route);
            coreRoutes.put(routeName, coreRoute);
        }
        return coreRoutes;
    }

    public abstract List<Pattern> mapPaths(List<String> paths);

    public Pattern mapPath(String path) {
        return Pattern.compile(path);
    }

}
