package com.epam.aidial.cfg.service.config.transfer.exporter;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.model.Adapter;
import com.epam.aidial.cfg.domain.model.ExportComponentInfo;
import com.epam.aidial.cfg.domain.model.ExportConfigComponentType;
import com.epam.aidial.cfg.domain.service.AdapterService;
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
import java.util.stream.Collectors;

import static com.epam.aidial.cfg.service.config.transfer.exporter.util.ExportUtils.filterComponentsByTypeAndCollectToMap;
import static com.epam.aidial.cfg.service.config.transfer.exporter.util.ExportUtils.toLinkedHashMap;

@Service
@LogExecution
@RequiredArgsConstructor
public class AdapterExporter {

    private final AdapterService adapterService;

    protected Map<String, Adapter> getAdapters(ExportRequest request) {
        if (request instanceof FullExportRequest fullExportRequest) {
            return fullExportRequest.getComponentTypes().contains(ExportConfigComponentType.ADAPTER)
                    ? getAdaptersWithRemovedDependencies().stream()
                    .collect(toLinkedHashMap(Adapter::getName))
                    : new HashMap<>();
        } else if (request instanceof SelectedItemsExportRequest selectedItemsExportRequest) {
            return getAdaptersWithRemovedDependencies(selectedItemsExportRequest.getComponents()).stream()
                    .collect(toLinkedHashMap(Adapter::getName));
        }
        throw new IllegalArgumentException("Unsupported request type: " + request.getClass());
    }

    private Collection<Adapter> getAdaptersWithRemovedDependencies() {
        return adapterService.getAllOrderedByDisplayNameAscNameAsc().stream()
                .map(this::removeDependency)
                .toList();
    }

    private List<Adapter> getAdaptersWithRemovedDependencies(List<ExportConfigComponent> elements) {
        var componentsByName = filterComponentsByTypeAndCollectToMap(elements, ExportConfigComponentType.ADAPTER);
        if (componentsByName.isEmpty()) {
            return List.of();
        }
        return adapterService.getAllByNamesOrderedByDisplayNameAscNameAsc(componentsByName.keySet()).stream()
                .map(this::removeDependency)
                .toList();
    }

    private Adapter removeDependency(Adapter adapter) {
        adapter.setModels(null);
        return adapter;
    }

    protected Collection<ExportComponentInfo> preview(ExportRequest request) {
        return getAdapters(request).values().stream()
                .map(component -> ExportComponentInfo.builder()
                        .name(component.getName())
                        .description(component.getDescription())
                        .displayName(component.getDisplayName())
                        .type(ExportConfigComponentType.ADAPTER)
                        .build())
                .collect(Collectors.toList());
    }
}