package com.epam.aidial.cfg.service.config.transfer.exporter;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.model.ExportComponentInfo;
import com.epam.aidial.cfg.domain.model.ExportConfigComponentType;
import com.epam.aidial.cfg.domain.model.ExportFormat;
import com.epam.aidial.cfg.domain.model.ToolSet;
import com.epam.aidial.cfg.domain.service.ToolSetService;
import com.epam.aidial.cfg.model.ExportRequest;
import com.epam.aidial.cfg.model.FullExportRequest;
import com.epam.aidial.cfg.model.SelectedItemsExportRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.epam.aidial.cfg.service.config.transfer.exporter.util.ExportUtils.filterComponentsByTypeAndCollectToMap;
import static com.epam.aidial.cfg.service.config.transfer.exporter.util.ExportUtils.toLinkedHashMap;

@Service
@LogExecution
@RequiredArgsConstructor
public class ToolSetExporter {

    private final ToolSetService toolSetService;

    protected Map<String, ToolSet> getToolSets(ExportRequest request) {
        if (request instanceof FullExportRequest fullExportRequest) {
            return fullExportRequest.getComponentTypes().contains(ExportConfigComponentType.TOOL_SET)
                    ? getToolSets(fullExportRequest).stream()
                    .collect(toLinkedHashMap(toolSet -> toolSet.getDeployment().getName()))
                    : new LinkedHashMap<>();
        } else if (request instanceof SelectedItemsExportRequest selectedItemsExportRequest) {
            return getToolSets(selectedItemsExportRequest, request.isAddSecrets()).stream()
                    .collect(toLinkedHashMap(toolSet -> toolSet.getDeployment().getName()));
        }
        throw new IllegalArgumentException("Unsupported request type: " + request.getClass());
    }

    protected Collection<ToolSet> getToolSets() {
        return toolSetService.getAll();
    }

    private List<ToolSet> getToolSets(SelectedItemsExportRequest selectedItemsExportRequest, boolean addSecrets) {
        var componentsByName = filterComponentsByTypeAndCollectToMap(selectedItemsExportRequest.getComponents(), ExportConfigComponentType.TOOL_SET);
        if (componentsByName.isEmpty()) {
            return List.of();
        }
        return toolSetService.getAllByNamesOrderedByDisplayNameAscNameAsc(componentsByName.keySet()).stream()
                .map(toolSet -> removeDependency(toolSet, componentsByName.get(toolSet.getDeployment().getName()).getDependencies(),
                        selectedItemsExportRequest.getExportFormat()))
                .map(toolSet -> removeClientSecret(toolSet, addSecrets))
                .toList();
    }

    private Collection<ToolSet> getToolSets(FullExportRequest fullExportRequest) {
        return toolSetService.getAllOrderedByDisplayNameAscNameAsc().stream()
                .map(toolSet -> removeClientSecret(toolSet, fullExportRequest.isAddSecrets()))
                .map(toolSet -> removeDependency(toolSet, fullExportRequest.getComponentTypes(), fullExportRequest.getExportFormat()))
                .toList();
    }

    protected Collection<ExportComponentInfo> preview(ExportRequest request) {
        return getToolSets(request).values().stream()
                .map(component -> ExportComponentInfo.builder()
                        .name(component.getDeployment().getName())
                        .displayName(component.getDisplayName())
                        .type(ExportConfigComponentType.TOOL_SET)
                        .description(component.getDescription())
                        .build())
                .collect(Collectors.toList());
    }

    private ToolSet removeDependency(ToolSet toolSet, Set<ExportConfigComponentType> componentTypes, ExportFormat exportFormat) {
        // Exclude role limits from deployment for Admin export format in order to have unidirectional association
        // between deployments and roles, so it means that role with its limits will be defined only under "roles" section
        if (!componentTypes.contains(ExportConfigComponentType.ROLE) || exportFormat == ExportFormat.ADMIN) {
            toolSet.getDeployment().setRoleLimits(null);
        }
        return toolSet;
    }

    private ToolSet removeClientSecret(ToolSet toolSet, boolean addSecrets) {
        var deployment = toolSet.getDeployment();
        if (deployment.getAuthSettings() != null && !addSecrets) {
            deployment.getAuthSettings().setClientSecret(null);
        }
        return toolSet;
    }

}
