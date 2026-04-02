package com.epam.aidial.cfg.domain.mapper;

import com.epam.aidial.cfg.domain.model.Upstream;
import com.epam.aidial.cfg.domain.model.route.DependentRoute;
import com.epam.aidial.core.config.CoreApplicationTypeSchemaRoute;
import com.epam.aidial.core.config.CoreApplicationTypeSchemaUpstream;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Pattern;

@Mapper(
        componentModel = "spring",
        uses = DeploymentCoreMapper.class
)
public abstract class ApplicationTypeSchemaRouteCoreMapper {

    @Mapping(target = "userRoles", source = "deployment")
    public abstract CoreApplicationTypeSchemaRoute mapRoute(DependentRoute route);

    @Mapping(target = "deployment.name", source = "name")
    @Mapping(target = "description", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deployment.roleLimits", ignore = true)
    @Mapping(target = "deployment.defaultRoleLimit", ignore = true)
    public abstract DependentRoute mapDependentRoute(String name, CoreApplicationTypeSchemaRoute route);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "responsesEndpoint", ignore = true)
    public abstract Upstream mapUpstream(CoreApplicationTypeSchemaUpstream upstream);

    public List<DependentRoute> map(LinkedHashMap<String, CoreApplicationTypeSchemaRoute> coreRoutes) {
        List<DependentRoute> routes = new ArrayList<>();
        if (MapUtils.isEmpty(coreRoutes)) {
            return null;
        }
        for (var entry : coreRoutes.entrySet()) {
            var route = mapDependentRoute(entry.getKey(), entry.getValue());
            routes.add(route);
        }
        return routes;
    }

    public LinkedHashMap<String, CoreApplicationTypeSchemaRoute> map(List<DependentRoute> routes) {
        LinkedHashMap<String, CoreApplicationTypeSchemaRoute> coreRoutes = new LinkedHashMap<>();
        if (CollectionUtils.isEmpty(routes)) {
            return null;
        }
        for (DependentRoute route : routes) {
            CoreApplicationTypeSchemaRoute coreRoute = mapRoute(route);
            coreRoutes.put(route.getDeployment().getName(), coreRoute);
        }
        return coreRoutes;
    }

    public abstract List<String> mapPathsToString(List<Pattern> value);

    public abstract List<Pattern> mapStingsToPaths(List<String> paths);

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