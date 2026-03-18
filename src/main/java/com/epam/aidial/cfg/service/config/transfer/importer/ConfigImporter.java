package com.epam.aidial.cfg.service.config.transfer.importer;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dao.audit.listener.AuditParentActivityHolder;
import com.epam.aidial.cfg.domain.model.ExportConfig;
import com.epam.aidial.cfg.domain.model.ImportComponent;
import com.epam.aidial.cfg.domain.model.ImportConfigPreview;
import com.epam.aidial.cfg.domain.service.AuditActivityLogService;
import com.epam.aidial.cfg.model.ConfigImportOptions;
import com.epam.aidial.cfg.service.config.export.ConflictResolutionPolicy;
import com.epam.aidial.cfg.service.config.transfer.importer.compatibility.backward.AdminConfigImportBackwardCompatibilityHandler;
import com.epam.aidial.cfg.service.config.transfer.importer.util.CoreRolesImportPreProcessor;
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

    private final CoreRolesImportPreProcessor coreRolesImportPreProcessor;

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
    private final GlobalSettingsImporter globalSettingsImporter;

    private final AdminConfigImportBackwardCompatibilityHandler adminConfigImportBackwardCompatibilityHandler;
    private final AuditActivityLogService auditActivityLogService;
    private final AuditParentActivityHolder auditParentActivityHolder;

    @Transactional
    public ImportConfigPreview importPreview(Config config, ConfigImportOptions importOptions) {
        // mark that transaction should be rolled back. This is a trick to get actual state of importing objects
        // with all associations but doesn't save the state into DB
        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();

        var resolutionPolicy = importOptions.conflictResolutionPolicy();
        var rolesPreImportInfo = coreRolesImportPreProcessor.preProcessRolesImport(config, importOptions.createRoleIfAbsent());

        var interceptors = interceptorImporter.importInterceptors(config.getInterceptors(), resolutionPolicy);
        var applicationRunners = applicationTypeSchemaImporter.importSchemas(config.getApplicationTypeSchemas(), resolutionPolicy);
        var adapters = adapterImporter.importAdapters(config.getModels(), importOptions, true);
        var models = modelImporter.importModels(config.getModels(), importOptions);
        var addons = addonImporter.importAddons(config.getAddons(), importOptions);
        var applications = applicationImporter.importApplications(config.getApplications(), config.getApplicationTypeSchemas(), importOptions);
        var routes = routeImporter.importRoutes(config.getRoutes(), importOptions);
        var assistants = assistantImporter.importAssistants(config.getAssistant(), importOptions);
        var toolSets = toolSetImporter.importToolSets(config.getToolsets(), importOptions);
        var roles = roleImporter.importRoles(config.getRoles(), rolesPreImportInfo, resolutionPolicy);
        var keys = keyImporter.importKeys(config.getKeys(), resolutionPolicy, true);
        var globalSettings = globalSettingsImporter.importGlobalSettings(config.getGlobalInterceptors(), resolutionPolicy);
        var globalInterceptors = new ImportComponent<>(
                globalSettings.getImportAction(),
                globalSettings.getPrev().getGlobalInterceptors(),
                globalSettings.getNext().getGlobalInterceptors());

        interceptors = interceptorImporter.getActualImportedInterceptors(interceptors);
        applicationRunners = applicationTypeSchemaImporter.getActualImportedApplicationTypeSchemas(applicationRunners);
        adapters = adapterImporter.getActualImportedAdapters(adapters);
        models = modelImporter.getActualImportedModels(models);
        addons = addonImporter.getActualImportedAddons(addons);
        applications = applicationImporter.getActualImportedApplications(applications);
        routes = routeImporter.getActualImportedRoutes(routes);
        assistants = assistantImporter.getActualImportedAssistants(assistants);
        toolSets = toolSetImporter.getActualImportedToolSets(toolSets);
        roles = roleImporter.getActualImportedRoles(roles);
        keys = keyImporter.getActualImportedKeys(keys);

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
                .globalInterceptors(globalInterceptors)
                .build();
    }

    @Transactional
    public ImportConfigPreview importPreviewAdminConfig(ExportConfig config, ConflictResolutionPolicy resolutionPolicy) {
        // mark that transaction should be rolled back. This is a trick to get actual state of importing objects
        // with all associations but doesn't save the state into DB
        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();

        adminConfigImportBackwardCompatibilityHandler.transformToLatestVersion(config);

        var importOptions = new ConfigImportOptions(resolutionPolicy, false, false);

        var interceptorRunners = interceptorRunnerImporter.importAdminInterceptorRunners(config.getInterceptorRunners(), resolutionPolicy);
        var interceptors = interceptorImporter.importAdminInterceptors(config.getInterceptors(), resolutionPolicy);
        var applicationRunners = applicationTypeSchemaImporter.importAdminSchemas(config.getApplicationRunners(), resolutionPolicy);
        var routes = routeImporter.importAdminRoutes(config.getRoutes(), importOptions);
        var adapters = adapterImporter.importAdminAdapters(config.getAdapters(), importOptions);
        var models = modelImporter.importAdminModels(config.getModels(), importOptions);
        var applications = applicationImporter.importAdminApplications(config.getApplications(), importOptions);
        var toolSets = toolSetImporter.importAdminToolSets(config.getToolsets(), importOptions);
        var roles = roleImporter.importAdminRoles(config.getRoles(), resolutionPolicy);
        var keys = keyImporter.importAdminKeys(config.getKeys(), resolutionPolicy);
        var globalSettings = globalSettingsImporter.importGlobalSettings(config.getGlobalInterceptors(), resolutionPolicy);
        var globalInterceptors = new ImportComponent<>(
                globalSettings.getImportAction(),
                globalSettings.getPrev().getGlobalInterceptors(),
                globalSettings.getNext().getGlobalInterceptors());

        interceptorRunners = interceptorRunnerImporter.getActualImportedInterceptorRunners(interceptorRunners);
        interceptors = interceptorImporter.getActualImportedInterceptors(interceptors);
        applicationRunners = applicationTypeSchemaImporter.getActualImportedApplicationTypeSchemas(applicationRunners);
        routes = routeImporter.getActualImportedRoutes(routes);
        adapters = adapterImporter.getActualImportedAdapters(adapters);
        models = modelImporter.getActualImportedModels(models);
        applications = applicationImporter.getActualImportedApplications(applications);
        toolSets = toolSetImporter.getActualImportedToolSets(toolSets);
        roles = roleImporter.getActualImportedRoles(roles);
        keys = keyImporter.getActualImportedKeys(keys);

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
                .globalInterceptors(globalInterceptors)
                .build();
    }

    public void importConfigWithOverride(Config config) {
        var parentId = auditActivityLogService.logImportOperation("core", new ConfigImportOptions(ConflictResolutionPolicy.OVERRIDE));
        try (var scope = auditParentActivityHolder.openScope(parentId)) {
            importConfigTransactional(config, new ConfigImportOptions(ConflictResolutionPolicy.OVERRIDE));
        }
    }

    public void importConfig(Config config, ConfigImportOptions importOptions) {
        var parentId = auditActivityLogService.logImportOperation("core", importOptions);
        try (var scope = auditParentActivityHolder.openScope(parentId)) {
            importConfigTransactional(config, importOptions);
        }
    }

    @Transactional
    public void importConfigTransactional(Config config, ConfigImportOptions importOptions) {
        var parentId = auditActivityLogService.logImportOperation("core", importOptions);
        try (var scope = auditParentActivityHolder.openScope(parentId)) {
            var resolutionPolicy = importOptions.conflictResolutionPolicy();
            var rolesPreImportInfo = coreRolesImportPreProcessor.preProcessRolesImport(config, importOptions.createRoleIfAbsent());

            interceptorImporter.importInterceptors(config.getInterceptors(), resolutionPolicy);
            applicationTypeSchemaImporter.importSchemas(config.getApplicationTypeSchemas(), resolutionPolicy);
            adapterImporter.importAdapters(config.getModels(), importOptions, false);
            modelImporter.importModels(config.getModels(), importOptions);
            addonImporter.importAddons(config.getAddons(), importOptions);
            applicationImporter.importApplications(config.getApplications(), config.getApplicationTypeSchemas(), importOptions);
            routeImporter.importRoutes(config.getRoutes(), importOptions);
            assistantImporter.importAssistants(config.getAssistant(), importOptions);
            toolSetImporter.importToolSets(config.getToolsets(), importOptions);
            roleImporter.importRoles(config.getRoles(), rolesPreImportInfo, resolutionPolicy);
            keyImporter.importKeys(config.getKeys(), resolutionPolicy, false);
            globalSettingsImporter.importGlobalSettings(config.getGlobalInterceptors(), resolutionPolicy);
        }
    }

    public void importAdminConfig(ExportConfig config, ConfigImportOptions importOptions) {
        adminConfigImportBackwardCompatibilityHandler.transformToLatestVersion(config);
        var parentId = auditActivityLogService.logImportOperation("admin", importOptions);
        try (var scope = auditParentActivityHolder.openScope(parentId)) {
            importAdminConfigTransactional(config, importOptions);
        }
    }

    @Transactional
    public void importAdminConfigTransactional(ExportConfig config, ConfigImportOptions importOptions) {
        adminConfigImportBackwardCompatibilityHandler.transformToLatestVersion(config);
        var parentId = auditActivityLogService.logImportOperation("admin", importOptions);
        try (var scope = auditParentActivityHolder.openScope(parentId)) {
            var resolutionPolicy = importOptions.conflictResolutionPolicy();

            interceptorRunnerImporter.importAdminInterceptorRunners(config.getInterceptorRunners(), resolutionPolicy);
            interceptorImporter.importAdminInterceptors(config.getInterceptors(), resolutionPolicy);
            applicationTypeSchemaImporter.importAdminSchemas(config.getApplicationRunners(), resolutionPolicy);
            routeImporter.importAdminRoutes(config.getRoutes(), importOptions);
            adapterImporter.importAdminAdapters(config.getAdapters(), importOptions);
            modelImporter.importAdminModels(config.getModels(), importOptions);
            applicationImporter.importAdminApplications(config.getApplications(), importOptions);
            toolSetImporter.importAdminToolSets(config.getToolsets(), importOptions);
            roleImporter.importAdminRoles(config.getRoles(), resolutionPolicy);
            keyImporter.importAdminKeys(config.getKeys(), resolutionPolicy);
            globalSettingsImporter.importGlobalSettings(config.getGlobalInterceptors(), resolutionPolicy);
        }
    }
}