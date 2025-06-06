package com.epam.aidial.cfg.service.transfer.exporter;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.model.ApplicationTypeSchema;
import com.epam.aidial.cfg.domain.model.ExportApplicationTypeSchemaInfo;
import com.epam.aidial.cfg.domain.model.ExportConfigComponentType;
import com.epam.aidial.cfg.domain.service.ApplicationTypeSchemaService;
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
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@LogExecution
@RequiredArgsConstructor
public class ApplicationTypeSchemaExporter {

    private final ApplicationTypeSchemaService service;

    protected Map<String, ApplicationTypeSchema> getApplicationTypeSchemas(ExportRequest request) {
        if (request instanceof FullExportRequest fullExportRequest) {
            return fullExportRequest.getComponentTypes().contains(ExportConfigComponentType.APPLICATION_TYPE_SCHEMA)
                    ? getApplicationTypeSchemasWithRemovedDependencies().stream()
                    .collect(Collectors.toMap(ApplicationTypeSchema::getSchemaId, Function.identity()))
                    : new HashMap<>();
        } else if (request instanceof SelectedItemsExportRequest selectedItemsExportRequest) {
            List<ApplicationTypeSchema> applicationRunners = getApplicationTypeSchemas(selectedItemsExportRequest.getComponents());
            return applicationRunners.stream()
                    .collect(Collectors.toMap(ApplicationTypeSchema::getSchemaId, Function.identity()));
        }
        throw new IllegalArgumentException("Unsupported request type: " + request.getClass());
    }

    private List<ApplicationTypeSchema> getApplicationTypeSchemas(List<ExportConfigComponent> components) {
        return components.stream()
                .filter(component -> component.getType() == ExportConfigComponentType.APPLICATION_TYPE_SCHEMA)
                .collect(Collectors.toMap(ExportConfigComponent::getName, component -> component,
                        (existing, replacement) -> {
                            existing.addDependencies(replacement.getDependencies());
                            return existing;
                        }
                ))
                .values()
                .stream()
                .map(component -> service.get(component.getName()))
                .map(this::removeDependency)
                .toList();
    }

    private Collection<ApplicationTypeSchema> getApplicationTypeSchemasWithRemovedDependencies() {
        return service.getAll().stream()
                .map(this::removeDependency)
                .toList();
    }

    private ApplicationTypeSchema removeDependency(ApplicationTypeSchema applicationTypeSchema) {
        applicationTypeSchema.setApplications(null);
        return applicationTypeSchema;
    }

    protected Collection<ExportApplicationTypeSchemaInfo> preview(ExportRequest request) {
        return getApplicationTypeSchemas(request).values().stream()
                .map(component -> ExportApplicationTypeSchemaInfo.builder()
                        .id(component.getSchemaId())
                        .displayName(component.getApplicationTypeDisplayName())
                        .description(component.getDescription())
                        .type(ExportConfigComponentType.APPLICATION_TYPE_SCHEMA)
                        .build())
                .collect(Collectors.toList());
    }

}
