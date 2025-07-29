package com.epam.aidial.cfg.service.transfer.exporter;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.model.ExportComponentInfo;
import com.epam.aidial.cfg.domain.model.ExportConfigComponentType;
import com.epam.aidial.cfg.domain.model.ExportFormat;
import com.epam.aidial.cfg.domain.model.Model;
import com.epam.aidial.cfg.domain.model.Upstream;
import com.epam.aidial.cfg.domain.service.ModelService;
import com.epam.aidial.cfg.model.ExportConfigComponent;
import com.epam.aidial.cfg.model.ExportRequest;
import com.epam.aidial.cfg.model.FullExportRequest;
import com.epam.aidial.cfg.model.SelectedItemsExportRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
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
public class ModelExporter {

    private final ModelService modelService;

    protected Map<String, Model> getModels(ExportRequest request) {
        if (request instanceof FullExportRequest fullExportRequest) {
            return fullExportRequest.getComponentTypes().contains(ExportConfigComponentType.MODEL)
                    ? getModels(fullExportRequest).stream()
                    .collect(Collectors.toMap(model -> model.getDeployment().getName(), Function.identity()))
                    : new HashMap<>();
        } else if (request instanceof SelectedItemsExportRequest selectedItemsExportRequest) {
            return getModels(selectedItemsExportRequest, request.isAddSecrets()).stream()
                    .collect(Collectors.toMap(model -> model.getDeployment().getName(), Function.identity()));
        }
        throw new IllegalArgumentException("Unsupported request type: " + request.getClass());
    }

    protected Collection<Model> getModels() {
        return modelService.getAll();
    }

    private List<Model> getModels(SelectedItemsExportRequest selectedItemsExportRequest, boolean addSecrets) {
        List<ExportConfigComponent> components = selectedItemsExportRequest.getComponents();
        return components.stream()
                .filter(component -> component.getType() == ExportConfigComponentType.MODEL)
                .collect(Collectors.toMap(ExportConfigComponent::getName, Function.identity(),
                        (existing, replacement) -> {
                            existing.addDependencies(replacement.getDependencies());
                            return existing;
                        }
                ))
                .values()
                .stream()
                .map(component -> {
                    Model model = getModel(component.getName());
                    return removeDependency(model, component.getDependencies(), selectedItemsExportRequest.getExportFormat());
                })
                .map(model -> removeUpstreamKey(model, addSecrets))
                .toList();
    }

    private Collection<Model> getModels(FullExportRequest fullExportRequest) {
        return getModels().stream()
                .map(model -> removeUpstreamKey(model, fullExportRequest.isAddSecrets()))
                .map(model -> removeDependency(model, fullExportRequest.getComponentTypes(), fullExportRequest.getExportFormat()))
                .toList();
    }

    protected Collection<ExportComponentInfo> preview(ExportRequest request) {
        return getModels(request).values().stream()
                .map(component -> ExportComponentInfo.builder()
                        .name(component.getDeployment().getName())
                        .displayName(component.getDisplayName())
                        .displayVersion(component.getDisplayVersion())
                        .description(component.getDescription())
                        .type(ExportConfigComponentType.MODEL)
                        .build())
                .collect(Collectors.toList());
    }

    protected Model getModel(String modelName) {
        return modelService.getModel(modelName);
    }

    private Model removeDependency(Model model, Set<ExportConfigComponentType> componentTypes, ExportFormat exportFormat) {
        // Exclude role limits from deployment for Admin export format in order to have unidirectional association
        // between deployments and roles, so it means that role with its limits will be defined only under "roles" section
        if (!componentTypes.contains(ExportConfigComponentType.ROLE) || exportFormat == ExportFormat.ADMIN) {
            model.getDeployment().setRoleLimits(null);
        }
        if (!componentTypes.contains(ExportConfigComponentType.INTERCEPTOR)) {
            model.setInterceptors(null);
        }
        if (!componentTypes.contains(ExportConfigComponentType.ADAPTER) && exportFormat != ExportFormat.CORE) {
            model.setAdapter(null);
        }
        return model;
    }

    private Model removeUpstreamKey(Model model, boolean addSecrets) {
        List<Upstream> upstreams = model.getUpstreams();
        if (CollectionUtils.isNotEmpty(upstreams) && !addSecrets) {
            for (Upstream upstream : upstreams) {
                upstream.setKey(null);
            }
        }
        return model;
    }
}
