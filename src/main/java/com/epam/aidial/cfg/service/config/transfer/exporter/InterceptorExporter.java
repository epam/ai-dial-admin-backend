package com.epam.aidial.cfg.service.config.transfer.exporter;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.model.ExportComponentInfo;
import com.epam.aidial.cfg.domain.model.ExportConfigComponentType;
import com.epam.aidial.cfg.domain.model.ExportFormat;
import com.epam.aidial.cfg.domain.model.Interceptor;
import com.epam.aidial.cfg.domain.model.source.InterceptorRunnerSource;
import com.epam.aidial.cfg.domain.service.InterceptorRunnerService;
import com.epam.aidial.cfg.domain.service.InterceptorService;
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
public class InterceptorExporter {

    private final InterceptorService interceptorService;
    private final InterceptorRunnerService interceptorRunnerService;

    protected Map<String, Interceptor> getInterceptors(ExportRequest request) {
        if (request instanceof FullExportRequest fullExportRequest) {
            return fullExportRequest.getComponentTypes().contains(ExportConfigComponentType.INTERCEPTOR)
                    ? getInterceptorsWithRemovedDependencies(fullExportRequest.getComponentTypes(), request.getExportFormat()).stream()
                    .collect(Collectors.toMap(Interceptor::getName, Function.identity()))
                    : new HashMap<>();
        } else if (request instanceof SelectedItemsExportRequest selectedItemsExportRequest) {
            return getInterceptorsWithRemovedDependencies(selectedItemsExportRequest.getComponents(), request.getExportFormat()).stream()
                    .collect(Collectors.toMap(Interceptor::getName, Function.identity()));
        }
        throw new IllegalArgumentException("Unsupported request type: " + request.getClass());
    }

    private Collection<Interceptor> getInterceptorsWithRemovedDependencies(Set<ExportConfigComponentType> componentTypes, ExportFormat exportFormat) {
        return interceptorService.getAll().stream()
            .map(interceptor -> removeDependencies(interceptor, componentTypes, exportFormat))
            .toList();
    }

    private List<Interceptor> getInterceptorsWithRemovedDependencies(List<ExportConfigComponent> components, ExportFormat exportFormat) {
        return components.stream()
                .filter(component -> component.getType() == ExportConfigComponentType.INTERCEPTOR)
                .collect(Collectors.toMap(ExportConfigComponent::getName, Function.identity(),
                        (existing, replacement) -> {
                            existing.addDependencies(replacement.getDependencies());
                            return existing;
                        }))
                .values()
                .stream()
                .map(component -> {
                    Interceptor interceptor = getInterceptor(component.getName());
                    return removeDependencies(interceptor, component.getDependencies(), exportFormat);
                })
                .toList();
    }

    protected Interceptor getInterceptor(String name) {
        return interceptorService.get(name);
    }

    private Interceptor removeDependencies(Interceptor interceptor, Set<ExportConfigComponentType> componentTypes, ExportFormat exportFormat) {
        if (interceptor.getSource() != null && interceptor.getSource() instanceof InterceptorRunnerSource runnerSource) {
            if (exportFormat == ExportFormat.CORE) {
                var interceptorRunner = interceptorRunnerService.get(runnerSource.getRunnerName());
                interceptor.setEndpoint(interceptorRunner.getCompletionEndpoint());
                interceptor.getFeatures().setConfigurationEndpoint(interceptorRunner.getConfigurationEndpoint());
            }
            if (exportFormat == ExportFormat.CORE || !componentTypes.contains(ExportConfigComponentType.INTERCEPTOR_RUNNER)) {
                interceptor.setSource(null);
            }
        }
        interceptor.setEntities(null);
        interceptor.setApplicationTypeSchemas(null);
        return interceptor;
    }

    protected Collection<ExportComponentInfo> preview(ExportRequest request) {
        return getInterceptors(request).values().stream()
                .map(component -> ExportComponentInfo.builder()
                        .name(component.getName())
                        .displayName(component.getDisplayName())
                        .description(component.getDescription())
                        .type(ExportConfigComponentType.INTERCEPTOR)
                        .build())
                .collect(Collectors.toList());
    }
}
