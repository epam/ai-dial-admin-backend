package com.epam.aidial.cfg.service.config.transfer.importer;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.model.GlobalSettings;
import com.epam.aidial.cfg.domain.model.ImportAction;
import com.epam.aidial.cfg.domain.model.ImportComponent;
import com.epam.aidial.cfg.domain.service.GlobalSettingsService;
import com.epam.aidial.cfg.service.config.export.ConflictResolutionPolicy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.epam.aidial.cfg.domain.model.ImportAction.SKIP;
import static com.epam.aidial.cfg.domain.model.ImportAction.UPDATE;

@Service
@Slf4j
@LogExecution
@RequiredArgsConstructor
public class GlobalSettingsImporter {

    private final GlobalSettingsService globalSettingsService;

    public ImportComponent<GlobalSettings> importGlobalSettings(List<String> globalInterceptors,
                                                                ConflictResolutionPolicy resolutionPolicy) {
        var existingGlobalSettings = globalSettingsService.getGlobalSettings();

        var newGlobalInterceptors = CollectionUtils.isNotEmpty(globalInterceptors)
                ? globalInterceptors
                : existingGlobalSettings.getGlobalInterceptors();

        var newGlobalSettings = new GlobalSettings();
        newGlobalSettings.setGlobalInterceptors(newGlobalInterceptors);
        ImportAction importAction = handleExistingGlobalSettings(newGlobalSettings, existingGlobalSettings, resolutionPolicy);
        return new ImportComponent<>(importAction, existingGlobalSettings, newGlobalSettings);
    }

    private ImportAction handleExistingGlobalSettings(GlobalSettings newGlobalSettings,
                                                      GlobalSettings existingGlobalSettings,
                                                      ConflictResolutionPolicy resolutionPolicy) {
        return switch (resolutionPolicy) {
            case SKIP -> {
                if (existingGlobalSettings.isEmpty()){
                    globalSettingsService.update(newGlobalSettings);
                    yield UPDATE;
                }
                yield SKIP;
            }
            case OVERRIDE -> {
                globalSettingsService.update(newGlobalSettings);
                yield UPDATE;
            }
        };
    }

}