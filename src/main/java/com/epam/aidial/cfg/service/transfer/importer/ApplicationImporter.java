package com.epam.aidial.cfg.service.transfer.importer;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.mapper.ApplicationCoreMapper;
import com.epam.aidial.cfg.domain.model.Application;
import com.epam.aidial.cfg.domain.model.Deployment;
import com.epam.aidial.cfg.domain.model.ImportAction;
import com.epam.aidial.cfg.domain.model.ImportComponent;
import com.epam.aidial.cfg.domain.model.Role;
import com.epam.aidial.cfg.domain.model.RoleLimit;
import com.epam.aidial.cfg.domain.model.RoleShareResourceLimit;
import com.epam.aidial.cfg.domain.model.ShareResourceLimit;
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
import java.util.function.Function;
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
                                                                       ConfigImportOptions importOptions) {
        if (MapUtils.isNotEmpty(coreApplications)) {
            Map<String, Application> applications = coreApplications.entrySet()
                    .stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, entry -> map(entry.getKey(), entry.getValue())));
            return importAdminApplications(applications, roles, importOptions);
        }
        return Collections.emptyList();
    }

    public Collection<ImportComponent<Application>> importAdminApplications(Map<String, Application> applications,
                                                                            Map<String, Role> roles,
                                                                            ConfigImportOptions importOptions) {
        if (MapUtils.isNotEmpty(applications)) {
            return applications.entrySet().stream()
                    .map((appEntry) -> {
                                var application = appEntry.getValue();
                                return processApplication(appEntry.getKey(), application, roles, importOptions.conflictResolutionPolicy());
                            }
                    )
                    .toList();
        }
        return Collections.emptyList();
    }

    private ImportComponent<Application> processApplication(String applicationName,
                                                            Application newApplication,
                                                            Map<String, Role> roles,
                                                            ConflictResolutionPolicy resolutionPolicy) {
        Optional<Application> application = applicationService.tryGetApplication(applicationName);
        if (application.isPresent()) {
            Application existingApplication = application.get();
            setLimits(applicationName, existingApplication.getDeployment(), roles, newApplication.getDeployment());
            ImportAction importAction = handleExisting(newApplication, resolutionPolicy, applicationName);
            return new ImportComponent<>(importAction, existingApplication, newApplication);
        } else {
            setLimits(applicationName, roles, newApplication.getDeployment());
            applicationService.createApplication(newApplication);
            return new ImportComponent<>(CREATE, null, newApplication);
        }
    }

    private ImportAction handleExisting(Application newApplication,
                                        ConflictResolutionPolicy resolutionPolicy,
                                        String applicationName) {
        return switch (resolutionPolicy) {
            case SKIP -> SKIP; // Do nothing, the existing application will remain unchanged.
            case OVERRIDE -> {
                applicationService.updateApplication(applicationName, newApplication);
                yield UPDATE;
            }
        };
    }

    private Application map(String appName, CoreApplication application) {
        application.setName(appName);
        ShareResourceLimit shareResourceLimit = new ShareResourceLimit();
        shareResourceLimit.setMaxAcceptedUsers(10);
        return applicationCoreMapper.mapApplication(application, shareResourceLimit);
    }

    public List<ImportComponent<Application>> getActualImportedApplications(Collection<ImportComponent<Application>> applicationImportComponents,
                                                                            Collection<ImportComponent<Role>> roleImportComponents) {
        List<String> names = applicationImportComponents.stream()
                .map(ImportComponent::getNext)
                .map(Application::getDeployment)
                .map(Deployment::getName)
                .toList();
        Map<String, Application> importedApplicationsByNames = applicationService.getAllByNames(names)
                .stream()
                .collect(Collectors.toMap(application -> application.getDeployment().getName(), Function.identity()));

        Collection<Role> importedRoles = roleImportComponents.stream().map(ImportComponent::getNext).toList();
        List<RoleLimit> importedRoleLimits = importedRoles.stream().map(Role::getLimits).flatMap(Collection::stream).toList();
        List<RoleShareResourceLimit> importedRoleShareResourceLimits = importedRoles.stream().map(Role::getShare).flatMap(Collection::stream).toList();

        return applicationImportComponents.stream()
                .map(importComponent -> {
                    var next = importedApplicationsByNames.get(importComponent.getNext().getDeployment().getName());
                    setImportedLimits(next, importedRoleLimits, importedRoleShareResourceLimits);
                    var prev = importComponent.getPrev();
                    clearTxDependentFields(next);
                    clearTxDependentFields(prev);
                    return new ImportComponent<>(importComponent.getImportAction(), prev, next);
                })
                .toList();
    }

    private void clearTxDependentFields(Application application) {
        if (application != null) {
            application.setCreatedAt(null);
            application.setUpdatedAt(null);
        }
    }
}
