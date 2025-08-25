package com.epam.aidial.cfg.service.transfer.importer;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.mapper.AssistantCoreMapper;
import com.epam.aidial.cfg.domain.model.Assistant;
import com.epam.aidial.cfg.domain.model.AssistantsProperty;
import com.epam.aidial.cfg.domain.model.ImportAction;
import com.epam.aidial.cfg.domain.model.ImportComponent;
import com.epam.aidial.cfg.domain.model.Role;
import com.epam.aidial.cfg.domain.model.ShareResourceLimit;
import com.epam.aidial.cfg.domain.service.AssistantService;
import com.epam.aidial.cfg.domain.service.AssistantsPropertyService;
import com.epam.aidial.cfg.model.ConfigImportOptions;
import com.epam.aidial.cfg.service.export.ConflictResolutionPolicy;
import com.epam.aidial.core.config.Assistants;
import com.epam.aidial.core.config.CoreAssistant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.epam.aidial.cfg.domain.model.ImportAction.CREATE;
import static com.epam.aidial.cfg.domain.model.ImportAction.SKIP;
import static com.epam.aidial.cfg.domain.model.ImportAction.UPDATE;

@Service
@Slf4j
@LogExecution
@RequiredArgsConstructor
public class AssistantImporter extends RoleBasedImporter {

    private final AssistantService assistantService;
    private final AssistantsPropertyService assistantsPropertyService;
    private final AssistantCoreMapper mapper;

    public Collection<ImportComponent<Assistant>> importAssistants(Assistants coreAssistants,
                                                                   Map<String, Role> roles,
                                                                   ConfigImportOptions importOptions,
                                                                   boolean isPreview) {
        if (coreAssistants != null) {
            AssistantsProperty assistantsProperty = mapper.mapAssistantsProperty(coreAssistants);
            processAssistantsProperty(assistantsProperty, importOptions.conflictResolutionPolicy());
            Map<String, CoreAssistant> coreAssistantsMap = coreAssistants.getAssistants();
            if (MapUtils.isNotEmpty(coreAssistantsMap)) {
                Map<String, Assistant> assistants = coreAssistantsMap.entrySet()
                        .stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, entry -> map(entry.getKey(), entry.getValue())));
                return importAdminAssistants(assistants, roles, importOptions, isPreview);
            }
        }
        return Collections.emptyList();
    }

    private void processAssistantsProperty(AssistantsProperty newAssistantsProperty, ConflictResolutionPolicy resolutionPolicy) {
        AssistantsProperty currentAssistantsProperty = assistantsPropertyService.getAssistantsProperty();

        updatePropertyIfNeeded(
                currentAssistantsProperty::getEndpoint,
                newAssistantsProperty.getEndpoint(),
                currentAssistantsProperty::setEndpoint,
                resolutionPolicy
        );

        updatePropertyIfNeeded(
                currentAssistantsProperty::getFeatures,
                newAssistantsProperty.getFeatures(),
                currentAssistantsProperty::setFeatures,
                resolutionPolicy
        );

        assistantsPropertyService.updateAssistantsProperty(currentAssistantsProperty);
    }

    private <T> void updatePropertyIfNeeded(
            Supplier<T> getCurrentProperty,
            T newProperty,
            Consumer<T> setCurrentProperty,
            ConflictResolutionPolicy resolutionPolicy) {

        if (newProperty != null) {
            T currentProperty = getCurrentProperty.get();

            if (currentProperty == null) {
                setCurrentProperty.accept(newProperty);
            } else {
                switch (resolutionPolicy) {
                    case OVERRIDE -> setCurrentProperty.accept(newProperty);
                    case SKIP -> {
                        // doNothing
                    }
                    default -> throw new IllegalArgumentException("Unexpected resolutionPolicy: " + resolutionPolicy);
                }
            }
        }
    }

    public Collection<ImportComponent<Assistant>> importAdminAssistants(Map<String, Assistant> assistants,
                                                                        Map<String, Role> roles,
                                                                        ConfigImportOptions importOptions,
                                                                        boolean isPreview) {
        if (MapUtils.isNotEmpty(assistants)) {
            return assistants.entrySet().stream()
                    .map(assistantEntry -> {
                                var assistant = assistantEntry.getValue();
                                var importAction = processAssistant(assistantEntry.getKey(), assistant, roles, importOptions.conflictResolutionPolicy(), isPreview);
                                return new ImportComponent<>(importAction, assistant);
                            }
                    )
                    .toList();
        }
        return Collections.emptyList();
    }

    private ImportAction processAssistant(String assistantName,
                                          Assistant newAssistant,
                                          Map<String, Role> roles,
                                          ConflictResolutionPolicy resolutionPolicy,
                                          boolean isPreview) {
        Optional<Assistant> assistant = assistantService.tryGetAssistant(assistantName);
        if (assistant.isPresent()) {
            Assistant existingAssistant = assistant.get();
            setLimits(assistantName, existingAssistant.getDeployment(), roles, newAssistant.getDeployment(), isPreview);
            return handleExisting(newAssistant, resolutionPolicy, assistantName, isPreview);
        } else {
            setLimits(assistantName, roles, newAssistant.getDeployment(), isPreview);
            if (!isPreview) {
                assistantService.createAssistant(newAssistant);
            }
            return CREATE;
        }
    }

    private ImportAction handleExisting(Assistant assistant,
                                        ConflictResolutionPolicy resolutionPolicy,
                                        String assistantName,
                                        boolean isPreview) {
        switch (resolutionPolicy) {
            case SKIP -> {
                // Do nothing, the existing assistant will remain unchanged.
                return SKIP;
            }
            case OVERRIDE -> {
                if (!isPreview) {
                    assistantService.updateAssistant(assistantName, assistant);
                }
                return UPDATE;
            }
            default -> throw new IllegalArgumentException("Unexpected resolutionPolicy: " + resolutionPolicy);
        }
    }

    private Assistant map(String assistantName, CoreAssistant assistant) {
        assistant.setName(assistantName);
        return mapper.mapAssistant(assistant, new ShareResourceLimit());
    }

}
