package com.epam.aidial.cfg.domain.service;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@LogExecution
public class RollbackService {

    private final RoleService roleService;
    private final KeyService keyService;
    private final AdapterService adapterService;
    private final ModelService modelService;
    private final AddonService addonService;
    private final ToolSetService toolSetService;
    private final ApplicationTypeSchemaService applicationTypeSchemaService;
    private final ApplicationService applicationService;
    private final AssistantService assistantService;
    private final AssistantsPropertyService assistantsPropertyService;
    private final RouteService routeService;
    private final InterceptorService interceptorService;
    private final InterceptorRunnerService interceptorRunnerService;
    private final GlobalSettingsService globalSettingsService;
    private final AdminSettingsService adminSettingsService;

    @Transactional
    public void rollbackToRevision(Number revision) {
        roleService.rollbackRoles(revision);
        keyService.rollbackKeys(revision);
        modelService.rollbackModels(revision);
        adapterService.rollbackAdapters(revision);
        addonService.rollbackAddons(revision);
        toolSetService.rollbackToolSets(revision);
        applicationService.rollbackApplications(revision);
        applicationTypeSchemaService.rollbackApplicationTypeSchemas(revision);
        assistantService.rollbackAssistants(revision);
        assistantsPropertyService.rollbackAssistantsProperties(revision);
        routeService.rollbackRoutes(revision);
        interceptorService.rollbackInterceptors(revision);
        interceptorRunnerService.rollbackInterceptorRunners(revision);
        globalSettingsService.rollbackGlobalSettings(revision);
        adminSettingsService.rollbackAdminSettings(revision);
    }
}