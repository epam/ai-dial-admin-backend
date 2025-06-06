package com.epam.aidial.cfg.service.transfer.exporter;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.model.Application;
import com.epam.aidial.cfg.domain.model.ExportComponentInfo;
import com.epam.aidial.cfg.domain.model.ExportConfigComponentType;
import com.epam.aidial.cfg.domain.service.ApplicationService;
import com.epam.aidial.cfg.model.ExportConfigComponent;
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
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@LogExecution
@RequiredArgsConstructor
public class ApplicationExporter {

    private final ApplicationService applicationService;

    protected Map<String, Application> getApplications(ExportRequest request) {
        if (request instanceof FullExportRequest fullExportRequest) {
            return fullExportRequest.getComponentTypes().contains(ExportConfigComponentType.APPLICATION)
                    ? getApplicationsWithRemovedDependencies(fullExportRequest.getComponentTypes()).stream()
                    .collect(Collectors.toMap(app -> app.getDeployment().getName(), Function.identity()))
                    : new HashMap<>();
        } else if (request instanceof SelectedItemsExportRequest selectedItemsExportRequest) {
            return getApplicationsWithRemovedDependencies(selectedItemsExportRequest.getComponents()).stream()
                    .collect(Collectors.toMap(app -> app.getDeployment().getName(), Function.identity()));
        }
        throw new IllegalArgumentException("Unsupported request type: " + request.getClass());
    }

    protected Collection<Application> getApplications() {
        return applicationService.getAllApplications();
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

    private Collection<Application> getApplicationsWithRemovedDependencies(Set<ExportConfigComponentType> componentTypes) {
        return getApplications().stream()
                .map(app -> removeDependency(app, componentTypes))
                .toList();
    }

    private List<Application> getApplicationsWithRemovedDependencies(List<ExportConfigComponent> components) {
        return components.stream()
                .filter(component -> component.getType() == ExportConfigComponentType.APPLICATION)
                .collect(Collectors.toMap(ExportConfigComponent::getName, Function.identity(),
                        (existing, replacement) -> {
                            existing.addDependencies(replacement.getDependencies());
                            return existing;
                        }))
                .values()
                .stream()
                .map(component -> {
                    Application application = getApplication(component.getName());
                    return removeDependency(application, component.getDependencies());
                })
                .toList();
    }

    private Application removeDependency(Application app, Set<ExportConfigComponentType> componentTypes) {
        if (!componentTypes.contains(ExportConfigComponentType.INTERCEPTOR)) {
            app.setInterceptors(null);
        }
        if (!componentTypes.contains(ExportConfigComponentType.APPLICATION_TYPE_SCHEMA)) {
            app.setApplicationTypeSchemaId(null);
        }
        app.getDeployment().setRoleLimits(null);
        return app;
    }

}
