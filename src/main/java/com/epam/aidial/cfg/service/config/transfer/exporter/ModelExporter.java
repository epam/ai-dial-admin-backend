package com.epam.aidial.cfg.service.config.transfer.exporter;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.model.ExportComponentInfo;
import com.epam.aidial.cfg.domain.model.ExportConfigComponentType;
import com.epam.aidial.cfg.domain.model.ExportFormat;
import com.epam.aidial.cfg.domain.model.Model;
import com.epam.aidial.cfg.domain.model.Upstream;
import com.epam.aidial.cfg.domain.model.source.ModelAdapterSource;
import com.epam.aidial.cfg.domain.service.AdapterService;
import com.epam.aidial.cfg.domain.service.ModelService;
import com.epam.aidial.cfg.domain.utils.ModelEndpointUtils;
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
import java.util.stream.Collectors;

import static com.epam.aidial.cfg.service.config.transfer.exporter.util.ExportUtils.filterComponentsByTypeAndCollectToMap;
import static com.epam.aidial.cfg.service.config.transfer.exporter.util.ExportUtils.toLinkedHashMap;

@Service
@LogExecution
@RequiredArgsConstructor
public class ModelExporter {

    private final ModelService modelService;
    private final AdapterService adapterService;

    protected Map<String, Model> getModels(ExportRequest request) {
        if (request instanceof FullExportRequest fullExportRequest) {
            return fullExportRequest.getComponentTypes().contains(ExportConfigComponentType.MODEL)
                    ? getModels(fullExportRequest).stream()
                    .collect(toLinkedHashMap(model -> model.getDeployment().getName()))
                    : new HashMap<>();
        } else if (request instanceof SelectedItemsExportRequest selectedItemsExportRequest) {
            return getModels(selectedItemsExportRequest, request.isAddSecrets()).stream()
                    .collect(toLinkedHashMap(model -> model.getDeployment().getName()));
        }
        throw new IllegalArgumentException("Unsupported request type: " + request.getClass());
    }

    protected Collection<Model> getModels() {
        return modelService.getAll();
    }

    private List<Model> getModels(SelectedItemsExportRequest selectedItemsExportRequest, boolean addSecrets) {
        var componentsByName = filterComponentsByTypeAndCollectToMap(selectedItemsExportRequest.getComponents(), ExportConfigComponentType.MODEL);
        if (componentsByName.isEmpty()) {
            return List.of();
        }
        return modelService.getAllByNamesOrderedByDisplayNameAscDisplayVersionAscNameAsc(componentsByName.keySet()).stream()
                .map(model -> {
                    ExportConfigComponent component = componentsByName.get(model.getDeployment().getName());
                    return removeDependency(model, component.getDependencies(), selectedItemsExportRequest.getExportFormat());
                })
                .map(model -> removeSecretData(model, addSecrets))
                .toList();
    }

    private Collection<Model> getModels(FullExportRequest fullExportRequest) {
        return modelService.getAllOrderedByDisplayNameAscDisplayVersionAscNameAsc().stream()
                .map(model -> removeSecretData(model, fullExportRequest.isAddSecrets()))
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

        if ((exportFormat == ExportFormat.CORE || !componentTypes.contains(ExportConfigComponentType.ADAPTER))
                && model.getSource() != null
                && model.getSource() instanceof ModelAdapterSource adapterSource) {
            var adapter = adapterService.get(adapterSource.getAdapterName());
            model.setEndpoint(ModelEndpointUtils.concatEndpointAndPath(adapter.getBaseEndpoint(), adapterSource.getCompletionEndpointPath()));
            model.setResponsesEndpoint(adapter.getResponsesEndpoint());
            model.setSource(null);
        }

        return model;
    }

    private Model removeSecretData(Model model, boolean addSecrets) {
        List<Upstream> upstreams = model.getUpstreams();
        if (CollectionUtils.isNotEmpty(upstreams) && !addSecrets) {
            for (Upstream upstream : upstreams) {
                upstream.setKey(null);
                upstream.setSecretExtraData(null);
            }
        }
        return model;
    }
}