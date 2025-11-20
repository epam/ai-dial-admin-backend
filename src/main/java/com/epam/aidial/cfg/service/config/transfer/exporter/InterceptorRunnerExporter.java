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
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@LogExecution
@RequiredArgsConstructor
public class InterceptorRunnerExporter {

    private final InterceptorRunnerService interceptorRunnerService;

    protected Map<String, InterceptorRunner> getInterceptorRunners(ExportRequest request) {
        if (request instanceof FullExportRequest fullExportRequest) {
            return fullExportRequest.getComponentTypes().contains(ExportConfigComponentType.INTERCEPTOR_RUNNER)
                    ? getInterceptorRunnersWithRemovedDependencies().stream()
                    .collect(Collectors.toMap(InterceptorRunner::getName, Function.identity()))
                    : new HashMap<>();
        } else if (request instanceof SelectedItemsExportRequest selectedItemsExportRequest) {
            return getInterceptorRunners(selectedItemsExportRequest.getComponents()).stream()
                    .collect(Collectors.toMap(InterceptorRunner::getName, Function.identity()));
        }
        throw new IllegalArgumentException("Unsupported request type: " + request.getClass());
    }

    private List<InterceptorRunner> getInterceptorRunners(List<ExportConfigComponent> components) {
        return components.stream()
                .filter(component -> component.getType() == ExportConfigComponentType.INTERCEPTOR_RUNNER)
                .collect(Collectors.toMap(ExportConfigComponent::getName, Function.identity(),
                        (existing, replacement) -> {
                            existing.addDependencies(replacement.getDependencies());
                            return existing;
                        }
                ))
                .values()
                .stream()
                .map(component -> interceptorRunnerService.get(component.getName()))
                .map(this::removeDependency)
                .toList();
    }

    private Collection<InterceptorRunner> getInterceptorRunnersWithRemovedDependencies() {
        return interceptorRunnerService.getAll().stream()
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