package com.epam.aidial.cfg.service.transfer.importer;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.mapper.ApplicationCoreMapper;
import com.epam.aidial.cfg.domain.model.Application;
import com.epam.aidial.cfg.domain.model.ImportAction;
import com.epam.aidial.cfg.domain.model.ImportComponent;
import com.epam.aidial.cfg.domain.model.RoleLimit;
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
public class ApplicationImporter extends DeploymentHolderImporter {

    private final ApplicationService applicationService;
    private final ApplicationCoreMapper applicationCoreMapper;

    public Collection<ImportComponent<Application>> importApplications(Map<String, CoreApplication> coreApplications,
                                                                       ConfigImportOptions importOptions) {
        if (MapUtils.isEmpty(coreApplications)) {
            return Collections.emptyList();
        }

        return coreApplications.entrySet()
                .stream()
                .map(entry -> processApplication(entry.getKey(), entry.getValue(), importOptions.conflictResolutionPolicy()))
                .toList();
    }

    public Collection<ImportComponent<Application>> importAdminApplications(Map<String, Application> applications,
                                                                            ConfigImportOptions importOptions) {
        if (MapUtils.isEmpty(applications)) {
            return Collections.emptyList();
        }

        return applications.entrySet().stream()
                .map(entry -> processApplication(entry.getKey(), entry.getValue(), importOptions.conflictResolutionPolicy()))
                .toList();
    }

    private ImportComponent<Application> processApplication(String applicationName,
                                                            CoreApplication coreApplication,
                                                            ConflictResolutionPolicy resolutionPolicy) {
        Optional<Application> application = applicationService.tryGetApplication(applicationName);
        if (application.isPresent()) {
            Application existingApplication = application.get();
            Application existingApplicationCopy = applicationCoreMapper.copy(existingApplication);
            List<RoleLimit> roleLimits = getRoleLimits(existingApplicationCopy.getDeployment(), coreApplication.getUserRoles());
            Application newApplication = map(applicationName, coreApplication, roleLimits, existingApplicationCopy);
            ImportAction importAction = handleExisting(newApplication, resolutionPolicy, applicationName);
            return new ImportComponent<>(importAction, existingApplication, newApplication);
        } else {
            List<RoleLimit> roleLimits = getRoleLimits(applicationName, coreApplication.getUserRoles());
            Application newApplication = map(applicationName, coreApplication, roleLimits, new Application());
            applicationService.createApplication(newApplication);
            return new ImportComponent<>(CREATE, null, newApplication);
        }
    }

    private ImportComponent<Application> processApplication(String applicationName,
                                                            Application newApplication,
                                                            ConflictResolutionPolicy resolutionPolicy) {
        Optional<Application> application = applicationService.tryGetApplication(applicationName);
        if (application.isPresent()) {
            Application existingApplication = application.get();
            newApplication.getDeployment().setRoleLimits(existingApplication.getDeployment().getRoleLimits());
            ImportAction importAction = handleExisting(newApplication, resolutionPolicy, applicationName);
            return new ImportComponent<>(importAction, existingApplication, newApplication);
        } else {
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

    private Application map(String appName, CoreApplication coreApplication, List<RoleLimit> roleLimits, Application application) {
        coreApplication.setName(appName);
        return applicationCoreMapper.mapApplication(coreApplication, roleLimits, application);
    }

    public List<ImportComponent<Application>> getActualImportedApplications(Collection<ImportComponent<Application>> applicationImportComponents) {
        List<String> names = getNextImportComponentNames(applicationImportComponents);
        Map<String, Application> importedApplicationsByNames = applicationService.getAllByNames(names)
                .stream()
                .collect(Collectors.toMap(application -> application.getDeployment().getName(), Function.identity()));

        return applicationImportComponents.stream()
                .map(importComponent -> {
                    var next = importedApplicationsByNames.get(importComponent.getNext().getDeployment().getName());
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
