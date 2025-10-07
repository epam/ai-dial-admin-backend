package com.epam.aidial.cfg.service.transfer.importer;

import com.epam.aidial.cfg.client.dto.DeploymentInfoDto;
import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.mapper.ToolSetCoreMapper;
import com.epam.aidial.cfg.domain.model.ImportAction;
import com.epam.aidial.cfg.domain.model.ImportComponent;
import com.epam.aidial.cfg.domain.model.Role;
import com.epam.aidial.cfg.domain.model.RoleLimit;
import com.epam.aidial.cfg.domain.model.ToolSet;
import com.epam.aidial.cfg.domain.model.source.ToolSetContainerSource;
import com.epam.aidial.cfg.domain.service.DeploymentManagerService;
import com.epam.aidial.cfg.domain.service.ToolSetService;
import com.epam.aidial.cfg.exception.DeploymentClientNotExistsException;
import com.epam.aidial.cfg.model.ConfigImportOptions;
import com.epam.aidial.cfg.service.export.ConflictResolutionPolicy;
import com.epam.aidial.core.config.CoreToolSet;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.epam.aidial.cfg.domain.model.ImportAction.CREATE;
import static com.epam.aidial.cfg.domain.model.ImportAction.SKIP;
import static com.epam.aidial.cfg.domain.model.ImportAction.UPDATE;

@Slf4j
@Service
@LogExecution
public class ToolSetImporter extends DeploymentHolderImporter {

    private final DeploymentManagerService deploymentManagerService;
    private final ToolSetCoreMapper toolSetCoreMapper;
    private final ToolSetService toolSetService;
    private final Validator validator;

    public ToolSetImporter(DeploymentManagerService deploymentManagerService,
                           ToolSetCoreMapper toolSetCoreMapper,
                           ToolSetService toolSetService) {
        this.deploymentManagerService = deploymentManagerService;
        this.toolSetCoreMapper = toolSetCoreMapper;
        this.toolSetService = toolSetService;
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    public Collection<ImportComponent<ToolSet>> importToolSets(Map<String, CoreToolSet> coreToolSets,
                                                               Map<String, Role> roles,
                                                               ConfigImportOptions importOptions) {
        if (MapUtils.isNotEmpty(coreToolSets)) {
            Map<String, ToolSet> toolSets = coreToolSets.entrySet()
                    .stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, entry -> map(entry.getKey(), entry.getValue())));
            return importAdminToolSets(toolSets, roles, importOptions);
        }
        return Collections.emptyList();
    }

    public Collection<ImportComponent<ToolSet>> importAdminToolSets(Map<String, ToolSet> toolSets,
                                                                    Map<String, Role> roles,
                                                                    ConfigImportOptions importOptions) {
        if (MapUtils.isNotEmpty(toolSets)) {
            return toolSets.entrySet().stream()
                    .map(toolSetEntry -> {
                                var toolSet = toolSetEntry.getValue();
                                return processToolSet(toolSetEntry.getKey(), toolSet, roles, importOptions.conflictResolutionPolicy());
                            }
                    )
                    .toList();
        }
        return Collections.emptyList();
    }

    private ImportComponent<ToolSet> processToolSet(String toolSetName,
                                                    ToolSet newToolSet,
                                                    Map<String, Role> roles,
                                                    ConflictResolutionPolicy resolutionPolicy) {
        removeContainerSourceDependencyIfContainerIsAbsent(newToolSet);
        Optional<ToolSet> toolSet = toolSetService.tryGetToolSet(toolSetName);
        if (toolSet.isPresent()) {
            ToolSet existingToolSet = toolSet.get();
            setLimits(toolSetName, existingToolSet.getDeployment(), roles, newToolSet.getDeployment());
            ImportAction importAction = handleExisting(newToolSet, resolutionPolicy, toolSetName);
            return new ImportComponent<>(importAction, existingToolSet, newToolSet);
        } else {
            validate(toolSetName, newToolSet);
            setLimits(toolSetName, roles, newToolSet.getDeployment());
            toolSetService.create(newToolSet);
            return new ImportComponent<>(CREATE, null, newToolSet);
        }
    }

    private void removeContainerSourceDependencyIfContainerIsAbsent(ToolSet newToolSet) {
        if (!(newToolSet.getSource() instanceof ToolSetContainerSource containerSource)) {
            return;
        }

        String containerId = containerSource.getContainerId();
        DeploymentInfoDto deploymentInfo = null;

        try {
            deploymentInfo = deploymentManagerService.getById(containerId);
        } catch (DeploymentClientNotExistsException e) {
            log.warn("Failed to get deployment by ID '%s' on ToolSet '%s' import"
                    .formatted(containerId, newToolSet.getDeployment().getName()), e);
        }

        if (deploymentInfo == null) {
            newToolSet.setSource(null);
        }
    }

    private ImportAction handleExisting(ToolSet newToolSet,
                                        ConflictResolutionPolicy resolutionPolicy,
                                        String toolSetName) {
        return switch (resolutionPolicy) {
            case SKIP -> SKIP; // Do nothing, the existing ToolSet will remain unchanged.
            case OVERRIDE -> {
                validate(toolSetName, newToolSet);
                toolSetService.update(toolSetName, newToolSet);
                yield UPDATE;
            }
        };
    }

    private ToolSet map(String toolSetName, CoreToolSet toolSet) {
        toolSet.setName(toolSetName);
        return toolSetCoreMapper.mapToolSet(toolSet);
    }

    private void validate(String toolSetName, ToolSet toolSet) {
        Set<ConstraintViolation<ToolSet>> violations = validator.validate(toolSet);
        if (CollectionUtils.isEmpty(violations)) {
            return;
        }
        for (ConstraintViolation<ToolSet> violation : violations) {
            String message = violation.getMessage();
            Path propertyPath = violation.getPropertyPath();
            log.error("ToolSet '{}' is invalid: {} {}", toolSetName, propertyPath, message);
            throw new IllegalArgumentException("ToolSet '" + toolSetName + "' is invalid: " + propertyPath + " " + message);
        }
    }

    public List<ImportComponent<ToolSet>> getActualImportedToolSets(Collection<ImportComponent<ToolSet>> toolSetImportComponents,
                                                                    Collection<ImportComponent<Role>> roleImportComponents) {
        List<String> names = getNextImportComponentNames(toolSetImportComponents);
        Map<String, ToolSet> importedToolSetsByNames = toolSetService.getAllByNames(names)
                .stream()
                .collect(Collectors.toMap(toolSet -> toolSet.getDeployment().getName(), Function.identity()));

        List<RoleLimit> importedRoleLimits = getImportedLimits(roleImportComponents);

        return toolSetImportComponents.stream()
                .map(importComponent -> {
                    var next = importedToolSetsByNames.get(importComponent.getNext().getDeployment().getName());
                    setImportedLimits(next, importedRoleLimits);
                    var prev = importComponent.getPrev();
                    clearTxDependentFields(next);
                    clearTxDependentFields(prev);
                    return new ImportComponent<>(importComponent.getImportAction(), prev, next);
                })
                .toList();
    }

    private void clearTxDependentFields(ToolSet toolSet) {
        if (toolSet != null) {
            toolSet.setCreatedAt(null);
            toolSet.setUpdatedAt(null);
        }
    }
}
