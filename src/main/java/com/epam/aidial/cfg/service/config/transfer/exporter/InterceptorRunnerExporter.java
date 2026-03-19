package com.epam.aidial.cfg.service.config.transfer.exporter;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.model.ExportComponentInfo;
import com.epam.aidial.cfg.domain.model.ExportConfigComponentType;
import com.epam.aidial.cfg.domain.model.InterceptorRunner;
import com.epam.aidial.cfg.domain.service.InterceptorRunnerService;
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
public class InterceptorRunnerExporter {

    private final InterceptorRunnerService interceptorRunnerService;

    protected Map<String, InterceptorRunner> getInterceptorRunners(ExportRequest request) {
        if (request instanceof FullExportRequest fullExportRequest) {
            return fullExportRequest.getComponentTypes().contains(ExportConfigComponentType.INTERCEPTOR_RUNNER)
                    ? getInterceptorRunnersWithRemovedDependencies().stream()
                    .collect(toLinkedHashMap(InterceptorRunner::getName))
                    : new HashMap<>();
        } else if (request instanceof SelectedItemsExportRequest selectedItemsExportRequest) {
            return getInterceptorRunners(selectedItemsExportRequest.getComponents()).stream()
                    .collect(toLinkedHashMap(InterceptorRunner::getName));
        }
        throw new IllegalArgumentException("Unsupported request type: " + request.getClass());
    }

    private List<InterceptorRunner> getInterceptorRunners(List<ExportConfigComponent> components) {
        var componentsByName = filterComponentsByTypeAndCollectToMap(components, ExportConfigComponentType.INTERCEPTOR_RUNNER);
        if (componentsByName.isEmpty()) {
            return List.of();
        }
        return interceptorRunnerService.getAllByNamesOrderedByDisplayNameAscNameAsc(componentsByName.keySet()).stream()
                .map(this::removeDependency)
                .toList();
    }

    private Collection<InterceptorRunner> getInterceptorRunnersWithRemovedDependencies() {
        return interceptorRunnerService.getAllOrderedByDisplayNameAscNameAsc().stream()
                .map(this::removeDependency)
                .toList();
    }

    private InterceptorRunner removeDependency(InterceptorRunner interceptorRunner) {
        interceptorRunner.setInterceptors(null);
        return interceptorRunner;
    }

    protected Collection<ExportComponentInfo> preview(ExportRequest request) {
        return getInterceptorRunners(request).values().stream()
                .map(component -> ExportComponentInfo.builder()
                        .name(component.getName())
                        .displayName(component.getDisplayName())
                        .description(component.getDescription())
                        .type(ExportConfigComponentType.INTERCEPTOR_RUNNER)
                        .build())
                .collect(Collectors.toList());
    }
}