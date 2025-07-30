package com.epam.aidial.cfg.service.transfer.exporter;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.model.ExportComponentInfo;
import com.epam.aidial.cfg.domain.model.ExportConfigComponentType;
import com.epam.aidial.cfg.domain.model.ExportFormat;
import com.epam.aidial.cfg.domain.model.Route;
import com.epam.aidial.cfg.domain.model.Upstream;
import com.epam.aidial.cfg.domain.service.RouteService;
import com.epam.aidial.cfg.model.ExportConfigComponent;
import com.epam.aidial.cfg.model.ExportRequest;
import com.epam.aidial.cfg.model.FullExportRequest;
import com.epam.aidial.cfg.model.SelectedItemsExportRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@LogExecution
@RequiredArgsConstructor
public class RouteExporter {

    private final RouteService routeService;

    protected LinkedHashMap<String, Route> getRoutes(ExportRequest request) {
        if (request instanceof FullExportRequest fullExportRequest) {
            return fullExportRequest.getComponentTypes().contains(ExportConfigComponentType.ROUTE)
                    ? getRoutes(fullExportRequest).stream()
                    .collect(Collectors.toMap(route -> route.getDeployment().getName(), Function.identity(),
                            (existing, newRoute) -> newRoute, LinkedHashMap::new))
                    : new LinkedHashMap<>();
        } else if (request instanceof SelectedItemsExportRequest selectedItemsExportRequest) {
            return getRoutes(selectedItemsExportRequest, request.isAddSecrets()).stream()
                    .collect(Collectors.toMap(
                            route -> route.getDeployment().getName(), Function.identity(),
                            (existing, replacement) -> {
                                throw new IllegalStateException("Duplicate routes found: %s".formatted(existing));
                            },
                            LinkedHashMap::new
                    ));
        }
        throw new IllegalArgumentException("Unsupported request type: " + request.getClass());
    }

    protected Collection<Route> getRoutes() {
        return routeService.getAll();
    }

    private List<Route> getRoutes(SelectedItemsExportRequest selectedItemsExportRequest, boolean addSecrets) {
        List<ExportConfigComponent> elements = selectedItemsExportRequest.getComponents();
        return elements.stream()
                .filter(component -> component.getType() == ExportConfigComponentType.ROUTE)
                .collect(Collectors.toMap(ExportConfigComponent::getName, Function.identity(),
                        (existing, replacement) -> {
                            existing.addDependencies(replacement.getDependencies());
                            return existing;
                        }
                ))
                .values()
                .stream()
                .map(component -> {
                    Route route = routeService.get(component.getName());
                    return removeDependency(route, component.getDependencies(), selectedItemsExportRequest.getExportFormat());
                })
                .map(route -> removeUpstreamKey(route, addSecrets))
                .toList();
    }

    private Collection<Route> getRoutes(FullExportRequest fullExportRequest) {
        return getRoutes().stream()
                .map(route -> removeUpstreamKey(route, fullExportRequest.isAddSecrets()))
                .map(route -> removeDependency(route, fullExportRequest.getComponentTypes(), fullExportRequest.getExportFormat()))
                .toList();
    }

    protected Collection<ExportComponentInfo> preview(ExportRequest request) {
        return getRoutes(request).values().stream()
                .map(component -> ExportComponentInfo.builder()
                        .name(component.getDeployment().getName())
                        .description(component.getDescription())
                        .type(ExportConfigComponentType.ROUTE)
                        .build())
                .collect(Collectors.toList());
    }

    private Route removeDependency(Route route, Set<ExportConfigComponentType> componentTypes, ExportFormat exportFormat) {
        // Exclude role limits from deployment for Admin export format in order to have unidirectional association
        // between deployments and roles, so it means that role with its limits will be defined only under "roles" section
        if (!componentTypes.contains(ExportConfigComponentType.ROLE) || exportFormat == ExportFormat.ADMIN) {
            route.getDeployment().setRoleLimits(null);
        }
        return route;
    }

    private Route removeUpstreamKey(Route route, boolean addSecrets) {
        List<Upstream> upstreams = route.getUpstreams();
        if (CollectionUtils.isNotEmpty(upstreams) && !addSecrets) {
            for (Upstream upstream : upstreams) {
                upstream.setKey(null);
            }
        }
        return route;
    }
}
