package com.epam.aidial.cfg.service.config.transfer.importer;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.model.GlobalSettings;
import com.epam.aidial.cfg.domain.model.ImportComponent;
import com.epam.aidial.cfg.domain.service.GlobalSettingsService;
import com.epam.aidial.cfg.service.config.export.ConflictResolutionPolicy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.epam.aidial.cfg.domain.model.ImportAction.CREATE;
import static com.epam.aidial.cfg.domain.model.ImportAction.SKIP;
import static com.epam.aidial.cfg.domain.model.ImportAction.UPDATE;

@Service
@Slf4j
@LogExecution
@RequiredArgsConstructor
public class GlobalSettingsImporter {

    private final GlobalSettingsService globalSettingsService;

    public Map<GlobalSettingsImportType, Collection<ImportComponent<Collection<String>>>> importGlobalSettings(Collection<String> globalInterceptors,
                                                                                                               ConflictResolutionPolicy resolutionPolicy) {
        var existingGlobalSettings = globalSettingsService.getGlobalSettings();
        var globalSettingsImport = new GlobalSettings();
        Map<GlobalSettingsImportType, Collection<ImportComponent<Collection<String>>>> globalSettingsImportComponent = new HashMap<>();
        globalSettingsImportComponent.put(GlobalSettingsImportType.GLOBAL_INTERCEPTORS, importGlobalInterceptors(
                globalInterceptors,
                existingGlobalSettings,
                globalSettingsImport, resolutionPolicy));
        if (!existingGlobalSettings.equals(globalSettingsImport)) {
            globalSettingsService.saveGlobalSettings(globalSettingsImport);
        }
        return globalSettingsImportComponent;
    }

    public Collection<ImportComponent<Collection<String>>> importGlobalInterceptors(Collection<String> globalInterceptors,
                                                                                    GlobalSettings existingGlobalSettings,
                                                                                    GlobalSettings globalSettingsImport,
                                                                                    ConflictResolutionPolicy resolutionPolicy) {
        var existingInterceptors = existingGlobalSettings.getGlobalInterceptors();

        if (CollectionUtils.isEmpty(globalInterceptors)) {
            globalSettingsImport.setGlobalInterceptors(existingInterceptors);
            return Collections.emptyList();
        }

        if (CollectionUtils.isEmpty(existingInterceptors)) {
            globalSettingsImport.setGlobalInterceptors(globalInterceptors.stream().toList());
            return List.of(new ImportComponent<>(CREATE, existingInterceptors, globalInterceptors));
        }

        if (ConflictResolutionPolicy.OVERRIDE.equals(resolutionPolicy)) {
            globalSettingsImport.setGlobalInterceptors(globalInterceptors.stream().toList());
            return List.of(new ImportComponent<>(UPDATE, existingInterceptors, globalInterceptors));
        } else {
            globalSettingsImport.setGlobalInterceptors(existingInterceptors);
            return List.of(new ImportComponent<>(SKIP, existingInterceptors, globalInterceptors));
        }
    }

}