package com.epam.aidial.cfg.service.transfer.exporter;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.model.ExportComponentInfo;
import com.epam.aidial.cfg.domain.model.ExportConfigComponentType;
import com.epam.aidial.cfg.domain.model.Interceptor;
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

    protected Map<String, Interceptor> getInterceptors(ExportRequest request) {
        if (request instanceof FullExportRequest fullExportRequest) {
            return fullExportRequest.getComponentTypes().contains(ExportConfigComponentType.INTERCEPTOR)
                    ? getInterceptorsWithRemovedDependencies(fullExportRequest.getComponentTypes()).stream()
                    .collect(Collectors.toMap(Interceptor::getName, Function.identity()))
                    : new HashMap<>();
        } else if (request instanceof SelectedItemsExportRequest selectedItemsExportRequest) {
            return getInterceptorsWithRemovedDependencies(selectedItemsExportRequest.getComponents()).stream()
                    .collect(Collectors.toMap(Interceptor::getName, Function.identity()));
        }
        throw new IllegalArgumentException("Unsupported request type: " + request.getClass());
    }

    private Collection<Interceptor> getInterceptorsWithRemovedDependencies(Set<ExportConfigComponentType> componentTypes) {
        return interceptorService.getAll().stream()
            .map(interceptor -> removeDependency(interceptor, componentTypes))
            .toList();
    }

    private List<Interceptor> getInterceptorsWithRemovedDependencies(List<ExportConfigComponent> components) {
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
                    Interceptor interceptor = interceptorService.get(component.getName());
                    return removeDependency(interceptor, component.getDependencies());
                })
                .toList();
    }

    private Interceptor removeDependency(Interceptor interceptor, Set<ExportConfigComponentType> componentTypes) {
        if (!componentTypes.contains(ExportConfigComponentType.INTERCEPTOR_RUNNER)) {
            interceptor.setInterceptorRunner(null);
        }
        interceptor.setEntities(null);
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
