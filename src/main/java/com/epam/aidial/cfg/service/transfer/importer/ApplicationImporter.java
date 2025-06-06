package com.epam.aidial.cfg.service.transfer.importer;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.mapper.ApplicationCoreMapper;
import com.epam.aidial.cfg.domain.model.Application;
import com.epam.aidial.cfg.domain.model.ImportAction;
import com.epam.aidial.cfg.domain.model.ImportComponent;
import com.epam.aidial.cfg.domain.service.ApplicationService;
import com.epam.aidial.cfg.domain.service.RoleService;
import com.epam.aidial.cfg.model.ConfigImportOptions;
import com.epam.aidial.cfg.service.export.ConflictResolutionPolicy;
import com.epam.aidial.core.config.CoreApplication;
import com.epam.aidial.core.config.CoreRole;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import static com.epam.aidial.cfg.domain.model.ImportAction.CREATE;
import static com.epam.aidial.cfg.domain.model.ImportAction.SKIP;
import static com.epam.aidial.cfg.domain.model.ImportAction.UPDATE;

@Service
@Slf4j
@LogExecution

public class ApplicationImporter extends RoleBasedImporter {

    private final ApplicationService applicationService;
    private final ApplicationCoreMapper applicationCoreMapper;

    public ApplicationImporter(RoleService roleService, ApplicationService applicationService, ApplicationCoreMapper applicationCoreMapper) {
        super(roleService);
        this.applicationService = applicationService;
        this.applicationCoreMapper = applicationCoreMapper;
    }

    public Collection<ImportComponent<Application>> importApplications(Map<String, CoreApplication> coreApplications,
                                                                       Map<String, CoreRole> roles,
                                                                       ConfigImportOptions importOptions,
                                                                       boolean isPreview) {
        if (MapUtils.isNotEmpty(coreApplications)) {
            Map<String, Application> applications = coreApplications.entrySet()
                    .stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, entry -> map(entry.getKey(), entry.getValue(), roles)));
            return importAdminApplications(applications, importOptions, isPreview);
        }
        return Collections.emptyList();
    }

    public Collection<ImportComponent<Application>> importAdminApplications(Map<String, Application> applications,
                                                                            ConfigImportOptions importOptions,
                                                                            boolean isPreview) {
        if (MapUtils.isNotEmpty(applications)) {
            return applications.entrySet().stream()
                    .map((appEntry) -> {
                                var application = appEntry.getValue();
                                createRoleIfAbsent(importOptions, application.getDeployment().getRoleLimits());
                                var importAction = processApplication(appEntry.getKey(), application, importOptions.getConflictResolutionPolicy(), isPreview);
                                return new ImportComponent<>(importAction, application);
                            }
                    )
                    .toList();
        }
        return Collections.emptyList();
    }

    private ImportAction processApplication(String applicationName,
                                            Application newApplication,
                                            ConflictResolutionPolicy resolutionPolicy,
                                            boolean isPreview) {
        if (applicationService.exists(applicationName)) {
            return handleExisting(newApplication, resolutionPolicy, applicationName, isPreview);
        } else {
            return create(newApplication, isPreview);
        }
    }

    private ImportAction handleExisting(Application newApplication,
                                        ConflictResolutionPolicy resolutionPolicy,
                                        String applicationName,
                                        boolean isPreview) {
        switch (resolutionPolicy) {
            case SKIP -> {
                // Do nothing, the existing application will remain unchanged.
                return SKIP;
            }
            case OVERRIDE -> {
                if (!isPreview) {
                    applicationService.updateApplication(applicationName, newApplication);
                }
                return UPDATE;
            }
            default -> throw new IllegalArgumentException("Unexpected resolutionPolicy: " + resolutionPolicy);
        }
    }

    private ImportAction create(Application application, boolean isPreview) {
        if (!isPreview) {
            applicationService.createApplication(application);
        }
        return CREATE;
    }

    private Application map(String appName, CoreApplication application, Map<String, CoreRole> roles) {
        application.setName(appName);
        return applicationCoreMapper.mapApplication(application, roles);
    }

}
