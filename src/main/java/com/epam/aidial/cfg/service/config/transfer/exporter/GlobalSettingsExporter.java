package com.epam.aidial.cfg.service.config.transfer.exporter;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.model.ExportComponentInfo;
import com.epam.aidial.cfg.domain.model.ExportConfigComponentType;
import com.epam.aidial.cfg.domain.service.GlobalSettingsService;
import com.epam.aidial.cfg.model.ExportRequest;
import com.epam.aidial.cfg.model.FullExportRequest;
import com.epam.aidial.cfg.model.SelectedItemsExportRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

@Service
@LogExecution
@RequiredArgsConstructor
public class GlobalSettingsExporter {

    private final GlobalSettingsService globalSettingsService;

    protected Collection<String> getGlobalInterceptors(ExportRequest request) {
        if (request instanceof FullExportRequest fullExportRequest) {
            return fullExportRequest.getComponentTypes().contains(ExportConfigComponentType.GLOBAL_INTERCEPTOR)
                    ? globalSettingsService.getAllGlobalInterceptors()
                    : new ArrayList<>();
        } else if (request instanceof SelectedItemsExportRequest selectedItemsExportRequest) {
            return globalSettingsService.getAllGlobalInterceptors();
        }
        throw new IllegalArgumentException("Unsupported request type: " + request.getClass());
    }

    protected Collection<ExportComponentInfo> previewGlobalInterceptors(ExportRequest request) {
        return getGlobalInterceptors(request).stream()
                .map(nameGlobalInterceptor -> ExportComponentInfo.builder()
                        .name(nameGlobalInterceptor)
                        .type(ExportConfigComponentType.GLOBAL_INTERCEPTOR)
                        .build())
                .collect(Collectors.toList());
    }

}