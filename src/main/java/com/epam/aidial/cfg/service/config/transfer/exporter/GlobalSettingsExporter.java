package com.epam.aidial.cfg.service.config.transfer.exporter;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.model.ExportComponentInfo;
import com.epam.aidial.cfg.domain.model.ExportConfigComponentType;
import com.epam.aidial.cfg.domain.model.GlobalSettings;
import com.epam.aidial.cfg.domain.service.GlobalSettingsService;
import com.epam.aidial.cfg.model.ExportRequest;
import com.epam.aidial.cfg.model.FullExportRequest;
import com.epam.aidial.cfg.model.SelectedItemsExportRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@LogExecution
@RequiredArgsConstructor
public class GlobalSettingsExporter {

    private final GlobalSettingsService globalSettingsService;

    protected GlobalSettings getGlobalSettings(ExportRequest request) {
        var currentGlobalSettings = globalSettingsService.getGlobalSettings();
        var exportGlobalSettings = new GlobalSettings();
        exportGlobalSettings.setGlobalInterceptors(
                shouldExport(request, ExportConfigComponentType.GLOBAL_INTERCEPTOR)
                        ? currentGlobalSettings.getGlobalInterceptors()
                        : new ArrayList<>());
        return exportGlobalSettings;
    }

    protected Map<ExportConfigComponentType, Collection<ExportComponentInfo>> previewGlobalSettings(ExportRequest request) {
        var previewGlobalSettings = new HashMap<ExportConfigComponentType, Collection<ExportComponentInfo>>();
        var exportGlobalSettings = getGlobalSettings(request);
        previewGlobalSettings.put(ExportConfigComponentType.GLOBAL_INTERCEPTOR,
                previewGlobalInterceptors(exportGlobalSettings.getGlobalInterceptors()));
        return previewGlobalSettings;
    }

    private Collection<ExportComponentInfo> previewGlobalInterceptors(List<String> globalInterceptors) {
        return globalInterceptors.stream()
                .map(nameGlobalInterceptor -> ExportComponentInfo.builder()
                        .name(nameGlobalInterceptor)
                        .type(ExportConfigComponentType.GLOBAL_INTERCEPTOR)
                        .build())
                .collect(Collectors.toList());
    }

    private boolean shouldExport(ExportRequest request, ExportConfigComponentType type) {
        if (request instanceof FullExportRequest full) {
            return full.getComponentTypes().contains(type);
        }
        if (request instanceof SelectedItemsExportRequest selectedItemsExportRequest) {
            return selectedItemsExportRequest.getComponents().stream().anyMatch(component -> component.getType() == type);
        }
        throw new IllegalArgumentException("Unsupported request type: " + request.getClass());
    }

}