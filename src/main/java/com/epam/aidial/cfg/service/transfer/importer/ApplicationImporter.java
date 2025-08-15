package com.epam.aidial.cfg.service.transfer.importer;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.mapper.ApplicationCoreMapper;
import com.epam.aidial.cfg.domain.model.Application;
import com.epam.aidial.cfg.domain.model.ImportAction;
import com.epam.aidial.cfg.domain.model.ImportComponent;
import com.epam.aidial.cfg.domain.model.Role;
import com.epam.aidial.cfg.domain.service.ApplicationService;
import com.epam.aidial.cfg.model.ConfigImportOptions;
import com.epam.aidial.cfg.service.export.ConflictResolutionPolicy;
import com.epam.aidial.core.config.CoreApplication;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.epam.aidial.cfg.domain.model.ImportAction.CREATE;
import static com.epam.aidial.cfg.domain.model.ImportAction.SKIP;
import static com.epam.aidial.cfg.domain.model.ImportAction.UPDATE;

@Service
@Slf4j
@LogExecution
@RequiredArgsConstructor
public class ApplicationImporter extends RoleBasedImporter {

    private final ApplicationService applicationService;
    private final ApplicationCoreMapper applicationCoreMapper;

    public Collection<ImportComponent<Application>> importApplications(Map<String, CoreApplication> coreApplications,
                                                                       Map<String, Role> roles,
                                                                       ConfigImportOptions importOptions,
                                                                       boolean isPreview) {
        if (MapUtils.isNotEmpty(coreApplications)) {
            Map<String, Application> applications = coreApplications.entrySet()
                    .stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, entry -> map(entry.getKey(), entry.getValue())));
            return importAdminApplications(applications, roles, importOptions, isPreview);
        }
        return Collections.emptyList();
    }

    public Collection<ImportComponent<Application>> importAdminApplications(Map<String, Application> applications,
                                                                            Map<String, Role> roles,
                                                                            ConfigImportOptions importOptions,
                                                                            boolean isPreview) {
        if (MapUtils.isNotEmpty(applications)) {
            return applications.entrySet().stream()
                    .map((appEntry) -> {
                                var application = appEntry.getValue();
                                var importAction = processApplication(appEntry.getKey(), application, roles, importOptions.conflictResolutionPolicy(), isPreview);
                                return new ImportComponent<>(importAction, application);
                            }
                    )
                    .toList();
        }
        return Collections.emptyList();
    }

    private ImportAction processApplication(String applicationName,
                                            Application newApplication,
                                            Map<String, Role> roles,
                                            ConflictResolutionPolicy resolutionPolicy,
                                            boolean isPreview) {
        Optional<Application> application = applicationService.tryGetApplication(applicationName);
        if (application.isPresent()) {
            Application existingApplication = application.get();
            setRoleLimits(applicationName, existingApplication.getDeployment().getRoleLimits(), roles, newApplication.getDeployment(), isPreview);
            return handleExisting(newApplication, resolutionPolicy, applicationName, isPreview);
        } else {
            setRoleLimits(applicationName, List.of(), roles, newApplication.getDeployment(), isPreview);
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

    private Application map(String appName, CoreApplication application) {
        application.setName(appName);
        return applicationCoreMapper.mapApplication(application);
    }

}
