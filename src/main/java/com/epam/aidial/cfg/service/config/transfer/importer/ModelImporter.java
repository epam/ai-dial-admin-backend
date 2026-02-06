package com.epam.aidial.cfg.service.config.transfer.importer;

import com.epam.aidial.cfg.client.dto.DeploymentInfoDto;
import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.mapper.ModelCoreMapper;
import com.epam.aidial.cfg.domain.model.ImportAction;
import com.epam.aidial.cfg.domain.model.ImportComponent;
import com.epam.aidial.cfg.domain.model.Model;
import com.epam.aidial.cfg.domain.model.RoleLimit;
import com.epam.aidial.cfg.domain.model.source.ModelContainerSource;
import com.epam.aidial.cfg.domain.model.source.ModelSource;
import com.epam.aidial.cfg.domain.service.DeploymentManagerService;
import com.epam.aidial.cfg.domain.service.ModelService;
import com.epam.aidial.cfg.exception.DeploymentClientNotExistsException;
import com.epam.aidial.cfg.model.ConfigImportOptions;
import com.epam.aidial.cfg.service.config.export.ConflictResolutionPolicy;
import com.epam.aidial.cfg.service.config.transfer.importer.util.ModelSourceResolver;
import com.epam.aidial.core.config.CoreModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.epam.aidial.cfg.domain.model.ImportAction.CREATE;
import static com.epam.aidial.cfg.domain.model.ImportAction.SKIP;
import static com.epam.aidial.cfg.domain.model.ImportAction.UPDATE;

@Service
@Slf4j
@LogExecution
@RequiredArgsConstructor
public class ModelImporter extends DeploymentHolderImporter {

    private final ModelService modelService;
    private final ModelCoreMapper modelMapper;
    private final ModelSourceResolver modelSourceResolver;
    private final DeploymentManagerService deploymentManagerService;

    public Collection<ImportComponent<Model>> importModels(Map<String, CoreModel> coreModels,
                                                           ConfigImportOptions importOptions) {
        if (MapUtils.isEmpty(coreModels)) {
            return Collections.emptyList();
        }

        return coreModels.entrySet()
                .stream()
                .map(entry -> processModel(entry.getKey(), entry.getValue(), importOptions.conflictResolutionPolicy()))
                .toList();
    }

    public Collection<ImportComponent<Model>> importAdminModels(Map<String, Model> models,
                                                                ConfigImportOptions importOptions) {
        if (MapUtils.isEmpty(models)) {
            return Collections.emptyList();
        }

        return models.entrySet().stream()
                .map(modelEntry -> processModel(modelEntry.getKey(), modelEntry.getValue(), importOptions.conflictResolutionPolicy()))
                .toList();
    }

    private ImportComponent<Model> processModel(String modelName,
                                                CoreModel coreModel,
                                                ConflictResolutionPolicy resolutionPolicy) {
        Optional<Model> model = modelService.tryGetModel(modelName);
        if (model.isPresent()) {
            Model existingModel = model.get();
            Model existingModelCopy = modelMapper.copy(existingModel);
            List<RoleLimit> roleLimits = getRoleLimits(existingModelCopy.getDeployment(), coreModel.getUserRoles());
            ModelSource modelSource = modelSourceResolver.resolveSourceForExistingModel(coreModel, existingModelCopy);
            Model newModel = map(modelName, coreModel, roleLimits, existingModelCopy, modelSource);
            ImportAction importAction = handleExistingModel(newModel, resolutionPolicy, modelName);
            return new ImportComponent<>(importAction, existingModel, newModel);
        } else {
            List<RoleLimit> roleLimits = getRoleLimits(modelName, coreModel.getUserRoles());
            ModelSource modelSource = modelSourceResolver.resolveSourceForNewModel(coreModel);
            Model newModel = map(modelName, coreModel, roleLimits, new Model(), modelSource);
            modelService.createModel(newModel);
            return new ImportComponent<>(CREATE, null, newModel);
        }
    }

    private ImportComponent<Model> processModel(String modelName,
                                                Model newModel,
                                                ConflictResolutionPolicy resolutionPolicy) {
        removeContainerSourceDependencyIfContainerIsAbsent(newModel);
        Optional<Model> model = modelService.tryGetModel(modelName);
        if (model.isPresent()) {
            Model existingModel = model.get();
            newModel.getDeployment().setRoleLimits(existingModel.getDeployment().getRoleLimits());
            ImportAction importAction = handleExistingModel(newModel, resolutionPolicy, modelName);
            return new ImportComponent<>(importAction, existingModel, newModel);
        } else {
            modelService.createModel(newModel);
            return new ImportComponent<>(CREATE, null, newModel);
        }
    }

    private void removeContainerSourceDependencyIfContainerIsAbsent(Model newModel) {
        if (!(newModel.getSource() instanceof ModelContainerSource containerSource)) {
            return;
        }

        String containerId = containerSource.getContainerId();
        DeploymentInfoDto deploymentInfo = null;

        try {
            deploymentInfo = deploymentManagerService.getById(containerId);
        } catch (DeploymentClientNotExistsException e) {
            log.warn("Failed to get deployment by ID '%s' on Model '%s' import"
                    .formatted(containerId, newModel.getDeployment().getName()), e);
        }

        if (deploymentInfo == null || StringUtils.isBlank(deploymentInfo.getUrl())) {
            log.debug("Container is missing or not deployed. ContainerId: {}, deploymentInfo: {}", containerId, deploymentInfo);
            newModel.setSource(null);
        }
    }

    private ImportAction handleExistingModel(Model newModel,
                                             ConflictResolutionPolicy resolutionPolicy,
                                             String modelName) {
        return switch (resolutionPolicy) {
            case SKIP -> SKIP; // Do nothing, the existing model will remain unchanged.
            case OVERRIDE -> {
                modelService.updateModel(modelName, newModel);
                yield UPDATE;
            }
        };
    }

    private Model map(String modelName, CoreModel coreModel, List<RoleLimit> roleLimits, Model model, ModelSource source) {
        coreModel.setName(modelName);
        return modelMapper.mapModel(coreModel, source, roleLimits, model);
    }

    public List<ImportComponent<Model>> getActualImportedModels(Collection<ImportComponent<Model>> modelImportComponents) {
        List<String> names = getNextImportComponentNames(modelImportComponents);
        Map<String, Model> importedModelsByNames = modelService.getAllByNames(names)
                .stream()
                .collect(Collectors.toMap(model -> model.getDeployment().getName(), Function.identity()));

        return modelImportComponents.stream()
                .map(importComponent -> {
                    var next = importedModelsByNames.get(importComponent.getNext().getDeployment().getName());
                    var prev = importComponent.getPrev();
                    clearTxDependentFields(next);
                    clearTxDependentFields(prev);
                    return new ImportComponent<>(importComponent.getImportAction(), prev, next);
                })
                .toList();
    }

    private void clearTxDependentFields(Model model) {
        if (model != null) {
            model.setCreatedAt(null);
            model.setUpdatedAt(null);
        }
    }
}
