package com.epam.aidial.cfg.service.transfer.importer;

import com.epam.aidial.cfg.client.dto.DeploymentInfoDto;
import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.mapper.ModelCoreMapper;
import com.epam.aidial.cfg.domain.model.Adapter;
import com.epam.aidial.cfg.domain.model.Deployment;
import com.epam.aidial.cfg.domain.model.ImportAction;
import com.epam.aidial.cfg.domain.model.ImportComponent;
import com.epam.aidial.cfg.domain.model.Model;
import com.epam.aidial.cfg.domain.model.Role;
import com.epam.aidial.cfg.domain.model.RoleLimit;
import com.epam.aidial.cfg.domain.model.RoleShareResourceLimit;
import com.epam.aidial.cfg.domain.model.ShareResourceLimit;
import com.epam.aidial.cfg.domain.model.source.AdapterSource;
import com.epam.aidial.cfg.domain.model.source.ModelContainerSource;
import com.epam.aidial.cfg.domain.model.source.ModelEndpointsSource;
import com.epam.aidial.cfg.domain.model.source.ModelSource;
import com.epam.aidial.cfg.domain.service.AdapterService;
import com.epam.aidial.cfg.domain.service.DeploymentManagerService;
import com.epam.aidial.cfg.domain.service.ModelService;
import com.epam.aidial.cfg.domain.utils.ModelEndpointUtils;
import com.epam.aidial.cfg.domain.utils.ModelEndpointUtils.ModelEndpointComponents;
import com.epam.aidial.cfg.exception.DeploymentClientNotExistsException;
import com.epam.aidial.cfg.model.ConfigImportOptions;
import com.epam.aidial.cfg.service.export.ConflictResolutionPolicy;
import com.epam.aidial.core.config.CoreModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
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
public class ModelImporter extends RoleBasedImporter {

    private final ModelService modelService;
    private final AdapterService adapterService;
    private final ModelCoreMapper modelMapper;
    private final ModelEndpointUtils modelEndpointUtils;
    private final DeploymentManagerService deploymentManagerService;

    public Collection<ImportComponent<Model>> importModels(Map<String, CoreModel> coreModels,
                                                           Map<String, Role> roles,
                                                           ConfigImportOptions importOptions) {
        if (MapUtils.isNotEmpty(coreModels)) {
            Map<String, Model> models = coreModels.entrySet()
                    .stream()
                    .collect(Collectors.toMap(
                                    Map.Entry::getKey,
                                    entry -> map(entry.getKey(), entry.getValue())
                            )
                    );
            return importAdminModels(models, roles, importOptions);
        }
        return Collections.emptyList();
    }

    public Collection<ImportComponent<Model>> importAdminModels(Map<String, Model> models,
                                                                Map<String, Role> roles,
                                                                ConfigImportOptions importOptions) {
        if (MapUtils.isNotEmpty(models)) {
            return models.entrySet().stream()
                    .map(modelEntry -> {
                                var model = modelEntry.getValue();
                                return processModel(modelEntry.getKey(), model, roles, importOptions.conflictResolutionPolicy());
                            }
                    )
                    .toList();
        }
        return Collections.emptyList();
    }

    private ImportComponent<Model> processModel(String modelName,
                                                Model newModel,
                                                Map<String, Role> roles,
                                                ConflictResolutionPolicy resolutionPolicy) {
        removeContainerSourceDependencyIfContainerIsAbsent(newModel);
        Optional<Model> model = modelService.tryGetModel(modelName);
        if (model.isPresent()) {
            Model existingModel = model.get();
            setLimits(modelName, existingModel.getDeployment(), roles, newModel.getDeployment());
            ImportAction importAction = handleExistingModel(newModel, resolutionPolicy, modelName);
            return new ImportComponent<>(importAction, existingModel, newModel);
        } else {
            setLimits(modelName, roles, newModel.getDeployment());
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

        if (deploymentInfo == null) {
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

    private Model map(String modelName, CoreModel model) {
        model.setName(modelName);

        ModelSource source = new ModelEndpointsSource();
        ModelEndpointComponents modelEndpointComponents = getModelEndpointComponents(model);
        if (modelEndpointComponents == null) {
            return modelMapper.mapModel(model, source, new ShareResourceLimit());
        }

        Adapter adapter = resolveAdapter(modelEndpointComponents);
        if (adapter != null) {
            source = new AdapterSource(adapter.getName(), modelEndpointComponents.completionEndpointPath());
        }

        return modelMapper.mapModel(model, source, new ShareResourceLimit());
    }

    private ModelEndpointComponents getModelEndpointComponents(CoreModel coreModel) {
        if (coreModel == null || coreModel.getEndpoint() == null) {
            return null;
        }
        return modelEndpointUtils.parseModelEndpoint(coreModel.getEndpoint(), coreModel.getType());
    }

    private Adapter resolveAdapter(ModelEndpointComponents modelEndpointComponents) {
        String adapterEndpoint = modelEndpointComponents.adapterEndpoint();
        return adapterService.getByEndpoint(adapterEndpoint);

    }

    public List<ImportComponent<Model>> getActualImportedModels(Collection<ImportComponent<Model>> modelImportComponents,
                                                                Collection<ImportComponent<Role>> roleImportComponents) {
        List<String> names = modelImportComponents.stream()
                .map(ImportComponent::getNext)
                .map(Model::getDeployment)
                .map(Deployment::getName)
                .toList();
        Map<String, Model> importedModelsByNames = modelService.getAllByNames(names)
                .stream()
                .collect(Collectors.toMap(model -> model.getDeployment().getName(), Function.identity()));

        Collection<Role> importedRoles = roleImportComponents.stream().map(ImportComponent::getNext).toList();
        List<RoleLimit> importedRoleLimits = importedRoles.stream().map(Role::getLimits).flatMap(Collection::stream).toList();
        List<RoleShareResourceLimit> importedRoleShareResourceLimits = importedRoles.stream().map(Role::getShare).flatMap(Collection::stream).toList();

        return modelImportComponents.stream()
                .map(importComponent -> {
                    var next = importedModelsByNames.get(importComponent.getNext().getDeployment().getName());
                    setImportedLimits(next, importedRoleLimits, importedRoleShareResourceLimits);
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
