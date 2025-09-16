package com.epam.aidial.cfg.service.transfer.importer;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.model.ExportConfig;
import com.epam.aidial.cfg.domain.model.ImportConfigPreview;
import com.epam.aidial.cfg.model.ConfigImportOptions;
import com.epam.aidial.cfg.service.export.ConflictResolutionPolicy;
import com.epam.aidial.cfg.service.transfer.importer.util.CoreRolesMerger;
import com.epam.aidial.core.config.Config;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

@Service
@LogExecution
@Slf4j
@RequiredArgsConstructor
public class ConfigImporter {

    private final CoreRolesMerger coreRolesMerger;

    private final ModelImporter modelImporter;
    private final AddonImporter addonImporter;
    private final ApplicationImporter applicationImporter;
    private final KeyImporter keyImporter;
    private final RoleImporter roleImporter;
    private final InterceptorImporter interceptorImporter;
    private final InterceptorRunnerImporter interceptorRunnerImporter;
    private final ApplicationTypeSchemaImporter applicationTypeSchemaImporter;
    private final RouteImporter routeImporter;
    private final AssistantImporter assistantImporter;
    private final AdapterImporter adapterImporter;
    private final ToolSetImporter toolSetImporter;

    @Transactional
    public ImportConfigPreview importPreview(Config config, ConfigImportOptions importOptions) {
        // mark that transaction should be rolled back. This is a trick to get actual state of importing objects
        // with all associations but doesn't save the state into DB
        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();

        var resolutionPolicy = importOptions.conflictResolutionPolicy();
        var configRoles = coreRolesMerger.mergeCoreRoles(config, importOptions.createRoleIfAbsent());

        var interceptors = interceptorImporter.importInterceptors(config.getInterceptors(), resolutionPolicy);
        var applicationRunners = applicationTypeSchemaImporter.importSchemas(config.getApplicationTypeSchemas(), resolutionPolicy);
        var adapters = adapterImporter.importAdapters(config.getModels(), importOptions, true);
        var models = modelImporter.importModels(config.getModels(), configRoles, importOptions);
        var addons = addonImporter.importAddons(config.getAddons(), configRoles, importOptions);
        var applications = applicationImporter.importApplications(config.getApplications(), configRoles, importOptions);
        var routes = routeImporter.importRoutes(config.getRoutes(), configRoles, importOptions);
        var assistants = assistantImporter.importAssistants(config.getAssistant(), configRoles, importOptions);
        var toolSets = toolSetImporter.importToolSets(config.getToolsets(), configRoles, importOptions);
        var roles = roleImporter.importRoles(configRoles, resolutionPolicy);
        var keys = keyImporter.importKeys(config.getKeys(), resolutionPolicy, true);

        keys = keyImporter.getActualImportedKeys(keys);
        roles = roleImporter.getActualImportedRoles(roles);
        toolSets = toolSetImporter.getActualImportedToolSets(toolSets, roles);
        assistants = assistantImporter.getActualImportedAssistants(assistants, roles);
        routes = routeImporter.getActualImportedRoutes(routes, roles);
        applications = applicationImporter.getActualImportedApplications(applications, roles);
        addons = addonImporter.getActualImportedAddons(addons, roles);
        models = modelImporter.getActualImportedModels(models, roles);
        adapters = adapterImporter.getActualImportedAdapters(adapters);
        applicationRunners = applicationTypeSchemaImporter.getActualImportedApplicationTypeSchemas(applicationRunners);
        interceptors = interceptorImporter.getActualImportedInterceptors(interceptors);

        return ImportConfigPreview.builder()
                .roles(roles)
                .keys(keys)
                .interceptors(interceptors)
                .applicationRunners(applicationRunners)
                .routes(routes)
                .adapters(adapters)
                .models(models)
                .applications(applications)
                .addons(addons)
                .assistants(assistants)
                .toolSets(toolSets)
                .build();
    }

    @Transactional
    public ImportConfigPreview importPreviewAdminConfig(ExportConfig config, ConflictResolutionPolicy resolutionPolicy) {
        // mark that transaction should be rolled back. This is a trick to get actual state of importing objects
        // with all associations but doesn't save the state into DB
        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();

        var importOptions = new ConfigImportOptions(resolutionPolicy, false, false);

        var interceptorRunners = interceptorRunnerImporter.importAdminInterceptorRunners(config.getInterceptorRunners(), resolutionPolicy);
        var interceptors = interceptorImporter.importAdminInterceptors(config.getInterceptors(), resolutionPolicy);
        var applicationRunners = applicationTypeSchemaImporter.importAdminSchemas(config.getApplicationRunners(), resolutionPolicy);
        var routes = routeImporter.importAdminRoutes(config.getRoutes(), config.getRoles(), importOptions);
        var adapters = adapterImporter.importAdminAdapters(config.getAdapters(), importOptions);
        var models = modelImporter.importAdminModels(config.getModels(), config.getRoles(), importOptions);
        var applications = applicationImporter.importAdminApplications(config.getApplications(), config.getRoles(), importOptions);
        var toolSets = toolSetImporter.importAdminToolSets(config.getToolsets(), config.getRoles(), importOptions);
        var roles = roleImporter.importAdminRoles(config.getRoles(), resolutionPolicy);
        var keys = keyImporter.importAdminKeys(config.getKeys(), resolutionPolicy);

        keys = keyImporter.getActualImportedKeys(keys);
        roles = roleImporter.getActualImportedRoles(roles);
        toolSets = toolSetImporter.getActualImportedToolSets(toolSets, roles);
        applications = applicationImporter.getActualImportedApplications(applications, roles);
        models = modelImporter.getActualImportedModels(models, roles);
        adapters = adapterImporter.getActualImportedAdapters(adapters);
        routes = routeImporter.getActualImportedRoutes(routes, roles);
        applicationRunners = applicationTypeSchemaImporter.getActualImportedApplicationTypeSchemas(applicationRunners);
        interceptors = interceptorImporter.getActualImportedInterceptors(interceptors);
        interceptorRunners = interceptorRunnerImporter.getActualImportedInterceptorRunners(interceptorRunners);

        return ImportConfigPreview.builder()
                .roles(roles)
                .keys(keys)
                .interceptors(interceptors)
                .interceptorRunners(interceptorRunners)
                .applicationRunners(applicationRunners)
                .routes(routes)
                .adapters(adapters)
                .models(models)
                .applications(applications)
                .toolSets(toolSets)
                .build();
    }

    @Transactional
    public void importConfig(Config config, ConfigImportOptions importOptions) {
        var resolutionPolicy = importOptions.conflictResolutionPolicy();
        var configRoles = coreRolesMerger.mergeCoreRoles(config, importOptions.createRoleIfAbsent());

        interceptorImporter.importInterceptors(config.getInterceptors(), resolutionPolicy);
        applicationTypeSchemaImporter.importSchemas(config.getApplicationTypeSchemas(), resolutionPolicy);
        adapterImporter.importAdapters(config.getModels(), importOptions, false);
        modelImporter.importModels(config.getModels(), configRoles, importOptions);
        addonImporter.importAddons(config.getAddons(), configRoles, importOptions);
        applicationImporter.importApplications(config.getApplications(), configRoles, importOptions);
        routeImporter.importRoutes(config.getRoutes(), configRoles, importOptions);
        assistantImporter.importAssistants(config.getAssistant(), configRoles, importOptions);
        toolSetImporter.importToolSets(config.getToolsets(), configRoles, importOptions);
        roleImporter.importRoles(configRoles, resolutionPolicy);
        keyImporter.importKeys(config.getKeys(), resolutionPolicy, false);
    }

    @Transactional
    public void importAdminConfig(ExportConfig config, ConfigImportOptions importOptions) {
        var resolutionPolicy = importOptions.conflictResolutionPolicy();

        interceptorRunnerImporter.importAdminInterceptorRunners(config.getInterceptorRunners(), resolutionPolicy);
        interceptorImporter.importAdminInterceptors(config.getInterceptors(), resolutionPolicy);
        applicationTypeSchemaImporter.importAdminSchemas(config.getApplicationRunners(), resolutionPolicy);
        routeImporter.importAdminRoutes(config.getRoutes(), config.getRoles(), importOptions);
        adapterImporter.importAdminAdapters(config.getAdapters(), importOptions);
        modelImporter.importAdminModels(config.getModels(), config.getRoles(), importOptions);
        applicationImporter.importAdminApplications(config.getApplications(), config.getRoles(), importOptions);
        toolSetImporter.importAdminToolSets(config.getToolsets(), config.getRoles(), importOptions);
        roleImporter.importAdminRoles(config.getRoles(), resolutionPolicy);
        keyImporter.importAdminKeys(config.getKeys(), resolutionPolicy);
    }
}
