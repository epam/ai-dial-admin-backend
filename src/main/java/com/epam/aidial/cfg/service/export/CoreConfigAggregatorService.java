package com.epam.aidial.cfg.service.export;

import com.epam.aidial.cfg.domain.mapper.ApplicationCoreMapper;
import com.epam.aidial.cfg.domain.mapper.ApplicationTypeSchemaCoreMapper;
import com.epam.aidial.cfg.domain.mapper.InterceptorCoreMapper;
import com.epam.aidial.cfg.domain.mapper.KeyCoreMapper;
import com.epam.aidial.cfg.domain.mapper.ModelCoreMapper;
import com.epam.aidial.cfg.domain.mapper.RoleCoreMapper;
import com.epam.aidial.cfg.domain.mapper.RouteCoreMapper;
import com.epam.aidial.cfg.domain.mapper.ToolSetCoreMapper;
import com.epam.aidial.cfg.domain.model.ApplicationTypeSchema;
import com.epam.aidial.cfg.domain.model.Deployment;
import com.epam.aidial.cfg.domain.service.ApplicationService;
import com.epam.aidial.cfg.domain.service.ApplicationTypeSchemaService;
import com.epam.aidial.cfg.domain.service.DeploymentService;
import com.epam.aidial.cfg.domain.service.InterceptorService;
import com.epam.aidial.cfg.domain.service.KeyService;
import com.epam.aidial.cfg.domain.service.ModelService;
import com.epam.aidial.cfg.domain.service.RoleService;
import com.epam.aidial.cfg.domain.service.RouteService;
import com.epam.aidial.cfg.domain.service.ToolSetService;
import com.epam.aidial.core.config.Config;
import com.epam.aidial.core.config.CoreApplication;
import com.epam.aidial.core.config.CoreInterceptor;
import com.epam.aidial.core.config.CoreKey;
import com.epam.aidial.core.config.CoreModel;
import com.epam.aidial.core.config.CoreRole;
import com.epam.aidial.core.config.CoreRoute;
import com.epam.aidial.core.config.CoreToolSet;
import com.epam.aidial.core.config.RoleBasedEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CoreConfigAggregatorService {

    private final ApplicationService applicationService;
    private final ApplicationTypeSchemaService applicationTypeSchemaService;
    private final InterceptorService interceptorService;
    private final KeyService keyService;
    private final ModelService modelService;
    private final RoleService roleService;
    private final RouteService routeService;
    private final DeploymentService deploymentService;
    private final ToolSetService toolSetService;

    private final ApplicationCoreMapper applicationMapper;
    private final ApplicationTypeSchemaCoreMapper schemaMapper;
    private final InterceptorCoreMapper interceptorMapper;
    private final KeyCoreMapper keyMapper;
    private final ModelCoreMapper modelMapper;
    private final RoleCoreMapper roleMapper;
    private final RouteCoreMapper routeMapper;
    private final ToolSetCoreMapper toolSetMapper;

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public Config getConfig() {
        Config config = new Config();
        config.setRoutes(getRoutes());
        config.setModels(getModels());
        config.setApplications(getApplications());
        config.setKeys(getKeys());
        config.setRoles(getRoles());
        config.setInterceptors(getInterceptors());
        config.setApplicationTypeSchemas(getApplicationTypeSchemas());
        config.setToolsets(getToolSets());

        return config;
    }

    private LinkedHashMap<String, CoreRoute> getRoutes() {
        return routeService.getAll().stream()
                .map(routeMapper::mapRoute)
                .collect(Collectors.toMap(
                        RoleBasedEntity::getName,
                        route -> route,
                        (existing, replacement) -> {
                            throw new IllegalStateException("Duplicate routes found: %s".formatted(existing));
                        },
                        LinkedHashMap<String, CoreRoute>::new
                ));
    }

    private Map<String, CoreModel> getModels() {
        return modelService.getAll().stream()
                .map(modelMapper::mapModel)
                .collect(Collectors.toMap(RoleBasedEntity::getName, model -> model));
    }

    private Map<String, CoreApplication> getApplications() {
        return applicationService.getAllApplications().stream()
                .map(applicationMapper::mapApplication)
                .collect(Collectors.toMap(RoleBasedEntity::getName, model -> model));
    }

    private Map<String, CoreKey> getKeys() {
        return keyService.getAllKeys().stream()
                .map(keyMapper::mapKey)
                .collect(Collectors.toMap(CoreKey::getKey, model -> model));
    }

    private Map<String, CoreRole> getRoles() {
        Collection<Deployment> deployments = deploymentService.getAll();
        return roleService.getAllRoles().stream()
                .map(role -> roleMapper.mapRole(role, deployments))
                .collect(Collectors.toMap(CoreRole::getName, model -> model));
    }

    private Map<String, CoreInterceptor> getInterceptors() {
        return interceptorService.getAll().stream()
                .map(interceptorMapper::mapInterceptor)
                .collect(Collectors.toMap(CoreInterceptor::getName, model -> model));
    }

    private Map<String, String> getApplicationTypeSchemas() {
        return applicationTypeSchemaService.getAll().stream()
                .collect(Collectors.toMap(ApplicationTypeSchema::getSchemaId, schemaMapper::mapToCoreString));
    }

    private Map<String, CoreToolSet> getToolSets() {
        return toolSetService.getAll().stream()
                .map(toolSetMapper::mapToolSet)
                .collect(Collectors.toMap(RoleBasedEntity::getName, toolSet -> toolSet));
    }

}
