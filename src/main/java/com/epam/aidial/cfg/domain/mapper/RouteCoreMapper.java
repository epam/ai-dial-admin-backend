package com.epam.aidial.cfg.domain.mapper;

import com.epam.aidial.cfg.domain.model.ShareResourceLimit;
import com.epam.aidial.cfg.domain.model.Upstream;
import com.epam.aidial.cfg.domain.model.route.DependentRoute;
import com.epam.aidial.cfg.domain.model.route.Route;
import com.epam.aidial.core.config.CoreRoute;
import com.epam.aidial.core.config.CoreUpstream;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Pattern;

@Mapper(
        componentModel = "spring",
        uses = {
                DeploymentCoreMapper.class,
        }
)
public abstract class RouteCoreMapper {

    @Mapping(target = "name", source = "deployment.name")
    @Mapping(target = "userRoles", source = "deployment")
    @Mapping(target = "permissions", ignore = true)
    @Mapping(target = "attachmentPaths", ignore = true)
    public abstract CoreRoute mapRoute(Route route);

    @Mapping(target = "name", source = "deployment.name")
    @Mapping(target = "userRoles", source = "deployment")
    public abstract CoreRoute mapRoute(DependentRoute route);

    @Mapping(target = "deployment", source = "route")
    @Mapping(target = "description", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "displayName", ignore = true)
    public abstract Route mapRoute(CoreRoute route, @Context ShareResourceLimit defaultShareResourceLimit);

    @Mapping(target = "deployment.name", source = "name")
    @Mapping(target = "description", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deployment.roleLimits", ignore = true)
    @Mapping(target = "deployment.defaultRoleLimit", ignore = true)
    public abstract DependentRoute mapDependentRoute(CoreRoute route);

    public LinkedHashMap<String, CoreRoute> mapRoutes(List<DependentRoute> routes) {
        if (CollectionUtils.isEmpty(routes)) {
            return null;
        }
        LinkedHashMap<String, CoreRoute> coreRoutes = new LinkedHashMap<>();
        for (DependentRoute route : routes) {
            var coreRoute = mapRoute(route);
            coreRoutes.put(coreRoute.getName(), coreRoute);
        }
        return coreRoutes;
    }

    public List<DependentRoute> mapRoutes(LinkedHashMap<String, CoreRoute> coreRoutes) {
        if (MapUtils.isEmpty(coreRoutes)) {
            return null;
        }
        List<DependentRoute> routes = new ArrayList<>();
        for (CoreRoute coreRoute : coreRoutes.values()) {
            var route = mapDependentRoute(coreRoute);
            routes.add(route);
        }
        return routes;
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

}
