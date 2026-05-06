package com.epam.aidial.cfg.service.config.export;

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
import com.epam.aidial.cfg.domain.model.GlobalSettings;
import com.epam.aidial.cfg.domain.model.Key;
import com.epam.aidial.cfg.domain.model.Model;
import com.epam.aidial.cfg.domain.model.source.ModelAdapterSource;
import com.epam.aidial.cfg.domain.service.AdapterService;
import com.epam.aidial.cfg.domain.service.ApplicationService;
import com.epam.aidial.cfg.domain.service.ApplicationTypeSchemaService;
import com.epam.aidial.cfg.domain.service.DeploymentService;
import com.epam.aidial.cfg.domain.service.GlobalSettingsService;
import com.epam.aidial.cfg.domain.service.InterceptorService;
import com.epam.aidial.cfg.domain.service.KeyService;
import com.epam.aidial.cfg.domain.service.ModelService;
import com.epam.aidial.cfg.domain.service.RoleService;
import com.epam.aidial.cfg.domain.service.RouteService;
import com.epam.aidial.cfg.domain.service.ToolSetService;
import com.epam.aidial.cfg.domain.utils.ModelEndpointUtils;
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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CoreConfigAggregatorService {

    private final ApplicationService applicationService;
    private final ApplicationTypeSchemaService applicationTypeSchemaService;
    private final InterceptorService interceptorService;
    private final GlobalSettingsService globalSettingsService;
    private final KeyService keyService;
    private final ModelService modelService;
    private final RoleService roleService;
    private final RouteService routeService;
    private final DeploymentService deploymentService;
    private final ToolSetService toolSetService;
    private final AdapterService adapterService;

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
        var globalSettings = getGlobalSettings();
        config.setGlobalInterceptors(globalSettings.getGlobalInterceptors());
        return config;
    }

    private LinkedHashMap<String, CoreRoute> getRoutes() {
        return routeService.getAllOrderedByDisplayNameAscNameAsc().stream()
                .map(routeMapper::mapRoute)
                .collect(toLinkedHashMap(RoleBasedEntity::getName));
    }

    private LinkedHashMap<String, CoreModel> getModels() {
        return modelService.getAllOrderedByDisplayNameAscDisplayVersionAscNameAsc().stream()
                .map(model -> {
                    Pair<String, String> modelEndpoints = getModelEndpoints(model);
                    String endpoint = modelEndpoints.getLeft();
                    String responsesEndpoint = modelEndpoints.getRight();
                    return modelMapper.mapModel(model, endpoint, responsesEndpoint);
                })
                .collect(toLinkedHashMap(RoleBasedEntity::getName));
    }

    private Pair<String, String> getModelEndpoints(Model model) {
        if (model.getSource() instanceof ModelAdapterSource adapterSource) {
            var adapter = adapterService.get(adapterSource.getAdapterName());
            return Pair.of(
                    ModelEndpointUtils.concatEndpointAndPath(adapter.getBaseEndpoint(), adapterSource.getCompletionEndpointPath()),
                    adapter.getResponsesEndpoint()
            );
        }
        return Pair.of(model.getEndpoint(), model.getResponsesEndpoint());
    }

    private LinkedHashMap<String, CoreApplication> getApplications() {
        return applicationService.getAllValidApplicationsOrderedByDisplayNameAscDisplayVersionAscNameAsc().stream()
                .map(applicationMapper::mapApplication)
                .collect(toLinkedHashMap(RoleBasedEntity::getName));
    }

    private LinkedHashMap<String, CoreKey> getKeys() {
        return keyService.getAllValidKeysOrderedByDisplayNameAscNameAsc().stream()
                .filter(key -> {
                    if (StringUtils.isNotBlank(key.getKey())) {
                        return true;
                    } else {
                        log.debug("getKeys. remove invalid key with blank key value, key name: {}", key.getName());
                        return false;
                    }
                })
                .collect(toLinkedHashMap(Key::getKey, keyMapper::mapKey));
    }

    private LinkedHashMap<String, CoreRole> getRoles() {
        Collection<Deployment> deployments = deploymentService.getAll();
        return roleService.getAllOrderedByDisplayNameAscNameAsc().stream()
                .map(role -> roleMapper.mapRole(role, deployments))
                .collect(toLinkedHashMap(CoreRole::getName));
    }

    private LinkedHashMap<String, CoreInterceptor> getInterceptors() {
        return interceptorService.getAllOrderedByDisplayNameAscNameAsc().stream()
                .map(interceptorMapper::mapInterceptor)
                .collect(toLinkedHashMap(CoreInterceptor::getName));
    }

    private GlobalSettings getGlobalSettings() {
        return globalSettingsService.getGlobalSettings();
    }

    private LinkedHashMap<String, String> getApplicationTypeSchemas() {
        return applicationTypeSchemaService.getAllOrderedByDisplayNameAscIdAsc().stream()
                .collect(toLinkedHashMap(ApplicationTypeSchema::getSchemaId, schemaMapper::mapToCoreString));
    }

    private LinkedHashMap<String, CoreToolSet> getToolSets() {
        return toolSetService.getAllOrderedByDisplayNameAscNameAsc().stream()
                .map(toolSetMapper::mapToolSet)
                .collect(toLinkedHashMap(RoleBasedEntity::getName));
    }

    private <T> Collector<T, ?, LinkedHashMap<String, T>> toLinkedHashMap(Function<T, String> keyMapper) {
        return toLinkedHashMap(keyMapper, Function.identity());
    }

    private <T, R> Collector<T, ?, LinkedHashMap<String, R>> toLinkedHashMap(Function<T, String> keyMapper,
                                                                             Function<T, R> valueMapper) {
        return Collectors.toMap(
                keyMapper,
                valueMapper,
                (a, b) -> {
                    throw new IllegalStateException("Duplicated objects found: %s".formatted(a));
                },
                LinkedHashMap::new
        );
    }

}