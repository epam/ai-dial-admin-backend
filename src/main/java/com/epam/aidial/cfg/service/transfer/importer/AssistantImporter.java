package com.epam.aidial.cfg.service.transfer.importer;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.mapper.AssistantCoreMapper;
import com.epam.aidial.cfg.domain.model.Assistant;
import com.epam.aidial.cfg.domain.model.AssistantsProperty;
import com.epam.aidial.cfg.domain.model.ImportAction;
import com.epam.aidial.cfg.domain.model.ImportComponent;
import com.epam.aidial.cfg.domain.model.Role;
import com.epam.aidial.cfg.domain.model.RoleLimit;
import com.epam.aidial.cfg.domain.model.RoleShareResourceLimit;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.epam.aidial.cfg.domain.model.ImportAction.CREATE;
import static com.epam.aidial.cfg.domain.model.ImportAction.SKIP;
import static com.epam.aidial.cfg.domain.model.ImportAction.UPDATE;

@Service
@Slf4j
@LogExecution
@RequiredArgsConstructor
public class AssistantImporter extends DeploymentHolderImporter {

    private final AssistantService assistantService;
    private final AssistantsPropertyService assistantsPropertyService;
    private final AssistantCoreMapper mapper;

    public Collection<ImportComponent<Assistant>> importAssistants(Assistants coreAssistants,
                                                                   Map<String, Role> roles,
                                                                   ConfigImportOptions importOptions) {
        if (coreAssistants != null) {
            AssistantsProperty assistantsProperty = mapper.mapAssistantsProperty(coreAssistants);
            processAssistantsProperty(assistantsProperty, importOptions.conflictResolutionPolicy());
            Map<String, CoreAssistant> coreAssistantsMap = coreAssistants.getAssistants();
            if (MapUtils.isNotEmpty(coreAssistantsMap)) {
                Map<String, Assistant> assistants = coreAssistantsMap.entrySet()
                        .stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, entry -> map(entry.getKey(), entry.getValue())));
                return importAdminAssistants(assistants, roles, importOptions);
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
                                                                        ConfigImportOptions importOptions) {
        if (MapUtils.isNotEmpty(assistants)) {
            return assistants.entrySet().stream()
                    .map(assistantEntry -> {
                                var assistant = assistantEntry.getValue();
                                return processAssistant(assistantEntry.getKey(), assistant, roles, importOptions.conflictResolutionPolicy());
                            }
                    )
                    .toList();
        }
        return Collections.emptyList();
    }

    private ImportComponent<Assistant> processAssistant(String assistantName,
                                                        Assistant newAssistant,
                                                        Map<String, Role> roles,
                                                        ConflictResolutionPolicy resolutionPolicy) {
        Optional<Assistant> assistant = assistantService.tryGetAssistant(assistantName);
        if (assistant.isPresent()) {
            Assistant existingAssistant = assistant.get();
            setLimits(assistantName, existingAssistant.getDeployment(), roles, newAssistant.getDeployment());
            ImportAction importAction = handleExisting(newAssistant, resolutionPolicy, assistantName);
            return new ImportComponent<>(importAction, existingAssistant, newAssistant);
        } else {
            setLimits(assistantName, roles, newAssistant.getDeployment());
            assistantService.createAssistant(newAssistant);

            return new ImportComponent<>(CREATE, null, newAssistant);
        }
    }

    private ImportAction handleExisting(Assistant assistant,
                                        ConflictResolutionPolicy resolutionPolicy,
                                        String assistantName) {
        return switch (resolutionPolicy) {
            case SKIP -> SKIP; // Do nothing, the existing assistant will remain unchanged.
            case OVERRIDE -> {
                assistantService.updateAssistant(assistantName, assistant);
                yield UPDATE;
            }
        };
    }

    private Assistant map(String assistantName, CoreAssistant assistant) {
        assistant.setName(assistantName);
        return mapper.mapAssistant(assistant, new ShareResourceLimit());
    }

    public List<ImportComponent<Assistant>> getActualImportedAssistants(Collection<ImportComponent<Assistant>> assistantImportComponents,
                                                                        Collection<ImportComponent<Role>> roleImportComponents) {
        List<String> names = getNextImportComponentNames(assistantImportComponents);
        Map<String, Assistant> importedAssistantsByNames = assistantService.getAllByNames(names)
                .stream()
                .collect(Collectors.toMap(assistant -> assistant.getDeployment().getName(), Function.identity()));

        ImportedLimits importedLimits = getImportedLimits(roleImportComponents);
        List<RoleLimit> importedRoleLimits = importedLimits.importedRoleLimits();
        List<RoleShareResourceLimit> importedRoleShareResourceLimits = importedLimits.importedRoleShareResourceLimits();

        return assistantImportComponents.stream()
                .map(importComponent -> {
                    var next = importedAssistantsByNames.get(importComponent.getNext().getDeployment().getName());
                    setImportedLimits(next, importedRoleLimits, importedRoleShareResourceLimits);
                    var prev = importComponent.getPrev();
                    clearTxDependentFields(next);
                    clearTxDependentFields(prev);
                    return new ImportComponent<>(importComponent.getImportAction(), prev, next);
                })
                .toList();
    }

    private void clearTxDependentFields(Assistant assistant) {
        if (assistant != null) {
            assistant.setCreatedAt(null);
            assistant.setUpdatedAt(null);
        }
    }
}
