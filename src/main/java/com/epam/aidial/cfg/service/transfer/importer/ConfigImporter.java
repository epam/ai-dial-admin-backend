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

import java.util.List;

@Service
@LogExecution
@Slf4j
@RequiredArgsConstructor
public class ConfigImporter {

    private final CoreRolesMerger coreRolesMerger;

    private final ModelImporter modelImporter;
    private final AddonImporter addonTransfer;
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

    @Transactional(readOnly = true)
    public ImportConfigPreview importPreview(Config config, ConfigImportOptions importOptions) {
        var resolutionPolicy = importOptions.conflictResolutionPolicy();
        var configRoles = coreRolesMerger.mergeCoreRoles(config, importOptions.createRoleIfAbsent());

        var interceptors = interceptorImporter.importInterceptors(config.getInterceptors(), resolutionPolicy, true);
        var applicationRunners = applicationTypeSchemaImporter.importSchemas(config.getApplicationTypeSchemas(), resolutionPolicy, true);
        var adapters = adapterImporter.importAdapters(config.getModels(), importOptions, true);
        var models = modelImporter.importModels(config.getModels(), configRoles, importOptions, adapters, true);
        var addons = addonTransfer.importAddons(config.getAddons(), configRoles, importOptions, true);
        var applications = applicationImporter.importApplications(config.getApplications(), configRoles, importOptions, true);
        var routes = routeImporter.importRoutes(config.getRoutes(), configRoles, importOptions, true);
        var assistants = assistantImporter.importAssistants(config.getAssistant(), configRoles, importOptions, true);
        var toolSets = toolSetImporter.importToolSets(config.getToolsets(), configRoles, importOptions, true);
        var roles = roleImporter.importRoles(configRoles, resolutionPolicy, true);
        var keys = keyImporter.importKeys(config.getKeys(), resolutionPolicy, true);

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

    @Transactional(readOnly = true)
    public ImportConfigPreview importPreviewAdminConfig(ExportConfig config, ConflictResolutionPolicy resolutionPolicy) {
        var importOptions = new ConfigImportOptions(resolutionPolicy, false, false);

        var interceptorRunners = interceptorRunnerImporter.importAdminInterceptorRunners(config.getInterceptorRunners(), resolutionPolicy, true);
        var interceptors = interceptorImporter.importAdminInterceptors(config.getInterceptors(), resolutionPolicy, true);
        var applicationRunners = applicationTypeSchemaImporter.importAdminSchemas(config.getApplicationRunners(), resolutionPolicy, true);
        var routes = routeImporter.importAdminRoutes(config.getRoutes(), config.getRoles(), importOptions, true);
        var adapters = adapterImporter.importAdminAdapters(config.getAdapters(), importOptions, true);
        var models = modelImporter.importAdminModels(config.getModels(), config.getRoles(), importOptions, true);
        var applications = applicationImporter.importAdminApplications(config.getApplications(), config.getRoles(), importOptions, true);
        var toolSets = toolSetImporter.importAdminToolSets(config.getToolsets(), config.getRoles(), importOptions, true);
        var roles = roleImporter.importAdminRoles(config.getRoles(), resolutionPolicy, true);
        var keys = keyImporter.importAdminKeys(config.getKeys(), resolutionPolicy, true);

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

        interceptorImporter.importInterceptors(config.getInterceptors(), resolutionPolicy, false);
        applicationTypeSchemaImporter.importSchemas(config.getApplicationTypeSchemas(), resolutionPolicy, false);
        adapterImporter.importAdapters(config.getModels(), importOptions, false);
        modelImporter.importModels(config.getModels(), configRoles, importOptions, List.of(), false);
        addonTransfer.importAddons(config.getAddons(), configRoles, importOptions, false);
        applicationImporter.importApplications(config.getApplications(), configRoles, importOptions, false);
        routeImporter.importRoutes(config.getRoutes(), configRoles, importOptions, false);
        assistantImporter.importAssistants(config.getAssistant(), configRoles, importOptions, false);
        toolSetImporter.importToolSets(config.getToolsets(), configRoles, importOptions, false);
        roleImporter.importRoles(configRoles, resolutionPolicy, false);
        keyImporter.importKeys(config.getKeys(), resolutionPolicy, false);
    }

    @Transactional
    public void importAdminConfig(ExportConfig config, ConfigImportOptions importOptions) {
        var resolutionPolicy = importOptions.conflictResolutionPolicy();

        interceptorRunnerImporter.importAdminInterceptorRunners(config.getInterceptorRunners(), resolutionPolicy, false);
        interceptorImporter.importAdminInterceptors(config.getInterceptors(), resolutionPolicy, false);
        applicationTypeSchemaImporter.importAdminSchemas(config.getApplicationRunners(), resolutionPolicy, false);
        routeImporter.importAdminRoutes(config.getRoutes(), config.getRoles(), importOptions, false);
        adapterImporter.importAdminAdapters(config.getAdapters(), importOptions, false);
        modelImporter.importAdminModels(config.getModels(), config.getRoles(), importOptions, false);
        applicationImporter.importAdminApplications(config.getApplications(), config.getRoles(), importOptions, false);
        toolSetImporter.importAdminToolSets(config.getToolsets(), config.getRoles(), importOptions, false);
        roleImporter.importAdminRoles(config.getRoles(), resolutionPolicy, false);
        keyImporter.importAdminKeys(config.getKeys(), resolutionPolicy, false);
    }
}
