package com.epam.aidial.cfg.domain.mapper;

import com.epam.aidial.cfg.dao.jpa.RouteJpaRepository;
import com.epam.aidial.cfg.dao.mapper.RouteEntityMapper;
import com.epam.aidial.cfg.domain.model.Route;
import com.epam.aidial.cfg.exception.EntityNotFoundException;
import com.epam.aidial.core.config.CoreApplicationTypeSchemaRoute;
import org.apache.commons.collections4.CollectionUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Mapper(
        componentModel = "spring",
        uses = RoleLimitMapper.class
)
public abstract class ApplicationTypeSchemaRouteCoreMapper {

    @Autowired
    private RouteEntityMapper mapper;
    @Autowired
    private RouteJpaRepository routeJpaRepository;

    @Mapping(target = "userRoles", source = "deployment")
    public abstract CoreApplicationTypeSchemaRoute mapRoute(Route route);

    public List<String> map(LinkedHashMap<String, CoreApplicationTypeSchemaRoute> routes) {
        return routes != null ? new ArrayList<>(routes.keySet()) : new ArrayList<>();
    }

    public LinkedHashMap<String, CoreApplicationTypeSchemaRoute> map(List<String> routes) {
        LinkedHashMap<String, CoreApplicationTypeSchemaRoute> coreRoutes = new LinkedHashMap<>();
        if (CollectionUtils.isEmpty(routes)) {
            return null;
        }
        for (String routeName : routes) {
            var coreRoute = findRoute(routeName);
            coreRoutes.put(routeName, coreRoute);
        }
        return coreRoutes;
    }

    private CoreApplicationTypeSchemaRoute findRoute(String routeName) {
        return Optional.ofNullable(routeName)
                .flatMap(routeJpaRepository::findById)
                .map(mapper::toCoreApplicationTypeSchemaRoute)
                .orElseThrow(() -> new EntityNotFoundException("Route with name %s does not exist".formatted(routeName)));
    }

    public abstract List<Pattern> mapPaths(List<String> paths);

    public Pattern mapPath(String path) {
        return Pattern.compile(path);
    }

}
