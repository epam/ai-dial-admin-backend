package com.epam.aidial.cfg.service.transfer.exporter;

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
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@LogExecution
@RequiredArgsConstructor
public class AdapterExporter {

    private final AdapterService adapterService;

    protected Map<String, Adapter> getAdapters(ExportRequest request) {
        if (request instanceof FullExportRequest fullExportRequest) {
            return fullExportRequest.getComponentTypes().contains(ExportConfigComponentType.ADAPTER)
                    ? getAdaptersWithRemovedDependencies().stream()
                    .collect(Collectors.toMap(Adapter::getName, Function.identity()))
                    : new HashMap<>();
        } else if (request instanceof SelectedItemsExportRequest selectedItemsExportRequest) {
            return getAdaptersWithRemovedDependencies(selectedItemsExportRequest.getComponents()).stream()
                    .collect(Collectors.toMap(Adapter::getName, Function.identity()));
        }
        throw new IllegalArgumentException("Unsupported request type: " + request.getClass());
    }

    private Collection<Adapter> getAdaptersWithRemovedDependencies() {
        return adapterService.getAll().stream()
                .map(this::removeDependency)
                .toList();
    }

    private List<Adapter> getAdaptersWithRemovedDependencies(List<ExportConfigComponent> elements) {
        return elements.stream()
                .filter(component -> component.getType() == ExportConfigComponentType.ADAPTER)
                .collect(Collectors.toMap(ExportConfigComponent::getName, Function.identity(),
                        (existing, replacement) -> {
                            existing.addDependencies(replacement.getDependencies());
                            return existing;
                        }
                ))
                .values()
                .stream()
                .map(component -> adapterService.get(component.getName()))
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
                        .type(ExportConfigComponentType.ADAPTER)
                        .build())
                .collect(Collectors.toList());
    }
}
