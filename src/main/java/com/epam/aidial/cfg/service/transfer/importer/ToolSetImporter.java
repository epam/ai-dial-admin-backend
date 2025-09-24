package com.epam.aidial.cfg.service.transfer.importer;

import com.epam.aidial.cfg.client.dto.DeploymentInfoDto;
import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.mapper.ToolSetCoreMapper;
import com.epam.aidial.cfg.domain.model.ImportAction;
import com.epam.aidial.cfg.domain.model.ImportComponent;
import com.epam.aidial.cfg.domain.model.Role;
import com.epam.aidial.cfg.domain.model.ShareResourceLimit;
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
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.epam.aidial.cfg.domain.model.ImportAction.CREATE;
import static com.epam.aidial.cfg.domain.model.ImportAction.SKIP;
import static com.epam.aidial.cfg.domain.model.ImportAction.UPDATE;

@Slf4j
@Service
@LogExecution
public class ToolSetImporter extends RoleBasedImporter {

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
                                                               ConfigImportOptions importOptions,
                                                               boolean isPreview) {
        if (MapUtils.isNotEmpty(coreToolSets)) {
            Map<String, ToolSet> toolSets = coreToolSets.entrySet()
                    .stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, entry -> map(entry.getKey(), entry.getValue())));
            return importAdminToolSets(toolSets, roles, importOptions, isPreview);
        }
        return Collections.emptyList();
    }

    public Collection<ImportComponent<ToolSet>> importAdminToolSets(Map<String, ToolSet> toolSets,
                                                                    Map<String, Role> roles,
                                                                    ConfigImportOptions importOptions,
                                                                    boolean isPreview) {
        if (MapUtils.isNotEmpty(toolSets)) {
            return toolSets.entrySet().stream()
                    .map(toolSetEntry -> {
                                var toolSet = toolSetEntry.getValue();
                                var importAction = processToolSet(toolSetEntry.getKey(), toolSet, roles, importOptions.conflictResolutionPolicy(), isPreview);
                                return new ImportComponent<>(importAction, toolSet);
                            }
                    )
                    .toList();
        }
        return Collections.emptyList();
    }

    private ImportAction processToolSet(String toolSetName,
                                        ToolSet newToolSet,
                                        Map<String, Role> roles,
                                        ConflictResolutionPolicy resolutionPolicy,
                                        boolean isPreview) {
        removeContainerSourceDependencyIfContainerIsAbsent(newToolSet);
        Optional<ToolSet> toolSet = toolSetService.tryGetToolSet(toolSetName);
        if (toolSet.isPresent()) {
            ToolSet existingToolSet = toolSet.get();
            setLimits(toolSetName, existingToolSet.getDeployment(), roles, newToolSet.getDeployment(), isPreview);
            return handleExisting(newToolSet, resolutionPolicy, toolSetName, isPreview);
        } else {
            validate(toolSetName, newToolSet);
            setLimits(toolSetName,  roles, newToolSet.getDeployment(), isPreview);
            if (!isPreview) {
                toolSetService.create(newToolSet);
            }
            return CREATE;
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
                                        String toolSetName,
                                        boolean isPreview) {
        switch (resolutionPolicy) {
            case SKIP -> {
                // Do nothing, the existing ToolSet will remain unchanged.
                return SKIP;
            }
            case OVERRIDE -> {
                validate(toolSetName, newToolSet);
                if (!isPreview) {
                    toolSetService.update(toolSetName, newToolSet);
                }
                return UPDATE;
            }
            default -> throw new IllegalArgumentException("Unexpected resolution policy: " + resolutionPolicy);
        }
    }

    private ToolSet map(String toolSetName, CoreToolSet toolSet) {
        toolSet.setName(toolSetName);
        return toolSetCoreMapper.mapToolSet(toolSet, new ShareResourceLimit());
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
}
