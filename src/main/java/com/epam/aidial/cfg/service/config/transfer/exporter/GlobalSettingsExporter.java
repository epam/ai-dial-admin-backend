package com.epam.aidial.cfg.service.config.transfer.exporter;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.model.ExportConfigComponentType;
import com.epam.aidial.cfg.domain.model.GlobalSettings;
import com.epam.aidial.cfg.domain.service.GlobalSettingsService;
import com.epam.aidial.cfg.model.ExportRequest;
import com.epam.aidial.cfg.model.FullExportRequest;
import com.epam.aidial.cfg.model.SelectedItemsExportRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@LogExecution
@RequiredArgsConstructor
public class GlobalSettingsExporter {

    private final GlobalSettingsService globalSettingsService;

    protected GlobalSettings getGlobalSettings(ExportRequest request) {
        var currentGlobalSettings = globalSettingsService.getGlobalSettings();

        List<String> globalInterceptors = shouldExport(request)
                ? currentGlobalSettings.getGlobalInterceptors()
                : new ArrayList<>();

        var exportGlobalSettings = new GlobalSettings();
        exportGlobalSettings.setGlobalInterceptors(globalInterceptors);

        return exportGlobalSettings;
    }

    protected GlobalSettings getGlobalSettings() {
        return globalSettingsService.getGlobalSettings();
    }

    private boolean shouldExport(ExportRequest request) {
        if (request instanceof FullExportRequest full) {
            return full.getComponentTypes().contains(ExportConfigComponentType.GLOBAL_INTERCEPTOR);
        }
        if (request instanceof SelectedItemsExportRequest selectedItemsExportRequest) {
            return selectedItemsExportRequest.getComponents().stream().anyMatch(component -> component.getType() == ExportConfigComponentType.GLOBAL_INTERCEPTOR);
        }
        throw new IllegalArgumentException("Unsupported request type: " + request.getClass());
    }

}