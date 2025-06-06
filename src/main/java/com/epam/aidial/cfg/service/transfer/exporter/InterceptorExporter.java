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
                    ? getInterceptorWithRemovedDependencies().stream()
                    .collect(Collectors.toMap(Interceptor::getName, Function.identity()))
                    : new HashMap<>();
        } else if (request instanceof SelectedItemsExportRequest selectedItemsExportRequest) {
            return getInterceptors(selectedItemsExportRequest.getComponents()).stream()
                    .collect(Collectors.toMap(Interceptor::getName, Function.identity()));
        }
        throw new IllegalArgumentException("Unsupported request type: " + request.getClass());
    }

    private List<Interceptor> getInterceptors(List<ExportConfigComponent> components) {
        return components.stream()
                .filter(component -> component.getType() == ExportConfigComponentType.INTERCEPTOR)
                .collect(Collectors.toMap(ExportConfigComponent::getName, Function.identity(),
                        (existing, replacement) -> {
                            existing.addDependencies(replacement.getDependencies());
                            return existing;
                        }
                ))
                .values()
                .stream()
                .map(component -> interceptorService.get(component.getName()))
                .toList();
    }

    private Collection<Interceptor> getInterceptorWithRemovedDependencies() {
        return interceptorService.getAll().stream()
                .peek(interceptor -> interceptor.setEntities(null))
                .toList();
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
