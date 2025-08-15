package com.epam.aidial.cfg.service.transfer.exporter;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.model.Adapter;
import com.epam.aidial.cfg.domain.model.Application;
import com.epam.aidial.cfg.domain.model.ExportApplicationTypeSchemaInfo;
import com.epam.aidial.cfg.domain.model.ExportComponentInfo;
import com.epam.aidial.cfg.domain.model.ExportConfig;
import com.epam.aidial.cfg.domain.model.ExportConfigComponentType;
import com.epam.aidial.cfg.domain.model.ExportConfigPreview;
import com.epam.aidial.cfg.domain.model.ExportKeyInfo;
import com.epam.aidial.cfg.domain.model.Model;
import com.epam.aidial.cfg.domain.model.Role;
import com.epam.aidial.cfg.domain.model.RoleBased;
import com.epam.aidial.cfg.domain.model.ToolSet;
import com.epam.aidial.cfg.domain.model.route.Route;
import com.epam.aidial.cfg.model.ExportConfigComponent;
import com.epam.aidial.cfg.model.ExportRequest;
import com.epam.aidial.cfg.model.SelectedItemsExportRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Service
@LogExecution
public class ConfigExporter {

    private final ApplicationExporter applicationExporter;
    private final ModelExporter modelExporter;
    private final RouteExporter routeExporter;

    private final RoleExporter roleExporter;
    private final KeyExporter keyExporter;
    private final ApplicationTypeSchemaExporter applicationTypeSchemaExporter;
    private final InterceptorExporter interceptorExporter;
    private final InterceptorRunnerExporter interceptorRunnerExporter;
    private final AdapterExporter adapterExporter;
    private final ToolSetExporter toolSetExporter;

    public ExportConfig getConfig(ExportRequest request) {
        if (request instanceof SelectedItemsExportRequest exportRequest) {
            resolveDependencies(exportRequest);
        }
        ExportConfig config = new ExportConfig();
        Map<String, Application> applications = applicationExporter.getApplications(request);
        config.setApplications(applications);
        LinkedHashMap<String, Route> routes = routeExporter.getRoutes(request);
        config.setRoutes(routes);

        Map<String, Adapter> adapters = adapterExporter.getAdapters(request);
        config.setAdapters(adapters);

        Map<String, Model> models = modelExporter.getModels(request);
        config.setModels(models);

        Map<String, ToolSet> toolSets = toolSetExporter.getToolSets(request);
        config.setToolsets(toolSets);

        Set<String> allEntityNames = getAllEntityNames(applications.keySet(), models.keySet(), routes.keySet(), toolSets.keySet());
        Map<String, Role> roles = roleExporter.getRoles(request, allEntityNames);
        config.setRoles(roles);
        config.setKeys(keyExporter.getKeys(request));
        config.setInterceptors(interceptorExporter.getInterceptors(request));
        config.setInterceptorRunners(interceptorRunnerExporter.getInterceptorRunners(request));
        config.setApplicationRunners(applicationTypeSchemaExporter.getApplicationTypeSchemas(request));
        // todo prompts and files
        return config;
    }

    public ExportConfigPreview preview(ExportRequest request) {
        if (request instanceof SelectedItemsExportRequest exportRequest) {
            resolveDependencies(exportRequest);
        }

        Collection<ExportComponentInfo> applications = applicationExporter.preview(request);
        Collection<ExportComponentInfo> routes = routeExporter.preview(request);
        Collection<ExportComponentInfo> adapters = adapterExporter.preview(request);
        Collection<ExportComponentInfo> models = modelExporter.preview(request);
        Collection<ExportComponentInfo> roles = roleExporter.preview(request);
        Collection<ExportKeyInfo> keys = keyExporter.preview(request);
        Collection<ExportComponentInfo> interceptors = interceptorExporter.preview(request);
        Collection<ExportComponentInfo> interceptorRunners = interceptorRunnerExporter.preview(request);
        Collection<ExportApplicationTypeSchemaInfo> applicationRunners = applicationTypeSchemaExporter.preview(request);
        Collection<ExportComponentInfo> toolSets = toolSetExporter.preview(request);

        // todo prompts and files
        return ExportConfigPreview.builder()
                .routes(routes)
                .applications(applications)
                .models(models)
                .toolSets(toolSets)
                .roles(roles)
                .keys(keys)
                .interceptors(interceptors)
                .interceptorRunners(interceptorRunners)
                .applicationRunners(applicationRunners)
                .adapters(adapters)
                .build();
    }

    private void resolveDependencies(SelectedItemsExportRequest request) {
        List<ExportConfigComponent> components = request.getComponents();
        List<ExportConfigComponent> result = new ArrayList<>(components);
        resolveKeyDependencies(result);
        resolveRoleDependencies(result);
        resolveAppDependencies(result);
        resolveModelDependencies(result);
        request.setComponents(result);
    }

    private void resolveKeyDependencies(List<ExportConfigComponent> result) {
        List<ExportConfigComponent> keys = filterComponentsByType(result, ExportConfigComponentType.KEY);
        for (ExportConfigComponent component : keys) {
            String keyName = component.getName();
            Set<ExportConfigComponentType> dependencies = component.getDependencies();
            if (CollectionUtils.isEmpty(dependencies) || !dependencies.contains(ExportConfigComponentType.ROLE)) {
                continue;
            }
            Collection<Role> roles = roleExporter.getRoles().stream()
                    .filter(role -> isKeyPresentInRole(role, keyName))
                    .toList();
            roles.forEach(role -> result.add(new ExportConfigComponent(role.getName(), ExportConfigComponentType.ROLE, dependencies)));
        }
    }

    private boolean isKeyPresentInRole(Role role, String keyName) {
        List<String> keys = role.getKeys();
        if (CollectionUtils.isEmpty(keys)) {
            return false;
        }
        return keys.stream().anyMatch(key -> Objects.equals(keyName, key));
    }

    private void resolveRoleDependencies(List<ExportConfigComponent> updatedComponents) {
        List<ExportConfigComponent> roles = filterComponentsByType(updatedComponents, ExportConfigComponentType.ROLE);
        for (ExportConfigComponent component : roles) {
            String roleName = component.getName();
            Set<ExportConfigComponentType> dependencies = component.getDependencies();
            if (CollectionUtils.isEmpty(dependencies)) {
                continue;
            }
            processDependency(roleName, dependencies, ExportConfigComponentType.APPLICATION,
                    applicationExporter.getApplications(), updatedComponents);
            processDependency(roleName, dependencies, ExportConfigComponentType.MODEL,
                    modelExporter.getModels(), updatedComponents);
            processDependency(roleName, dependencies, ExportConfigComponentType.ROUTE,
                    routeExporter.getRoutes(), updatedComponents);
            processDependency(roleName, dependencies, ExportConfigComponentType.TOOL_SET,
                    toolSetExporter.getToolSets(), updatedComponents);
        }
    }

    private <T extends RoleBased> void processDependency(String roleName,
                                                         Set<ExportConfigComponentType> dependencies,
                                                         ExportConfigComponentType dependencyType,
                                                         Collection<T> entities,
                                                         List<ExportConfigComponent> updatedComponents) {
        if (!dependencies.contains(dependencyType)) {
            return;
        }
        List<? extends RoleBased> enabledEntities = entities.stream()
                .filter(entity -> entity.getDeployment().getIsPublic()
                        || entity.getDeployment().getRoleLimits().stream()
                        .anyMatch(roleLimit -> Objects.equals(roleLimit.getRole(), roleName) && roleLimit.isEnabled()))
                .toList();

        enabledEntities.forEach(entity -> updatedComponents.add(new ExportConfigComponent(entity.getDeployment().getName(), dependencyType, dependencies)));
    }

    private List<ExportConfigComponent> filterComponentsByType(List<ExportConfigComponent> components, ExportConfigComponentType type) {
        return components.stream()
                .filter(c -> c.getType().equals(type))
                .toList();
    }

    private void resolveAppDependencies(List<ExportConfigComponent> updatedComponents) {
        List<ExportConfigComponent> apps = filterComponentsByType(updatedComponents, ExportConfigComponentType.APPLICATION);

        for (ExportConfigComponent component : apps) {
            Set<ExportConfigComponentType> dependencies = component.getDependencies();
            if (CollectionUtils.isEmpty(dependencies)) {
                continue;
            }

            Application application = applicationExporter.getApplication(component.getName());
            processInterceptorDependencies(application.getInterceptors(), dependencies, updatedComponents);
            processApplicationTypeSchemaDependencies(application, dependencies, updatedComponents);
        }
    }

    private void resolveModelDependencies(List<ExportConfigComponent> updatedComponents) {
        List<ExportConfigComponent> models = filterComponentsByType(updatedComponents, ExportConfigComponentType.MODEL);

        for (ExportConfigComponent component : models) {
            Set<ExportConfigComponentType> dependencies = component.getDependencies();
            if (CollectionUtils.isEmpty(dependencies)) {
                continue;
            }
            Model model = modelExporter.getModel(component.getName());
            processInterceptorDependencies(model.getInterceptors(), dependencies, updatedComponents);
        }
    }

    private void processInterceptorDependencies(List<String> interceptors, Set<ExportConfigComponentType> dependencies, List<ExportConfigComponent> updatedComponents) {
        if (dependencies.contains(ExportConfigComponentType.INTERCEPTOR) && CollectionUtils.isNotEmpty(interceptors)) {
            interceptors.forEach(interceptor ->
                    updatedComponents.add(new ExportConfigComponent(interceptor, ExportConfigComponentType.INTERCEPTOR)));
        }
    }

    private void processApplicationTypeSchemaDependencies(Application application, Set<ExportConfigComponentType> dependencies, List<ExportConfigComponent> updatedComponents) {
        if (dependencies.contains(ExportConfigComponentType.APPLICATION_TYPE_SCHEMA)) {
            URI applicationTypeSchemaId = application.getApplicationTypeSchemaId();
            if (applicationTypeSchemaId != null) {
                updatedComponents.add(new ExportConfigComponent(applicationTypeSchemaId.toString(), ExportConfigComponentType.APPLICATION_TYPE_SCHEMA));
            }
        }
    }

    protected Set<String> getAllEntityNames(Set<String> applications,
                                            Set<String> models,
                                            Set<String> routes,
                                            Set<String> toolSets) {
        return Stream.of(applications, models, routes, toolSets)
                .flatMap(Set::stream)
                .collect(Collectors.toSet());
    }

}
