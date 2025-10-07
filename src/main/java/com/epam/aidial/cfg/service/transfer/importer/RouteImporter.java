package com.epam.aidial.cfg.service.transfer.importer;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.mapper.RouteCoreMapper;
import com.epam.aidial.cfg.domain.model.ImportAction;
import com.epam.aidial.cfg.domain.model.ImportComponent;
import com.epam.aidial.cfg.domain.model.Role;
import com.epam.aidial.cfg.domain.model.RoleLimit;
import com.epam.aidial.cfg.domain.model.route.Route;
import com.epam.aidial.cfg.domain.service.RouteService;
import com.epam.aidial.cfg.model.ConfigImportOptions;
import com.epam.aidial.cfg.service.export.ConflictResolutionPolicy;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.epam.aidial.cfg.domain.model.ImportAction.CREATE;
import static com.epam.aidial.cfg.domain.model.ImportAction.SKIP;
import static com.epam.aidial.cfg.domain.model.ImportAction.UPDATE;

@Service
@Slf4j
@LogExecution
public class RouteImporter extends DeploymentHolderImporter {

    private final RouteService routeService;
    private final RouteCoreMapper routeCoreMapper;
    private final Validator validator;

    public RouteImporter(RouteService routeService, RouteCoreMapper routeCoreMapper) {
        this.routeService = routeService;
        this.routeCoreMapper = routeCoreMapper;
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    public Collection<ImportComponent<Route>> importRoutes(Map<String, CoreRoute> coreRoutes,
                                                           Map<String, Role> roles,
                                                           ConfigImportOptions importOptions) {
        if (MapUtils.isNotEmpty(coreRoutes)) {
            Map<String, Route> routes = coreRoutes.entrySet()
                    .stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, entry -> map(entry.getKey(), entry.getValue())));
            return importAdminRoutes(routes, roles, importOptions);
        }
        return Collections.emptyList();
    }

    public Collection<ImportComponent<Route>> importAdminRoutes(Map<String, Route> routes,
                                                                Map<String, Role> roles,
                                                                ConfigImportOptions importOptions) {
        if (MapUtils.isNotEmpty(routes)) {
            return routes.entrySet().stream()
                    .map(routeEntry -> {
                                var route = routeEntry.getValue();
                                return processRoute(routeEntry.getKey(), route, roles, importOptions.conflictResolutionPolicy());
                            }
                    )
                    .toList();
        }
        return Collections.emptyList();
    }

    private ImportComponent<Route> processRoute(String routeName,
                                                Route newRoute,
                                                Map<String, Role> roles,
                                                ConflictResolutionPolicy resolutionPolicy) {
        Optional<Route> route = routeService.tryGetRoute(routeName);
        if (route.isPresent()) {
            Route existingRoute = route.get();
            setLimits(routeName, existingRoute.getDeployment(), roles, newRoute.getDeployment());
            ImportAction importAction = handleExisting(newRoute, resolutionPolicy, routeName);
            return new ImportComponent<>(importAction, existingRoute, newRoute);
        } else {
            validate(routeName, newRoute);
            setLimits(routeName, roles, newRoute.getDeployment());
            routeService.create(newRoute);
            return new ImportComponent<>(CREATE, null, newRoute);
        }
    }

    private ImportAction handleExisting(Route newRoute,
                                        ConflictResolutionPolicy resolutionPolicy,
                                        String routeName) {
        return switch (resolutionPolicy) {
            case SKIP -> SKIP; // Do nothing, the existing route will remain unchanged.
            case OVERRIDE -> {
                validate(routeName, newRoute);
                routeService.update(routeName, newRoute);
                yield UPDATE;
            }
        };
    }

    private Route map(String routeName, CoreRoute route) {
        route.setName(routeName);
        return routeCoreMapper.mapRoute(route);
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

    public List<ImportComponent<Route>> getActualImportedRoutes(Collection<ImportComponent<Route>> routeImportComponents,
                                                                Collection<ImportComponent<Role>> roleImportComponents) {
        List<String> names = getNextImportComponentNames(routeImportComponents);
        Map<String, Route> importedRoutesByNames = routeService.getAllByNames(names)
                .stream()
                .collect(Collectors.toMap(route -> route.getDeployment().getName(), Function.identity()));

        List<RoleLimit> importedRoleLimits = getImportedLimits(roleImportComponents);

        return routeImportComponents.stream()
                .map(importComponent -> {
                    var next = importedRoutesByNames.get(importComponent.getNext().getDeployment().getName());
                    setImportedLimits(next, importedRoleLimits);
                    var prev = importComponent.getPrev();
                    clearTxDependentFields(next);
                    clearTxDependentFields(prev);
                    return new ImportComponent<>(importComponent.getImportAction(), prev, next);
                })
                .toList();
    }

    private void clearTxDependentFields(Route route) {
        if (route != null) {
            route.setCreatedAt(null);
            route.setUpdatedAt(null);
        }
    }
}
