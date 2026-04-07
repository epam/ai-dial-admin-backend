package com.epam.aidial.cfg.service.config.transfer.exporter;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.model.Application;
import com.epam.aidial.cfg.domain.model.ExportComponentInfo;
import com.epam.aidial.cfg.domain.model.ExportConfigComponentType;
import com.epam.aidial.cfg.domain.model.ExportFormat;
import com.epam.aidial.cfg.domain.service.ApplicationService;
import com.epam.aidial.cfg.model.ExportRequest;
import com.epam.aidial.cfg.model.FullExportRequest;
import com.epam.aidial.cfg.model.SelectedItemsExportRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.epam.aidial.cfg.domain.model.ExportFormat.CORE;
import static com.epam.aidial.cfg.service.config.transfer.exporter.util.ExportUtils.filterComponentsByTypeAndCollectToMap;
import static com.epam.aidial.cfg.service.config.transfer.exporter.util.ExportUtils.toLinkedHashMap;

@Service
@LogExecution
@RequiredArgsConstructor
public class ApplicationExporter {

    private final ApplicationService applicationService;

    protected Map<String, Application> getApplications(ExportRequest request) {
        if (request instanceof FullExportRequest fullExportRequest) {
            return fullExportRequest.getComponentTypes().contains(ExportConfigComponentType.APPLICATION)
                    ? getApplicationsWithRemovedDependencies(fullExportRequest).stream()
                    .collect(toLinkedHashMap(app -> app.getDeployment().getName()))
                    : new HashMap<>();
        } else if (request instanceof SelectedItemsExportRequest selectedItemsExportRequest) {
            return getApplicationsWithRemovedDependencies(selectedItemsExportRequest).stream()
                    .collect(toLinkedHashMap(app -> app.getDeployment().getName()));
        }
        throw new IllegalArgumentException("Unsupported request type: " + request.getClass());
    }

    protected Collection<Application> getValidApplications(ExportRequest request) {
        return request.getExportFormat() == CORE
                ? applicationService.getAllValidApplicationsOrderedByDisplayNameAscDisplayVersionAscNameAsc()
                : applicationService.getAllApplicationsOrderedByDisplayNameAscDisplayVersionAscNameAsc();
    }

    protected Collection<ExportComponentInfo> preview(ExportRequest request) {
        return getApplications(request).values().stream()
                .map(component -> ExportComponentInfo.builder()
                        .name(component.getDeployment().getName())
                        .displayName(component.getDisplayName())
                        .displayVersion(component.getDisplayVersion())
                        .description(component.getDescription())
                        .type(ExportConfigComponentType.APPLICATION)
                        .build())
                .collect(Collectors.toList());
    }

    protected Application getApplication(String applicationName) {
        return applicationService.getApplication(applicationName);
    }

    private Collection<Application> getApplicationsWithRemovedDependencies(FullExportRequest fullExportRequest) {
        return getValidApplications(fullExportRequest).stream()
                .map(app -> removeDependency(app, fullExportRequest.getComponentTypes(), fullExportRequest.getExportFormat()))
                .toList();
    }

    private List<Application> getApplicationsWithRemovedDependencies(SelectedItemsExportRequest selectedItemsExportRequest) {
        var componentsByName = filterComponentsByTypeAndCollectToMap(selectedItemsExportRequest.getComponents(), ExportConfigComponentType.APPLICATION);
        if (componentsByName.isEmpty()) {
            return List.of();
        }
        return applicationService.getAllByNamesOrderedByDisplayNameAscDisplayVersionAscNameAsc(componentsByName.keySet()).stream()
                .map(application -> removeDependency(
                        application,
                        componentsByName.get(application.getDeployment().getName()).getDependencies(),
                        selectedItemsExportRequest.getExportFormat())
                )
                .filter(app -> isValidApplication(app, selectedItemsExportRequest))
                .toList();
    }

    private Application removeDependency(Application app, Set<ExportConfigComponentType> componentTypes, ExportFormat exportFormat) {
        if (!componentTypes.contains(ExportConfigComponentType.INTERCEPTOR)) {
            app.setInterceptors(null);
        }
        if (!componentTypes.contains(ExportConfigComponentType.APPLICATION_TYPE_SCHEMA)) {
            app.setApplicationTypeSchemaId(null);
        }
        // Exclude role limits from deployment for Admin export format in order to have unidirectional association
        // between deployments and roles, so it means that role with its limits will be defined only under "roles" section
        if (!componentTypes.contains(ExportConfigComponentType.ROLE) || exportFormat == ExportFormat.ADMIN) {
            app.getDeployment().setRoleLimits(null);
        }
        return app;
    }

    private boolean isValidApplication(Application application, SelectedItemsExportRequest selectedItemsExportRequest) {
        return selectedItemsExportRequest.getExportFormat() != CORE || application.getValidityState().isValid();
    }

}
