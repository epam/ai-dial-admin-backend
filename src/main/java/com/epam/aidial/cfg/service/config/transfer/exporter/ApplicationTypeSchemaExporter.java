package com.epam.aidial.cfg.service.config.transfer.exporter;

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
import java.util.Set;
import java.util.stream.Collectors;

import static com.epam.aidial.cfg.service.config.transfer.exporter.util.ExportUtils.filterComponentsByTypeAndCollectToMap;
import static com.epam.aidial.cfg.service.config.transfer.exporter.util.ExportUtils.toLinkedHashMap;

@Service
@LogExecution
@RequiredArgsConstructor
public class ApplicationTypeSchemaExporter {

    private final ApplicationTypeSchemaService service;

    protected Map<String, ApplicationTypeSchema> getApplicationTypeSchemas(ExportRequest request) {
        if (request instanceof FullExportRequest fullExportRequest) {
            return fullExportRequest.getComponentTypes().contains(ExportConfigComponentType.APPLICATION_TYPE_SCHEMA)
                    ? getApplicationTypeSchemasWithRemovedDependencies(fullExportRequest).stream()
                    .collect(toLinkedHashMap(ApplicationTypeSchema::getSchemaId))
                    : new HashMap<>();
        } else if (request instanceof SelectedItemsExportRequest selectedItemsExportRequest) {
            List<ApplicationTypeSchema> applicationRunners = getApplicationTypeSchemas(selectedItemsExportRequest.getComponents());
            return applicationRunners.stream()
                    .collect(toLinkedHashMap(ApplicationTypeSchema::getSchemaId));
        }
        throw new IllegalArgumentException("Unsupported request type: " + request.getClass());
    }

    private List<ApplicationTypeSchema> getApplicationTypeSchemas(List<ExportConfigComponent> components) {
        var componentsById = filterComponentsByTypeAndCollectToMap(components, ExportConfigComponentType.APPLICATION_TYPE_SCHEMA);
        if (componentsById.isEmpty()) {
            return List.of();
        }
        return service.getAllByIdsOrderedByDisplayNameAscIdAsc(componentsById.keySet()).stream()
                .map(schema -> removeDependency(schema, componentsById.get(schema.getSchemaId()).getDependencies()))
                .toList();
    }

    protected ApplicationTypeSchema getApplicationTypeSchema(String id) {
        return service.get(id);
    }

    private Collection<ApplicationTypeSchema> getApplicationTypeSchemasWithRemovedDependencies(FullExportRequest fullExportRequest) {
        return service.getAllOrderedByDisplayNameAscIdAsc().stream()
                .map(schema -> removeDependency(schema, fullExportRequest.getComponentTypes()))
                .toList();
    }

    private ApplicationTypeSchema removeDependency(ApplicationTypeSchema applicationTypeSchema, Set<ExportConfigComponentType> componentTypes) {
        applicationTypeSchema.setApplications(null);

        if (!componentTypes.contains(ExportConfigComponentType.INTERCEPTOR)) {
            applicationTypeSchema.setInterceptors(null);
        }

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
