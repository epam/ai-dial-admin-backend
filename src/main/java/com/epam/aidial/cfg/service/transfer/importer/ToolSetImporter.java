package com.epam.aidial.cfg.service.transfer.importer;

import com.epam.aidial.cfg.client.dto.DeploymentInfoDto;
import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.mapper.ToolSetCoreMapper;
import com.epam.aidial.cfg.domain.model.ImportAction;
import com.epam.aidial.cfg.domain.model.ImportComponent;
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
import org.apache.commons.lang3.StringUtils;
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
                                                               ConfigImportOptions importOptions) {
        if (MapUtils.isEmpty(coreToolSets)) {
            return Collections.emptyList();
        }

        return coreToolSets.entrySet()
                .stream()
                .map(entry -> processToolSet(entry.getKey(), entry.getValue(), importOptions.conflictResolutionPolicy()))
                .toList();
    }

    public Collection<ImportComponent<ToolSet>> importAdminToolSets(Map<String, ToolSet> toolSets,
                                                                    ConfigImportOptions importOptions) {
        if (MapUtils.isEmpty(toolSets)) {
            return Collections.emptyList();
        }

        return toolSets.entrySet().stream()
                .map(entry -> processToolSet(entry.getKey(), entry.getValue(), importOptions.conflictResolutionPolicy()))
                .toList();

    }

    private ImportComponent<ToolSet> processToolSet(String toolSetName,
                                                    CoreToolSet coreToolSet,
                                                    ConflictResolutionPolicy resolutionPolicy) {
        Optional<ToolSet> toolSet = toolSetService.tryGetToolSet(toolSetName);
        if (toolSet.isPresent()) {
            ToolSet existingToolSet = toolSet.get();
            ToolSet existingToolSetCopy = toolSetCoreMapper.copy(existingToolSet);
            List<RoleLimit> roleLimits = getRoleLimits(existingToolSetCopy.getDeployment(), coreToolSet.getUserRoles());
            ToolSet newToolSet = map(toolSetName, coreToolSet, roleLimits, existingToolSetCopy);
            ImportAction importAction = handleExisting(newToolSet, resolutionPolicy, toolSetName);
            return new ImportComponent<>(importAction, existingToolSet, newToolSet);
        } else {
            List<RoleLimit> roleLimits = getRoleLimits(toolSetName, coreToolSet.getUserRoles());
            ToolSet newToolSet = map(toolSetName, coreToolSet, roleLimits, new ToolSet());
            validate(toolSetName, newToolSet);
            toolSetService.create(newToolSet);
            return new ImportComponent<>(CREATE, null, newToolSet);
        }
    }

    private ImportComponent<ToolSet> processToolSet(String toolSetName,
                                                    ToolSet newToolSet,
                                                    ConflictResolutionPolicy resolutionPolicy) {
        removeContainerSourceDependencyIfContainerIsAbsent(newToolSet);
        Optional<ToolSet> toolSet = toolSetService.tryGetToolSet(toolSetName);
        if (toolSet.isPresent()) {
            ToolSet existingToolSet = toolSet.get();
            newToolSet.getDeployment().setRoleLimits(existingToolSet.getDeployment().getRoleLimits());
            ImportAction importAction = handleExisting(newToolSet, resolutionPolicy, toolSetName);
            return new ImportComponent<>(importAction, existingToolSet, newToolSet);
        } else {
            validate(toolSetName, newToolSet);
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

        if (deploymentInfo == null || StringUtils.isBlank(deploymentInfo.getUrl())) {
            log.debug("Container is missing or not deployed. ContainerId: {}, deploymentInfo: {}", containerId, deploymentInfo);
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

    private ToolSet map(String toolSetName, CoreToolSet coreToolSet, List<RoleLimit> roleLimits, ToolSet toolSet) {
        coreToolSet.setName(toolSetName);
        return toolSetCoreMapper.mapToolSet(coreToolSet, roleLimits, toolSet);
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

    public List<ImportComponent<ToolSet>> getActualImportedToolSets(Collection<ImportComponent<ToolSet>> toolSetImportComponents) {
        List<String> names = getNextImportComponentNames(toolSetImportComponents);
        Map<String, ToolSet> importedToolSetsByNames = toolSetService.getAllByNames(names)
                .stream()
                .collect(Collectors.toMap(toolSet -> toolSet.getDeployment().getName(), Function.identity()));

        return toolSetImportComponents.stream()
                .map(importComponent -> {
                    var next = importedToolSetsByNames.get(importComponent.getNext().getDeployment().getName());
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
