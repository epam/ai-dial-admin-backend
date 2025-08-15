package com.epam.aidial.cfg.service.transfer.importer;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.mapper.RouteCoreMapper;
import com.epam.aidial.cfg.domain.model.ImportAction;
import com.epam.aidial.cfg.domain.model.ImportComponent;
import com.epam.aidial.cfg.domain.model.route.Route;
import com.epam.aidial.cfg.domain.service.RoleService;
import com.epam.aidial.cfg.domain.service.RouteService;
import com.epam.aidial.cfg.model.ConfigImportOptions;
import com.epam.aidial.cfg.service.export.ConflictResolutionPolicy;
import com.epam.aidial.core.config.CoreRole;
import com.epam.aidial.core.config.CoreRoute;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.epam.aidial.cfg.domain.model.ImportAction.CREATE;
import static com.epam.aidial.cfg.domain.model.ImportAction.SKIP;
import static com.epam.aidial.cfg.domain.model.ImportAction.UPDATE;

@Service
@Slf4j
@LogExecution
public class RouteImporter extends RoleBasedImporter {

    private final RouteService routeService;
    private final RouteCoreMapper routeCoreMapper;
    private final Validator validator;

    public RouteImporter(RoleService roleService, RouteService routeService, RouteCoreMapper routeCoreMapper) {
        super(roleService);
        this.routeService = routeService;
        this.routeCoreMapper = routeCoreMapper;
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    public Collection<ImportComponent<Route>> importRoutes(Map<String, CoreRoute> coreRoutes,
                                                           Map<String, CoreRole> roles,
                                                           ConfigImportOptions importOptions,
                                                           boolean isPreview) {
        if (MapUtils.isNotEmpty(coreRoutes)) {
            Map<String, Route> routes = coreRoutes.entrySet()
                    .stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, entry -> map(entry.getKey(), entry.getValue(), roles)));
            return importAdminRoutes(routes, importOptions, isPreview);
        }
        return Collections.emptyList();
    }

    public Collection<ImportComponent<Route>> importAdminRoutes(Map<String, Route> routes,
                                                                ConfigImportOptions importOptions,
                                                                boolean isPreview) {
        if (MapUtils.isNotEmpty(routes)) {
            return routes.entrySet().stream()
                    .map(routeEntry -> {
                                var route = routeEntry.getValue();
                                createRoleIfAbsent(importOptions, route.getDeployment().getRoleLimits());
                                var importAction = processRoute(routeEntry.getKey(), route, importOptions.conflictResolutionPolicy(), isPreview);
                                return new ImportComponent<>(importAction, route);
                            }
                    )
                    .toList();
        }
        return Collections.emptyList();
    }

    private ImportAction processRoute(String routeName,
                                      Route newRoute,
                                      ConflictResolutionPolicy resolutionPolicy,
                                      boolean isPreview) {
        if (routeService.exists(routeName)) {
            return handleExisting(newRoute, resolutionPolicy, routeName, isPreview);
        } else {
            validate(routeName, newRoute);
            if (!isPreview) {
                routeService.create(newRoute);
            }
            return CREATE;
        }
    }

    private ImportAction handleExisting(Route newRoute,
                                        ConflictResolutionPolicy resolutionPolicy,
                                        String routeName,
                                        boolean isPreview) {
        switch (resolutionPolicy) {
            case SKIP -> {
                // Do nothing, the existing route will remain unchanged.
                return SKIP;
            }
            case OVERRIDE -> {
                validate(routeName, newRoute);
                if (!isPreview) {
                    routeService.update(routeName, newRoute);
                }
                return UPDATE;
            }
            default -> throw new IllegalArgumentException("Unexpected resolutionPolicy: " + resolutionPolicy);
        }
    }

    private Route map(String routeName, CoreRoute route, Map<String, CoreRole> roles) {
        route.setName(routeName);
        return routeCoreMapper.mapRoute(route, roles);
    }

    private void validate(String routeName, Route route) {
        Set<ConstraintViolation<Route>> violations = validator.validate(route);
        if (CollectionUtils.isNotEmpty(violations)) {
            for (ConstraintViolation<Route> violation : violations) {
                String message = violation.getMessage();
                Path propertyPath = violation.getPropertyPath();
                log.error("Route '{}' invalid: {} {}", routeName, propertyPath, message);
                throw new IllegalArgumentException("Route '" + routeName + "' invalid: " + propertyPath + " " + message);
            }
        }
    }
}
